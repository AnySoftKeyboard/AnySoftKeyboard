package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class SwissKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.swiss.azerty";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "ch_fr_qwerty";
	}
	
	@Override
	protected int getKeyboardSortValue() {
		return 141;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "ch_fr_keyboard";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "ch_fr_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}

	@Override
	protected String getDefaultDictionary() {
		return "French";
	}
}