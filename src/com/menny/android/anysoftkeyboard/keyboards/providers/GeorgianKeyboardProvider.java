package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class GeorgianKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.georgian";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "ka_qwerty";
	}
	
	@Override
	protected int getKeyboardSortValue() {
		return 201;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "ka_keyboard";
	}

	@Override
	protected String getKeyboardIconResId() {
		return "ka";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "ka_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}
}