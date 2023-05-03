package com.anysoftkeyboard.dictionaries;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.quicktextkeys.TagsExtractor;
import java.util.List;

public interface Suggest {
  void setCorrectionMode(
      boolean enabledSuggestions, int maxLengthDiff, int maxDistance, boolean splitWords);

  @VisibleForTesting
  boolean isSuggestionsEnabled();

  void closeDictionaries();

  void setupSuggestionsForKeyboard(
      @NonNull List<DictionaryAddOnAndBuilder> dictionaryBuilders,
      @NonNull DictionaryBackgroundLoader.Listener cb);

  /**
   * Number of suggestions to generate from the input key sequence. This has to be a number between
   * 1 and 100 (inclusive).
   *
   * @throws IllegalArgumentException if the number is out of range
   */
  void setMaxSuggestions(int maxSuggestions);

  void resetNextWordSentence();

  /**
   * Returns a list of suggested next words for the given typed word
   *
   * @return list of suggestions.
   */
  List<CharSequence> getNextSuggestions(CharSequence previousWord, boolean inAllUpperCaseState);

  /**
   * Returns a list of words that match the list of character codes passed in. This list will be
   * overwritten the next time this function is called.
   *
   * @return list of suggestions.
   */
  List<CharSequence> getSuggestions(WordComposer wordComposer);

  /**
   * Returns the index of the valid word from the last call to getSuggestions. In most cases, if the
   * typed word is valid then the index is 0, if not and there is a close correction, then it will
   * probably be 1. If nothing can be suggested the returned index will be -1;
   */
  int getLastValidSuggestionIndex();

  boolean isValidWord(CharSequence word);

  boolean addWordToUserDictionary(String word);

  void removeWordFromUserDictionary(String word);

  void setTagsSearcher(@NonNull TagsExtractor extractor);

  boolean tryToLearnNewWord(CharSequence newWord, AdditionType additionType);

  void setIncognitoMode(boolean incognitoMode);

  boolean isIncognitoMode();

  void destroy();

  public enum AdditionType {
    Picked(3),
    Typed(2);

    private final int mFrequencyDelta;

    AdditionType(int frequencyDelta) {
      mFrequencyDelta = frequencyDelta;
    }

    public int getFrequencyDelta() {
      return mFrequencyDelta;
    }
  }
}
