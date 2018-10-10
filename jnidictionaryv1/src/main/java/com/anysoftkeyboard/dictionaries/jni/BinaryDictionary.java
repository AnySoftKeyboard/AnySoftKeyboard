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

package com.anysoftkeyboard.dictionaries.jni;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.anysoftkeyboard.base.utils.CompatUtils;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.KeyCodesProvider;

import java.io.FileDescriptor;
import java.util.Arrays;

/**
 * Implements a static, compacted, binary dictionary of standard words.
 */
public class BinaryDictionary extends Dictionary {
    public static final int MAX_WORD_LENGTH = 20;
    private static final String TAG = "ASK_BinaryDictionary";
    private static final int MAX_ALTERNATIVES = 16;
    private static final int MAX_WORDS = 16;
    private static final boolean ENABLE_MISSED_CHARACTERS = true;
    private final AssetFileDescriptor mAfd;
    private volatile long mNativeDict;
    private int[] mInputCodes = new int[MAX_WORD_LENGTH * MAX_ALTERNATIVES];
    private char[] mOutputChars = new char[MAX_WORD_LENGTH * MAX_WORDS];
    private int[] mFrequencies = new int[MAX_WORDS];

    public BinaryDictionary(@NonNull Context context, @NonNull CharSequence dictionaryName, @NonNull AssetFileDescriptor afd, boolean isDebug) {
        super(dictionaryName);
        CompatUtils.loadNativeLibrary(context, "anysoftkey_jni", "1.0", isDebug);
        mAfd = afd;
    }

    @Override
    protected final void loadAllResources() {
        //The try-catch is for issue 878: http://code.google.com/p/softkeyboard/issues/detail?id=878
        try {
            mNativeDict = 0;
            long startTime = SystemClock.uptimeMillis();
            mNativeDict = openNative(mAfd.getFileDescriptor(), mAfd.getStartOffset(), mAfd.getLength(), Dictionary.TYPED_LETTER_MULTIPLIER, Dictionary.FULL_WORD_FREQ_MULTIPLIER);
            Log.d(TAG, "Loaded dictionary in " + (SystemClock.uptimeMillis() - startTime) + "ms");
        } catch (UnsatisfiedLinkError ex) {
            Log.w(TAG, "Failed to load binary JNI connection! Error: " + ex.getMessage());
        }
    }

    private native long openNative(FileDescriptor fd, long offset, long length, int typedLetterMultiplier, int fullWordMultiplier);

    private native void closeNative(long dictPointer);

    private native boolean isValidWordNative(long dictPointer, char[] word, int wordLength);

    private native int getSuggestionsNative(long dictPointer, int[] inputCodes, int codesSize, char[] outputChars, int[] frequencies, int maxWordLength, int maxWords, int maxAlternatives,
            int skipPos);

    @Override
    public char[][] getWords() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getWords(final KeyCodesProvider codes, final WordCallback callback) {
        if (mNativeDict == 0 || isClosed()) return;
        final int codesSize = codes.length();
        // Wont deal with really long words.
        if (codesSize > MAX_WORD_LENGTH - 1) return;

        Arrays.fill(mInputCodes, -1);
        for (int i = 0; i < codesSize; i++) {
            int[] alternatives = codes.getCodesAt(i);
            System.arraycopy(alternatives, 0, mInputCodes, i * MAX_ALTERNATIVES, Math.min(alternatives.length, MAX_ALTERNATIVES));
        }
        Arrays.fill(mOutputChars, (char) 0);
        Arrays.fill(mFrequencies, 0);

        int count = getSuggestionsNative(mNativeDict, mInputCodes, codesSize, mOutputChars, mFrequencies, MAX_WORD_LENGTH, MAX_WORDS, MAX_ALTERNATIVES, -1);

        // If there aren't sufficient suggestions, search for words by allowing wild cards at
        // the different character positions. This feature is not ready for prime-time as we need
        // to figure out the best ranking for such words compared to proximity corrections and
        // completions.
        if (ENABLE_MISSED_CHARACTERS && count < 5) {
            for (int skip = 0; skip < codesSize; skip++) {
                int tempCount = getSuggestionsNative(mNativeDict, mInputCodes, codesSize, mOutputChars, mFrequencies, MAX_WORD_LENGTH, MAX_WORDS, MAX_ALTERNATIVES, skip);
                count = Math.max(count, tempCount);
                if (tempCount > 0) break;
            }
        }

        boolean requestContinue = true;
        for (int j = 0; j < count && requestContinue; j++) {
            if (mFrequencies[j] < 1) break;
            final int start = j * MAX_WORD_LENGTH;

            int position = start;
            while ((mOutputChars.length > position) && (mOutputChars[position] != 0)) {
                position++;
            }
            final int len = (position - start);
            if (len > 0) {
                requestContinue = callback.addWord(mOutputChars, start, len, mFrequencies[j], this);
            }
        }
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        if (word == null || mNativeDict == 0 || isClosed()) return false;
        char[] chars = word.toString().toCharArray();
        return isValidWordNative(mNativeDict, chars, chars.length);
    }

    @Override
    protected final void closeAllResources() {
        if (mNativeDict != 0) {
            closeNative(mNativeDict);
            mNativeDict = 0;
        }
    }
}
