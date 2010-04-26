/*
 * Copyright (C) 2008-2009 Google Inc.
 * Copyright (C) 2009 Spiros Papadimitriou <spapadim@cs.cmu.edu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.menny.android.anysoftkeyboard.dictionary2;

import java.io.FileDescriptor;
import java.util.Arrays;

import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.menny.android.anysoftkeyboard.WordComposer;

/**
 * Implements a static, compacted, binary dictionary of standard words.
 */
class BinaryDictionary extends Dictionary {
    private static final String TAG = "ASK_BinaryDictionary";

    public static final int MAX_WORD_LENGTH = 48;
    private static final int MAX_ALTERNATIVES = 16;
    private static final int MAX_WORDS = 16;
    private final AssetFileDescriptor mAfd;

    private static final int TYPED_LETTER_MULTIPLIER = 2;

    private int mNativeDict;
    private int[] mInputCodes = new int[MAX_WORD_LENGTH * MAX_ALTERNATIVES];
    private char[] mOutputChars = new char[MAX_WORD_LENGTH * MAX_WORDS];
    private int[] mFrequencies = new int[MAX_WORDS];


    static {
        try {
            System.loadLibrary("nativeime");
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "Could not load native library nativeim", ule);
        }
    }

    /**
     * Create a dictionary from a raw resource file
     * @param context application context for reading resources
     * @param resId the resource containing the raw binary dictionary
     */
    public BinaryDictionary(AssetFileDescriptor afd) {
    	mAfd = afd;
    }

    @Override
    public void loadDictionary() throws Exception {
    	if (mAfd != null) {
            loadDictionary(mAfd);
        }
    }

    private native int openNative(FileDescriptor fd, long offset, long length,
            int typedLetterMultiplier, int fullWordMultiplier);
    private native void closeNative(int dict);
    private native boolean isValidWordNative(int nativeData, char[] word, int wordLength);
    private native int getSuggestionsNative(int dict, int[] inputCodes, int codesSize,
            char[] outputChars, int[] frequencies,
            int maxWordLength, int maxWords, int maxAlternatives);

    private final void loadDictionary(AssetFileDescriptor afd) {
        long startTime = System.currentTimeMillis();
        mNativeDict = openNative(afd.getFileDescriptor(),
                afd.getStartOffset(), afd.getLength(),
                TYPED_LETTER_MULTIPLIER, FULL_WORD_FREQ_MULTIPLIER);
        Log.i(TAG, "Loaded dictionary in " + (System.currentTimeMillis() - startTime) + "msec");
    }

    @Override
    public void getWords(final WordComposer codes, final WordCallback callback) {
        final int codesSize = codes.size();
        // Wont deal with really long words.
        if (codesSize > MAX_WORD_LENGTH - 1) return;

        Arrays.fill(mInputCodes, -1);
        for (int i = 0; i < codesSize; i++) {
            int[] alternatives = codes.getCodesAt(i);
            System.arraycopy(alternatives, 0, mInputCodes, i * MAX_ALTERNATIVES,
                    Math.min(alternatives.length, MAX_ALTERNATIVES));
        }
        Arrays.fill(mOutputChars, (char) 0);

        final int count = getSuggestionsNative(mNativeDict, mInputCodes, codesSize, mOutputChars, mFrequencies,
                MAX_WORD_LENGTH, MAX_WORDS, MAX_ALTERNATIVES);

        for (int j = 0; j < count; j++) {
            if (mFrequencies[j] < 1) break;
            int start = j * MAX_WORD_LENGTH;
            int len = 0;
            while (mOutputChars[start + len] != 0) {
                len++;
            }
            if (len > 0) {
                callback.addWord(mOutputChars, start, len, mFrequencies[j]);
            }
        }
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        if (word == null) return false;
        char[] chars = word.toString().toLowerCase().toCharArray();
        return isValidWordNative(mNativeDict, chars, chars.length);
    }

    public synchronized void close() {
        if (mNativeDict != 0) {
            closeNative(mNativeDict);
            mNativeDict = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
