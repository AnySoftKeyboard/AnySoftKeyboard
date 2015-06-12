package com.anysoftkeyboard.nextword;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.anysoftkeyboard.base.dictionaries.Dictionary;
import com.anysoftkeyboard.base.dictionaries.WordComposer;

import java.util.Random;

public class NextWordDictionary extends Dictionary {
    private static final Random msRandom = new Random();

    private static final int MAX_NEXT_SUGGESTIONS = 8;
    private static final int MAX_NEXT_WORD_CONTAINERS = 900;

    private final NextWordsStorage mStorage;

    private String mPreviousWord = null;

    private final ArrayMap<String, NextWordsContainer> mNextWordMap = new ArrayMap<>();

    public NextWordDictionary(Context context, String locale) {
        super("NextWordDictionary_" + locale);
        mStorage = new NextWordsStorage(context, locale);
    }

    @Override
    public void getWords(WordComposer wordComposer, WordCallback callback) {
        final String currentWord = wordComposer.getPreferredWord().toString();
        //firstly, updating the relations to the previous word
        if (mPreviousWord != null) {
            NextWordsContainer previousSet = mNextWordMap.get(mPreviousWord);
            if (previousSet == null) {
                if (mNextWordMap.size() > MAX_NEXT_WORD_CONTAINERS) {
                    String randomWordToDelete = mNextWordMap.keyAt(msRandom.nextInt(mNextWordMap.size()));
                    mNextWordMap.remove(randomWordToDelete);
                }
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
                if (!callback.addWord(suggestion.toCharArray(), 0, suggestion.length(), frequency--, this))
                    break;
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
        mStorage.storeNextWords(mNextWordMap.values());
    }

    @Override
    protected void loadAllResources() {
        for (NextWordsContainer container : mStorage.loadStoredNextWords()) {
            mNextWordMap.put(container.word, container);
        }
    }
}
