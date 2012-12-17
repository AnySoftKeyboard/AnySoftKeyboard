/*
 * Copyright (C) 2008 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.anysoftkeyboard;

import java.util.ArrayList;

import com.menny.android.anysoftkeyboard.AnyApplication;

import android.util.Log;

/**
 * A place to store the currently composing word with information such as adjacent key codes as well
 */
public class WordComposer {
	private static final String CHEWBACCAONTHEDRUMS = "chewbacca";
	private static final String TAG = "ASK _WC";
    /**
     * The list of unicode values for each keystroke (including surrounding keys)
     */
    private final ArrayList<int[]> mCodes;
    
    /**
     * The word chosen from the candidate list, until it is committed.
     */
    private CharSequence mPreferredWord;
    
    private final StringBuilder mTypedWord;
    
    private int mCursorPosition;
    private int mGlobalCursorPosition;

    private int mCapsCount;

    private boolean mAutoCapitalized;
    
    /**
     * Whether the user chose to capitalize the first char of the word.
     */
    private boolean mIsFirstCharCapitalized;

    public WordComposer() {
        mCodes = new ArrayList<int[]>(12);
        mTypedWord = new StringBuilder(20);
    }
/*
    WordComposer(WordComposer copy) {
        mCodes = new ArrayList<int[]>(copy.mCodes);
        mPreferredWord = copy.mPreferredWord;
        mTypedWord = new StringBuilder(copy.mTypedWord);
        mCapsCount = copy.mCapsCount;
        mAutoCapitalized = copy.mAutoCapitalized;
        mIsFirstCharCapitalized = copy.mIsFirstCharCapitalized;
    }
*/
    /**
     * Clear out the keys registered so far.
     */
    public void reset() {
        mCodes.clear();
        mIsFirstCharCapitalized = false;
        mPreferredWord = null;
        mTypedWord.setLength(0);
        mCapsCount = 0;
        mCursorPosition = 0;
        mGlobalCursorPosition = 0;
    }

    /**
     * Number of keystrokes in the composing word.
     * @return the number of keystrokes
     */
    public int size() {
        return mTypedWord.length();
    }
    
    /**
     * Cursor position
     */
    public int cursorPosition() {
        return mCursorPosition;
    }
    
    public int globalCursorPosition() {
    	return mGlobalCursorPosition;
    }
    
    public void setGlobalCursorPosition(int position) { 
    	mGlobalCursorPosition = position;
    }
    
    public boolean setCursorPostion(int position/*, int candidatesStartPosition*/)
    {
    	if (position < 0 || position > size())//note: the cursor can be AFTER the word, so it can be equal to size()
    	{
    		Log.w(TAG, "New cursor position is invalid! It is outside the word (size "+size()+", new position "+position+". Disregarding!!!!");
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
     * @param index the position in the word
     * @return the unicode for the pressed and surrounding keys
     */
    public int[] getCodesAt(int index) {
        return mCodes.get(index);
    }

    /**
     * Add a new keystroke, with codes[0] containing the pressed key's unicode and the rest of
     * the array containing unicode for adjacent keys, sorted by reducing probability/proximity.
     * @param codes the array of unicode values
     */
    public boolean add(int primaryCode, int[] codes) {
    	
        mTypedWord.insert(mCursorPosition, (char) primaryCode);
        /*if (codes != null)
        {
        	for(int i=0; i<codes.length; i++)
        	{
        		if (codes[i] > 32) codes[i] = Character.toLowerCase(codes[i]);
        	}
        }*/
        
        correctPrimaryJuxtapos(primaryCode, codes);
        mCodes.add(mCursorPosition, codes);
        mCursorPosition++;
        if (Character.isUpperCase((char) primaryCode)) mCapsCount++;
		
		if (mTypedWord.length() == CHEWBACCAONTHEDRUMS.length())
        {
        	if (mTypedWord.toString().equalsIgnoreCase(CHEWBACCAONTHEDRUMS))
        	{
        		return true;
        	}
        }
		
		return false;
    }

    /**
     * Swaps the first and second values in the codes array if the primary code is not the first
     * value in the array but the second. This happens when the preferred key is not the key that
     * the user released the finger on.
     * @param primaryCode the preferred character
     * @param nearByKeyCodes array of codes based on distance from touch point
     */
    private void correctPrimaryJuxtapos(int primaryCode, int[] nearByKeyCodes) {
        /*if (codes.length < 2) return;
        if (codes[0] > 0 && codes[1] > 0 && codes[0] != primaryCode && codes[1] == primaryCode) {
            codes[1] = codes[0];
            codes[0] = primaryCode;
        }*/
    	if(nearByKeyCodes != null && nearByKeyCodes.length > 1 && primaryCode != nearByKeyCodes[0] && primaryCode != Character.toLowerCase((char)nearByKeyCodes[0])){
			int swapedItem = nearByKeyCodes[0];
			nearByKeyCodes[0] = primaryCode;
			boolean found = false;
			for(int i=1;i<nearByKeyCodes.length; i++)
			{
				if (nearByKeyCodes[i] == primaryCode)
				{
					nearByKeyCodes[i] = swapedItem;
					found = true;
					break;
				}
			}
			if (!found) //reverting
				nearByKeyCodes[0] = swapedItem;
		}
    }
    
    /**
     * Delete the last keystroke as a result of hitting backspace.
     */
    public void deleteLast() {
        if (mCursorPosition > 0) {
            mCodes.remove(mCursorPosition - 1);
            //final int lastPos = mTypedWord.length() - 1;
            char last = mTypedWord.charAt(mCursorPosition - 1);
            mTypedWord.deleteCharAt(mCursorPosition - 1);
            mCursorPosition--;
            if (Character.isUpperCase(last)) mCapsCount--;
        }
    }

    /**
     * Returns the word as it was typed, without any correction applied.
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
     * @return capitalization preference
     */
    public boolean isFirstCharCapitalized() {
        return mIsFirstCharCapitalized;
    }

    /**
     * Whether or not all of the user typed chars are upper case
     * @return true if all user typed chars are upper case, false otherwise
     */
    public boolean isAllUpperCase() {
        return (mCapsCount > 0) && (mCapsCount == size());
    }

    /**
     * Stores the user's selected word, before it is actually committed to the text field.
     * @param preferred
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
     * @param auto whether it was an automatic capitalization due to start of sentence
     */
    public void setAutoCapitalized(boolean auto) {
        mAutoCapitalized = auto;
    }

    /**
     * Returns whether the word was automatically capitalized.
     * @return whether the word was automatically capitalized
     */
    public boolean isAutoCapitalized() {
        return mAutoCapitalized;
    }
	public void logCodes() {
		if (!AnyApplication.DEBUG) return;
		Log.d(TAG, "Word: "+mTypedWord+", prefered word:"+mPreferredWord);
		int i = 0;
		for(int[] codes : mCodes)
		{
			String codesString = "Codes #"+i+": ";
			for(int c : codes)
			{
				codesString += ""+c+",";
			}
			Log.d(TAG, codesString);
		}
	}
}
