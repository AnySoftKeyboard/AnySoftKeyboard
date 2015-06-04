package com.anysoftkeyboard.dictionaries.nextword;

import com.anysoftkeyboard.base.dictionaries.Dictionary;
import com.anysoftkeyboard.base.dictionaries.WordComposer;

import java.util.HashMap;
import java.util.Map;

public class NextWordDictionary extends Dictionary {

    private static final int MAX_NEXT_SUGGESTIONS = 8;
    private String mPreviousWord = null;

    private final Map<String, NextWordsContainer> mNextWordMap = new HashMap<>();

    public NextWordDictionary(String locale) {
        super("NextWordDictionary_" + locale);
    }

    @Override
    public void getWords(WordComposer wordComposer, WordCallback callback) {
        final String currentWord = wordComposer.getPreferredWord().toString();
        //firstly, updating the relations to the previous word
        if (mPreviousWord != null) {
            NextWordsContainer previousSet = mNextWordMap.get(mPreviousWord);
            if (previousSet == null) {
                previousSet = new NextWordsContainer(mPreviousWord);
                mNextWordMap.put(mPreviousWord, previousSet);
            }

            previousSet.markWordAsUsed(currentWord);
        }

        //secondly, get a list of suggestions
        NextWordsContainer nextSet = mNextWordMap.get(currentWord);
        if (nextSet != null) {
            int frequency = Dictionary.MAX_WORD_FREQUENCY;
            final int minFrequency = frequency - MAX_NEXT_SUGGESTIONS;
            for (NextWord nextWord : nextSet.getNextWordSuggestions()) {
                final String suggestion = nextWord.nextWord;
                if (!callback.addWord(suggestion.toCharArray(), 0, suggestion.length(), frequency--, this)) break;
                if (frequency == minFrequency) break;
            }
        }

        mPreviousWord = currentWord;
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        return false;
    }

    @Override
    protected void closeAllResources() {

    }

    @Override
    protected void loadAllResources() {
    }
}
