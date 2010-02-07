package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class NorwegianKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.norwegian";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "no_qwerty";
	}
	
	@Override
	protected int getKeyboardSortValue() {
		return 101;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "norwegian_keyboard";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "norwegian_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}

	@Override
	protected String getDefaultDictionary() {
		return "Norwegian";
	}
}