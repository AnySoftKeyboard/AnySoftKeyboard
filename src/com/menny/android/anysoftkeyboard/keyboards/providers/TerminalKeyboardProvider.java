package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class TerminalKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.english.terminal";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "terminal";
	}

	@Override
	protected String getKeyboardLandscapeLayoutId() {
		return null;
	}	
	
	@Override
	protected int getKeyboardSortValue() {
		return 9;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "terminal_keyboard";
	}

	@Override
	protected String getKeyboardIconResId() {
		return null;
	}

	@Override
	protected String getKeyboardNameResId() {
		return "terminal_keyboard";
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