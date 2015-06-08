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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anysoftkeyboard.base.dictionaries.Dictionary;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.dictionaries.sqlite.AbbreviationsDictionary;
import com.anysoftkeyboard.utils.CompatUtils;
import com.anysoftkeyboard.utils.IMEUtil;
import com.anysoftkeyboard.base.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * This class loads a dictionary and provides a list of suggestions for a given
 * sequence of characters. This includes corrections and completions.
 */
public class Suggest implements Dictionary.WordCallback {

    private static final String TAG = "ASK Suggest";

    private Dictionary mMainDict;
    @NonNull
    private Locale mLocale = Locale.getDefault();
    private AutoText mAutoText;

    private int mMinimumWordSizeToStartCorrecting = 2;

    private final DictionaryFactory mDictionaryFactory;

    private UserDictionary mUserDictionary;

    private Dictionary mAutoDictionary;

    private Dictionary mContactsDictionary;

    private Dictionary mAbbreviationDictionary;

    private int mPrefMaxSuggestions = 12;

    private final List<String> mFallbackInitialSuggestions;
    private List<String> mInitialSuggestions = new ArrayList<>();

    private int[] mPriorities = new int[mPrefMaxSuggestions];
    private List<CharSequence> mSuggestions = new ArrayList<>();
    // private boolean mIncludeTypedWordIfValid;
    private List<CharSequence> mStringPool = new ArrayList<>();
    // private Context mContext;
    private boolean mHaveCorrection;
    private CharSequence mOriginalWord;
    private final List<String> mExplodedAbbreviations = new ArrayList<>();
    private String mLowerOriginalWord;

    // TODO: Remove these member variables by passing more context to addWord()
    // callback method
    private boolean mIsFirstCharCapitalized;
    private boolean mIsAllUpperCase;

    // private int mCorrectionMode = CORRECTION_FULL;
    private boolean mAutoTextEnabled = true;
    private boolean mMainDictionaryEnabled = true;
    private final Dictionary.WordCallback mNextWordsCallback = new Dictionary.WordCallback() {
        @Override
        public boolean addWord(char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            if (mSuggestions.size() < mPrefMaxSuggestions) {
                String nextWord = new String(word, wordOffset, wordLength);
                if (mIsAllUpperCase) nextWord = nextWord.toUpperCase(mLocale);
                mSuggestions.add(nextWord);
                return true;
            } else {
                return false;
            }
        }
    };

    public Suggest(Context context) {
        mDictionaryFactory = new DictionaryFactory();
        for (int i = 0; i < mPrefMaxSuggestions; i++) {
            StringBuilder sb = new StringBuilder(32);
            mStringPool.add(sb);
        }

        mFallbackInitialSuggestions = Arrays.asList(context.getResources().getStringArray(R.array.english_initial_suggestions));
    }

    public void setCorrectionMode(boolean autoText, boolean mainDictionary) {
        mAutoTextEnabled = autoText;
        mMainDictionaryEnabled = mainDictionary;
    }

    /**
     * Sets an optional user dictionary resource to be loaded. The user
     * dictionary is consulted before the main dictionary, if set.
     */
    public void setUserDictionary(Dictionary userDictionary) {
        if (mUserDictionary != userDictionary && mUserDictionary != null)
            mUserDictionary.close();

        mUserDictionary = (UserDictionary)userDictionary;
    }

    public void setMainDictionary(Context askContext, @Nullable DictionaryAddOnAndBuilder dictionaryBuilder) {
        Log.d(TAG, "Suggest: Got main dictionary! Type: " + ((dictionaryBuilder == null) ? "NULL" : dictionaryBuilder.getName()));
        if (mMainDict != null) {
            mMainDict.close();
            mMainDict = null;
        }
        mLocale = CompatUtils.getLocaleForLanguageTag(dictionaryBuilder == null? null : dictionaryBuilder.getLanguage());

        if (mAbbreviationDictionary != null) {
            mAbbreviationDictionary.close();
            mAbbreviationDictionary = null;
        }

        if (dictionaryBuilder == null) {
            mMainDict = null;
            mAutoText = null;
            mAbbreviationDictionary = null;
            mInitialSuggestions = mFallbackInitialSuggestions;
        } else {
            try {
                System.gc();

                mMainDict = dictionaryBuilder.createDictionary();
                DictionaryASyncLoader loader = new DictionaryASyncLoader(null);
                loader.execute(mMainDict);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mAutoText = dictionaryBuilder.createAutoText();
            mInitialSuggestions = dictionaryBuilder.createInitialSuggestions();
            if (mInitialSuggestions == null) mInitialSuggestions = mFallbackInitialSuggestions;

            mAbbreviationDictionary = new AbbreviationsDictionary(askContext, dictionaryBuilder.getLanguage());
            DictionaryASyncLoader loader = new DictionaryASyncLoader(null);
            loader.execute(mAbbreviationDictionary);
        }
    }

    /**
     * Sets an optional contacts dictionary resource to be loaded.
     */
    public void setContactsDictionary(Context context, boolean enabled) {
        if (!enabled && mContactsDictionary != null) {
            // had one, but now config says it should be off
            Log.i(TAG, "Contacts dictionary has been disabled! Closing resources.");
            mContactsDictionary.close();
            mContactsDictionary = null;
        } else if (enabled && mContactsDictionary == null) {
            // config says it should be on, but I have none.
            mContactsDictionary = mDictionaryFactory.createContactsDictionary(context);
            if (mContactsDictionary != null) {//not all devices has contacts-dictionary
                DictionaryASyncLoader loader = new DictionaryASyncLoader(null);
                loader.execute(mContactsDictionary);
            }
        }
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
     * @throws IllegalArgumentException if the number is out of range
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

    /**
     * Returns a list of suggested next words for the given typed word
     *
     * @return list of suggestions.
     */
    public List<CharSequence> getNextSuggestions(WordComposer wordComposerOfCompletedWord) {
        if (mUserDictionary == null || wordComposerOfCompletedWord.length() < mMinimumWordSizeToStartCorrecting) return Collections.emptyList();

        mHaveCorrection = false;
        mIsFirstCharCapitalized = false;
        mIsAllUpperCase = wordComposerOfCompletedWord.isAllUpperCase();
        collectGarbage();
        Arrays.fill(mPriorities, 0);

        //only adding VALID words
        if (isValidWord(wordComposerOfCompletedWord.getPreferredWord())) {
            mUserDictionary.getNextWords(wordComposerOfCompletedWord, mNextWordsCallback);
        }

        int initialsFromDefaultToAdd = mPrefMaxSuggestions - mSuggestions.size();
        final Iterator<String> initialsIterator = mInitialSuggestions.iterator();
        while (initialsIterator.hasNext() && initialsFromDefaultToAdd > 0) {
            initialsFromDefaultToAdd--;
            mSuggestions.add(initialsIterator.next());
        }
        return mSuggestions;
    }

    /**
     * Returns a list of words that match the list of character codes passed in.
     * This list will be overwritten the next time this function is called.
     *
     * @return list of suggestions.
     */
    public List<CharSequence> getSuggestions(WordComposer wordComposer, boolean includeTypedWordIfValid) {
        mExplodedAbbreviations.clear();
        mHaveCorrection = false;
        mIsFirstCharCapitalized = wordComposer.isFirstCharCapitalized();
        mIsAllUpperCase = wordComposer.isAllUpperCase();
        collectGarbage();
        Arrays.fill(mPriorities, 0);

        // Save a lowercase version of the original word
        mOriginalWord = wordComposer.getTypedWord();
        if (mOriginalWord.length() > 0) {
            mOriginalWord = mOriginalWord.toString();
            mLowerOriginalWord = mOriginalWord.toString().toLowerCase(mLocale);
        } else {
            mLowerOriginalWord = "";
        }
        // Search the dictionary only if there are at least 2 (configurable)
        // characters
        if (wordComposer.length() >= mMinimumWordSizeToStartCorrecting) {
            if (mContactsDictionary != null) {
                Log.v(TAG, "getSuggestions from contacts-dictionary");
                mContactsDictionary.getWords(wordComposer, this);
            }

            if (mUserDictionary != null) {
                Log.v(TAG, "getSuggestions from user-dictionary");
                mUserDictionary.getWords(wordComposer, this);
            }

            if (mSuggestions.size() > 0 && isValidWord(mOriginalWord)) {
                mHaveCorrection = true;
            }

            if (mMainDict != null) {
                Log.v(TAG, "getSuggestions from main-dictionary");
                mMainDict.getWords(wordComposer, this);
            }

            if (mAutoTextEnabled && mAbbreviationDictionary != null) {
                Log.v(TAG, "getSuggestions from mAbbreviationDictionary");
                mAbbreviationDictionary.getWords(wordComposer, this);
            }

            if (/*mMainDictionaryEnabled &&*/ mSuggestions.size() > 0) {
                mHaveCorrection = true;
            }
        }

        if (mOriginalWord != null) {
            mSuggestions.add(0, mOriginalWord.toString());
        }
        if (mExplodedAbbreviations.size() > 0) {
            //typed at zero, exploded at 1 index.
            for(String explodedWord : mExplodedAbbreviations)
                mSuggestions.add(1, explodedWord);

            mHaveCorrection = true;//so the exploded text will be auto-committed.
        }
        // Check if the first suggestion has a minimum number of characters in
        // common
        if (mMainDictionaryEnabled && mSuggestions.size() > 1 && mExplodedAbbreviations.size() == 0) {
            if (!haveSufficientCommonality(mLowerOriginalWord,
                    mSuggestions.get(1))) {
                mHaveCorrection = false;
            }
        }

        int i = 0;
        int max = 6;
        // Don't autotext the suggestions from the dictionaries
        if (!mMainDictionaryEnabled && mAutoTextEnabled)
            max = 1;
        while (i < mSuggestions.size() && i < max) {
            String suggestedWord = mSuggestions.get(i).toString().toLowerCase(mLocale);

            CharSequence autoText = mAutoTextEnabled && mAutoText != null ? mAutoText
                    .lookup(suggestedWord, 0, suggestedWord.length()) : null;
            // Is there an AutoText correction?
            boolean canAdd = autoText != null;
            // Is that correction already the current prediction (or original
            // word)?
            canAdd &= !TextUtils.equals(autoText, mSuggestions.get(i));
            // Is that correction already the next predicted word?
            if (canAdd && i + 1 < mSuggestions.size() && mMainDictionaryEnabled) {
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
                if (lowerOriginalWord.charAt(i) != Character.toLowerCase(word[offset + i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean addWord(final char[] word, final int offset,
                           final int length, final int freq, final Dictionary from) {
        Log.v(TAG, "Suggest::addWord");
        if (from == mAbbreviationDictionary) {
            mExplodedAbbreviations.add(new String(word, offset, length));
            return true;
        }
        int pos = 0;
        final int[] priorities = mPriorities;
        final int prefMaxSuggestions = mPrefMaxSuggestions;
        // Check if it's the same word, only caps are different
        if (compareCaseInsensitive(mLowerOriginalWord, word, offset, length)) {
            Log.v(TAG, "Suggest::addWord - forced at position 0.");
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
            sb.append(new String(word, offset, length).toUpperCase(mLocale));
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
            return false;
        }

        Log.v(TAG, "Suggest::isValidWord(%s) mMainDictionaryEnabled:%s mAutoTextEnabled: %s user-dictionary-enabled: %s contacts-dictionary-enabled: %s",
                word, mMainDictionaryEnabled, mAutoTextEnabled, mUserDictionary != null, mContactsDictionary != null);

        if (mMainDictionaryEnabled || mAutoTextEnabled) {
            final boolean validFromMain = (mMainDictionaryEnabled && mMainDict != null && mMainDict.isValidWord(word));
            final boolean validFromUser = (mUserDictionary != null && mUserDictionary.isValidWord(word));
            final boolean validFromContacts = (mContactsDictionary != null && mContactsDictionary.isValidWord(word));

            Log.v(TAG, "Suggest::isValidWord(%s)validFromMain: %s validFromUser: %s validFromContacts: %s",
                    word, validFromMain, validFromUser, validFromContacts);
            return validFromMain || validFromUser
                    || /* validFromAuto || */validFromContacts;
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

    public void setMinimumWordLengthForCorrection(int minLength) {
        // making sure it is not negative or zero
        mMinimumWordSizeToStartCorrecting = Math.max(1, minLength);
    }

    public DictionaryFactory getDictionaryFactory() {
        return mDictionaryFactory;
    }
}
