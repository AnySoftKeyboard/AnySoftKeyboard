package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class DanishKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.danish";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "dk_qwerty";
	}
	
	@Override
	protected int getKeyboardSortValue() {
		return 91;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "danish_keyboard";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "danish_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}

	@Override
	protected String getDefaultDictionary() {
		return "Danish";
	}
}