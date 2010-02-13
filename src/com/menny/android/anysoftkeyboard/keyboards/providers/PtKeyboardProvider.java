package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class PtKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.pt";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "pt_qwerty";
	}

	@Override
	protected String getKeyboardLandscapeLayoutId() {
		return null;
	}	
	
	@Override
	protected int getKeyboardSortValue() {
		return 181;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "pt_keyboard";
	}

	@Override
	protected String getKeyboardIconResId() {
		return "pt";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "pt_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}

	@Override
	protected String getDefaultDictionary() {
		return "Portuguese";
	}
}