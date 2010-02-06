package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class ColemakKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.english.colemak";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "colemak";
	}

	@Override
	protected String getKeyboardLandscapeLayoutId() {
		return null;
	}	
	
	@Override
	protected int getKeyboardSortValue() {
		return 3;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "colemak_keyboard";
	}

	@Override
	protected String getKeyboardIconResId() {
		return "en";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "colemak_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}

	@Override
	protected String getDefaultDictionary() {
		return "English";
	}
}