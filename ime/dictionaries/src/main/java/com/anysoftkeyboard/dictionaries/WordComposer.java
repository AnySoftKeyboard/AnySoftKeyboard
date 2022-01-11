/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.dictionaries;

import android.text.TextUtils;

import com.anysoftkeyboard.api.KeyCodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A place to store the currently composing word with information such as adjacent key codes as well
 */
public class WordComposer implements KeyCodesProvider {
    public static final int NOT_A_KEY_INDEX = -1;
    public static final char START_TAGS_SEARCH_CHARACTER = ':';

    private static final int MAX_POSSIBLE_SUB_WORDS = 2;
    private final ArrayList<SimpleKeysProvider> mPossibleSubWordsWorkSpace = new ArrayList<>();
    private final ArrayList<SimpleKeysProvider> mPossibleSubWordsReturn = new ArrayList<>();
    private static final int[] EMPTY_CODES_ARRAY = new int[0];
    /** The list of unicode values for each keystroke (including surrounding keys) */
    private final ArrayList<int[]> mCodes = new ArrayList<>(Dictionary.MAX_WORD_LENGTH);

    /** This holds arrays for reuse. Will not exceed AndroidUserDictionary.MAX_WORD_LENGTH */
    private final List<int[]> mArraysToReuse = new ArrayList<>(Dictionary.MAX_WORD_LENGTH);

    /** The word chosen from the candidate list, until it is committed. */
    private CharSequence mPreferredWord;

    private final StringBuilder mTypedWord = new StringBuilder(Dictionary.MAX_WORD_LENGTH);

    private int mCursorPosition;

    private int mCapsCount;

    private boolean mAutoCapitalized;

    /** Whether the user chose to capitalize the first char of the word. */
    private boolean mIsFirstCharCapitalized;

    public WordComposer() {
        while (mPossibleSubWordsWorkSpace.size() <= MAX_POSSIBLE_SUB_WORDS)
            mPossibleSubWordsWorkSpace.add(new SimpleKeysProvider());
    }

    public void cloneInto(WordComposer newWord) {
        newWord.reset();
        for (int[] codes : mCodes) {
            int[] newCodes = new int[codes.length];
            System.arraycopy(codes, 0, newCodes, 0, codes.length);
            newWord.mCodes.add(newCodes);
        }
        newWord.mTypedWord.append(mTypedWord);
        newWord.mPreferredWord = mPreferredWord;
        newWord.mAutoCapitalized = mAutoCapitalized;
        newWord.mCapsCount = mCapsCount;
        newWord.mCursorPosition = mCursorPosition;
        newWord.mIsFirstCharCapitalized = mIsFirstCharCapitalized;
    }

    /** Clear out the keys registered so far. */
    public void reset() {
        // moving arrays back to re-use list
        mArraysToReuse.addAll(mCodes);
        if (mArraysToReuse.size() > 1024) mArraysToReuse.clear();
        mCodes.clear();
        mIsFirstCharCapitalized = false;
        mPreferredWord = null;
        mTypedWord.setLength(0);
        mCapsCount = 0;
        mCursorPosition = 0;
    }

    /**
     * Returns a list of words can be constructed from the typed word
     * if a key was SPACE rather and a letter. Never returns this.
     */
    public List<? extends KeyCodesProvider> getPossibleSubWords() {
        mPossibleSubWordsReturn.clear();
        int providerIndex = 0;
        SimpleKeysProvider keyCodesProvider = mPossibleSubWordsWorkSpace.get(providerIndex);
        keyCodesProvider.reset();
        //looking for keys which are close to SPACE
        for(int keyIndex=0; keyIndex<mCodes.size(); keyIndex++) {
            final int[] nearByCodes = mCodes.get(keyIndex);
            if (hasSpaceInCodes(nearByCodes)) {
                if (keyCodesProvider.mCodes.size() > 0) {
                    providerIndex++;
                    if (providerIndex == MAX_POSSIBLE_SUB_WORDS) break;
                    keyCodesProvider = mPossibleSubWordsWorkSpace.get(providerIndex);
                    keyCodesProvider.reset();
                }
            } else {
                keyCodesProvider.addTypedCode(mTypedWord.codePointAt(keyIndex), mCodes.get(keyIndex));
                if (keyCodesProvider.mCodes.size() == 1) {
                    mPossibleSubWordsReturn.add(keyCodesProvider);
                }
            }
        }

        if (keyCodesProvider.codePointCount() == codePointCount())
            return Collections.emptyList();
        return mPossibleSubWordsReturn;
    }

    private static boolean hasSpaceInCodes(int[] nearByCodes) {
        for (final int nearByCode : nearByCodes) {
            if (nearByCode == 0)
                return false;//the end of the array
            else if (nearByCode == KeyCodes.SPACE)
                return true;
        }
        return false;
    }

    /**
     * Number of keystrokes (codepoints, not chars) in the composing word.
     *
     * @return the number of keystrokes
     */
    @Override
    public int codePointCount() {
        return mCodes.size();
    }

    /**
     * Number of chars in the composing word.
     *
     * @return the number of chars
     */
    public int charCount() {
        return mTypedWord.length();
    }

    /** Cursor position (in characters count!) */
    public int cursorPosition() {
        return mCursorPosition;
    }

    public void setCursorPosition(int position) {
        mCursorPosition = position;
    }

    /**
     * Returns the codes at a particular position in the word.
     *
     * @param index the position in the word (measured in Unicode codepoints, not chars)
     * @return the unicode for the pressed and surrounding keys
     */
    @Override
    public int[] getCodesAt(int index) {
        return mCodes.get(index);
    }

    private static final int[] PRIMARY_CODE_CREATE = new int[1];

    /**
     * Add a new keystroke, with codes[0] containing the pressed key's unicode and the rest of the
     * array containing unicode for adjacent keys, sorted by reducing probability/proximity.
     *
     * @param codes the array of unicode values
     */
    public void add(int primaryCode, int[] codes) {
        PRIMARY_CODE_CREATE[0] = primaryCode;
        mTypedWord.insert(mCursorPosition, new String(PRIMARY_CODE_CREATE, 0, 1));

        correctPrimaryJuxtapos(primaryCode, codes);
        // this will return a copy of the codes array, stored in an array with sufficent storage
        int[] reusableArray = getReusableArray(codes);
        mCodes.add(mTypedWord.codePointCount(0, mCursorPosition), reusableArray);
        mCursorPosition += Character.charCount(primaryCode);
        if (Character.isUpperCase(primaryCode)) mCapsCount++;
    }

    public void simulateTypedWord(CharSequence typedWord) {
        mCursorPosition -= charCount();

        mTypedWord.setLength(0);
        mTypedWord.insert(mCursorPosition, typedWord);

        int index = 0;
        while (index < typedWord.length()) {
            final int codePoint = Character.codePointAt(typedWord, index);
            mCodes.add(mCursorPosition, EMPTY_CODES_ARRAY);
            if (Character.isUpperCase(codePoint)) mCapsCount++;
            index += Character.charCount(codePoint);
        }
        mCursorPosition += typedWord.length();
    }

    private int[] getReusableArray(int[] codes) {
        while (mArraysToReuse.size() > 0) {
            int[] possibleArray = mArraysToReuse.remove(0);
            // is it usable in this situation?
            if (possibleArray.length >= codes.length) {
                System.arraycopy(codes, 0, possibleArray, 0, codes.length);
                if (possibleArray.length > codes.length)
                    Arrays.fill(possibleArray, codes.length, possibleArray.length, NOT_A_KEY_INDEX);
                return possibleArray;
            }
        }
        // if I got here, it means that the reusableArray does not contain a long enough array
        int[] newArray = new int[codes.length];
        mArraysToReuse.add(newArray);
        return getReusableArray(codes);
    }

    /**
     * Swaps the first and second values in the codes array if the primary code is not the first
     * value in the array but the second. This happens when the preferred key is not the key that
     * the user released the finger on.
     *
     * @param primaryCode the preferred character
     * @param nearByKeyCodes array of codes based on distance from touch point
     */
    private static void correctPrimaryJuxtapos(int primaryCode, int[] nearByKeyCodes) {
        if (nearByKeyCodes != null
                && nearByKeyCodes.length > 1
                && primaryCode != nearByKeyCodes[0]
                && primaryCode != Character.toLowerCase(nearByKeyCodes[0])) {
            int swappedItem = nearByKeyCodes[0];
            nearByKeyCodes[0] = primaryCode;
            boolean found = false;
            for (int i = 1; i < nearByKeyCodes.length; i++) {
                if (nearByKeyCodes[i] == primaryCode) {
                    nearByKeyCodes[i] = swappedItem;
                    found = true;
                    break;
                }
            }
            if (!found) // reverting
            nearByKeyCodes[0] = swappedItem;
        }
    }

    public void deleteTextAtCurrentPositionTillEnd(CharSequence typedTextToDeleteAtEnd) {
        final String suffixToDelete = typedTextToDeleteAtEnd.toString();
        if (mTypedWord.toString().endsWith(suffixToDelete)) {
            mTypedWord.setLength(mTypedWord.length() - suffixToDelete.length());
            int codePointsToDelete =
                    Character.codePointCount(suffixToDelete, 0, suffixToDelete.length());
            mCursorPosition -= codePointsToDelete;
            while (codePointsToDelete > 0) {
                mArraysToReuse.add(mCodes.remove(mCodes.size() - 1));
                codePointsToDelete--;
            }
        } else if (BuildConfig.DEBUG) {
            throw new IllegalStateException(
                    "mTypedWord is '"
                            + mTypedWord.toString()
                            + "' while asking to delete '"
                            + typedTextToDeleteAtEnd
                            + "'.");
        } else {
            reset();
        }
    }

    /**
     * Delete the last keystroke (codepoint) as a result of hitting backspace.
     *
     * @return the number of chars (not codepoints) deleted.
     */
    public int deleteCodePointAtCurrentPosition() {
        if (mCursorPosition > 0) {
            // removing from the codes list, and taking it back to the reusable list
            final int codePointsTillCurrentPosition = mTypedWord.codePointCount(0, mCursorPosition);
            mArraysToReuse.add(mCodes.remove(codePointsTillCurrentPosition - 1));
            final int lastCodePoint = Character.codePointBefore(mTypedWord, mCursorPosition);
            final int lastCodePointLength = Character.charCount(lastCodePoint);
            mTypedWord.delete(mCursorPosition - lastCodePointLength, mCursorPosition);
            mCursorPosition -= lastCodePointLength;
            if (Character.isUpperCase(lastCodePoint)) mCapsCount--;
            return lastCodePointLength;
        } else {
            return 0;
        }
    }

    /**
     * Delete the character after the cursor
     *
     * @return the number of chars (not codepoints) deleted.
     */
    public int deleteForward() {
        if (mCursorPosition < charCount()) {
            mArraysToReuse.add(mCodes.remove(mTypedWord.codePointCount(0, mCursorPosition)));
            int last = Character.codePointAt(mTypedWord, mCursorPosition);
            mTypedWord.delete(mCursorPosition, mCursorPosition + Character.charCount(last));
            if (Character.isUpperCase(last)) mCapsCount--;
            return Character.charCount(last);
        } else {
            return 0;
        }
    }

    /**
     * Returns the word as it was typed, without any correction applied.
     *
     * @return the word that was typed so far
     */
    @Override
    public CharSequence getTypedWord() {
        return mCodes.size() == 0 ? "" : mTypedWord.toString();
    }

    public boolean isAtTagsSearchState() {
        return charCount() > 0 && mTypedWord.charAt(0) == ':';
    }

    public void setFirstCharCapitalized(boolean capitalized) {
        mIsFirstCharCapitalized = capitalized;
    }

    /**
     * Whether or not the user typed a capital letter as the first letter in the word
     *
     * @return capitalization preference
     */
    public boolean isFirstCharCapitalized() {
        return mIsFirstCharCapitalized;
    }

    /**
     * Whether or not all of the user typed chars are upper case
     *
     * @return true if all user typed chars are upper case, false otherwise
     */
    public boolean isAllUpperCase() {
        return (mCapsCount > 0) && (mCapsCount == mCodes.size());
    }

    /** Stores the user's selected word, before it is actually committed to the text field. */
    public void setPreferredWord(CharSequence preferred) {
        mPreferredWord = preferred;
    }

    public CharSequence getPreferredWord() {
        return TextUtils.isEmpty(mPreferredWord) ? getTypedWord() : mPreferredWord.toString();
    }

    /** Returns true if more than one character is upper case, otherwise returns false. */
    public boolean isMostlyCaps() {
        return mCapsCount > 1;
    }

    /**
     * Saves the reason why the word is capitalized - whether it was automatic or due to the user
     * hitting shift in the middle of a sentence.
     *
     * @param auto whether it was an automatic capitalization due to start of sentence
     */
    public void setAutoCapitalized(boolean auto) {
        mAutoCapitalized = auto;
    }

    /**
     * Returns whether the word was automatically capitalized.
     *
     * @return whether the word was automatically capitalized
     */
    public boolean isAutoCapitalized() {
        return mAutoCapitalized;
    }

    public String logCodes() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Word: ")
                .append(mTypedWord)
                .append(", preferred word:")
                .append(mPreferredWord);
        int i = 0;
        for (int[] codes : mCodes) {
            stringBuilder.append("\n");
            stringBuilder.append("Codes #").append(i).append(": ");
            for (int c : codes) {
                stringBuilder.append(c).append(",");
            }
        }
        return stringBuilder.toString();
    }

    public boolean isEmpty() {
        return mCodes.isEmpty();
    }

    private static class SimpleKeysProvider implements KeyCodesProvider {
        private final List<int[]> mCodes = new ArrayList<>(Dictionary.MAX_WORD_LENGTH);
        private final StringBuilder mTypedWord = new StringBuilder();
        @Override
        public int codePointCount() {
            return mCodes.size();
        }

        @Override
        public int[] getCodesAt(int index) {
            return mCodes.get(index);
        }

        @Override
        public CharSequence getTypedWord() {
            return mTypedWord;
        }

        void reset() {
            mCodes.clear();
            mTypedWord.setLength(0);
        }

        public void addTypedCode(int codePoint, int[] nearByCodes) {
            mTypedWord.appendCodePoint(codePoint);
            mCodes.add(nearByCodes);
        }
    }
}
