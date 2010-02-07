package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class FinnishKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.finnish";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "fin_swedish_qwerty";
	}
	
	@Override
	protected int getKeyboardSortValue() {
		return 111;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "finnish_swedish_keyboard";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "finnish_swedish_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}

	@Override
	protected String getDefaultDictionary() {
		return "Swedish";
	}
}