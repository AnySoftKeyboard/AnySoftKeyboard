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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;
import com.anysoftkeyboard.base.utils.CompatUtils;
import com.anysoftkeyboard.base.utils.GCUtils;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.GetWordsCallback;
import com.anysoftkeyboard.dictionaries.KeyCodesProvider;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/** Implements a static, compacted, binary dictionary of standard words. */
public class ResourceBinaryDictionary extends Dictionary {

    /**
     * There is difference between what java and native code can handle. This value should only be
     * used in BinaryDictionary.java It is necessary to keep it at this value because some languages
     * e.g. German have really long words.
     */
    private static final int MAX_WORD_LENGTH = 48;

    private static final String TAG = "ASK_ResBinDict";
    private static final int MAX_ALTERNATIVES = 16;
    private static final int MAX_WORDS = 18;
    private static final boolean ENABLE_MISSED_CHARACTERS = true;
    private final Context mOriginPackageContext;
    private final int mDictResId;
    private final int[] mInputCodes = new int[MAX_WORD_LENGTH * MAX_ALTERNATIVES];
    private final char[] mOutputChars = new char[MAX_WORD_LENGTH * MAX_WORDS];
    private final int[] mFrequencies = new int[MAX_WORDS];

    /**
     * NOTE! Keep a reference to the native dict direct buffer in Java to avoid unexpected
     * de-allocation of the direct buffer.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private ByteBuffer mNativeDictDirectBuffer;

    private final AtomicLong mNativeDictPointer = new AtomicLong(0L);

    /**
     * Create a dictionary from a raw resource file
     *
     * @param originPackageContext application context for reading resources
     * @param resId the resource containing the raw binary dictionary
     */
    public ResourceBinaryDictionary(
            @NonNull CharSequence dictionaryName,
            @NonNull Context originPackageContext,
            @XmlRes int resId) {
        super(dictionaryName);
        CompatUtils.loadNativeLibrary(originPackageContext, "anysoftkey2_jni", "1.0.3");
        mOriginPackageContext = originPackageContext;
        mDictResId = resId;
    }

    private native long openNative(
            ByteBuffer bb, int typedLetterMultiplier, int fullWordMultiplier);

    private native void closeNative(long dictPointer);

    private native boolean isValidWordNative(long dictPointer, char[] word, int wordLength);

    private native int getSuggestionsNative(
            long dictPointer,
            int[] inputCodes,
            int codesSize,
            char[] outputChars,
            int[] frequencies,
            int maxWordLength,
            int maxWords,
            int maxAlternatives,
            int skipPos,
            @Nullable int[] nextLettersFrequencies,
            int nextLettersSize);

    private native void getWordsNative(long dictPointer, GetWordsCallback callback);

    @Override
    protected void loadAllResources() {
        Resources pkgRes = mOriginPackageContext.getResources();
        final int[] resId;
        // is it an array of dictionaries? Or a ref to raw?
        final String dictResType = pkgRes.getResourceTypeName(mDictResId);
        if (dictResType.equalsIgnoreCase("raw")) {
            resId = new int[] {mDictResId};
        } else {
            Log.d(TAG, "type " + dictResType);
            TypedArray a = pkgRes.obtainTypedArray(mDictResId);
            try {
                resId = new int[a.length()];
                for (int index = 0; index < a.length(); index++) {
                    resId[index] = a.getResourceId(index, 0);
                }
            } finally {
                a.recycle();
            }
        }
        if (isClosed()) return;
        GCUtils.getInstance()
                .performOperationWithMemRetry(
                        TAG,
                        () -> {
                            // The try-catch is for issue 878:
                            // http://code.google.com/p/softkeyboard/issues/detail?id=878
                            try {
                                loadDictionaryFromResource(resId);
                            } catch (UnsatisfiedLinkError ex) {
                                Log.w(
                                        TAG,
                                        "Failed to load binary JNI connection! Error: "
                                                + ex.getMessage());
                            }
                        });
    }

    private void loadDictionaryFromResource(int[] resId) {
        final InputStream[] is = new InputStream[resId.length];
        try {
            // merging separated dictionary into one if dictionary is separated
            int total = 0;
            for (int i = 0; i < resId.length; i++) {
                // http://ponystyle.com/blog/2010/03/26/dealing-with-asset-compression-in-android-apps/
                // NOTE: the resource file can not be larger than 1MB
                is[i] = mOriginPackageContext.getResources().openRawResource(resId[i]);
                if (isClosed()) return;
                final int dictSize = is[i].available();
                Log.d(
                        TAG,
                        "Will load a resource dictionary id "
                                + resId[i]
                                + " whose size is "
                                + dictSize
                                + " bytes.");
                total += dictSize;
            }

            mNativeDictDirectBuffer =
                    ByteBuffer.allocateDirect(total).order(ByteOrder.nativeOrder());
            int got = 0;
            for (int i = 0; i < resId.length; i++) {
                got += Channels.newChannel(is[i]).read(mNativeDictDirectBuffer);
                if (isClosed()) return;
            }
            if (got != total) {
                Log.e(TAG, "Read " + got + " bytes, expected " + total);
            } else {
                mNativeDictPointer.set(
                        openNative(
                                mNativeDictDirectBuffer,
                                Dictionary.TYPED_LETTER_MULTIPLIER,
                                Dictionary.FULL_WORD_FREQ_MULTIPLIER));
                Logger.d(
                        TAG,
                        "Will use pointer %d for %s - %d",
                        mNativeDictPointer.get(),
                        toString(),
                        hashCode());
            }
        } catch (IOException e) {
            Log.w(TAG, "No available memory for binary dictionary: " + e.getMessage());
        } finally {
            for (InputStream i1 : is) {
                try {
                    if (i1 != null) i1.close();
                } catch (IOException e) {
                    Log.w(TAG, "Failed to close input stream");
                }
            }
        }
    }

    @Override
    public void getSuggestions(
            final KeyCodesProvider codes,
            final WordCallback callback /*, int[] nextLettersFrequencies*/) {
        if (isLoading() || isClosed()) return;
        final int codesSize = codes.codePointCount();
        // Won't deal with really long words.
        if (codesSize > MAX_WORD_LENGTH - 1) return;

        Arrays.fill(mInputCodes, -1);
        for (int i = 0; i < codesSize; i++) {
            int[] alternatives = codes.getCodesAt(i);
            System.arraycopy(
                    alternatives,
                    0,
                    mInputCodes,
                    i * MAX_ALTERNATIVES,
                    Math.min(alternatives.length, MAX_ALTERNATIVES));
        }
        Arrays.fill(mOutputChars, (char) 0);
        Arrays.fill(mFrequencies, 0);

        int count =
                getSuggestionsNative(
                        mNativeDictPointer.get(),
                        mInputCodes,
                        codesSize,
                        mOutputChars,
                        mFrequencies,
                        MAX_WORD_LENGTH,
                        MAX_WORDS,
                        MAX_ALTERNATIVES,
                        -1,
                        null,
                        0);

        // If there aren't sufficient suggestions, search for words by allowing
        // wild cards at
        // the different character positions. This feature is not ready for
        // prime-time as we need
        // to figure out the best ranking for such words compared to proximity
        // corrections and
        // completions.
        if (ENABLE_MISSED_CHARACTERS && count < 5) {
            for (int skip = 0; skip < codesSize; skip++) {
                int tempCount =
                        getSuggestionsNative(
                                mNativeDictPointer.get(),
                                mInputCodes,
                                codesSize,
                                mOutputChars,
                                mFrequencies,
                                MAX_WORD_LENGTH,
                                MAX_WORDS,
                                MAX_ALTERNATIVES,
                                skip,
                                null,
                                0);
                count = Math.max(count, tempCount);
                if (tempCount > 0) break;
            }
        }

        boolean requestContinue = true;
        for (int j = 0; j < count && requestContinue; j++) {
            if (mFrequencies[j] < 1) break;
            int start = j * MAX_WORD_LENGTH;
            int len = 0;
            while (mOutputChars[start + len] != 0) {
                len++;
            }
            if (len > 0) {
                requestContinue =
                        callback.addWord(
                                mOutputChars,
                                start,
                                len,
                                mFrequencies[j] /*, mDicTypeId, DataType.UNIGRAM*/,
                                this);
            }
        }
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        if (word == null || isLoading() || isClosed()) return false;
        char[] chars = word.toString().toCharArray();
        return isValidWordNative(mNativeDictPointer.get(), chars, chars.length);
    }

    @Override
    protected void closeAllResources() {
        final long dictionaryPointer = mNativeDictPointer.getAndSet(0L);
        if (dictionaryPointer != 0) {
            Logger.d(
                    TAG,
                    "Going to close pointer %d for %s - %d",
                    dictionaryPointer,
                    toString(),
                    hashCode());
            closeNative(dictionaryPointer);
        }
    }

    @Override
    public void getLoadedWords(@NonNull GetWordsCallback callback) {
        getWordsNative(mNativeDictPointer.get(), callback);
    }
}
