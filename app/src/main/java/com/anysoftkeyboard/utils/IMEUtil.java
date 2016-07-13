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

package com.anysoftkeyboard.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.anysoftkeyboard.api.KeyCodes;
import com.menny.android.anysoftkeyboard.BuildConfig;

import java.util.List;

public class IMEUtil {


    private static final String TAG = "ASK IMEUtils";
    // In dictionary.cpp, getSuggestion() method,
    // suggestion scores are computed using the below formula.
    // original score
    //  := pow(mTypedLetterMultiplier (this is defined 2),
    //         (the number of matched characters between typed word and suggested word))
    //     * (individual word's score which defined in the unigram dictionary,
    //         and this score is defined in range [0, 255].)
    // Then, the following processing is applied.
    //     - If the dictionary word is matched up to the point of the user entry
    //       (full match up to min(before.length(), after.length())
    //       => Then multiply by FULL_MATCHED_WORDS_PROMOTION_RATE (this is defined 1.2)
    //     - If the word is a true full match except for differences in accents or
    //       capitalization, then treat it as if the score was 255.
    //     - If before.length() == after.length()
    //       => multiply by mFullWordMultiplier (this is defined 2))
    // So, maximum original score is pow(2, min(before.length(), after.length())) * 255 * 2 * 1.2
    // For historical reasons we ignore the 1.2 modifier (because the measure for a good
    // autocorrection threshold was done at a time when it didn't exist). This doesn't change
    // the result.
    // So, we can normalize original score by dividing pow(2, min(b.l(),a.l())) * 255 * 2.
    private static final int MAX_INITIAL_SCORE = 255;
    private static final int TYPED_LETTER_MULTIPLIER = 2;
    private static final int FULL_WORD_MULTIPLIER = 2;
    private static final int S_INT_MAX = 2147483647;

    public static double calcNormalizedScore(@NonNull CharSequence before, @NonNull CharSequence after, int score) {
        final int beforeLength = before.length();
        final int afterLength = after.length();
        if (beforeLength == 0 || afterLength == 0) return 0;
        final int distance = editDistance(before, after);
        // If afterLength < beforeLength, the algorithm is suggesting a word by excessive character
        // correction.
        int spaceCount = 0;
        for (int i = 0; i < afterLength; ++i) {
            if (after.charAt(i) == KeyCodes.SPACE) {
                ++spaceCount;
            }
        }
        if (spaceCount == afterLength) return 0;
        final double maximumScore = score == S_INT_MAX ? S_INT_MAX : MAX_INITIAL_SCORE
                * Math.pow(
                TYPED_LETTER_MULTIPLIER, Math.min(beforeLength, afterLength - spaceCount))
                * FULL_WORD_MULTIPLIER;
        // add a weight based on edit distance.
        // distance <= max(afterLength, beforeLength) == afterLength,
        // so, 0 <= distance / afterLength <= 1
        final double weight = 1.0 - (double) distance / afterLength;
        return (score / maximumScore) * weight;
    }

    /* Damerau-Levenshtein distance */
    public static int editDistance(@NonNull CharSequence s, @NonNull CharSequence t) {
        final int sl = s.length();
        final int tl = t.length();
        int[][] dp = new int[sl + 1][tl + 1];
        for (int i = 0; i <= sl; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= tl; j++) {
            dp[0][j] = j;
        }
        for (int i = 0; i < sl; ++i) {
            for (int j = 0; j < tl; ++j) {
                final char sc = Character.toLowerCase(s.charAt(i));
                final char tc = Character.toLowerCase(t.charAt(j));
                final int cost = sc == tc ? 0 : 1;
                dp[i + 1][j + 1] = Math.min(
                        dp[i][j + 1] + 1, Math.min(dp[i + 1][j] + 1, dp[i][j] + cost));
                // Overwrite for transposition cases
                if (i > 0 && j > 0
                        && sc == Character.toLowerCase(t.charAt(j - 1))
                        && tc == Character.toLowerCase(s.charAt(i - 1))) {
                    dp[i + 1][j + 1] = Math.min(dp[i + 1][j + 1], dp[i - 1][j - 1] + cost);
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Logger.d(TAG, "editDistance:" + s + "," + t);
            for (int i = 0; i < dp.length; ++i) {
                StringBuffer sb = new StringBuffer();
                for (int j = 0; j < dp[i].length; ++j) {
                    sb.append(dp[i][j]).append(',');
                }
                Logger.d(TAG, i + ":" + sb.toString());
            }
        }
        return dp[sl][tl];
    }

    /**
     * Remove duplicates from an array of strings.
     * <p/>
     * This method will always keep the first occurrence of all strings at their position
     * in the array, removing the subsequent ones.
     */
    public static void removeDupes(final List<CharSequence> suggestions, List<CharSequence> stringsPool) {
        if (suggestions.size() < 2) return;
        int i = 1;
        // Don't cache suggestions.size(), since we may be removing items
        while (i < suggestions.size()) {
            final CharSequence cur = suggestions.get(i);
            // Compare each suggestion with each previous suggestion
            for (int j = 0; j < i; j++) {
                CharSequence previous = suggestions.get(j);
                if (TextUtils.equals(cur, previous)) {
                    removeSuggestion(suggestions, i, stringsPool);
                    i--;
                    break;
                }
            }
            i++;
        }
    }

    public static void tripSuggestions(List<CharSequence> suggestions, final int maxSuggestions, List<CharSequence> stringsPool) {
        while (suggestions.size() > maxSuggestions) {
            removeSuggestion(suggestions, maxSuggestions, stringsPool);
        }
    }

    private static void removeSuggestion(List<CharSequence> suggestions, int indexToRemove, List<CharSequence> stringsPool) {
        CharSequence garbage = suggestions.remove(indexToRemove);
        if (garbage instanceof StringBuilder) {
            stringsPool.add(garbage);
        }
    }

    /* package */ static class RingCharBuffer {
        /* package */ static final int BUFSIZE = 20;
        private static final char PLACEHOLDER_DELIMITER_CHAR = '\uFFFC';
        private static final int INVALID_COORDINATE = -2;
        private static RingCharBuffer sRingCharBuffer = new RingCharBuffer();
        /* package */ int mLength = 0;
        private Context mContext;
        private boolean mEnabled = false;
        private int mEnd = 0;
        private char[] mCharBuf = new char[BUFSIZE];
        private int[] mXBuf = new int[BUFSIZE];
        private int[] mYBuf = new int[BUFSIZE];

        private RingCharBuffer() {
        }

        public static RingCharBuffer getInstance() {
            return sRingCharBuffer;
        }

        public static RingCharBuffer init(Context context, boolean enabled) {
            sRingCharBuffer.mContext = context;
            sRingCharBuffer.mEnabled = enabled;
            return sRingCharBuffer;
        }

        private int normalize(int in) {
            int ret = in % BUFSIZE;
            return ret < 0 ? ret + BUFSIZE : ret;
        }

        public void push(char c, int x, int y) {
            if (!mEnabled) return;
            mCharBuf[mEnd] = c;
            mXBuf[mEnd] = x;
            mYBuf[mEnd] = y;
            mEnd = normalize(mEnd + 1);
            if (mLength < BUFSIZE) {
                ++mLength;
            }
        }

        public char pop() {
            if (mLength < 1) {
                return PLACEHOLDER_DELIMITER_CHAR;
            } else {
                mEnd = normalize(mEnd - 1);
                --mLength;
                return mCharBuf[mEnd];
            }
        }

        public char getLastChar() {
            if (mLength < 1) {
                return PLACEHOLDER_DELIMITER_CHAR;
            } else {
                return mCharBuf[normalize(mEnd - 1)];
            }
        }

        public int getPreviousX(char c, int back) {
            int index = normalize(mEnd - 2 - back);
            if (mLength <= back
                    || Character.toLowerCase(c) != Character.toLowerCase(mCharBuf[index])) {
                return INVALID_COORDINATE;
            } else {
                return mXBuf[index];
            }
        }

        public int getPreviousY(char c, int back) {
            int index = normalize(mEnd - 2 - back);
            if (mLength <= back
                    || Character.toLowerCase(c) != Character.toLowerCase(mCharBuf[index])) {
                return INVALID_COORDINATE;
            } else {
                return mYBuf[index];
            }
        }

        public void reset() {
            mLength = 0;
        }
    }
}
