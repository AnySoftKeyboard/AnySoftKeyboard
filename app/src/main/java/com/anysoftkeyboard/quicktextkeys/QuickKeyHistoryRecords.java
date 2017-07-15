package com.anysoftkeyboard.quicktextkeys;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.SharedPreferencesCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuickKeyHistoryRecords {
    static final int MAX_LIST_SIZE = 30;
    static final String HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY = "HistoryQuickTextKey_encoded_history_key";
    static final String HISTORY_TOKEN_SEPARATOR = ",";

    public static final String DEFAULT_EMOJI = "\uD83D\uDE03";
    private final List<HistoryKey> mLoadedKeys;
    @NonNull
    private final SharedPreferences mSharedPreferences;
    private boolean mIncognitoMode;

    public QuickKeyHistoryRecords(@NonNull SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
        mLoadedKeys = new ArrayList<>(MAX_LIST_SIZE);
        final String encodedHistory = sharedPreferences.getString(HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, "");
        if (!TextUtils.isEmpty(encodedHistory)) {
            decodeForOldDevices(encodedHistory, mLoadedKeys);
        }
        if (mLoadedKeys.size() == 0) {
            //must have at least one!
            mLoadedKeys.add(new HistoryKey(DEFAULT_EMOJI, DEFAULT_EMOJI));
        }
    }

    private static void decodeForOldDevices(@NonNull String encodedHistory, @NonNull List<HistoryKey> outputSet) {
        String[] historyTokens = encodedHistory.split(HISTORY_TOKEN_SEPARATOR);
        int tokensIndex = 0;
        while (tokensIndex + 1 < historyTokens.length && outputSet.size() < MAX_LIST_SIZE) {
            String name = historyTokens[tokensIndex];
            String value = historyTokens[tokensIndex + 1];
            if (!(TextUtils.isEmpty(name) || TextUtils.isEmpty(value))) {
                outputSet.add(new HistoryKey(name, value));
            }

            tokensIndex += 2;
        }
    }

    public void store(@NonNull String name, @NonNull String value) {
        if (mIncognitoMode) return;

        final HistoryKey usedKey = new HistoryKey(name, value);
        mLoadedKeys.remove(usedKey);
        mLoadedKeys.add(usedKey);

        while (mLoadedKeys.size() > MAX_LIST_SIZE) mLoadedKeys.remove(0/*dropping the first key*/);

        final String encodedHistory = encodeForOldDevices(mLoadedKeys);

        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, encodedHistory);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    private static String encodeForOldDevices(@NonNull List<HistoryKey> outputSet) {
        StringBuilder stringBuilder = new StringBuilder(5 * 2 * MAX_LIST_SIZE/*just a guess: each Emoji is four bytes, plus one for the coma separator.*/);
        for (int i = 0; i < outputSet.size(); i++) {
            HistoryKey historyKey = outputSet.get(i);
            stringBuilder.append(historyKey.name).append(HISTORY_TOKEN_SEPARATOR).append(historyKey.value).append(HISTORY_TOKEN_SEPARATOR);
        }
        return stringBuilder.toString();
    }

    public List<HistoryKey> getCurrentHistory() {
        return Collections.unmodifiableList(mLoadedKeys);
    }

    @VisibleForTesting
    public boolean isIncognitoMode() {
        return mIncognitoMode;
    }

    public void setIncognitoMode(boolean incognitoMode) {
        mIncognitoMode = incognitoMode;
    }

    public static class HistoryKey {
        public final String name;
        public final String value;

        HistoryKey(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof HistoryKey && ((HistoryKey) o).name.equals(name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
