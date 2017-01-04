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

package com.anysoftkeyboard.base.dictionaries;

/**
 * Abstract base class for a dictionary that can do a fuzzy search for words based on a set of key
 * strokes.
 */
public abstract class Dictionary {
    public static final int MAX_WORD_LENGTH = 32;
    public static final int MAX_WORD_FREQUENCY = 255;

    /**
     * Whether or not to replicate the typed word in the suggested list, even if it's valid.
     */
    protected static final boolean INCLUDE_TYPED_WORD_IF_VALID = false;

    /**
     * The weight to give to a word if it's length is the same as the number of typed characters.
     */
    protected static final int FULL_WORD_FREQ_MULTIPLIER = 3;

    /**
     * The weight to give to a letter if it is typed.
     */
    protected static final int TYPED_LETTER_MULTIPLIER = 3;

    /**
     * Interface to be implemented by classes requesting words to be fetched from the dictionary.
     *
     * @see #getWords(WordComposer, WordCallback)
     */
    public interface WordCallback {
        /**
         * Adds a word to a list of suggestions. The word is expected to be ordered based on
         * the provided frequency.
         *
         * @param word       the character array containing the word
         * @param wordOffset starting offset of the word in the character array
         * @param wordLength length of valid characters in the character array
         * @param frequency  the frequency of occurence. This is normalized between 1 and 255, but
         *                   can exceed those limits
         * @return true if the word was added, false if no more words are required
         */
        boolean addWord(char[] word, int wordOffset, int wordLength, int frequency, Dictionary from);
    }

    private volatile boolean mLoadingResources = true;
    protected final Object mResourceMonitor = new Object();
    private final String mDictionaryName;
    private volatile boolean mClosed = false;

    protected Dictionary(String dictionaryName) {
        mDictionaryName = dictionaryName;
    }

    protected boolean isLoading() {
        return mLoadingResources;
    }

    /**
     * Searches for words in the dictionary that match the characters in the composer. Matched
     * words are added through the callback object.
     *
     * @param composer the key sequence to match
     * @param callback the callback object to send matched words to as possible candidates
     * @see WordCallback#addWord(char[], int, int, int, Dictionary)
     */
    public abstract void getWords(final WordComposer composer, final WordCallback callback);

    public void getWordsForPath(final int[] charactersInPath, final int pathLength, final WordCallback callback) {
        //nothing here.
    }

    /**
     * Checks if the given word occurs in the dictionary
     *
     * @param word the word to search for. The search should be case-insensitive.
     * @return true if the word exists, false otherwise
     */
    abstract public boolean isValidWord(CharSequence word);

    /**
     * Compares the contents of the character array with the typed word and returns true if they
     * are the same.
     *
     * @param word      the array of characters that make up the word
     * @param length    the number of valid characters in the character array
     * @param typedWord the word to compare with
     * @return true if they are the same, false otherwise.
     */
    static protected boolean same(final char[] word, final int length, final CharSequence typedWord) {
        if (typedWord.length() != length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (word[i] != typedWord.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public final void close() {
        if (mClosed)
            return;
        mClosed = true;
        synchronized (mResourceMonitor) {
            closeAllResources();
        }
    }

    public final boolean isClosed() {
        return mClosed;
    }

    protected abstract void closeAllResources();

    public final void loadDictionary() {
        if (mClosed)
            return;
        synchronized (mResourceMonitor) {
            try {
                mLoadingResources = true;
                if (mClosed)
                    return;
                loadAllResources();
            } finally {
                mLoadingResources = false;
            }
        }
    }

    protected abstract void loadAllResources();

    public final String getDictionaryName() {
        return mDictionaryName;
    }

    @Override
    public String toString() {
        return mDictionaryName;
    }
}
