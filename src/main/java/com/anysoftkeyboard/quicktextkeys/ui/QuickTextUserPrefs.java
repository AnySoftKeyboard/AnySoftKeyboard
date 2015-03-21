package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/*package*/ class QuickTextUserPrefs {
	/*package*/ static final String KEY_QUICK_TEXT_PREF_LAST_SELECTED_TAB_ADD_ON_ID = "KEY_QUICK_TEXT_PREF_LAST_SELECTED_TAB_ADD_ON_ID";
	private final SharedPreferences mSharedPreferences;

	public QuickTextUserPrefs(Context context) {
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}

	@Nullable
	public String getLastSelectedAddOnId() {
		return mSharedPreferences.getString(KEY_QUICK_TEXT_PREF_LAST_SELECTED_TAB_ADD_ON_ID, "");
	}

	public void setLastSelectedAddOnId(@Nullable String addOnId) {
		mSharedPreferences.edit().putString(KEY_QUICK_TEXT_PREF_LAST_SELECTED_TAB_ADD_ON_ID, addOnId).commit();
	}
}
