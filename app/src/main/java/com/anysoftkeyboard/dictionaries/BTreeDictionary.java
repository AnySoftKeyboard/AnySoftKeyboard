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

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public abstract class BTreeDictionary extends EditableDictionary {

    @NonNull
    private Disposable mDictionaryChangedLoader = Disposables.empty();

    public interface WordReadListener {
        /**
         * Callback when a word has been read from storage.
         *
         * @return true if to continue, false to stop.
         */
        boolean onWordRead(String word, int frequency);
    }


    protected static final int MAX_WORD_LENGTH = 32;
    protected static final String TAG = "ASK UDict";
    private static final int INITIAL_ROOT_CAPACITY = 26/*number of letters in the English Alphabet. Why bother with auto-increment, when we can start at roughly the right final size..*/;
    protected final Context mContext;
    private final int mMaxWordsToRead;

    private NodeArray mRoots;
    private int mMaxDepth;
    private int mInputLength;
    private ContentObserver mObserver = null;
    private char[] mWordBuilder = new char[MAX_WORD_LENGTH];
    private final boolean mIncludeTypedWord;

    protected BTreeDictionary(String dictionaryName, Context context) {
        this(dictionaryName, context, false);
    }

    protected BTreeDictionary(String dictionaryName, Context context, boolean includeTypedWord) {
        super(dictionaryName);
        mMaxWordsToRead = context.getResources().getInteger(R.integer.maximum_dictionary_words_to_load);
        mContext = context;
        mIncludeTypedWord = includeTypedWord;
        //creating the root node.
        clearDictionary();
    }

    @Override
    protected void loadAllResources() {
        WordReadListener listener = createWordReadListener();
        readWordsFromActualStorage(listener);

        if (!isClosed() && mObserver == null) {
            mObserver = AnyApplication.getDeviceSpecific().createDictionaryContentObserver(this);
            registerObserver(mObserver, mContext.getContentResolver());
        }
    }

    @NonNull
    protected WordReadListener createWordReadListener() {
        return new WordReadListener() {
                private int mReadWords = 0;

                @Override
                public boolean onWordRead(String word, int frequency) {
                    if (!TextUtils.isEmpty(word) && frequency > 0) {
                        //adding only good words
                        addWordFromStorageToMemory(word, frequency);
                    }
                    return ++mReadWords < mMaxWordsToRead && !isClosed();
                }
            };
    }

    protected abstract void readWordsFromActualStorage(WordReadListener wordReadListener);

    /**
     * Adds a word to the dictionary and makes it persistent.
     *
     * @param word      the word to add. If the word is capitalized, then the
     *                  dictionary will recognize it as a capitalized word when
     *                  searched.
     * @param frequency the frequency of occurrence of the word. A frequency of 255 is
     *                  considered the highest.
     */
    @Override
    public boolean addWord(String word, int frequency) {
        synchronized (mResourceMonitor) {
            if (isClosed()) {
                Logger.d(TAG, "Dictionary (type " + this.getClass().getName() + ") " + this.getDictionaryName() + " is closed! Can not add word.");
                return false;
            }
            // Safeguard against adding long words. Can cause stack overflow.
            if (word.length() >= getMaxWordLength()) return false;

            Logger.i(TAG, "Adding word '" + word + "' to dictionary (in " + getClass().getSimpleName() + ") with frequency " + frequency);
            //first deleting the word, so it wont conflict in the adding (_ID is unique).
            deleteWord(word);
            //add word to in-memory structure
            addWordRec(mRoots, word, 0, frequency);
            //add word to storage
            addWordToStorage(word, frequency);
        }
        return true;
    }

    protected int getMaxWordLength() {
        return MAX_WORD_LENGTH;
    }

    protected void onStorageChanged() {
        if (isClosed()) return;
        clearDictionary();
        mDictionaryChangedLoader = DictionaryBackgroundLoader.reloadDictionaryInBackground(this);
    }

    @Override
    public final void deleteWord(String word) {
        synchronized (mResourceMonitor) {
            if (isClosed()) {
                Logger.d(TAG, "Dictionary (type " + this.getClass().getName() + ") " + this.getDictionaryName() + " is closed! Can not delete word.");
                return;
            }
            deleteWordRec(mRoots, word, 0, word.length());
            deleteWordFromStorage(word);
        }
    }

    private boolean deleteWordRec(final NodeArray children, final CharSequence word, final int offset, final int length) {
        final int count = children.length;
        final char currentChar = word.charAt(offset);
        for (int j = 0; j < count; j++) {
            final Node node = children.data[j];
            if (node.code == currentChar) {
                if (offset == length - 1) {//last character in the word to delete
                    //we need to delete this node. But only if it terminal
                    if (node.terminal) {
                        if (node.children == null || node.children.length == 0) {
                            //terminal node, with no children - can be safely removed
                            children.deleteNode(j);
                        } else {
                            //terminal node with children. So, it is no longer terminal
                            node.terminal = false;
                        }
                        //let's tell that we deleted a node
                        return true;
                    } else {
                        //it is not terminal, and the word to delete is longer
                        //let's tell that we didn't delete
                        return false;
                    }
                } else if (node.terminal &&//a terminal node
                        (node.children == null || node.children.length == 0)) {//has no children
                    //this is not the last character, but this is a terminal node with no children! Nothing to delete here.
                    return false;
                } else {
                    //not the last character in the word to delete, and not a terminal node.
                    //but if the node forward was deleted, then this one might also need to be deleted.
                    final boolean aChildNodeWasDeleted = deleteWordRec(node.children, word, offset + 1, length);
                    if (aChildNodeWasDeleted) {//something was deleted in my children
                        if (node.children.length == 0 && !node.terminal) {
                            //this node just deleted its last child, and it is not a terminal character.
                            //it is not necessary anymore.
                            children.deleteNode(j);
                            //let's tell that we deleted.
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return false;//nothing to delete here, move along.
    }

    protected abstract void deleteWordFromStorage(String word);

    protected abstract void registerObserver(ContentObserver dictionaryContentObserver, ContentResolver contentResolver);

    protected abstract void addWordToStorage(String word, int frequency);

    @Override
    public void getWords(final KeyCodesProvider codes, final Dictionary.WordCallback callback) {
        if (isLoading() || isClosed()) return;
        mInputLength = codes.length();
        mMaxDepth = mInputLength * 2;
        getWordsRec(mRoots, codes, mWordBuilder, 0, false, 1.0f, 0, callback);
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        return getWordFrequency(word) > 0;
    }


    /**
     * Checks for the given word's frequency.
     *
     * @param word the word to search for. The search should be case-insensitive.
     * @return frequency value (higher is better. 0 means not exists, 1 is minimum, 255 is maximum).
     */
    public final int getWordFrequency(CharSequence word) {
        if (isLoading() || isClosed()) return 0;
        return getWordFrequencyRec(mRoots, word, 0, word.length());
    }

    private int getWordFrequencyRec(final NodeArray children, final CharSequence word, final int offset, final int length) {
        final int count = children.length;
        char currentChar = word.charAt(offset);
        for (int j = 0; j < count; j++) {
            final Node node = children.data[j];
            if (node.code == currentChar) {
                if (offset == length - 1) {
                    if (node.terminal) {
                        return node.frequency;
                    }
                } else {
                    if (node.children != null) {
                        int frequency = getWordFrequencyRec(node.children, word, offset + 1, length);
                        if (frequency > 0)
                            return frequency;
                    }
                }
            }
        }
        //no luck, can't find the word
        return 0;
    }

    /**
     * Recursively traverse the tree for words that match the input. Input
     * consists of a list of arrays. Each item in the list is one input
     * character position. An input character is actually an array of multiple
     * possible candidates. This function is not optimized for speed, assuming
     * that the user dictionary will only be a few hundred words in size.
     *
     * @param roots      node whose children have to be search for matches
     * @param codes      the input character mCodes
     * @param word       the word being composed as a possible match
     * @param depth      the depth of traversal - the length of the word being composed
     *                   thus far
     * @param completion whether the traversal is now in completion mode - meaning that
     *                   we've exhausted the input and we're looking for all possible
     *                   suffixes.
     * @param snr        current weight of the word being formed
     * @param inputIndex position in the input characters. This can be off from the
     *                   depth in case we skip over some punctuations such as
     *                   apostrophe in the traversal. That is, if you type "wouldve",
     *                   it could be matching "would've", so the depth will be one more
     *                   than the inputIndex
     * @param callback   the callback class for adding a word
     */
    private void getWordsRec(NodeArray roots, final KeyCodesProvider codes, final char[] word, final int depth, boolean completion, float snr, int inputIndex, WordCallback callback) {
        final int count = roots.length;
        final int codeSize = mInputLength;
        // Optimization: Prune out words that are too long compared to how much
        // was typed.
        if (depth > mMaxDepth) {
            return;
        }
        int[] currentChars = null;
        if (codeSize <= inputIndex) {
            completion = true;
        } else {
            currentChars = codes.getCodesAt(inputIndex);
        }

        for (int i = 0; i < count; i++) {
            final Node node = roots.data[i];
            final char nodeC = node.code;
            final char nodeLowerC = toLowerCase(nodeC);
            boolean terminal = node.terminal;
            NodeArray children = node.children;
            int freq = node.frequency;
            if (completion) {
                word[depth] = nodeC;
                if (terminal && !callback.addWord(word, 0, depth + 1, (int) (freq * snr), this)) {
                    return;
                }
                if (children != null) {
                    getWordsRec(children, codes, word, depth + 1, completion, snr, inputIndex, callback);
                }
            } else {
                for (int j = 0; j < currentChars.length; j++) {
                    float addedAttenuation = (j > 0 ? 1f : 3f);
                    if (currentChars[j] == -1) {
                        break;
                    }
                    final char currentTypedChar = (char) currentChars[j];
                    final char currentLowerTypedChar = toLowerCase(currentTypedChar);

                    if (currentLowerTypedChar == nodeLowerC || currentTypedChar == nodeC) {
                        //note: we are suggesting the word in the b-tree, not the one
                        //the user typed. We want to keep capitalized letters, quotes etc.
                        word[depth] = nodeC;

                        if (codeSize == depth + 1) {
                            if (terminal && (mIncludeTypedWord || !same(word, depth + 1, codes.getTypedWord()))) {
                                callback.addWord(word, 0, depth + 1, (int) (freq * snr * addedAttenuation * FULL_WORD_FREQ_MULTIPLIER), this);
                            }
                            if (children != null) {
                                getWordsRec(children, codes, word, depth + 1, true, snr * addedAttenuation, inputIndex + 1, callback);
                            }
                        } else if (children != null) {
                            getWordsRec(children, codes, word, depth + 1, false, snr * addedAttenuation, inputIndex + 1, callback);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected final void closeAllResources() {
        clearDictionary();
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }

        closeStorage();
    }

    protected void addWordFromStorageToMemory(String word, int frequency) {
        addWordRec(mRoots, word, 0, frequency);
    }

    private void addWordRec(NodeArray children, final String word, final int depth, final int frequency) {
        final int wordLength = word.length();
        final char c = word.charAt(depth);
        // Does children have the current character?
        final int childrenLength = children.length;
        Node childNode = null;
        boolean found = false;
        for (int i = 0; i < childrenLength; i++) {
            childNode = children.data[i];
            if (childNode.code == c) {
                found = true;
                break;
            }
        }
        if (!found) {
            childNode = new Node();
            childNode.code = c;
            children.add(childNode);
        }
        if (wordLength == depth + 1) {
            // Terminate this word
            childNode.terminal = true;
            childNode.frequency = frequency;
            // words
            return;
        }
        if (childNode.children == null) {
            childNode.children = new NodeArray();
        }
        addWordRec(childNode.children, word, depth + 1, frequency);
    }

    private void clearDictionary() {
        mDictionaryChangedLoader.dispose();
        mRoots = new NodeArray(INITIAL_ROOT_CAPACITY);
    }

    protected abstract void closeStorage();

    static class Node {
        char code;
        int frequency;
        boolean terminal;
        NodeArray children;
    }

    static class NodeArray {
        private static final int INCREMENT = 2;
        Node[] data;
        int length = 0;

        NodeArray(int initialCapacity) {
            data = new Node[initialCapacity];
        }

        NodeArray() {
            this(INCREMENT);
        }

        void add(Node n) {
            length++;
            if (length > data.length) {
                Node[] tempData = new Node[length + INCREMENT];
                System.arraycopy(data, 0, tempData, 0, data.length);
                data = tempData;
            }
            data[length - 1] = n;
        }

        public void deleteNode(int nodeIndexToDelete) {
            length--;
            if (length > 0) {
                for (int i = nodeIndexToDelete; i < length; i++) {
                    data[i] = data[i + 1];
                }
            }
        }
    }

    @Override
    public char[][] getWords() {
        throw new UnsupportedOperationException();
    }
}
