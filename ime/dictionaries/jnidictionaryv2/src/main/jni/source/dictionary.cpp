/*
**
** Copyright 2009, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

#include <stdio.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <string.h>

#include "dictionary.h"
#include "char_utils.h"

#define NOT_VALID_WORD -99

namespace nativeime {

    Dictionary::Dictionary(void *dict, int typedLetterMultiplier, int fullWordMultiplier) {
        mDict = (unsigned char *) dict;
        mTypedLetterMultiplier = typedLetterMultiplier;
        mFullWordMultiplier = fullWordMultiplier;
    }

    Dictionary::~Dictionary() {
    }

    int Dictionary::getSuggestions(int *codes, int codesSize, unsigned short *outWords,
                                   int *frequencies,
                                   int maxWordLength, int maxWords, int maxAlternatives,
                                   int skipPos,
                                   int *nextLetters, int nextLettersSize) {
        int suggWords;
        mFrequencies = frequencies;
        mOutputChars = outWords;
        mInputCodes = codes;
        mInputLength = codesSize;
        mMaxAlternatives = maxAlternatives;
        mMaxWordLength = maxWordLength;
        mMaxWords = maxWords;
        mSkipPos = skipPos;
        mMaxEditDistance = mInputLength < 5 ? 2 : mInputLength / 2;
        mNextLettersFrequencies = nextLetters;
        mNextLettersSize = nextLettersSize;

        getWordsRec(0, 0, mInputLength * 3, false, 1, 0, 0);

        // Get the word count
        suggWords = 0;
        while (suggWords < mMaxWords && mFrequencies[suggWords] > 0) suggWords++;

        return suggWords;
    }

    void
    Dictionary::registerNextLetter(unsigned short c) {
        if (c < mNextLettersSize) {
            mNextLettersFrequencies[c]++;
        }
    }

    unsigned short
    Dictionary::getChar(int *pos) {
        unsigned short ch = (unsigned short) (mDict[(*pos)++] & 0xFF);
        // If the code is 255, then actual 16 bit code follows (in big endian)
        if (ch == 0xFF) {
            ch = ((mDict[*pos] & 0xFF) << 8) | (mDict[*pos + 1] & 0xFF);
            (*pos) += 2;
        }
        return ch;
    }

    int
    Dictionary::getAddress(int *pos) {
        int address = 0;
        if ((mDict[*pos] & FLAG_ADDRESS_MASK) == 0) {
            *pos += 1;
        } else {
            address += (mDict[*pos] & (ADDRESS_MASK >> 16)) << 16;
            address += (mDict[*pos + 1] & 0xFF) << 8;
            address += (mDict[*pos + 2] & 0xFF);
            *pos += 3;
        }
        return address;
    }

    int
    Dictionary::getFreq(int *pos) {
        return mDict[(*pos)++] & 0xFF;
    }

    int
    Dictionary::wideStrLen(unsigned short *str) {
        if (!str) return 0;
        unsigned short *end = str;
        while (*end)
            end++;
        return end - str;
    }

    bool
    Dictionary::addWord(unsigned short *word, int length, int frequency) {
        word[length] = 0;

        // Find the right insertion point
        int insertAt = 0;
        while (insertAt < mMaxWords) {
            if (frequency > mFrequencies[insertAt]
                || (mFrequencies[insertAt] == frequency
                    && length < wideStrLen(mOutputChars + insertAt * mMaxWordLength))) {
                break;
            }
            insertAt++;
        }
        if (insertAt < mMaxWords) {
            memmove((char *) mFrequencies + (insertAt + 1) * sizeof(mFrequencies[0]),
                    (char *) mFrequencies + insertAt * sizeof(mFrequencies[0]),
                    (mMaxWords - insertAt - 1) * sizeof(mFrequencies[0]));
            mFrequencies[insertAt] = frequency;
            memmove((char *) mOutputChars + (insertAt + 1) * mMaxWordLength * sizeof(short),
                    (char *) mOutputChars + (insertAt) * mMaxWordLength * sizeof(short),
                    (mMaxWords - insertAt - 1) * sizeof(short) * mMaxWordLength);
            unsigned short *dest = mOutputChars + (insertAt) * mMaxWordLength;
            while (length--) {
                *dest++ = *word++;
            }
            *dest = 0; // NULL terminate
            return true;
        }
        return false;
    }

    bool
    Dictionary::sameAsTyped(unsigned short *word, int length) {
        if (length != mInputLength) {
            return false;
        }
        int *inputCodes = mInputCodes;
        while (length--) {
            if ((unsigned int) *inputCodes != (unsigned int) *word) {
                return false;
            }
            inputCodes += mMaxAlternatives;
            word++;
        }
        return true;
    }

    void
    Dictionary::getWordsRec(int pos, int depth, int maxDepth, bool completion, int snr,
                            int inputIndex,
                            int diffs) {
        // Optimization: Prune out words that are too long compared to how much was typed.
        if (depth > maxDepth) {
            return;
        }
        if (diffs > mMaxEditDistance) {
            return;
        }
        const int count = getCount(&pos);
        int *currentChars = NULL;
        if (mInputLength <= inputIndex) {
            completion = true;
        } else {
            //currentChars will point to the current character TYPED by the user
            //and after that all the alternative characters (e.g., near-by keys)
            //note that the alternative will include the letter but in lower case!
            // so, F will have f,e,r,t,g,b,v,c,d
            //and f will have f,e,r,t,g,b,v,c,d
            currentChars = mInputCodes + (inputIndex * mMaxAlternatives);
        }

        for (int i = 0; i < count; i++) {
            // -- at char
            const unsigned short c = getChar(&pos);
            // -- at flag/add
            const unsigned short lowerC = CharUtils::toBaseLowerCase(c);
            const bool terminal = getTerminal(&pos);
            const int childrenAddress = getAddress(&pos);
            // -- after address or flag
            int freq = 1;
            if (terminal) freq = getFreq(&pos);
            // -- after add or freq

            // If we are only doing completions, no need to look at the typed characters.
            if (completion) {
                mWord[depth] = c;
                if (terminal) {
                    addWord(mWord, depth + 1, freq * snr);
                    if (depth >= mInputLength && mSkipPos < 0) {
                        registerNextLetter(mWord[mInputLength]);
                    }
                }
                if (childrenAddress != 0) {
                    getWordsRec(childrenAddress, depth + 1, maxDepth,
                                completion, snr, inputIndex, diffs);
                }
            } else {
                int j = 0;
                while (currentChars[j] > 0) {
                    const unsigned short currentChar = (const unsigned short) currentChars[j];
                    const unsigned short lowerCurrentChar = CharUtils::toBaseLowerCase(currentChar);
                    //currentChar can be upper or lower
                    //c can be upper or lower
                    //lowerC is lower or c (in the case where we do not know how to convert to lower)
                    //lowerCurrentChar is lower or c  (in the case where we do not know how to convert to lower)
                    //so, c must be checked against currentChar (in cases where we do not know how to convert)
                    //and lowerCurrent should be compared to lowerC (will verify the cases where we do know how to convert)
                    if (lowerCurrentChar == lowerC || currentChar == c) {
                        int addedWeight = j == 0 ? mTypedLetterMultiplier : 1;
                        //note: we are suggesting the word in the b-tree, not the one
                        //the user typed. We want to keep capitalized letters, quotes etc.
                        mWord[depth] = c;
                        if (mInputLength == inputIndex + 1) {
                            if (terminal) {
                                int finalFreq = 0;
                                if (sameAsTyped(mWord, depth + 1)) {
                                    if (INCLUDE_WORD_IF_VALID) {
                                        finalFreq = 16 * 1024;
                                    } else {
                                        finalFreq = 0;
                                    }
                                } else {
                                    finalFreq = freq * snr * addedWeight;
                                }
                                if (mSkipPos < 0) finalFreq *= mFullWordMultiplier;
                                addWord(mWord, depth + 1, finalFreq);
                            }
                            if (childrenAddress != 0) {
                                getWordsRec(childrenAddress, depth + 1,
                                            maxDepth, true, snr * addedWeight, inputIndex + 1,
                                            diffs + (j > 0));
                            }
                        } else if (childrenAddress != 0) {
                            getWordsRec(childrenAddress, depth + 1, maxDepth,
                                        false, snr * addedWeight, inputIndex + 1, diffs + (j > 0));
                        }
                    }
                    j++;
                    if (mSkipPos >= 0) break;
                }
            }
        }
    }

    bool
    Dictionary::checkFirstCharacter(unsigned short *word) {
        // Checks whether this word starts with same character or neighboring characters of
        // what user typed.

        int *inputCodes = mInputCodes;
        int maxAlt = mMaxAlternatives;
        while (maxAlt > 0) {
            if ((unsigned int) *inputCodes == (unsigned int) *word) {
                return true;
            }
            inputCodes++;
            maxAlt--;
        }
        return false;
    }

    bool
    Dictionary::isValidWord(unsigned short *word, int length) {
        bool isValid = (isValidWordRec(0, word, 0, length) != NOT_VALID_WORD);

        if (!isValid) {
            //checking the special case when the word is capitalized
            const unsigned short lowerCaseFirstCharacter = CharUtils::toBaseLowerCase(word[0]);
            if (lowerCaseFirstCharacter == word[0])
                return false;

            word[0] = lowerCaseFirstCharacter;
            isValid = (isValidWordRec(0, word, 0, length) != NOT_VALID_WORD);
        }

        return isValid;
    }

    void Dictionary::countWordsHelper(int pos, int depth, int &wordCount, int &wordsCharsCount,
                                      unsigned short *&words, int *&freqs) {
        if (depth + 1 >= 128) return;
        const int count = getCount(&pos);

        const bool fillWord = words != NULL;

        for (int i = 0; i < count; i++) {
            const unsigned short c = getChar(&pos);
            const bool terminal = getTerminal(&pos);
            const int childrenAddress = getAddress(&pos);
            mWord[depth] = c;

            if (terminal) {
                const int freq = getFreq(&pos);
                if (fillWord) {
                    (*freqs) = freq;
                    mWord[depth + 1] = 0x00;
                    memcpy(words, mWord, (size_t) (depth + 2) * sizeof(mWord[0]));
                    words += depth + 2;
                    freqs++;
                }
                wordsCharsCount += depth + 3;
                wordCount++;
            }
            if (childrenAddress != 0) {
                countWordsHelper(childrenAddress, depth + 1, wordCount, wordsCharsCount, words,
                                 freqs);
            }
        }
    }

    int
    Dictionary::isValidWordRec(int pos, unsigned short *word, int offset, int length) {
        // returns address of bigram data of that word
        // return -99 if not found

        int count = getCount(&pos);
        const unsigned short currentChar = word[offset];
        for (int j = 0; j < count; j++) {
            const unsigned short c = getChar(&pos);
            int terminal = getTerminal(&pos);
            int childPos = getAddress(&pos);
            if (c == currentChar) {
                if (offset == length - 1) {
                    if (terminal) {
                        return (pos + 1);
                    }
                } else {
                    if (childPos != 0) {
                        int t = isValidWordRec(childPos, word, offset + 1, length);
                        if (t > 0) {
                            return t;
                        }
                    }
                }
            }
            if (terminal) {
                // if this is a terminal node, we also need to consume (and advance pos)
                getFreq(&pos);
            }
            // There could be two instances of each alphabet - upper and lower case. So continue
            // looking ...
        }
        return NOT_VALID_WORD;
    }
} // namespace nativeime
