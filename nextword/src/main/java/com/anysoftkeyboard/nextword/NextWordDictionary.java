package com.anysoftkeyboard.nextword;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

public class NextWordDictionary implements NextWordSuggestions {
    private static final String TAG = "NextWordDictionary";

    private static final Random msRandom = new Random();

    private static final int MAX_NEXT_SUGGESTIONS = 8;
    private static final int MAX_NEXT_WORD_CONTAINERS = 900;

    private final NextWordsStorage mStorage;

    private CharSequence mPreviousWord = null;

    private final ArrayMap<CharSequence, NextWordsContainer> mNextWordMap = new ArrayMap<>();

    private final String[] mReusableNextWordsResponse = new String[MAX_NEXT_SUGGESTIONS];
    private final SimpleIterable mReusableNextWordsIterable;

    public NextWordDictionary(Context context, String locale) {
        mStorage = new NextWordsStorage(context, locale);
        mReusableNextWordsIterable = new SimpleIterable(mReusableNextWordsResponse);
    }

    @Override
    public void notifyNextTypedWord(@NonNull CharSequence currentWord) {
        if (mPreviousWord != null) {
            NextWordsContainer previousSet = mNextWordMap.get(mPreviousWord);
            if (previousSet == null) {
                if (mNextWordMap.size() > MAX_NEXT_WORD_CONTAINERS) {
                    CharSequence randomWordToDelete = mNextWordMap.keyAt(msRandom.nextInt(mNextWordMap.size()));
                    mNextWordMap.remove(randomWordToDelete);
                }
                previousSet = new NextWordsContainer(mPreviousWord);
                mNextWordMap.put(mPreviousWord, previousSet);
            }

            previousSet.markWordAsUsed(currentWord);
        }

        mPreviousWord = currentWord;
    }

    @Override
    @NonNull
    public Iterable<String> getNextWords(@NonNull CharSequence currentWord, int maxResults, final int minWordUsage) {
        maxResults = Math.min(MAX_NEXT_SUGGESTIONS, maxResults);

        //secondly, get a list of suggestions
        NextWordsContainer nextSet = mNextWordMap.get(currentWord);
        int suggestionsCount = 0;
        if (nextSet != null) {
            for (NextWord nextWord : nextSet.getNextWordSuggestions()) {
                if (nextWord.getUsedCount() < minWordUsage) continue;

                mReusableNextWordsResponse[suggestionsCount] = nextWord.nextWord;
                suggestionsCount++;
                if (suggestionsCount == maxResults) break;
            }
        }

        mReusableNextWordsIterable.setArraySize(suggestionsCount);
        return mReusableNextWordsIterable;
    }

    public void close() {
        mStorage.storeNextWords(mNextWordMap.values());
    }

    public void load() {
        for (NextWordsContainer container : mStorage.loadStoredNextWords()) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Loaded " + container);
            mNextWordMap.put(container.word, container);
        }
    }

    @Override
    public void resetSentence() {
        mPreviousWord = null;
    }

    public NextWordStatistics dumpDictionaryStatistics() {
        int firstWordCount = 0;
        int secondWordCount = 0;

        for (Map.Entry<CharSequence, NextWordsContainer> entry : mNextWordMap.entrySet()) {
            firstWordCount++;
            secondWordCount += entry.getValue().getNextWordSuggestions().size();
        }

        return new NextWordStatistics(firstWordCount, secondWordCount);
    }

    public void clearData() {
        resetSentence();
        mNextWordMap.clear();
    }

    private static class SimpleIterable implements Iterable<String> {
        private final String[] mStrings;
        private int mLength;

        public SimpleIterable(String[] strings) {
            mStrings = strings;
            mLength = 0;
        }

        void setArraySize(int arraySize) {
            mLength = arraySize;
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
                    if (!hasNext()) throw new NoSuchElementException();
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
