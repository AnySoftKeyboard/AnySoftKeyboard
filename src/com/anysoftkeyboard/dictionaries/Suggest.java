/*
 * Copyright (C) 2011 AnySoftKeyboard.
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

package com.anysoftkeyboard.dictionaries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anysoftkeyboard.WordComposer;
import com.anysoftkeyboard.utils.IMEUtil;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

/**
 * This class loads a dictionary and provides a list of suggestions for a given
 * sequence of characters. This includes corrections and completions.
 * 
 * @hide pending API Council Approval
 */
public class Suggest implements Dictionary.WordCallback {

//	public static final int CORRECTION_NONE = 0;
//	public static final int CORRECTION_BASIC = 1;
//	public static final int CORRECTION_FULL = 2;
	private static final String TAG = "ASK Suggest";

	private Dictionary mMainDict;
	private AutoText mAutoText;
	
	private int mMinimumWordSizeToStartCorrecting = 2;
	
	private Dictionary mUserDictionary;

	private Dictionary mAutoDictionary;

	private Dictionary mContactsDictionary;

	private int mPrefMaxSuggestions = 12;

	private final List<CharSequence> mDefaultInitialSuggestions;
	private List<CharSequence> mInitialSuggestions = new ArrayList<CharSequence>();
	
	private int[] mPriorities = new int[mPrefMaxSuggestions];
	private List<CharSequence> mSuggestions = new ArrayList<CharSequence>();
	// private boolean mIncludeTypedWordIfValid;
	private List<CharSequence> mStringPool = new ArrayList<CharSequence>();
	// private Context mContext;
	private boolean mHaveCorrection;
	private CharSequence mOriginalWord;
	private String mLowerOriginalWord;

	// TODO: Remove these member variables by passing more context to addWord()
	// callback method
	private boolean mIsFirstCharCapitalized;
	private boolean mIsAllUpperCase;

	//private int mCorrectionMode = CORRECTION_FULL;
	private boolean mAutoTextEnabled = true;
	private boolean mMainDictioanryEnabled = true;

	public Suggest(Context context/* , int dictionaryResId */) {
		// mContext = context;
		// mMainDict = new BinaryDictionary(context, dictionaryResId);
		for (int i = 0; i < mPrefMaxSuggestions; i++) {
			StringBuilder sb = new StringBuilder(32);
			mStringPool.add(sb);
		}
		
		String[] initialSuggestions = context.getResources().getStringArray(R.array.english_initial_suggestions);
		if (initialSuggestions != null)
		{
			mDefaultInitialSuggestions = new ArrayList<CharSequence>(initialSuggestions.length);
			for(String suggestion : initialSuggestions)
				mDefaultInitialSuggestions.add(suggestion);
		}
		else
		{
			mDefaultInitialSuggestions = new ArrayList<CharSequence>(0);
		}
	}
/*
	public int getCorrectionMode() {
		return mCorrectionMode;
	}
*/
	public void setCorrectionMode(boolean autoText, boolean mainDictionary) {
		//mCorrectionMode = mode;
		mAutoTextEnabled = autoText;
		mMainDictioanryEnabled = mainDictionary;
	}

	/**
	 * Sets an optional user dictionary resource to be loaded. The user
	 * dictionary is consulted before the main dictionary, if set.
	 */
	public void setUserDictionary(Dictionary userDictionary) {
		if (mUserDictionary != userDictionary && mUserDictionary != null)
			mUserDictionary.close();

		mUserDictionary = userDictionary;
	}

	public void setMainDictionary(DictionaryAddOnAndBuilder dictionaryBuilder) {
		if (AnyApplication.DEBUG)
			Log.d(TAG, "Suggest: Got main dictionary! Type: "
					+ ((dictionaryBuilder == null) ? "NULL" : dictionaryBuilder.getName()));
		if (mMainDict != null) {
			mMainDict.close();
		}
		
		if (dictionaryBuilder == null)
		{
			mMainDict = null;
			mAutoText = null;
			mInitialSuggestions = mDefaultInitialSuggestions;
		}
		else
		{
			try
			{
				System.gc();
				
				mMainDict = dictionaryBuilder.createDictionary();
				mMainDict.loadDictionary();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mAutoText = dictionaryBuilder.createAutoText();
			mInitialSuggestions = dictionaryBuilder.createInitialSuggestions();
			if (mInitialSuggestions == null)
				mInitialSuggestions = mDefaultInitialSuggestions;
		}
	}

	/**
	 * Sets an optional contacts dictionary resource to be loaded.
	 */
	public void setContactsDictionary(Dictionary contactsDictionary) {
		if (mContactsDictionary != contactsDictionary && mContactsDictionary != null)
			mContactsDictionary.close();
		mContactsDictionary = contactsDictionary;
	}

	public void setAutoDictionary(Dictionary autoDictionary) {
		if (mAutoDictionary != autoDictionary && mAutoDictionary != null)
			mAutoDictionary.close();
		mAutoDictionary = autoDictionary;
	}

	/**
	 * Number of suggestions to generate from the input key sequence. This has
	 * to be a number between 1 and 100 (inclusive).
	 * 
	 * @param maxSuggestions
	 * @throws IllegalArgumentException
	 *             if the number is out of range
	 */
	public void setMaxSuggestions(int maxSuggestions) {
		if (maxSuggestions < 1 || maxSuggestions > 100) {
			throw new IllegalArgumentException(
					"maxSuggestions must be between 1 and 100");
		}
		mPrefMaxSuggestions = maxSuggestions;
		mPriorities = new int[mPrefMaxSuggestions];
		collectGarbage();
		while (mStringPool.size() < mPrefMaxSuggestions) {
			StringBuilder sb = new StringBuilder(32);
			mStringPool.add(sb);
		}
	}
	
	private boolean haveSufficientCommonality(String original,
			CharSequence suggestion) {
		final int originalLength = original.length();
		final int suggestionLength = suggestion.length();
		final int lengthDiff = suggestionLength - originalLength;

		if (lengthDiff == 0 || lengthDiff == 1) {
			return true;
		}
		
		final int distance = IMEUtil.editDistance(original, suggestion);
		
		return distance <= 1;
	}

	public List<CharSequence> getInitialSuggestions()
	{
		return mInitialSuggestions;
	}
	
	/**
	 * Returns a list of words that match the list of character codes passed in.
	 * This list will be overwritten the next time this function is called.
	 * 
	 * @param a
	 *            view for retrieving the context for AutoText
	 * @param codes
	 *            the list of codes. Each list item contains an array of
	 *            character codes in order of probability where the character at
	 *            index 0 in the array has the highest probability.
	 * @return list of suggestions.
	 */
	public List<CharSequence> getSuggestions(/*View view,*/ WordComposer wordComposer,
            boolean includeTypedWordIfValid) {
        mHaveCorrection = false;
        mIsFirstCharCapitalized = wordComposer.isFirstCharCapitalized();
        mIsAllUpperCase = wordComposer.isAllUpperCase();
        collectGarbage();
        Arrays.fill(mPriorities, 0);
        // mIncludeTypedWordIfValid = includeTypedWordIfValid;

        // Save a lowercase version of the original word
        mOriginalWord = wordComposer.getTypedWord();
        if (mOriginalWord.length() > 0) {
            mOriginalWord = mOriginalWord.toString();
            mLowerOriginalWord = mOriginalWord.toString().toLowerCase();
        } else {
            mLowerOriginalWord = "";
        }
        // Search the dictionary only if there are at least 2 (configurable) characters
         if (wordComposer.size() >= mMinimumWordSizeToStartCorrecting) {
        	 if (mContactsDictionary != null) {
        		 if (AnyApplication.DEBUG) 
        			 Log.v(TAG, "getSuggestions from contacts-dictionary");
        		 
        		 mContactsDictionary.getWords(wordComposer, this);
        	 }
        	 
        	 if (mUserDictionary != null) {
        		 if (AnyApplication.DEBUG)
        			 Log.v(TAG, "getSuggestions from user-dictionary");
        		 
        		 mUserDictionary.getWords(wordComposer, this);
        	 }
        	 
             if (mSuggestions.size() > 0 && isValidWord(mOriginalWord)) {
                  mHaveCorrection = true;
             }
             
             if (mMainDict != null) {
            	 if (AnyApplication.DEBUG)
            		 Log.v(TAG, "getSuggestions from main-dictionary");
            	 mMainDict.getWords(wordComposer, this);
             }

             if (mMainDictioanryEnabled && mSuggestions.size() > 0) {
                 mHaveCorrection = true;
             }
         }
        
        if (mOriginalWord != null) {
            mSuggestions.add(0, mOriginalWord.toString());
        }
        // Check if the first suggestion has a minimum number of characters in
        // common
        if (mMainDictioanryEnabled && mSuggestions.size() > 1) {
        	
            if (!haveSufficientCommonality(mLowerOriginalWord, mSuggestions.get(1))) {
                mHaveCorrection = false;
            }
        }

        int i = 0;
        int max = 6;
        // Don't autotext the suggestions from the dictionaries
        if (!mMainDictioanryEnabled && mAutoTextEnabled)
            max = 1;
        while (i < mSuggestions.size() && i < max) {
            String suggestedWord = mSuggestions.get(i).toString().toLowerCase();
            
            CharSequence autoText = mAutoTextEnabled && mAutoText != null?
            		mAutoText.lookup(suggestedWord, 0, suggestedWord.length()) : null;
            // Is there an AutoText correction?
            boolean canAdd = autoText != null;
            // Is that correction already the current prediction (or original
            // word)?
            canAdd &= !TextUtils.equals(autoText, mSuggestions.get(i));
            // Is that correction already the next predicted word?
            if (canAdd && i + 1 < mSuggestions.size() && mMainDictioanryEnabled) {
                canAdd &= !TextUtils.equals(autoText, mSuggestions.get(i + 1));
            }
            if (canAdd) {
                mHaveCorrection = true;
                mSuggestions.add(i + 1, autoText);
                i++;
            }
            i++;
        }

        return mSuggestions;
    }

	public boolean hasMinimalCorrection() {
		return mHaveCorrection;
	}

	private static boolean compareCaseInsensitive(
			final String lowerOriginalWord, final char[] word,
			final int offset, final int length) {
		final int originalLength = lowerOriginalWord.length();

		if (originalLength == length) {
			for (int i = 0; i < originalLength; i++) {
				if (lowerOriginalWord.charAt(i) != Character
						.toLowerCase(word[offset + i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean addWord(final char[] word, final int offset,
			final int length, final int freq) {
		if (AnyApplication.DEBUG)
			Log.v(TAG, "Suggest::addWord");
		int pos = 0;
		final int[] priorities = mPriorities;
		final int prefMaxSuggestions = mPrefMaxSuggestions;
		// Check if it's the same word, only caps are different
		if (compareCaseInsensitive(mLowerOriginalWord, word, offset, length)) {
			if (AnyApplication.DEBUG)
				Log.v(TAG, "Suggest::addWord - same word as typed");
			pos = 0;
		} else {
			// Check the last one's priority and bail
			if (priorities[prefMaxSuggestions - 1] >= freq)
				return true;
			while (pos < prefMaxSuggestions) {
				if (priorities[pos] < freq
						|| (priorities[pos] == freq && length < mSuggestions
								.get(pos).length())) {
					break;
				}
				pos++;
			}
		}

		if (pos >= prefMaxSuggestions) {
			return true;
		}
		System.arraycopy(priorities, pos, priorities, pos + 1,
				prefMaxSuggestions - pos - 1);
		priorities[pos] = freq;
		int poolSize = mStringPool.size();
		StringBuilder sb = poolSize > 0 ? (StringBuilder) mStringPool
				.remove(poolSize - 1) : new StringBuilder(32);
		sb.setLength(0);
		if (mIsAllUpperCase) {
			sb.append(new String(word, offset, length).toUpperCase());
		} else if (mIsFirstCharCapitalized) {
			sb.append(Character.toUpperCase(word[offset]));
			if (length > 1) {
				sb.append(word, offset + 1, length - 1);
			}
		} else {
			sb.append(word, offset, length);
		}
		mSuggestions.add(pos, sb);
		if (mSuggestions.size() > prefMaxSuggestions) {
			CharSequence garbage = mSuggestions.remove(prefMaxSuggestions);
			if (garbage instanceof StringBuilder) {
				mStringPool.add(garbage);
			}
		}
		return true;
	}

	public boolean isValidWord(final CharSequence word) {
		if (word == null || word.length() == 0) {
			if (AnyApplication.DEBUG)
				Log.v(TAG, "Suggest::isValidWord - word is empty");
			return false;
		}

		if (mMainDictioanryEnabled || mAutoTextEnabled) {
			final boolean validFromMain = (mMainDictioanryEnabled && mMainDict != null && mMainDict.isValidWord(word));
			final boolean validFromUser = (mUserDictionary != null && mUserDictionary.isValidWord(word));
			//final boolean validFromAuto = (mAutoDictionary != null && mAutoDictionary.isValidWord(word));
			final boolean validFromContacts = (mContactsDictionary != null && mContactsDictionary.isValidWord(word));

			if (AnyApplication.DEBUG)
				Log.v(TAG, "Suggest::isValidWord(" + word
						+ ") mMainDictioanryEnabled:" + mMainDictioanryEnabled+" mAutoTextEnabled:"+mAutoTextEnabled
						+ " validFromMain:" + validFromMain 
                                                + " validFromUser:"+ validFromUser
						//+" validFromAuto:"+validFromAuto
						+ " validFromContacts:" + validFromContacts);
			return validFromMain || validFromUser
					|| /*validFromAuto ||*/ validFromContacts;
		} else {
			return false;
		}
	}

	private void collectGarbage() {
		int poolSize = mStringPool.size();
		int garbageSize = mSuggestions.size();
		while (poolSize < mPrefMaxSuggestions && garbageSize > 0) {
			CharSequence garbage = mSuggestions.get(garbageSize - 1);
			if (garbage != null && garbage instanceof StringBuilder) {
				mStringPool.add(garbage);
				poolSize++;
			}
			garbageSize--;
		}
		if (poolSize == mPrefMaxSuggestions + 1) {
			Log.w(TAG, "String pool got too big: " + poolSize);
		}
		mSuggestions.clear();
	}
	
	public void setMinimumWordLengthForCorrection(int minLength)
	{
		//making sure it is not negative or zero
		mMinimumWordSizeToStartCorrecting = Math.max(1, minLength);
	}

	// public Dictionary getMainDictionary()
	// {
	// return mMainDict;
	// }
}
