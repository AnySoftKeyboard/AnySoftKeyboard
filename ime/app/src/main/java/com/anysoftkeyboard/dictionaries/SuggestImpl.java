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
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.quicktextkeys.TagsExtractor;
import com.anysoftkeyboard.quicktextkeys.TagsExtractorImpl;
import com.anysoftkeyboard.utils.IMEUtil;
import com.menny.android.anysoftkeyboard.BuildConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * This class loads a dictionary and provides a list of suggestions for a given sequence of
 * characters. This includes corrections and completions.
 */
public class SuggestImpl implements Suggest {
    private static final String TAG = "ASKSuggest";
    private static final int ABBREVIATION_TEXT_FREQUENCY = Integer.MAX_VALUE - 10;
    private static final int AUTO_TEXT_FREQUENCY = Integer.MAX_VALUE - 20;
    private static final int VALID_TYPED_WORD_FREQUENCY = Integer.MAX_VALUE - 25;
    private static final int FIXED_TYPED_WORD_FREQUENCY = Integer.MAX_VALUE - 30;
    private static final int TYPED_WORD_FREQUENCY = Integer.MAX_VALUE; // always the first
    @NonNull private final SuggestionsProvider mSuggestionsProvider;
    private final List<CharSequence> mSuggestions = new ArrayList<>();
    private final List<CharSequence> mNextSuggestions = new ArrayList<>();
    private final List<CharSequence> mStringPool = new ArrayList<>();
    private final Dictionary.WordCallback mAutoTextWordCallback;
    private final Dictionary.WordCallback mAbbreviationWordCallback;
    private final Dictionary.WordCallback mTypingDictionaryWordCallback;

    @NonNull private Locale mLocale = Locale.getDefault();
    private int mPrefMaxSuggestions = 12;
    @NonNull private TagsExtractor mTagsSearcher = TagsExtractorImpl.NO_OP;
    @NonNull private int[] mPriorities = new int[mPrefMaxSuggestions];
    private int mCorrectSuggestionIndex = -1;
    @NonNull private String mLowerOriginalWord = "";
    @NonNull private String mTypedOriginalWord = "";
    private boolean mIsFirstCharCapitalized;
    private boolean mIsAllUpperCase;
    private int mCommonalityMaxLengthDiff = 1;
    private int mCommonalityMaxDistance = 1;
    private boolean mEnabledSuggestions;

    @VisibleForTesting
    SuggestImpl(@NonNull SuggestionsProvider provider) {
        mSuggestionsProvider = provider;
        final SuggestionCallback basicWordCallback = new SuggestionCallback();
        mTypingDictionaryWordCallback = new DictionarySuggestionCallback(basicWordCallback);
        mAutoTextWordCallback = new AutoTextSuggestionCallback(basicWordCallback);
        mAbbreviationWordCallback = new AbbreviationSuggestionCallback(basicWordCallback);
        setMaxSuggestions(mPrefMaxSuggestions);
    }

    public SuggestImpl(@NonNull Context context) {
        this(new SuggestionsProvider(context));
    }

    private static boolean compareCaseInsensitive(
            final String lowerOriginalWord, final char[] word, final int offset, final int length) {
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
    public void setCorrectionMode(boolean enabledSuggestions, int maxLengthDiff, int maxDistance) {
        mEnabledSuggestions = enabledSuggestions;

        // making sure it is not negative or zero
        mCommonalityMaxLengthDiff = maxLengthDiff;
        mCommonalityMaxDistance = maxDistance;
    }

    @Override
    @VisibleForTesting
    public boolean isSuggestionsEnabled() {
        return mEnabledSuggestions;
    }

    @Override
    public void closeDictionaries() {
        mSuggestionsProvider.close();
    }

    @Override
    public void setupSuggestionsForKeyboard(
            @NonNull List<DictionaryAddOnAndBuilder> dictionaryBuilders,
            @NonNull DictionaryBackgroundLoader.Listener cb) {
        if (mEnabledSuggestions && dictionaryBuilders.size() > 0) {
            mSuggestionsProvider.setupSuggestionsForKeyboard(dictionaryBuilders, cb);
        } else {
            closeDictionaries();
        }
    }

    @Override
    public void setMaxSuggestions(int maxSuggestions) {
        if (maxSuggestions < 1 || maxSuggestions > 100) {
            throw new IllegalArgumentException("maxSuggestions must be between 1 and 100");
        }
        mPrefMaxSuggestions = maxSuggestions;
        mPriorities = new int[mPrefMaxSuggestions];
        collectGarbage();
        while (mStringPool.size() < mPrefMaxSuggestions) {
            StringBuilder sb = new StringBuilder(32);
            mStringPool.add(sb);
        }
    }

    private boolean haveSufficientCommonality(
            @NonNull CharSequence typedWord, @NonNull CharSequence toBeAutoPickedSuggestion) {
        final int originalLength = typedWord.length();
        final int suggestionLength = toBeAutoPickedSuggestion.length();
        final int lengthDiff = suggestionLength - originalLength;

        return lengthDiff <= mCommonalityMaxLengthDiff
                && IMEUtil.editDistance(typedWord, toBeAutoPickedSuggestion)
                        <= mCommonalityMaxDistance;
    }

    @Override
    public void resetNextWordSentence() {
        mNextSuggestions.clear();
        mSuggestionsProvider.resetNextWordSentence();
    }

    @Override
    public List<CharSequence> getNextSuggestions(
            final CharSequence previousWord, final boolean inAllUpperCaseState) {
        if (previousWord.length() == 0) {
            return Collections.emptyList();
        }

        mNextSuggestions.clear();
        mIsAllUpperCase = inAllUpperCaseState;

        // only adding VALID words
        if (isValidWord(previousWord)) {
            final String currentWord = previousWord.toString();
            mSuggestionsProvider.getNextWords(currentWord, mNextSuggestions, mPrefMaxSuggestions);
            if (BuildConfig.DEBUG) {
                Logger.d(
                        TAG,
                        "getNextSuggestions from user-dictionary for '%s' (capital? %s):",
                        previousWord,
                        mIsAllUpperCase);
                for (int suggestionIndex = 0;
                        suggestionIndex < mNextSuggestions.size();
                        suggestionIndex++) {
                    Logger.d(
                            TAG,
                            "* getNextSuggestions #%d :''%s'",
                            suggestionIndex,
                            mNextSuggestions.get(suggestionIndex));
                }
            }

            if (mIsAllUpperCase) {
                for (int suggestionIndex = 0;
                        suggestionIndex < mNextSuggestions.size();
                        suggestionIndex++) {
                    mNextSuggestions.set(
                            suggestionIndex,
                            mNextSuggestions.get(suggestionIndex).toString().toUpperCase(mLocale));
                }
            }
        } else {
            Logger.d(TAG, "getNextSuggestions for '%s' is invalid.", previousWord);
        }
        return mNextSuggestions;
    }

    @Override
    public List<CharSequence> getSuggestions(WordComposer wordComposer) {
        if (!mEnabledSuggestions) return Collections.emptyList();

        mCorrectSuggestionIndex = -1;
        mIsFirstCharCapitalized = wordComposer.isFirstCharCapitalized();
        mIsAllUpperCase = wordComposer.isAllUpperCase();
        collectGarbage();
        Arrays.fill(mPriorities, 0);

        // Save a lowercase version of the original word
        mTypedOriginalWord = wordComposer.getTypedWord().toString();
        if (mTypedOriginalWord.length() > 0) {
            mLowerOriginalWord = mTypedOriginalWord.toLowerCase(mLocale);
        } else {
            mLowerOriginalWord = "";
        }

        if (wordComposer.isAtTagsSearchState() && mTagsSearcher.isEnabled()) {
            final CharSequence typedTagToSearch = mLowerOriginalWord.substring(1);
            return mTagsSearcher.getOutputForTag(typedTagToSearch, wordComposer);
        }

        mSuggestions.add(0, mTypedOriginalWord);
        mPriorities[0] = TYPED_WORD_FREQUENCY;

        // searching dictionaries by priority order:
        // abbreviations
        mSuggestionsProvider.getAbbreviations(wordComposer, mAbbreviationWordCallback);
        // auto-text
        mSuggestionsProvider.getAutoText(wordComposer, mAutoTextWordCallback);
        // main-dictionary
        mSuggestionsProvider.getSuggestions(wordComposer, mTypingDictionaryWordCallback);

        // now, we'll look at the next-words-suggestions list, and add all the ones that begins
        // with the typed word. These suggestions are top priority, so they will be added
        // at the top of the list
        final int typedWordLength = mLowerOriginalWord.length();
        // since the next-word-suggestions are order by usage, we'd like to add them at the
        // same order
        int nextWordInsertionIndex = 0;
        for (CharSequence nextWordSuggestion : mNextSuggestions) {
            if (nextWordSuggestion.length() >= typedWordLength
                    && TextUtils.equals(
                            nextWordSuggestion.subSequence(0, typedWordLength),
                            mTypedOriginalWord)) {
                mSuggestions.add(nextWordInsertionIndex, nextWordSuggestion);
                // next next-word will have lower usage, so it should be added after this one.
                nextWordInsertionIndex++;
            }
        }

        // removing possible duplicates to typed.
        IMEUtil.removeDupes(mSuggestions, mStringPool);

        return mSuggestions;
    }

    @Override
    public int getLastValidSuggestionIndex() {
        return mCorrectSuggestionIndex;
    }

    @Override
    public boolean isValidWord(final CharSequence word) {
        return mSuggestionsProvider.isValidWord(word);
    }

    private void collectGarbage() {
        int poolSize = mStringPool.size();
        int garbageSize = mSuggestions.size();
        while (poolSize < mPrefMaxSuggestions && garbageSize > 0) {
            CharSequence garbage = mSuggestions.get(garbageSize - 1);
            if (garbage instanceof StringBuilder) {
                mStringPool.add(garbage);
                poolSize++;
            }
            garbageSize--;
        }
        if (poolSize == mPrefMaxSuggestions + 1) {
            Logger.w(TAG, "String pool got too big: " + poolSize);
        }
        mSuggestions.clear();
    }

    @Override
    public boolean addWordToUserDictionary(String word) {
        return mSuggestionsProvider.addWordToUserDictionary(word);
    }

    @Override
    public void removeWordFromUserDictionary(String word) {
        mSuggestionsProvider.removeWordFromUserDictionary(word);
    }

    @Override
    public void setTagsSearcher(@NonNull TagsExtractor extractor) {
        mTagsSearcher = extractor;
    }

    @Override
    public boolean tryToLearnNewWord(CharSequence newWord, AdditionType additionType) {
        return mSuggestionsProvider.tryToLearnNewWord(newWord, additionType.getFrequencyDelta());
    }

    @Override
    public void setIncognitoMode(boolean incognitoMode) {
        mSuggestionsProvider.setIncognitoMode(incognitoMode);
    }

    @Override
    public boolean isIncognitoMode() {
        return mSuggestionsProvider.isIncognitoMode();
    }

    @Override
    public void destroy() {
        closeDictionaries();
        mSuggestionsProvider.destroy();
    }

    private static class AutoTextSuggestionCallback implements Dictionary.WordCallback {
        private final Dictionary.WordCallback mBasicWordCallback;

        private AutoTextSuggestionCallback(Dictionary.WordCallback callback) {
            mBasicWordCallback = callback;
        }

        @Override
        public boolean addWord(
                char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            return mBasicWordCallback.addWord(
                    word, wordOffset, wordLength, AUTO_TEXT_FREQUENCY, from);
        }
    }

    private static class AbbreviationSuggestionCallback implements Dictionary.WordCallback {
        private final Dictionary.WordCallback mBasicWordCallback;

        private AbbreviationSuggestionCallback(Dictionary.WordCallback callback) {
            mBasicWordCallback = callback;
        }

        @Override
        public boolean addWord(
                char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            return mBasicWordCallback.addWord(
                    word, wordOffset, wordLength, ABBREVIATION_TEXT_FREQUENCY, from);
        }
    }

    private class DictionarySuggestionCallback implements Dictionary.WordCallback {
        private final Dictionary.WordCallback mBasicWordCallback;

        private DictionarySuggestionCallback(Dictionary.WordCallback callback) {
            mBasicWordCallback = callback;
        }

        @Override
        public boolean addWord(
                char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            // Check if it's the same word
            if (compareCaseInsensitive(mLowerOriginalWord, word, wordOffset, wordLength)) {
                frequency = FIXED_TYPED_WORD_FREQUENCY;
            }

            // we are not allowing the main dictionary to suggest fixes for 1 length words
            // (think just the alphabet letter)
            final boolean resetSuggestionsFix =
                    mLowerOriginalWord.length() < 2 && mCorrectSuggestionIndex == -1;
            final boolean addWord =
                    mBasicWordCallback.addWord(word, wordOffset, wordLength, frequency, from);
            if (resetSuggestionsFix) mCorrectSuggestionIndex = -1;
            return addWord;
        }
    }

    private class SuggestionCallback implements Dictionary.WordCallback {

        @Override
        public boolean addWord(
                char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            if (BuildConfig.DEBUG && TextUtils.isEmpty(mLowerOriginalWord))
                throw new IllegalStateException("mLowerOriginalWord is empty!!");

            int pos = 0;
            final int[] priorities = mPriorities;
            final int prefMaxSuggestions = mPrefMaxSuggestions;

            StringBuilder sb = getStringBuilderFromPool(word, wordOffset, wordLength);

            if (!TextUtils.equals(mTypedOriginalWord, sb)) {
                // Check the last one's priority and bail
                if (priorities[prefMaxSuggestions - 1] >= frequency) return true;
                // looking for the ordered position to insert the new word
                while (pos < prefMaxSuggestions) {
                    if (priorities[pos] < frequency
                            || (priorities[pos] == frequency
                                    && wordLength < mSuggestions.get(pos).length())) {
                        break;
                    }
                    pos++;
                }

                if (pos >= prefMaxSuggestions) {
                    // we reached a position which is outside the max, we'll skip
                    // this word and ask for more (maybe next one will have higher frequency)
                    return true;
                }

                System.arraycopy(
                        priorities, pos, priorities, pos + 1, prefMaxSuggestions - pos - 1);
                mSuggestions.add(pos, sb);
                priorities[pos] = frequency;
            } else {
                frequency = VALID_TYPED_WORD_FREQUENCY;
            }
            // should we mark this as a possible suggestion fix?
            if (frequency >= FIXED_TYPED_WORD_FREQUENCY
                    || haveSufficientCommonality(mLowerOriginalWord, sb)) {
                // this a suggestion that can be a fix
                if (mCorrectSuggestionIndex < 0
                        || priorities[mCorrectSuggestionIndex] < frequency) {
                    mCorrectSuggestionIndex = pos;
                }
            }

            // removing excess suggestion
            IMEUtil.tripSuggestions(mSuggestions, prefMaxSuggestions, mStringPool);
            return true; // asking for more
        }
    }

    @NonNull
    private StringBuilder getStringBuilderFromPool(char[] word, int wordOffset, int wordLength) {
        int poolSize = mStringPool.size();
        StringBuilder sb =
                poolSize > 0
                        ? (StringBuilder) mStringPool.remove(poolSize - 1)
                        : new StringBuilder(Dictionary.MAX_WORD_LENGTH);
        sb.setLength(0);
        if (mIsAllUpperCase) {
            sb.append(new String(word, wordOffset, wordLength).toUpperCase(mLocale));
        } else if (mIsFirstCharCapitalized) {
            sb.append(Character.toUpperCase(word[wordOffset]));
            if (wordLength > 1) {
                sb.append(word, wordOffset + 1, wordLength - 1);
            }
        } else {
            sb.append(word, wordOffset, wordLength);
        }
        return sb;
    }
}
