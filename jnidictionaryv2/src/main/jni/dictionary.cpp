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
#define LOG_TAG "MYPATH"
#include <android/log.h>
#define LOGI

#include "dictionary.h"
#include "basechars.h"
#include "char_utils.h"

#define DEBUG_DICT 0
#define DICTIONARY_VERSION_MIN 200
#define DICTIONARY_HEADER_SIZE 2
#define NOT_VALID_WORD -99

namespace nativeime {

Dictionary::Dictionary(void *dict, int typedLetterMultiplier, int fullWordMultiplier)
{
    mDict = (unsigned char*) dict;
    mTypedLetterMultiplier = typedLetterMultiplier;
    mFullWordMultiplier = fullWordMultiplier;
    //mPathWords = new PathPossibilities();
    getVersionNumber();
}

Dictionary::~Dictionary()
{
    //delete mPathWords;
}

int Dictionary::getSuggestions(int *codes, int codesSize, unsigned short *outWords, int *frequencies,
        int maxWordLength, int maxWords, int maxAlternatives, int skipPos,
        int *nextLetters, int nextLettersSize)
{
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

    if (checkIfDictVersionIsLatest()) {
        getWordsRec(DICTIONARY_HEADER_SIZE, 0, mInputLength * 3, false, 1, 0, 0);
    } else {
        getWordsRec(0, 0, mInputLength * 3, false, 1, 0, 0);
    }

    // Get the word count
    suggWords = 0;
    while (suggWords < mMaxWords && mFrequencies[suggWords] > 0) suggWords++;
    if (DEBUG_DICT) LOGI("Returning %d words", suggWords);

    if (DEBUG_DICT) {
        LOGI("Next letters: ");
        for (int k = 0; k < nextLettersSize; k++) {
            if (mNextLettersFrequencies[k] > 0) {
                LOGI("%c = %d,", k, mNextLettersFrequencies[k]);
            }
        }
        LOGI("\n");
    }
    return suggWords;
}

int Dictionary::getWordsForPath(int *codes, int codesSize, unsigned short *outWords, int *frequencies,
                                    int maxWordLength, int maxWords) {
    mFrequencies = frequencies;
    mOutputChars = outWords;
    mInputCodes = codes;
    mInputLength = codesSize;
    mMaxWordLength = maxWordLength;
    mMaxWords = maxWords;

    int pos = checkIfDictVersionIsLatest()? DICTIONARY_HEADER_SIZE : 0;
    getWordsForPathRec(pos, 0);

    // Get the word count
    int wordsForPath = 0;
    while (wordsForPath < mMaxWords && mFrequencies[wordsForPath] > 0) wordsForPath++;
    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "found %d words for path. Max words %d, max-length %d", wordsForPath, mMaxWords, mMaxWordLength);

    return wordsForPath;
}

void
Dictionary::getWordsForPathRec(int pos, int depth)
{
    if (depth > mMaxWordLength) {
        return;
    }

    //how many characters we have in this B-node
    const int childrenInNodeCount = getCount(&pos);

    for (int childInNodeIndex = 0; childInNodeIndex < childrenInNodeCount; childInNodeIndex++) {
        // -- at flag/add
        const unsigned short nodeCharacter = getChar(&pos);
        const unsigned short nodeLowerCharacter = toLowerCase(nodeCharacter);
        const bool terminal = getTerminal(&pos);
        const int childrenAddress = getAddress(&pos);
        // -- after address or flag
        int freq = 1;
        if (terminal) freq = getFreq(&pos);
        // -- after add or freq

        //1) we are at the start of the input, in this case currentChar must be exactly lowerC
        //2) we are at a terminal, the character must equals the last character in the input. Note: it may still have children!
        //3) we are somewhere in the middle of the input, in this case we just go deeper.

        if (depth == 0) {
            if (mInputCodes[0] != nodeLowerCharacter && mInputCodes[0] != nodeCharacter) {
                continue;
            }
        } else if (terminal) {
            if (mInputCodes[mInputLength-1] == nodeLowerCharacter || mInputCodes[mInputLength-1] == nodeCharacter) {
                mWord[depth] = nodeLowerCharacter;
                const int foundWordLength = depth+1;
                char s[foundWordLength+1];
                for (int i = 0; i < foundWordLength; i++) s[i] = (char)mWord[i];
                s[foundWordLength] = 0;
                __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "found a possible word '%s' with freq %d", s, freq);
                if (!addWord(mWord, foundWordLength, freq)) {
                    __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "No more space in output-words array. Skipping word.");
                    return;
                }
            }
        }
        if (childrenAddress != 0) {
            mWord[depth] = nodeLowerCharacter;
            getWordsForPathRec(childrenAddress, depth + 1);
        }
    }
}

void
Dictionary::registerNextLetter(unsigned short c)
{
    if (c < mNextLettersSize) {
        mNextLettersFrequencies[c]++;
    }
}

void
Dictionary::getVersionNumber()
{
    mVersion = (mDict[0] & 0xFF);
    mBigram = (mDict[1] & 0xFF);
    LOGI("IN NATIVE SUGGEST Version: %d Bigram : %d \n", mVersion, mBigram);
}

// Checks whether it has the latest dictionary or the old dictionary
bool
Dictionary::checkIfDictVersionIsLatest()
{
    return (mVersion >= DICTIONARY_VERSION_MIN) && (mBigram == 1 || mBigram == 0);
}

unsigned short
Dictionary::getChar(int *pos)
{
    unsigned short ch = (unsigned short) (mDict[(*pos)++] & 0xFF);
    // If the code is 255, then actual 16 bit code follows (in big endian)
    if (ch == 0xFF) {
        ch = ((mDict[*pos] & 0xFF) << 8) | (mDict[*pos + 1] & 0xFF);
        (*pos) += 2;
    }
    return ch;
}

int
Dictionary::getAddress(int *pos)
{
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
Dictionary::getFreq(int *pos)
{
    int freq = mDict[(*pos)++] & 0xFF;

    if (checkIfDictVersionIsLatest()) {
        // skipping bigram
        int bigramExist = (mDict[*pos] & FLAG_BIGRAM_READ);
        if (bigramExist > 0) {
            int nextBigramExist = 1;
            while (nextBigramExist > 0) {
                (*pos) += 3;
                nextBigramExist = (mDict[(*pos)++] & FLAG_BIGRAM_CONTINUED);
            }
        } else {
            (*pos)++;
        }
    }

    return freq;
}

int
Dictionary::wideStrLen(unsigned short *str)
{
    if (!str) return 0;
    unsigned short *end = str;
    while (*end)
        end++;
    return end - str;
}

bool
Dictionary::addWord(unsigned short *word, int length, int frequency)
{
    word[length] = 0;
    if (DEBUG_DICT) {
        char s[length + 1];
        for (int i = 0; i <= length; i++) s[i] = word[i];
        LOGI("Found word = %s, freq = %d : \n", s, frequency);
    }

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
        memmove((char*) mFrequencies + (insertAt + 1) * sizeof(mFrequencies[0]),
               (char*) mFrequencies + insertAt * sizeof(mFrequencies[0]),
               (mMaxWords - insertAt - 1) * sizeof(mFrequencies[0]));
        mFrequencies[insertAt] = frequency;
        memmove((char*) mOutputChars + (insertAt + 1) * mMaxWordLength * sizeof(short),
               (char*) mOutputChars + (insertAt    ) * mMaxWordLength * sizeof(short),
               (mMaxWords - insertAt - 1) * sizeof(short) * mMaxWordLength);
        unsigned short *dest = mOutputChars + (insertAt    ) * mMaxWordLength;
        while (length--) {
            *dest++ = *word++;
        }
        *dest = 0; // NULL terminate
        if (DEBUG_DICT) LOGI("Added word at %d\n", insertAt);
        return true;
    }
    return false;
}

bool
Dictionary::addWordBigram(unsigned short *word, int length, int frequency)
{
    word[length] = 0;
    if (DEBUG_DICT) {
        char s[length + 1];
        for (int i = 0; i <= length; i++) s[i] = word[i];
        LOGI("Bigram: Found word = %s, freq = %d : \n", s, frequency);
    }

    // Find the right insertion point
    int insertAt = 0;
    while (insertAt < mMaxBigrams) {
        if (frequency > mBigramFreq[insertAt]
                 || (mBigramFreq[insertAt] == frequency
                     && length < wideStrLen(mBigramChars + insertAt * mMaxWordLength))) {
            break;
        }
        insertAt++;
    }
    LOGI("Bigram: InsertAt -> %d maxBigrams: %d\n", insertAt, mMaxBigrams);
    if (insertAt < mMaxBigrams) {
        memmove((char*) mBigramFreq + (insertAt + 1) * sizeof(mBigramFreq[0]),
               (char*) mBigramFreq + insertAt * sizeof(mBigramFreq[0]),
               (mMaxBigrams - insertAt - 1) * sizeof(mBigramFreq[0]));
        mBigramFreq[insertAt] = frequency;
        memmove((char*) mBigramChars + (insertAt + 1) * mMaxWordLength * sizeof(short),
               (char*) mBigramChars + (insertAt    ) * mMaxWordLength * sizeof(short),
               (mMaxBigrams - insertAt - 1) * sizeof(short) * mMaxWordLength);
        unsigned short *dest = mBigramChars + (insertAt    ) * mMaxWordLength;
        while (length--) {
            *dest++ = *word++;
        }
        *dest = 0; // NULL terminate
        if (DEBUG_DICT) LOGI("Bigram: Added word at %d\n", insertAt);
        return true;
    }
    return false;
}

unsigned short
Dictionary::toLowerCase(unsigned short c) {
    if (c < sizeof(BASE_CHARS) / sizeof(BASE_CHARS[0])) {
        c = BASE_CHARS[c];
    }
    if (c >='A' && c <= 'Z') {
        c |= 32;
    } else if (c > 127) {
        c = latin_tolower(c);
    }
    return c;
}

bool
Dictionary::sameAsTyped(unsigned short *word, int length)
{
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

static char QUOTE = '\'';

void
Dictionary::getWordsRec(int pos, int depth, int maxDepth, bool completion, int snr, int inputIndex,
                        int diffs)
{
    // Optimization: Prune out words that are too long compared to how much was typed.
    if (depth > maxDepth) {
        return;
    }
    if (diffs > mMaxEditDistance) {
        return;
    }
    const int count = getCount(&pos);
    int *currentChars = 0;
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
        const unsigned short lowerC = toLowerCase(c);
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
        } else if ((c == QUOTE && currentChars[0] != QUOTE) || mSkipPos == depth) {
            // Skip the ' or other letter and continue deeper
            mWord[depth] = c;
            if (childrenAddress != 0) {
                getWordsRec(childrenAddress, depth + 1, maxDepth, false, snr, inputIndex, diffs);
            }
        } else {
            int j = 0;
            while (currentChars[j] > 0) {
                const unsigned short currentChar = (const unsigned short) currentChars[j];
                const unsigned short lowerCurrentChar = toLowerCase(currentChar);
                //currentChar can be upper or lower
                //c can be upper or lower
                //lowerC is lower or c (in the case where we do not know how to convert to lower)
                //lowerCurrentChar is lower or c  (in the case where we do not know how to convert to lower)
                //so, c must be checked against currentChar (in cases where we do not know how to convert)
                //and lowerCurrent should be compared to lowerC (will verify the cases where we do know how to convert)
                if (lowerCurrentChar == lowerC || currentChar == c) {
                    int addedWeight = j == 0 ? mTypedLetterMultiplier : 1;
                    mWord[depth] = c;
                    if (mInputLength == inputIndex + 1) {
                        if (terminal) {
                            if (//INCLUDE_TYPED_WORD_IF_VALID ||
                                !sameAsTyped(mWord, depth + 1)) {
                                int finalFreq = freq * snr * addedWeight;
                                if (mSkipPos < 0) finalFreq *= mFullWordMultiplier;
                                addWord(mWord, depth + 1, finalFreq);
                            }
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

int
Dictionary::getBigramAddress(int *pos, bool advance)
{
    int address = 0;

    address += (mDict[*pos] & 0x3F) << 16;
    address += (mDict[*pos + 1] & 0xFF) << 8;
    address += (mDict[*pos + 2] & 0xFF);

    if (advance) {
        *pos += 3;
    }

    return address;
}

int
Dictionary::getBigramFreq(int *pos)
{
    int freq = mDict[(*pos)++] & FLAG_BIGRAM_FREQ;

    return freq;
}


int
Dictionary::getBigrams(unsigned short *prevWord, int prevWordLength, int *codes, int codesSize,
        unsigned short *bigramChars, int *bigramFreq, int maxWordLength, int maxBigrams,
        int maxAlternatives)
{
    mBigramFreq = bigramFreq;
    mBigramChars = bigramChars;
    mInputCodes = codes;
    mInputLength = codesSize;
    mMaxWordLength = maxWordLength;
    mMaxBigrams = maxBigrams;
    mMaxAlternatives = maxAlternatives;

    if (mBigram == 1 && checkIfDictVersionIsLatest()) {
        int pos = isValidWordRec(DICTIONARY_HEADER_SIZE, prevWord, 0, prevWordLength);
        LOGI("Pos -> %d\n", pos);
        if (pos < 0) {
            return 0;
        }

        int bigramCount = 0;
        int bigramExist = (mDict[pos] & FLAG_BIGRAM_READ);
        if (bigramExist > 0) {
            int nextBigramExist = 1;
            while (nextBigramExist > 0 && bigramCount < maxBigrams) {
                int bigramAddress = getBigramAddress(&pos, true);
                int frequency = (FLAG_BIGRAM_FREQ & mDict[pos]);
                // search for all bigrams and store them
                searchForTerminalNode(bigramAddress, frequency);
                nextBigramExist = (mDict[pos++] & FLAG_BIGRAM_CONTINUED);
                bigramCount++;
            }
        }

        return bigramCount;
    }
    return 0;
}

void
Dictionary::searchForTerminalNode(int addressLookingFor, int frequency)
{
    // track word with such address and store it in an array
    unsigned short word[mMaxWordLength];

    int pos;
    int followDownBranchAddress = DICTIONARY_HEADER_SIZE;
    bool found = false;
    char followingChar = ' ';
    int depth = -1;

    while(!found) {
        bool followDownAddressSearchStop = false;
        bool firstAddress = true;
        bool haveToSearchAll = true;

        if (depth >= 0) {
            word[depth] = (unsigned short) followingChar;
        }
        pos = followDownBranchAddress; // pos start at count
        int count = mDict[pos] & 0xFF;
        LOGI("count - %d\n",count);
        pos++;
        for (int i = 0; i < count; i++) {
            // pos at data
            pos++;
            // pos now at flag
            if (!getFirstBitOfByte(&pos)) { // non-terminal
                if (!followDownAddressSearchStop) {
                    int addr = getBigramAddress(&pos, false);
                    if (addr > addressLookingFor) {
                        followDownAddressSearchStop = true;
                        if (firstAddress) {
                            firstAddress = false;
                            haveToSearchAll = true;
                        } else if (!haveToSearchAll) {
                            break;
                        }
                    } else {
                        followDownBranchAddress = addr;
                        followingChar = (char)(0xFF & mDict[pos-1]);
                        if (firstAddress) {
                            firstAddress = false;
                            haveToSearchAll = false;
                        }
                    }
                }
                pos += 3;
            } else if (getFirstBitOfByte(&pos)) { // terminal
                if (addressLookingFor == (pos-1)) { // found !!
                    depth++;
                    word[depth] = (0xFF & mDict[pos-1]);
                    found = true;
                    break;
                }
                if (getSecondBitOfByte(&pos)) { // address + freq (4 byte)
                    if (!followDownAddressSearchStop) {
                        int addr = getBigramAddress(&pos, false);
                        if (addr > addressLookingFor) {
                            followDownAddressSearchStop = true;
                            if (firstAddress) {
                                firstAddress = false;
                                haveToSearchAll = true;
                            } else if (!haveToSearchAll) {
                                break;
                            }
                        } else {
                            followDownBranchAddress = addr;
                            followingChar = (char)(0xFF & mDict[pos-1]);
                            if (firstAddress) {
                                firstAddress = false;
                                haveToSearchAll = true;
                            }
                        }
                    }
                    pos += 4;
                } else { // freq only (2 byte)
                    pos += 2;
                }

                // skipping bigram
                int bigramExist = (mDict[pos] & FLAG_BIGRAM_READ);
                if (bigramExist > 0) {
                    int nextBigramExist = 1;
                    while (nextBigramExist > 0) {
                        pos += 3;
                        nextBigramExist = (mDict[pos++] & FLAG_BIGRAM_CONTINUED);
                    }
                } else {
                    pos++;
                }
            }
        }
        depth++;
        if (followDownBranchAddress == 0) {
            LOGI("ERROR!!! Cannot find bigram!!");
            break;
        }
    }
    if (checkFirstCharacter(word)) {
        addWordBigram(word, depth, frequency);
    }
}

bool
Dictionary::checkFirstCharacter(unsigned short *word)
{
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
Dictionary::isValidWord(unsigned short *word, int length)
{
    bool isValid;
    if (checkIfDictVersionIsLatest()) {
        isValid = (isValidWordRec(DICTIONARY_HEADER_SIZE, word, 0, length) != NOT_VALID_WORD);
    } else {
        isValid = (isValidWordRec(0, word, 0, length) != NOT_VALID_WORD);
    }

    if (!isValid) {
        //checking the special case when the word is capitalized
        const unsigned short lowerCaseFirstCharacter = toLowerCase(word[0]);
        if (lowerCaseFirstCharacter == word[0])
            return false;

        word[0] = lowerCaseFirstCharacter;
        if (checkIfDictVersionIsLatest()) {
            isValid = (isValidWordRec(DICTIONARY_HEADER_SIZE, word, 0, length) != NOT_VALID_WORD);
        } else {
            isValid = (isValidWordRec(0, word, 0, length) != NOT_VALID_WORD);
        }
    }

    return isValid;
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
                    return (pos+1);
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
            getFreq(&pos);
        }
        // There could be two instances of each alphabet - upper and lower case. So continue
        // looking ...
    }
    return NOT_VALID_WORD;
}


} // namespace nativeime
