package com.anysoftkeyboard.nextword;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.util.Iterator;
import java.util.Random;

public class NextWordDictionary {
    private static final Random msRandom = new Random();

    private static final int MAX_NEXT_SUGGESTIONS = 8;
    private static final int MAX_NEXT_WORD_CONTAINERS = 900;
    private static final String TAG = "NextWordDictionary";

    private final NextWordsStorage mStorage;

    private String mPreviousWord = null;

    private final ArrayMap<String, NextWordsContainer> mNextWordMap = new ArrayMap<>();

    private final String[] mReusableNextWordsResponse = new String[MAX_NEXT_SUGGESTIONS];

    public NextWordDictionary(Context context, String locale) {
        mStorage = new NextWordsStorage(context, locale);
    }

    public Iterable<String> getNextWords(String currentWord) {
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
        int suggestionsCount = 0;
        if (nextSet != null) {
            for (NextWord nextWord : nextSet.getNextWordSuggestions()) {
                mReusableNextWordsResponse[suggestionsCount] = nextWord.nextWord;
                suggestionsCount++;
                if (suggestionsCount == MAX_NEXT_SUGGESTIONS) break;
            }
        }

        mPreviousWord = currentWord;

        return new SimpleIterable(mReusableNextWordsResponse, suggestionsCount);
    }

    public void saveToStorage() {
        mStorage.storeNextWords(mNextWordMap.values());
    }

    public void loadFromStorage() {
        for (NextWordsContainer container : mStorage.loadStoredNextWords()) {
            if (Utils.DEBUG) Log.d(TAG, "Loaded "+container);
            mNextWordMap.put(container.word, container);
        }
    }

    public void resetSentence() {
        mPreviousWord = null;
    }

    private static class SimpleIterable implements Iterable<String> {
        private final String[] mStrings;
        private final int mLength;

        public SimpleIterable(String[] strings, int length) {
            mStrings = strings;
            mLength = length;
        }

        @Override
        public Iterator<String> iterator() {

            return new Iterator<String>() {
                private int mIndex = 0;

                @Override
                public boolean hasNext() {
                    return mIndex < mLength;
                }

                @Override
                public String next() {
                    return mStrings[mIndex++];
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not supporting remove right now");
                }
            };
        }
    }
}
