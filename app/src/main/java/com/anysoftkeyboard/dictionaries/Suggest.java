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
import com.anysoftkeyboard.base.utils.CompatUtils;
import com.anysoftkeyboard.dictionaries.content.ContactsDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AbbreviationsDictionary;
import com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardTagsSearcher;
import com.anysoftkeyboard.nextword.NextWordGetter;
import com.anysoftkeyboard.quicktextkeys.TagsExtractor;
import com.anysoftkeyboard.utils.IMEUtil;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.BuildConfig;

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

    private int mMinimumWordLengthToStartCorrecting = 2;

    private final DictionaryFactory mDictionaryFactory;

    private UserDictionary mUserDictionary;

    private Dictionary mAutoDictionary;

    @Nullable
    private Dictionary mContactsDictionary;
    @Nullable
    private NextWordGetter mContactsNextWordDictionary;

    private Dictionary mAbbreviationDictionary;

    private int mPrefMaxSuggestions = 12;

    @Nullable
    private List<String> mLocaleSpecificPunctuations = null;
    @Nullable
    private TagsExtractor mTagsSearcher;
    @NonNull
    private AnySoftKeyboardKeyboardTagsSearcher.TagsSuggestionList mTagSuggestionsList = new AnySoftKeyboardKeyboardTagsSearcher.TagsSuggestionList();

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
    private int mCommonalityMaxDistance = 1;
    private final DictionaryASyncLoader.Listener mContactsDictionaryListener = new DictionaryASyncLoader.Listener() {
        @Override
        public void onDictionaryLoadingDone(Dictionary dictionary) {}

        @Override
        public void onDictionaryLoadingFailed(Dictionary dictionary, Exception exception) {
            if (dictionary == mContactsDictionary) {
                mContactsDictionary = null;//resetting it
            }
        }
    };

    public Suggest(Context context) {
        mDictionaryFactory = createDictionaryFactory();
        for (int i = 0; i < mPrefMaxSuggestions; i++) {
            StringBuilder sb = new StringBuilder(32);
            mStringPool.add(sb);
        }
    }

    @NonNull
    protected DictionaryFactory createDictionaryFactory() {
        return new DictionaryFactory();
    }

    public void setCorrectionMode(boolean autoText, boolean mainDictionary, int maxLengthDiff, int maxDistance, int minimumWorLength) {
        // making sure it is not negative or zero
        mMinimumWordLengthToStartCorrecting = minimumWorLength;
        mAutoTextEnabled = autoText;
        mMainDictionaryEnabled = mainDictionary;
        mCommonalityMaxLengthDiff = maxLengthDiff;
        mCommonalityMaxDistance = maxDistance;
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

    public void closeDictionaries() {
        Logger.d(TAG, "closeDictionaries");
        if (mMainDict != null) mMainDict.close();
        mMainDict = null;
        if (mAbbreviationDictionary != null) mAbbreviationDictionary.close();
        mAbbreviationDictionary = null;
        if (mAutoDictionary != null) mAutoDictionary.close();
        mAutoDictionary = null;
        if (mContactsDictionary != null) mContactsDictionary.close();
        mContactsDictionary = null;
        if (mUserDictionary != null) mUserDictionary.close();
        mUserDictionary = null;
    }

    public void setMainDictionary(Context askContext, @Nullable DictionaryAddOnAndBuilder dictionaryBuilder) {
        Logger.d(TAG, "Suggest: Got main dictionary! Type: " + ((dictionaryBuilder == null) ? "NULL" : dictionaryBuilder.getName()));
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
                DictionaryASyncLoader.executeLoaderParallel(null, mMainDict);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mAutoText = dictionaryBuilder.createAutoText();
            mLocaleSpecificPunctuations = dictionaryBuilder.createInitialSuggestions();

            mAbbreviationDictionary = new AbbreviationsDictionary(askContext, dictionaryBuilder.getLanguage());
            DictionaryASyncLoader.executeLoaderParallel(null, mAbbreviationDictionary);
        }
    }

    /**
     * Sets an optional contacts dictionary resource to be loaded.
     */
    public void setContactsDictionary(Context context, boolean enabled) {
        if (!enabled && mContactsDictionary != null) {
            // had one, but now config says it should be off
            Logger.i(TAG, "Contacts dictionary has been disabled! Closing resources.");
            mContactsDictionary.close();
            mContactsDictionary = null;
            mContactsNextWordDictionary = null;
        } else if (enabled && mContactsDictionary == null) {
            // config says it should be on, but I have none.
            ContactsDictionary contactsDictionary = mDictionaryFactory.createContactsDictionary(context);
            mContactsNextWordDictionary = contactsDictionary;
            mContactsDictionary = contactsDictionary;
            DictionaryASyncLoader.executeLoaderParallel(mContactsDictionaryListener, mContactsDictionary);
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

    private boolean haveSufficientCommonality(@NonNull String typedWord, @NonNull CharSequence toBeAutoPickedSuggestion) {
        final int originalLength = typedWord.length();
        final int suggestionLength = toBeAutoPickedSuggestion.length();
        final int lengthDiff = suggestionLength - originalLength;

        return lengthDiff <= mCommonalityMaxLengthDiff &&
                IMEUtil.editDistance(typedWord, toBeAutoPickedSuggestion) <= mCommonalityMaxDistance;
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
    @NonNull
    public List<CharSequence> getNextSuggestions(final CharSequence previousWord, final boolean inAllUpperCaseState) {
        if (mUserDictionary == null || previousWord.length() < mMinimumWordLengthToStartCorrecting) {
            Logger.d(TAG, "getNextSuggestions a word less than %d characters.", mMinimumWordLengthToStartCorrecting);
            return Collections.emptyList();
        }

        mNextSuggestions.clear();
        mIsAllUpperCase = inAllUpperCaseState;

        //only adding VALID words
        if (isValidWord(previousWord)) {
            final String currentWord = previousWord.toString().toLowerCase(mLocale);
            mUserDictionary.getNextWords(currentWord, mPrefMaxSuggestions, mNextSuggestions, mLocaleSpecificPunctuations);
            if (BuildConfig.DEBUG) {
                Logger.d(TAG, "getNextSuggestions from user-dictionary for '%s' (capital? %s):", previousWord, mIsAllUpperCase);
                for (int suggestionIndex=0; suggestionIndex<mNextSuggestions.size(); suggestionIndex++) {
                    Logger.d(TAG, "* getNextSuggestions #%d :''%s'", suggestionIndex, mNextSuggestions.get(suggestionIndex));
                }
            }
            if (mContactsNextWordDictionary != null) {
                int maxResults = mPrefMaxSuggestions - mNextSuggestions.size();
                if (maxResults > 0) {
                    Iterable<String> nextNames = mContactsNextWordDictionary.getNextWords(previousWord, maxResults, mMinimumWordLengthToStartCorrecting);
                    if (BuildConfig.DEBUG) {
                        Logger.d(TAG, "getNextSuggestions from contacts for '%s' (capital? %s):", previousWord, mIsAllUpperCase);
                        for (String nextWord : nextNames) {
                            Logger.d(TAG, "* getNextSuggestions ''%s'", nextWord);
                        }
                    }
                    for (String nextWord : nextNames) {
                        mNextSuggestions.add(nextWord);
                        if (--maxResults == 0) break;
                    }
                }
            }
            if (mIsAllUpperCase) {
                for (int suggestionIndex=0; suggestionIndex<mNextSuggestions.size(); suggestionIndex++) {
                    mNextSuggestions.set(suggestionIndex, mNextSuggestions.get(suggestionIndex).toString().toUpperCase(mLocale));
                }
            }
        } else {
            Logger.d(TAG, "getNextSuggestions for '%s' is invalid.");
        }
        return mNextSuggestions;
    }

    /**
     * Returns a list of words that match the list of character codes passed in.
     * This list will be overwritten the next time this function is called.
     *
     * @return list of suggestions.
     */
    @NonNull
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

        if (wordComposer.isAtTagsSearchState() && mTagsSearcher != null) {
            final CharSequence typedTagToSearch = mLowerOriginalWord.substring(1);
            mTagSuggestionsList.setTypedWord(typedTagToSearch);
            mTagSuggestionsList.setTagsResults(mTagsSearcher.getOutputForTag(typedTagToSearch));
            return mTagSuggestionsList;
        }

        // Search the dictionary only if there are at least mMinimumWordLengthToStartCorrecting (configurable)
        // characters
        if (wordComposer.length() >= mMinimumWordLengthToStartCorrecting) {
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
        IMEUtil.removeDupes(mSuggestions, mStringPool);

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
        // Check if it's the same word, only caps are different, or nothing was typed (gesture?)
        if (TextUtils.isEmpty(mLowerOriginalWord) || compareCaseInsensitive(mLowerOriginalWord, word, offset, length)) {
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
        addSuggestionToSuggestionsList(pos, word, offset, length);
        IMEUtil.trimSuggestions(mSuggestions, prefMaxSuggestions, mStringPool);
        return true;
    }

    public boolean isValidWord(final CharSequence word) {
        if (word == null || word.length() == 0) {
            return false;
        }

        if (BuildConfig.DEBUG) Logger.v(TAG, "Suggest::isValidWord(%s) mMainDictionaryEnabled:%s mAutoTextEnabled: %s user-dictionary-enabled: %s contacts-dictionary-enabled: %s",
                word, mMainDictionaryEnabled, mAutoTextEnabled, mUserDictionary != null, mContactsDictionary != null);

        if (mMainDictionaryEnabled || mAutoTextEnabled) {
            final boolean validFromMain = (mMainDictionaryEnabled && mMainDict != null && mMainDict.isValidWord(word));
            final boolean validFromUser = (mUserDictionary != null && mUserDictionary.isValidWord(word));
            final boolean validFromContacts = (mContactsDictionary != null && mContactsDictionary.isValidWord(word));

            if (BuildConfig.DEBUG) Logger.v(TAG, "Suggest::isValidWord(%s)validFromMain: %s validFromUser: %s validFromContacts: %s",
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
        while (poolSize < 2000 && garbageSize > 0) {
            CharSequence garbage = mSuggestions.get(garbageSize - 1);
            if (garbage instanceof StringBuilder) {
                ((StringBuilder)garbage).setLength(0);
                mStringPool.add(garbage);
                poolSize++;
            }
            garbageSize--;
        }
        mSuggestions.clear();
    }

    public DictionaryFactory getDictionaryFactory() {
        return mDictionaryFactory;
    }

    public boolean addWordToUserDictionary(String word) {
        return mUserDictionary != null && mUserDictionary.addWord(word, 128);
    }

    public void removeWordFromUserDictionary(String word) {
        if (mUserDictionary != null) mUserDictionary.deleteWord(word);
    }

    public void setTagsSearcher(@Nullable TagsExtractor extractor) {
        mTagsSearcher = extractor;
    }

    private final Dictionary.WordCallback mWordsForPathCallback = new Dictionary.WordCallback() {
        @Override
        public boolean addWord(char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            addSuggestionToSuggestionsList(-1/*at the end*/, word, wordOffset, wordLength);

            //as many words as you can!
            return true;
        }
    };

    private void addSuggestionToSuggestionsList(int atPosition, char[] word, int wordOffset, int wordLength) {
        int poolSize = mStringPool.size();
        CharSequence wordToAdd;
        if (mIsAllUpperCase) {
            wordToAdd = new String(word, wordOffset, wordLength).toUpperCase(mLocale);
        } else {
            StringBuilder sb = poolSize > 0 ? (StringBuilder) mStringPool.remove(poolSize - 1) : new StringBuilder(Dictionary.MAX_WORD_LENGTH);
            if (mIsFirstCharCapitalized) {
                sb.append(Character.toUpperCase(word[wordOffset]));
                if (wordLength > 1) {
                    sb.append(word, wordOffset + 1, wordLength - 1);
                }
            } else {
                sb.append(word, wordOffset, wordLength);
            }
            wordToAdd = sb;
        }
        if (atPosition >= 0) {
            mSuggestions.add(atPosition, wordToAdd);
        } else {
            mSuggestions.add(wordToAdd);
        }
    }

    public List<CharSequence> getWordsForPath(boolean isFirstCharCapitalized, boolean isAllUpperCase, int[] keyCodesInPath, int keyCodesInPathLength) {
        mExplodedAbbreviations.clear();
        mHaveCorrection = false;
        mIsFirstCharCapitalized = isFirstCharCapitalized;
        mIsAllUpperCase = isAllUpperCase;
        collectGarbage();
        Arrays.fill(mPriorities, 0);

        if (mContactsDictionary != null) {
            mContactsDictionary.getWordsForPath(keyCodesInPath, keyCodesInPathLength, mWordsForPathCallback);
        }

        if (mUserDictionary != null) {
            mUserDictionary.getWordsForPath(keyCodesInPath, keyCodesInPathLength, mWordsForPathCallback);
        }

        if (mMainDict != null) {
            mMainDict.getWordsForPath(keyCodesInPath, keyCodesInPathLength, mWordsForPathCallback);
        }

        return mSuggestions;
    }
}
