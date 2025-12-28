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
import com.anysoftkeyboard.api.KeyCodes;
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
  private static final int POSSIBLE_FIX_THRESHOLD_FREQUENCY = Integer.MAX_VALUE / 2;
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
  private final SubWordSuggestionCallback mSubWordDictionaryWordCallback;

  @NonNull private final Locale mLocale = Locale.getDefault();
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
  private boolean mSplitWords;

  // buffers for edit-distance calculations.
  // We want to avoid allocations in the tight loop of the dictionary suggestions
  private final int[] mEditDistancePPrev = new int[Dictionary.MAX_WORD_LENGTH + 1];
  private final int[] mEditDistancePrev = new int[Dictionary.MAX_WORD_LENGTH + 1];
  private final int[] mEditDistanceCurr = new int[Dictionary.MAX_WORD_LENGTH + 1];

  @VisibleForTesting
  public SuggestImpl(@NonNull SuggestionsProvider provider) {
    mSuggestionsProvider = provider;
    final SuggestionCallback basicWordCallback = new SuggestionCallback();
    mTypingDictionaryWordCallback = new DictionarySuggestionCallback(basicWordCallback);
    mSubWordDictionaryWordCallback = new SubWordSuggestionCallback(basicWordCallback);
    mAutoTextWordCallback = new AutoTextSuggestionCallback(basicWordCallback);
    mAbbreviationWordCallback = new AbbreviationSuggestionCallback(basicWordCallback);
    setMaxSuggestions(mPrefMaxSuggestions);
  }

  public SuggestImpl(@NonNull Context context) {
    this(new SuggestionsProvider(context));
  }

  private static boolean compareCaseInsensitive(
      final CharSequence lowerOriginalWord, final char[] word, final int offset, final int length) {
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
      boolean enabledSuggestions, int maxLengthDiff, int maxDistance, boolean splitWords) {
    mEnabledSuggestions = enabledSuggestions;

    // making sure it is not negative or zero
    mCommonalityMaxLengthDiff = maxLengthDiff;
    mCommonalityMaxDistance = maxDistance;
    mSplitWords = splitWords;
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

  private static boolean haveSufficientCommonality(
      final int maxLengthDiff,
      final int maxCommonDistance,
      @NonNull final CharSequence typedWord,
      @NonNull final char[] word,
      final int offset,
      final int length,
      int[] prevPrev,
      int[] prev,
      int[] curr) {
    final int originalLength = typedWord.length();
    final int lengthDiff = length - originalLength;

    return lengthDiff <= maxLengthDiff
        && IMEUtil.editDistance(typedWord, word, offset, length, prevPrev, prev, curr)
            <= maxCommonDistance;
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
    // for sub-word matching:
    // only if ALL words match, we should use the sub-words as an suggestion
    // only exact matches (for now) will be considered
    if (mSplitWords)
      mSubWordDictionaryWordCallback.performSubWordsMatching(wordComposer, mSuggestionsProvider);
    // contacts, user and main dictionaries
    mSuggestionsProvider.getSuggestions(wordComposer, mTypingDictionaryWordCallback);

    // now, we'll look at the next-words-suggestions list, and add all the ones that begins
    // with the typed word. These suggestions are top priority, so they will be added
    // at the top of the list
    final int typedWordLength = mLowerOriginalWord.length();
    // next-word beats any suggestion OTHER than identical typed
    int nextWordInsertionIndex = mCorrectSuggestionIndex == 0 ? 1 : 0;
    // since the next-word-suggestions are order by usage, we'd like to add them at the
    // same order
    for (CharSequence nextWordSuggestion : mNextSuggestions) {
      if (nextWordSuggestion.length() >= typedWordLength
          && TextUtils.equals(
              nextWordSuggestion.subSequence(0, typedWordLength), mTypedOriginalWord)) {
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

  private static class SubWordSuggestionCallback implements Dictionary.WordCallback {
    private final WordsSplitter mSplitter = new WordsSplitter();
    private final Dictionary.WordCallback mBasicWordCallback;

    // This will be used to find the best per suggestion word for a possible split
    @NonNull private CharSequence mCurrentSubWord = "";
    private final char[] mCurrentBestSubWordSuggestion = new char[Dictionary.MAX_WORD_LENGTH];
    private int mCurrentBestSubWordSuggestionLength;
    private int mCurrentBestSubWordSubWordAdjustedFrequency;
    private int mCurrentBestSubWordSubWordAdjustedRawFrequency;

    // This will be used to identify the best split
    private final char[] mCurrentMatchedWords =
        new char[WordsSplitter.MAX_SPLITS * Dictionary.MAX_WORD_LENGTH];

    // this will be used to hold the currently best split
    private final char[] mBestMatchedWords =
        new char[WordsSplitter.MAX_SPLITS * Dictionary.MAX_WORD_LENGTH];

    private final int[] mEditDistancePPrev = new int[Dictionary.MAX_WORD_LENGTH + 1];
    private final int[] mEditDistancePrev = new int[Dictionary.MAX_WORD_LENGTH + 1];
    private final int[] mEditDistanceCurr = new int[Dictionary.MAX_WORD_LENGTH + 1];

    private SubWordSuggestionCallback(Dictionary.WordCallback callback) {
      mBasicWordCallback = callback;
    }

    void performSubWordsMatching(
        @NonNull WordComposer wordComposer, @NonNull SuggestionsProvider suggestionsProvider) {
      int bestAdjustedFrequency = 0;
      int bestMatchWordsLength = 0;
      Iterable<Iterable<KeyCodesProvider>> splits = mSplitter.split(wordComposer);
      for (var split : splits) {
        int currentSplitLength = 0;
        int currentSplitAdjustedFrequency = 0;
        // split is a possible word splitting.
        // we first need to ensure all words are real words and get their frequency
        // the values will be in mMatchedWords
        // NOTE: we only pick a possible split if ALL words match something in the
        // dictionary
        int wordCount = 0;
        for (var subWord : split) {
          wordCount++;
          mCurrentSubWord = subWord.getTypedWord();
          mCurrentBestSubWordSubWordAdjustedFrequency = 0;
          mCurrentBestSubWordSubWordAdjustedRawFrequency = 0;
          mCurrentBestSubWordSuggestionLength = 0;
          suggestionsProvider.getSuggestions(subWord, this);
          // at this point, we have the best adjusted sub-word
          if (mCurrentBestSubWordSubWordAdjustedFrequency == 0) {
            Logger.d(TAG, "Did not find a match for sub-word '%s'", mCurrentSubWord);
            wordCount = -1;
            break;
          }
          currentSplitAdjustedFrequency += mCurrentBestSubWordSubWordAdjustedRawFrequency;
          if (currentSplitLength > 0) {
            // adding space after the previous word
            mCurrentMatchedWords[currentSplitLength] = KeyCodes.SPACE;
            currentSplitLength++;
          }
          System.arraycopy(
              mCurrentBestSubWordSuggestion,
              0,
              mCurrentMatchedWords,
              currentSplitLength,
              mCurrentBestSubWordSuggestionLength);
          currentSplitLength += mCurrentBestSubWordSuggestionLength;
        }
        // at this point, we have the best constructed split in mCurrentMatchedWords
        if (wordCount > 0 && currentSplitAdjustedFrequency > bestAdjustedFrequency) {
          System.arraycopy(mCurrentMatchedWords, 0, mBestMatchedWords, 0, currentSplitLength);
          bestAdjustedFrequency = currentSplitAdjustedFrequency;
          bestMatchWordsLength = currentSplitLength;
        }
      }
      // at this point, we have the most suitable split in mBestMatchedWords
      if (bestMatchWordsLength > 0) {
        mBasicWordCallback.addWord(
            mBestMatchedWords,
            0,
            bestMatchWordsLength,
            POSSIBLE_FIX_THRESHOLD_FREQUENCY + bestAdjustedFrequency,
            null);
      }
    }

    @Override
    public boolean addWord(
        char[] word, int wordOffset, int wordLength, final int frequency, Dictionary from) {
      int adjustedFrequency = 0;
      // giving bonuses
      if (compareCaseInsensitive(mCurrentSubWord, word, wordOffset, wordLength)) {
        adjustedFrequency = frequency * 4;
      } else if (haveSufficientCommonality(
          1,
          1,
          mCurrentSubWord,
          word,
          wordOffset,
          wordLength,
          mEditDistancePPrev,
          mEditDistancePrev,
          mEditDistanceCurr)) {
        adjustedFrequency = frequency * 2;
      }
      // only passing if the suggested word is close to the sub-word
      if (adjustedFrequency > mCurrentBestSubWordSubWordAdjustedFrequency) {
        System.arraycopy(word, wordOffset, mCurrentBestSubWordSuggestion, 0, wordLength);
        mCurrentBestSubWordSuggestionLength = wordLength;
        mCurrentBestSubWordSubWordAdjustedFrequency = adjustedFrequency;
        mCurrentBestSubWordSubWordAdjustedRawFrequency = frequency;
      }
      return true; // next word
    }
  }

  private static class AutoTextSuggestionCallback implements Dictionary.WordCallback {
    private final Dictionary.WordCallback mBasicWordCallback;

    private AutoTextSuggestionCallback(Dictionary.WordCallback callback) {
      mBasicWordCallback = callback;
    }

    @Override
    public boolean addWord(
        char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
      return mBasicWordCallback.addWord(word, wordOffset, wordLength, AUTO_TEXT_FREQUENCY, from);
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
      } else if (haveSufficientCommonality(
          mCommonalityMaxLengthDiff,
          mCommonalityMaxDistance,
          mLowerOriginalWord,
          word,
          wordOffset,
          wordLength,
          mEditDistancePPrev,
          mEditDistancePrev,
          mEditDistanceCurr)) {
        frequency += POSSIBLE_FIX_THRESHOLD_FREQUENCY;
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

      int pos;
      final int[] priorities = mPriorities;
      final int prefMaxSuggestions = mPrefMaxSuggestions;

      StringBuilder sb = getStringBuilderFromPool(word, wordOffset, wordLength);

      if (TextUtils.equals(mTypedOriginalWord, sb)) {
        frequency = VALID_TYPED_WORD_FREQUENCY;
        pos = 0;
      } else {
        // Check the last one's priority and bail
        if (priorities[prefMaxSuggestions - 1] >= frequency) return true;
        pos = 1; // never check with the first (typed) word
        // looking for the ordered position to insert the new word
        while (pos < prefMaxSuggestions) {
          if (priorities[pos] < frequency
              || (priorities[pos] == frequency && wordLength < mSuggestions.get(pos).length())) {
            break;
          }
          pos++;
        }

        if (pos >= prefMaxSuggestions) {
          // we reached a position which is outside the max, we'll skip
          // this word and ask for more (maybe next one will have higher frequency)
          return true;
        }

        System.arraycopy(priorities, pos, priorities, pos + 1, prefMaxSuggestions - pos - 1);
        mSuggestions.add(pos, sb);
        priorities[pos] = frequency;
      }
      // should we mark this as a possible suggestion fix?
      if (frequency >= POSSIBLE_FIX_THRESHOLD_FREQUENCY) {
        // this a suggestion that can be a fix
        if (mCorrectSuggestionIndex < 0 || priorities[mCorrectSuggestionIndex] < frequency) {
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
