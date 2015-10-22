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
import com.anysoftkeyboard.utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    @Nullable
    private List<String> mLocaleSpecificPunctuations = null;

    private int[] mPriorities = new int[mPrefMaxSuggestions];
    private final List<CharSequence> mSuggestions = new ArrayList<>();
    private final List<CharSequence> mNextSuggestions = new ArrayList<>();
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

    private int mCommonalityMaxLengthDiff = 1;
    private int mCommonalityMaxDisatance = 1;

    public Suggest(Context context) {
        mDictionaryFactory = new DictionaryFactory();
        for (int i = 0; i < mPrefMaxSuggestions; i++) {
            StringBuilder sb = new StringBuilder(32);
            mStringPool.add(sb);
        }
    }

    public void setCorrectionMode(boolean autoText, boolean mainDictionary, int maxLengthDiff, int maxDistance) {
        mAutoTextEnabled = autoText;
        mMainDictionaryEnabled = mainDictionary;
        mCommonalityMaxLengthDiff = maxLengthDiff;
        mCommonalityMaxDisatance = maxDistance;
    }

    /**
     * Sets an optional user dictionary resource to be loaded. The user
     * dictionary is consulted before the main dictionary, if set.
     */
    public void setUserDictionary(Dictionary userDictionary) {
        if (mUserDictionary != userDictionary && mUserDictionary != null)
            mUserDictionary.close();

        mUserDictionary = (UserDictionary) userDictionary;
    }

    public void setMainDictionary(Context askContext, @Nullable DictionaryAddOnAndBuilder dictionaryBuilder) {
        Log.d(TAG, "Suggest: Got main dictionary! Type: " + ((dictionaryBuilder == null) ? "NULL" : dictionaryBuilder.getName()));
        if (mMainDict != null) {
            mMainDict.close();
            mMainDict = null;
        }
        mLocale = CompatUtils.getLocaleForLanguageTag(dictionaryBuilder == null ? null : dictionaryBuilder.getLanguage());

        if (mAbbreviationDictionary != null) {
            mAbbreviationDictionary.close();
            mAbbreviationDictionary = null;
        }

        if (dictionaryBuilder == null) {
            mMainDict = null;
            mAutoText = null;
            mAbbreviationDictionary = null;
            mLocaleSpecificPunctuations = null;
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
            mLocaleSpecificPunctuations = dictionaryBuilder.createInitialSuggestions();

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

    private boolean haveSufficientCommonality(String typedWord, CharSequence toBeAutoPickedSuggestion) {
        final int originalLength = typedWord.length();
        final int suggestionLength = toBeAutoPickedSuggestion.length();
        final int lengthDiff = suggestionLength - originalLength;

        return lengthDiff <= mCommonalityMaxLengthDiff &&
                IMEUtil.editDistance(typedWord, toBeAutoPickedSuggestion) <= mCommonalityMaxDisatance;
    }

    public void resetNextWordSentence() {
        if (mUserDictionary != null) {
            mNextSuggestions.clear();
            mUserDictionary.resetNextWordMemory();
        }
    }
    /**
     * Returns a list of suggested next words for the given typed word
     *
     * @return list of suggestions.
     */
    public List<CharSequence> getNextSuggestions(WordComposer wordComposerOfCompletedWord) {
        if (mUserDictionary == null || wordComposerOfCompletedWord.length() < mMinimumWordSizeToStartCorrecting)
            return Collections.emptyList();
        mNextSuggestions.clear();
        mIsAllUpperCase = wordComposerOfCompletedWord.isAllUpperCase();

        //only adding VALID words
        final CharSequence preferredWord = wordComposerOfCompletedWord.getPreferredWord();
        if (isValidWord(preferredWord)) {
            mUserDictionary.getNextWords(preferredWord.toString().toLowerCase(mLocale), mPrefMaxSuggestions, mNextSuggestions, mLocaleSpecificPunctuations);
            if (mIsAllUpperCase) {
                for (int suggestionIndex=0; suggestionIndex<mNextSuggestions.size(); suggestionIndex++) {
                    mNextSuggestions.set(suggestionIndex, mNextSuggestions.get(suggestionIndex).toString().toUpperCase(mLocale));
                }
            }
        }
        return mNextSuggestions;
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

        // Search the dictionary only if there are at least mMinimumWordSizeToStartCorrecting (configurable)
        // characters
        if (wordComposer.length() >= mMinimumWordSizeToStartCorrecting) {
            if (mContactsDictionary != null) {
                mContactsDictionary.getWords(wordComposer, this);
            }

            if (mUserDictionary != null) {
                mUserDictionary.getWords(wordComposer, this);
            }

            if (mSuggestions.size() > 0 && isValidWord(mOriginalWord)) {
                mHaveCorrection = true;
            }

            if (mMainDict != null) {
                mMainDict.getWords(wordComposer, this);
            }

            if (mAutoTextEnabled && mAbbreviationDictionary != null) {
                mAbbreviationDictionary.getWords(wordComposer, this);
            }

            if (/*mMainDictionaryEnabled &&*/ mSuggestions.size() > 0) {
                mHaveCorrection = true;
            }
        }

        //now, we'll look at the next-words-suggestions list, and add all the ones that begins
        //with the typed word. These suggestions are top priority, so they will be added
        //at the top of the list
        final int typedWordLength = mLowerOriginalWord.length();
        //since the next-word-suggestions are order by usage, we'd like to add them at the
        //same order
        int nextWordInsertionIndex = 0;
        for (CharSequence nextWordSuggestion : mNextSuggestions) {
            if (nextWordSuggestion.length() >= typedWordLength && nextWordSuggestion.subSequence(0, typedWordLength).equals(mOriginalWord)) {
                mSuggestions.add(nextWordInsertionIndex, nextWordSuggestion);
                nextWordInsertionIndex++;//next next-word will have lower usage, so it should be added after this one.
            }
        }

        //adding the typed word at the head of the suggestions list
        if (!TextUtils.isEmpty(mOriginalWord)) {
            mSuggestions.add(0, mOriginalWord.toString());

            if (mExplodedAbbreviations.size() > 0) {
                //typed at zero, exploded at 1 index. These are super high priority
                int explodedWordInsertionIndex = 1;
                for (String explodedWord : mExplodedAbbreviations) {
                    mSuggestions.add(explodedWordInsertionIndex, explodedWord);
                    explodedWordInsertionIndex++;
                }

                mHaveCorrection = true;//so the exploded text will be auto-committed.
            }
        }

        if (mLowerOriginalWord.length() > 0) {
            CharSequence autoText = mAutoTextEnabled && mAutoText != null ? mAutoText.lookup(mLowerOriginalWord, 0, mLowerOriginalWord.length()) : null;
            // Is there an AutoText correction?
            // Is that correction already the current prediction (or original
            // word)?
            boolean canAdd = (!TextUtils.isEmpty(autoText)) && (!TextUtils.equals(autoText, mOriginalWord));
            if (canAdd) {
                mHaveCorrection = true;
                if (mSuggestions.size() == 0) {
                    mSuggestions.add(mOriginalWord);
                }
                mSuggestions.add(1, autoText);
            }
        }

        //removing possible duplicates to typed.
        int maxSearchIndex = Math.min(5, mSuggestions.size());
        for (int suggestionIndex = 1; suggestionIndex<maxSearchIndex; suggestionIndex++) {
            if (TextUtils.equals(mOriginalWord, mSuggestions.get(suggestionIndex))) {
                mSuggestions.remove(suggestionIndex);
                maxSearchIndex--;
            }
        }

        // Check if the first suggestion has a minimum number of characters in common
        if (mHaveCorrection && mMainDictionaryEnabled && mSuggestions.size() > 1 && mExplodedAbbreviations.size() == 0) {
            if (!haveSufficientCommonality(mLowerOriginalWord, mSuggestions.get(1))) {
                mHaveCorrection = false;
            }
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

    @Override
    public boolean addWord(final char[] word, final int offset,
                           final int length, final int freq, final Dictionary from) {
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
        System.arraycopy(priorities, pos, priorities, pos + 1, prefMaxSuggestions - pos - 1);
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
