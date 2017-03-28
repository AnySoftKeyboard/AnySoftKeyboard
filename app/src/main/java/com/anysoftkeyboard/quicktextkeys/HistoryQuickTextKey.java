package com.anysoftkeyboard.quicktextkeys;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.anysoftkeyboard.addons.AddOn;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public class HistoryQuickTextKey extends QuickTextKey {

    private final SharedPreferences mSharedPreferences;
    private final List<QuickKeyHistoryRecords.HistoryKey> mHistoryKeys;

    public HistoryQuickTextKey(Context askContext) {
        super(askContext, askContext, "b0316c86-ffa2-49e9-85f7-6cb6e63e18f9", R.string.history_quick_text_key_name,
                AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID,
                R.drawable.ic_quick_text_dark_theme, "\uD83D\uDD50", "\uD83D\uDD50",
                AddOn.INVALID_RES_ID, false, askContext.getResources().getString(R.string.history_quick_text_key_name), 0);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(askContext);
        mHistoryKeys = QuickKeyHistoryRecords.load(mSharedPreferences);
    }

    @Override
    public String[] getPopupListNames() {
        String[] names = new String[mHistoryKeys.size()];
        int index = names.length - 1;
        for (QuickKeyHistoryRecords.HistoryKey historyKey : mHistoryKeys) {
            names[index] = historyKey.name;
            index--;
        }
        return names;
    }

    @Override
    protected String[] getStringArrayFromNamesResId(int popupListNamesResId, Resources resources) {
        return new String[0];
    }

    @Override
    public String[] getPopupListValues() {
        String[] values = new String[mHistoryKeys.size()];
        int index = values.length - 1;
        for (QuickKeyHistoryRecords.HistoryKey historyKey : mHistoryKeys) {
            values[index] = historyKey.value;
            index--;
        }
        return values;
    }

    @Override
    protected String[] getStringArrayFromValuesResId(int popupListValuesResId, Resources resources) {
        return new String[0];
    }

    public void recordUsedKey(String name, String value) {
        QuickKeyHistoryRecords.store(mSharedPreferences, mHistoryKeys, new QuickKeyHistoryRecords.HistoryKey(name, value));
    }
}
