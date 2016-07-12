package com.anysoftkeyboard.quicktextkeys;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.anysoftkeyboard.addons.AddOn;
import com.menny.android.anysoftkeyboard.R;

import java.util.LinkedHashSet;

public class HistoryQuickTextKey extends QuickTextKey {

	public static final String HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY = "HistoryQuickTextKey_encoded_history_key";
	public static final String HISTORY_TOKEN_SEPARATOR = ",";
	private final SharedPreferences mSharedPreferences;
	private final LinkedHashSet<HistoryKey> mHistoryKeys = new LinkedHashSet<>(20);

	public HistoryQuickTextKey(Context askContext) {
		super(askContext, askContext, "b0316c86-ffa2-49e9-85f7-6cb6e63e18f9", R.string.history_quick_text_key_name,
				AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID,
				R.drawable.sym_keyboard_smiley, R.string.quick_text_smiley_key_history_output, R.string.quick_text_smiley_key_history_output,
				AddOn.INVALID_RES_ID, askContext.getResources().getString(R.string.history_quick_text_key_name), 0);
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(askContext);
		final String encodedHistory = mSharedPreferences.getString(HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, "");
		if (encodedHistory != null) {
			decodeForOldDevices(encodedHistory);
		}
		if (mHistoryKeys.size() == 0) {
			//must have at least one!
			recordUsedKey("\uD83D\uDE03", "\uD83D\uDE03");
		}
	}

	private void decodeForOldDevices(String encodedHistory) {
		String[] historyTokens = encodedHistory.split(HISTORY_TOKEN_SEPARATOR);
		int tokensIndex = 0;
		while (tokensIndex + 1 < historyTokens.length) {
			String name = historyTokens[tokensIndex];
			String value = historyTokens[tokensIndex + 1];
			if (!(TextUtils.isEmpty(name) || TextUtils.isEmpty(value))) {
				mHistoryKeys.add(new HistoryKey(name, value));
			}

			tokensIndex += 2;
		}
	}

	@Override
	public String[] getPopupListNames() {
		String[] names = new String[mHistoryKeys.size()];
		int index = names.length - 1;
		for (HistoryKey historyKey : mHistoryKeys) {
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
		for (HistoryKey historyKey : mHistoryKeys) {
			values[index] = historyKey.value;
			index--;
		}
		return values;
	}

	@Override
	protected String[] getStringArrayFromValuesResId(int popupListValuesResId, Resources resources) {
		return new String[0];
	}

	private static class HistoryKey {
		public final String name;
		public final String value;

		private HistoryKey(String name, String value) {
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

	public void recordUsedKey(String name, String value) {
		HistoryKey usedKey = new HistoryKey(name, value);
		if (mHistoryKeys.contains(usedKey)) mHistoryKeys.remove(usedKey);
		mHistoryKeys.add(usedKey);

		//storing to disk
		final String encodedHistory = encodeForOldDevices();

		final SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putString(HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, encodedHistory);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
			editor.apply();
		else
			editor.commit();
	}

	private String encodeForOldDevices() {
		StringBuilder stringBuilder = new StringBuilder(5 * 2 * 20/*just a guess: each Emoji is four bytes, plus one for the coma separator.*/);
		for (HistoryKey historyKey : mHistoryKeys) {
			stringBuilder.append(historyKey.name).append(HISTORY_TOKEN_SEPARATOR).append(historyKey.value).append(HISTORY_TOKEN_SEPARATOR);
		}
		return stringBuilder.toString();
	}
}
