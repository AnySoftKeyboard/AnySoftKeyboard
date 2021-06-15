package com.anysoftkeyboard.dictionaries;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.quicktextkeys.TagsExtractor;
import java.util.List;

public interface Suggest {
    void setCorrectionMode(
            boolean enabledSuggestions, int maxLengthDiff, int maxDistance, int minimumWorLength);

    @VisibleForTesting
    boolean isSuggestionsEnabled();

    void closeDictionaries();

    void setupSuggestionsForKeyboard(
            @NonNull List<DictionaryAddOnAndBuilder> dictionaryBuilders,
            @NonNull DictionaryBackgroundLoader.Listener cb);

    /**
     * Number of suggestions to generate from the input key sequence. This has to be a number
     * between 1 and 100 (inclusive).
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
    List<CharSequence> getSuggestions(WordComposer wordComposer, boolean includeTypedWordIfValid);

    boolean hasMinimalCorrection();

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
