package com.anysoftkeyboard.quicktextkeys;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.f2prateek.rx.preferences2.Preference;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuickKeyHistoryRecords {
    static final int MAX_LIST_SIZE = 30;
    static final String HISTORY_TOKEN_SEPARATOR = ",";

    public static final String DEFAULT_EMOJI = "\uD83D\uDE03";
    private final List<HistoryKey> mLoadedKeys = new ArrayList<>(MAX_LIST_SIZE);
    @NonNull private final Preference<String> mRxPref;
    private boolean mIncognitoMode;

    public QuickKeyHistoryRecords(@NonNull RxSharedPrefs rxSharedPrefs) {
        mRxPref =
                rxSharedPrefs.getString(
                        R.string.settings_key_quick_text_history, R.string.settings_default_empty);
        final String encodedHistory = mRxPref.get();
        if (!TextUtils.isEmpty(encodedHistory)) {
            decodeForOldDevices(encodedHistory, mLoadedKeys);
        }
        if (mLoadedKeys.size() == 0) {
            // must have at least one!
            mLoadedKeys.add(new HistoryKey(DEFAULT_EMOJI, DEFAULT_EMOJI));
        }
    }

    private static void decodeForOldDevices(
            @NonNull String encodedHistory, @NonNull List<HistoryKey> outputSet) {
        String[] historyTokens = encodedHistory.split(HISTORY_TOKEN_SEPARATOR, -1);
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

        while (mLoadedKeys.size() > MAX_LIST_SIZE) mLoadedKeys.remove(0 /*dropping the first key*/);

        final String encodedHistory = encodeForOldDevices(mLoadedKeys);

        mRxPref.set(encodedHistory);
    }

    private static String encodeForOldDevices(@NonNull List<HistoryKey> outputSet) {
        StringBuilder stringBuilder =
                new StringBuilder(
                        5
                                * 2
                                * MAX_LIST_SIZE /*just a guess: each Emoji is four bytes, plus one for the coma separator.*/);
        for (int i = 0; i < outputSet.size(); i++) {
            HistoryKey historyKey = outputSet.get(i);
            stringBuilder
                    .append(historyKey.name)
                    .append(HISTORY_TOKEN_SEPARATOR)
                    .append(historyKey.value)
                    .append(HISTORY_TOKEN_SEPARATOR);
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
