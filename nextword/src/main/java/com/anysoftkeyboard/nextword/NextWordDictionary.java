package com.anysoftkeyboard.nextword;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class NextWordDictionary implements NextWordGetter {
    private static final String TAG = "NextWordDictionary";

    private static final Random msRandom = new Random();

    private static final int MAX_NEXT_SUGGESTIONS = 8;
    private static final int MAX_NEXT_WORD_CONTAINERS = 900;

    /*
    static {
        try {
            System.loadLibrary("anysoftkey_next_word_jni");
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "******** Could not load native library anysoftkey_next_word_jni ********");
            Log.e(TAG, "******** Could not load native library anysoftkey_next_word_jni ********", ule);
            Log.e(TAG, "******** Could not load native library anysoftkey_next_word_jni ********");
        } catch (Throwable t) {
            Log.e(TAG, "******** Failed to load native dictionary anysoftkey_next_word_jni ********");
            Log.e(TAG, "******** Failed to load native dictionary anysoftkey_next_word_jni *******", t);
            Log.e(TAG, "******** Failed to load native dictionary anysoftkey_next_word_jni ********");
        }
    }
    */

    private final NextWordsStorage mStorage;

    private CharSequence mPreviousWord = null;

    private final ArrayMap<CharSequence, NextWordsContainer> mNextWordMap = new ArrayMap<>();

    private final String[] mReusableNextWordsResponse = new String[MAX_NEXT_SUGGESTIONS];
    private final SimpleIterable mReusableNextWordsIterable;

    //private volatile long mNativeDict;

    public NextWordDictionary(Context context, String locale) {
        mStorage = createNextWordsStorage(context, locale);
        mReusableNextWordsIterable = new SimpleIterable(mReusableNextWordsResponse);
        //mNativeDict = openNative("next_words_"+locale+".txt");
    }

    @NonNull
    protected NextWordsStorage createNextWordsStorage(Context context, String locale) {
        return new NextWordsStorage(context, locale);
    }

    /*
    private static native long openNative(String filename);

    private static native void loadNative(long dictPointer);

    private static native void clearNative(long dictPointer);

    private static native void closeNative(long dictPointer);
    */
    @Override
    public Iterable<String> getNextWords(CharSequence currentWord, int maxResults, final int minWordUsage) {
        maxResults = Math.min(MAX_NEXT_SUGGESTIONS, maxResults);
        //firstly, updating the relations to the previous word
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

        mPreviousWord = currentWord;

        mReusableNextWordsIterable.setArraySize(suggestionsCount);
        return mReusableNextWordsIterable;
    }

    public void close() {
        //closeNative(mNativeDict);
        mStorage.storeNextWords(mNextWordMap.values());
    }

    public void load() {
        //loadNative(mNativeDict);
        for (NextWordsContainer container : mStorage.loadStoredNextWords()) {
            if (Utils.DEBUG) Log.d(TAG, "Loaded " + container);
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
        //clearNative(mNativeDict);
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

        public void setArraySize(int arraySize) {
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
