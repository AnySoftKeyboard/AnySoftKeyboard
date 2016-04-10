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

package com.anysoftkeyboard.base.dictionaries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A place to store the currently composing word with information such as adjacent key codes as well
 */
public class WordComposer {
    public static final int NOT_A_KEY_INDEX = -1;
    /**
     * The list of unicode values for each keystroke (including surrounding keys)
     */
    private final ArrayList<int[]> mCodes = new ArrayList<>(Dictionary.MAX_WORD_LENGTH);

    /**
     * This holds arrays for reuse. Will not exceed AndroidUserDictionary.MAX_WORD_LENGTH
     */
    private final List<int[]> mArraysToReuse = new ArrayList<>(Dictionary.MAX_WORD_LENGTH);

    /**
     * The word chosen from the candidate list, until it is committed.
     */
    private CharSequence mPreferredWord;

    private final StringBuilder mTypedWord = new StringBuilder(Dictionary.MAX_WORD_LENGTH);

    private int mCursorPosition;

    private int mCapsCount;

    private boolean mAutoCapitalized;

    /**
     * Whether the user chose to capitalize the first char of the word.
     */
    private boolean mIsFirstCharCapitalized;

    public WordComposer() {
    }

    /**
     * Clear out the keys registered so far.
     */
    public void reset() {
        //moving arrays back to re-use list
        for (int[] array : mCodes) {
            mArraysToReuse.add(array);
        }
        mCodes.clear();
        mIsFirstCharCapitalized = false;
        mPreferredWord = null;
        mTypedWord.setLength(0);
        mCapsCount = 0;
        mCursorPosition = 0;
    }

    /**
     * Number of keystrokes in the composing word.
     *
     * @return the number of keystrokes
     */
    public int length() {
        return mTypedWord.length();
    }

    /**
     * Cursor position
     */
    public int cursorPosition() {
        return mCursorPosition;
    }

    public boolean setCursorPosition(int position/*, int candidatesStartPosition*/) {
        if (position < 0 || position > length()) {
            //note: the cursor can be AFTER the word, so it can be equal to size()
            return false;
        }
        final boolean changed = mCursorPosition != position;
        mCursorPosition = position;
        return changed;
        //mCandidatesStartPosition = candidatesStartPosition;
    }
    /*
    public boolean hasUserMovedCursor(int cursorPosition)
    {
        if (AnyApplication.DEBUG)
        {
            Log.d(TAG, "Current cursor position inside word is "+mCursorPosition+", and word starts at "+mCandidatesStartPosition+". Input's cursor is at "+cursorPosition);
        }
        return (cursorPosition != (mCursorPosition + mCandidatesStartPosition));
    }
    
    public boolean hasUserMovedCursorInsideOfWord(int cursorPosition)
    {
        if (AnyApplication.DEBUG)
        {
            Log.d(TAG, "Current word length is "+mTypedWord.length()+", and word starts at "+mCandidatesStartPosition+". Input's cursor is at "+cursorPosition);
        }
        return (cursorPosition >= mCandidatesStartPosition &&  cursorPosition <= (mCandidatesStartPosition+mTypedWord.length()));
    }
    */

    /**
     * Returns the codes at a particular position in the word.
     *
     * @param index the position in the word
     * @return the unicode for the pressed and surrounding keys
     */
    public int[] getCodesAt(int index) {
        return mCodes.get(index);
    }

    /**
     * Add a new keystroke, with codes[0] containing the pressed key's unicode and the rest of
     * the array containing unicode for adjacent keys, sorted by reducing probability/proximity.
     *
     * @param codes the array of unicode values
     */
    public void add(int primaryCode, int[] codes) {

        mTypedWord.insert(mCursorPosition, (char) primaryCode);

        correctPrimaryJuxtapos(primaryCode, codes);
        //this will return a copy of the codes array, stored in an array with sufficent storage 
        int[] reusableArray = getReusableArray(codes);
        mCodes.add(mCursorPosition, reusableArray);
        mCursorPosition++;
        if (Character.isUpperCase((char) primaryCode)) mCapsCount++;
    }

    private int[] getReusableArray(int[] codes) {
        while (mArraysToReuse.size() > 0) {
            int[] possibleArray = mArraysToReuse.remove(0);
            //is it usable in this situation?
            if (possibleArray.length >= codes.length) {
                System.arraycopy(codes, 0, possibleArray, 0, codes.length);
                if (possibleArray.length > codes.length)
                    Arrays.fill(possibleArray, codes.length, possibleArray.length, NOT_A_KEY_INDEX);
                return possibleArray;
            }
        }
        //if I got here, it means that the reusableArray does not contain a long enough array
        int[] newArray = new int[codes.length];
        mArraysToReuse.add(newArray);
        return getReusableArray(codes);
    }

    /**
     * Swaps the first and second values in the codes array if the primary code is not the first
     * value in the array but the second. This happens when the preferred key is not the key that
     * the user released the finger on.
     *
     * @param primaryCode    the preferred character
     * @param nearByKeyCodes array of codes based on distance from touch point
     */
    private static void correctPrimaryJuxtapos(int primaryCode, int[] nearByKeyCodes) {
        /*if (codes.length < 2) return;
        if (codes[0] > 0 && codes[1] > 0 && codes[0] != primaryCode && codes[1] == primaryCode) {
            codes[1] = codes[0];
            codes[0] = primaryCode;
        }*/
        if (nearByKeyCodes != null && nearByKeyCodes.length > 1 && primaryCode != nearByKeyCodes[0] && primaryCode != Character.toLowerCase((char) nearByKeyCodes[0])) {
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
            if (!found) //reverting
                nearByKeyCodes[0] = swappedItem;
        }
    }

    /**
     * Delete the last keystroke as a result of hitting backspace.
     */
    public void deleteLast() {
        if (mCursorPosition > 0) {
            //removing from the codes list, and taking it back to the reusable list
            mArraysToReuse.add(mCodes.remove(mCursorPosition - 1));
            //final int lastPos = mTypedWord.length() - 1;
            char last = mTypedWord.charAt(mCursorPosition - 1);
            mTypedWord.deleteCharAt(mCursorPosition - 1);
            mCursorPosition--;
            if (Character.isUpperCase(last)) mCapsCount--;
        }
    }

    /**
     * Returns the word as it was typed, without any correction applied.
     *
     * @return the word that was typed so far
     */
    public CharSequence getTypedWord() {
        int wordSize = mCodes.size();
        if (wordSize == 0) {
            return "";
        }
        return mTypedWord;
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
        return (mCapsCount > 0) && (mCapsCount == length());
    }

    /**
     * Stores the user's selected word, before it is actually committed to the text field.
     */
    public void setPreferredWord(CharSequence preferred) {
        mPreferredWord = preferred;
    }

    public CharSequence getPreferredWord() {
        return mPreferredWord;
    }

    /**
     * Returns true if more than one character is upper case, otherwise returns false.
     */
    public boolean isMostlyCaps() {
        return mCapsCount > 1;
    }

    /**
     * Saves the reason why the word is capitalized - whether it was automatic or
     * due to the user hitting shift in the middle of a sentence.
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
        stringBuilder.append("Word: ").append(mTypedWord).append(", preferred word:").append(mPreferredWord);
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
}
