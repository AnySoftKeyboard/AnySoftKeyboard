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
    @NonNull private final SuggestionsProvider mSuggestionsProvider;
    private final List<CharSequence> mSuggestions = new ArrayList<>();
    private final List<CharSequence> mNextSuggestions = new ArrayList<>();
    private final List<CharSequence> mStringPool = new ArrayList<>();
    private final List<String> mExplodedAbbreviations = new ArrayList<>();
    private final Dictionary.WordCallback mAbbreviationWordCallback =
            (word, wordOffset, wordLength, frequency, from) -> {
                mExplodedAbbreviations.add(new String(word, wordOffset, wordLength));
                return true;
            };

    @NonNull private Locale mLocale = Locale.getDefault();
    private int mMinimumWordLengthToStartCorrecting = 2;
    private int mPrefMaxSuggestions = 12;
    @NonNull private TagsExtractor mTagsSearcher = TagsExtractorImpl.NO_OP;
    @NonNull private int[] mPriorities = new int[mPrefMaxSuggestions];
    private boolean mHaveCorrection;
    @NonNull private String mLowerOriginalWord = "";
    private boolean mIsFirstCharCapitalized;
    private boolean mIsAllUpperCase;
    private final SuggestionCallback mTypingDictionaryWordCallback = new SuggestionCallback();
    private int mCommonalityMaxLengthDiff = 1;
    private int mCommonalityMaxDistance = 1;
    private boolean mEnabledSuggestions;

    @VisibleForTesting
    SuggestImpl(@NonNull SuggestionsProvider provider) {
        mSuggestionsProvider = provider;
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
    public void setCorrectionMode(
            boolean enabledSuggestions, int maxLengthDiff, int maxDistance, int minimumWorLength) {
        mEnabledSuggestions = enabledSuggestions;

        // making sure it is not negative or zero
        mMinimumWordLengthToStartCorrecting = minimumWorLength;
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
            @NonNull String typedWord, @NonNull CharSequence toBeAutoPickedSuggestion) {
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
        if (previousWord.length() < mMinimumWordLengthToStartCorrecting) {
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
    public List<CharSequence> getSuggestions(
            WordComposer wordComposer, boolean includeTypedWordIfValid) {
        if (!mEnabledSuggestions) return Collections.emptyList();

        mExplodedAbbreviations.clear();
        mHaveCorrection = false;
        mIsFirstCharCapitalized = wordComposer.isFirstCharCapitalized();
        mIsAllUpperCase = wordComposer.isAllUpperCase();
        collectGarbage();
        Arrays.fill(mPriorities, 0);

        // Save a lowercase version of the original word
        CharSequence originalWord = wordComposer.getTypedWord();
        if (originalWord.length() > 0) {
            originalWord =
                    originalWord
                            .toString(); // disconnecting from its source (could be a StringBuilder)
            mLowerOriginalWord = originalWord.toString().toLowerCase(mLocale);
        } else {
            mLowerOriginalWord = "";
        }

        if (wordComposer.isAtTagsSearchState() && mTagsSearcher.isEnabled()) {
            final CharSequence typedTagToSearch = mLowerOriginalWord.substring(1);
            return mTagsSearcher.getOutputForTag(typedTagToSearch, wordComposer);
        }

        // Search the dictionary only if there are at least mMinimumWordLengthToStartCorrecting
        // (configurable)
        // characters
        if (wordComposer.codePointCount() >= mMinimumWordLengthToStartCorrecting) {
            if (TextUtils.isEmpty(mLowerOriginalWord))
                throw new IllegalStateException("mLowerOriginalWord is empty");
            mSuggestionsProvider.getSuggestions(wordComposer, mTypingDictionaryWordCallback);
            mSuggestionsProvider.getAbbreviations(wordComposer, mAbbreviationWordCallback);

            if (mSuggestions.size() > 0) {
                mHaveCorrection = true;
            }
        }

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
                            nextWordSuggestion.subSequence(0, typedWordLength), originalWord)) {
                mSuggestions.add(nextWordInsertionIndex, nextWordSuggestion);
                nextWordInsertionIndex++; // next next-word will have lower usage, so it should be
                // added after this one.
            }
        }

        // adding the typed word at the head of the suggestions list
        if (!TextUtils.isEmpty(originalWord)) {
            mSuggestions.add(0, originalWord.toString());

            if (mExplodedAbbreviations.size() > 0) {
                // typed at zero, exploded at 1 index. These are super high priority
                int explodedWordInsertionIndex = 1;
                for (String explodedWord : mExplodedAbbreviations) {
                    mSuggestions.add(explodedWordInsertionIndex, explodedWord);
                    explodedWordInsertionIndex++;
                }

                mHaveCorrection = true; // so the exploded text will be auto-committed.
            }
        }

        if (mLowerOriginalWord.length() > 0) {
            CharSequence autoText = mSuggestionsProvider.lookupQuickFix(mLowerOriginalWord);
            // Is there an AutoText correction?
            // Is that correction already the current prediction (or original
            // word)?
            boolean canAdd =
                    !TextUtils.isEmpty(autoText) && !TextUtils.equals(autoText, originalWord);
            if (canAdd) {
                mHaveCorrection = true;
                if (mSuggestions.size() == 0) {
                    mSuggestions.add(originalWord);
                }
                // If the first character of the typed word is capitalized,
                // then also the suggestion will be
                String autoTextString = autoText.toString();
                if (mIsFirstCharCapitalized) {
                    // capitalize the autotext string
                    mSuggestions.add(
                            1,
                            (autoTextString.substring(0, 1).toUpperCase()
                                    + autoTextString.substring(1)));
                } else {
                    // no need to capitalize. Add as it is
                    mSuggestions.add(1, autoText);
                }
            }
        }

        // removing possible duplicates to typed.
        IMEUtil.removeDupes(mSuggestions, mStringPool);

        // Check if the first suggestion has a minimum number of characters in common
        if (mHaveCorrection
                && mSuggestions.size() > 1
                && mExplodedAbbreviations.size() == 0
                && !haveSufficientCommonality(mLowerOriginalWord, mSuggestions.get(1))) {
            mHaveCorrection = false;
        }
        return mSuggestions;
    }

    @Override
    public boolean hasMinimalCorrection() {
        return mHaveCorrection;
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

    private class SuggestionCallback implements Dictionary.WordCallback {

        @Override
        public boolean addWord(
                char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            int pos = 0;
            final int[] priorities = mPriorities;
            final int prefMaxSuggestions = mPrefMaxSuggestions;
            // Check if it's the same word, only caps are different
            if (TextUtils.isEmpty(mLowerOriginalWord))
                throw new IllegalStateException("mLowerOriginalWord should have already been set");
            if (compareCaseInsensitive(mLowerOriginalWord, word, wordOffset, wordLength)) {
                pos = 0;
            } else {
                // Check the last one's priority and bail
                if (priorities[prefMaxSuggestions - 1] >= frequency) return true;
                while (pos < prefMaxSuggestions) {
                    if (priorities[pos] < frequency
                            || (priorities[pos] == frequency
                                    && wordLength < mSuggestions.get(pos).length())) {
                        break;
                    }
                    pos++;
                }
            }

            if (pos >= prefMaxSuggestions) {
                return true;
            }
            System.arraycopy(priorities, pos, priorities, pos + 1, prefMaxSuggestions - pos - 1);
            priorities[pos] = frequency;
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
            mSuggestions.add(pos, sb);
            IMEUtil.tripSuggestions(mSuggestions, prefMaxSuggestions, mStringPool);
            return true;
        }
    }
}
