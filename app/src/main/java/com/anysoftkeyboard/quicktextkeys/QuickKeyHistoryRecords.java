package com.anysoftkeyboard.quicktextkeys;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.content.SharedPreferencesCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class QuickKeyHistoryRecords {
    static final int MAX_LIST_SIZE = 30;
    static final String HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY = "HistoryQuickTextKey_encoded_history_key";
    static final String HISTORY_TOKEN_SEPARATOR = ",";

    static final String DEFAULT_EMOJI = "\uD83D\uDE03";

    public static List<HistoryKey> load(@NonNull SharedPreferences sharedPreferences) {
        List<HistoryKey> loadedKeys = new ArrayList<>(MAX_LIST_SIZE);
        final String encodedHistory = sharedPreferences.getString(QuickKeyHistoryRecords.HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, "");
        if (encodedHistory != null) {
            decodeForOldDevices(encodedHistory, loadedKeys);
        }
        if (loadedKeys.size() == 0) {
            //must have at least one!
            loadedKeys.add(new QuickKeyHistoryRecords.HistoryKey(DEFAULT_EMOJI, DEFAULT_EMOJI));
        }

        return loadedKeys;
    }

    private static void decodeForOldDevices(@NonNull String encodedHistory, @NonNull List<HistoryKey> outputSet) {
        String[] historyTokens = encodedHistory.split(QuickKeyHistoryRecords.HISTORY_TOKEN_SEPARATOR);
        int tokensIndex = 0;
        while (tokensIndex + 1 < historyTokens.length && outputSet.size() < MAX_LIST_SIZE) {
            String name = historyTokens[tokensIndex];
            String value = historyTokens[tokensIndex + 1];
            if (!(TextUtils.isEmpty(name) || TextUtils.isEmpty(value))) {
                outputSet.add(new QuickKeyHistoryRecords.HistoryKey(name, value));
            }

            tokensIndex += 2;
        }
    }

    public static void store(@NonNull SharedPreferences sharedPreferences, @NonNull List<HistoryKey> historyKeys, @NonNull HistoryKey usedKey) {
        if (historyKeys.contains(usedKey)) historyKeys.remove(usedKey);
        historyKeys.add(0, usedKey);

        while (historyKeys.size() > MAX_LIST_SIZE) historyKeys.remove(MAX_LIST_SIZE);

        final String encodedHistory = encodeForOldDevices(historyKeys);

        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(QuickKeyHistoryRecords.HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, encodedHistory);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    private static String encodeForOldDevices(@NonNull List<HistoryKey> outputSet) {
        StringBuilder stringBuilder = new StringBuilder(5 * 2 * MAX_LIST_SIZE/*just a guess: each Emoji is four bytes, plus one for the coma separator.*/);
        for (int i = 0; i < outputSet.size(); i++) {
            HistoryKey historyKey = outputSet.get(i);
            stringBuilder.append(historyKey.name).append(QuickKeyHistoryRecords.HISTORY_TOKEN_SEPARATOR).append(historyKey.value).append(QuickKeyHistoryRecords.HISTORY_TOKEN_SEPARATOR);
        }
        return stringBuilder.toString();
    }

    static class HistoryKey {
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
