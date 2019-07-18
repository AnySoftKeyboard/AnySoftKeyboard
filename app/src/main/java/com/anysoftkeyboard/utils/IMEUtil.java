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

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.BuildConfig;
import java.util.List;

public class IMEUtil {
    public static final int IME_ACTION_CUSTOM_LABEL = EditorInfo.IME_MASK_ACTION + 1;

    private static final String TAG = "ASK IMEUtils";

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
                dp[i + 1][j + 1] =
                        Math.min(dp[i][j + 1] + 1, Math.min(dp[i + 1][j] + 1, dp[i][j] + cost));
                // Overwrite for transposition cases
                if (i > 0
                        && j > 0
                        && sc == Character.toLowerCase(t.charAt(j - 1))
                        && tc == Character.toLowerCase(s.charAt(i - 1))) {
                    dp[i + 1][j + 1] = Math.min(dp[i + 1][j + 1], dp[i - 1][j - 1] + cost);
                }
            }
        }
        if (BuildConfig.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("editDistance: ").append(s).append(", ").append(t);
            Logger.d(TAG, sb.toString());
            for (int i = 0; i < dp.length; ++i) {
                sb.setLength(0);
                sb.append(i).append(':');
                for (int j = 0; j < dp[i].length; ++j) {
                    sb.append(dp[i][j]).append(',');
                }
                Logger.d(TAG, sb.toString());
            }
        }
        return dp[sl][tl];
    }

    /**
     * Remove duplicates from an array of strings.
     *
     * <p>This method will always keep the first occurrence of all strings at their position in the
     * array, removing the subsequent ones.
     */
    public static void removeDupes(
            final List<CharSequence> suggestions, List<CharSequence> stringsPool) {
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

    public static void tripSuggestions(
            List<CharSequence> suggestions,
            final int maxSuggestions,
            List<CharSequence> stringsPool) {
        while (suggestions.size() > maxSuggestions) {
            removeSuggestion(suggestions, maxSuggestions, stringsPool);
        }
    }

    private static void removeSuggestion(
            List<CharSequence> suggestions, int indexToRemove, List<CharSequence> stringsPool) {
        CharSequence garbage = suggestions.remove(indexToRemove);
        if (garbage instanceof StringBuilder) {
            stringsPool.add(garbage);
        }
    }

    public static int getImeOptionsActionIdFromEditorInfo(final EditorInfo editorInfo) {
        if ((editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            // IME_FLAG_NO_ENTER_ACTION:
            // Flag of imeOptions: used in conjunction with one of the actions masked by
            // IME_MASK_ACTION.
            // If this flag is not set, IMEs will normally replace the "enter" key with the action
            // supplied.
            // This flag indicates that the action should not be available in-line as a replacement
            // for the "enter" key.
            // Typically this is because the action has such a significant impact or is not
            // recoverable enough
            // that accidentally hitting it should be avoided, such as sending a message.
            // Note that TextView will automatically set this flag for you on multi-line text views.
            return EditorInfo.IME_ACTION_NONE;
        } else if (editorInfo.actionLabel != null) {
            return IME_ACTION_CUSTOM_LABEL;
        } else {
            // Note: this is different from editorInfo.actionId, hence "ImeOptionsActionId"
            return editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;
        }
    }
}
