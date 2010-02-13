package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class ThaiKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.thai";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "thai_qwerty";
	}
	
	@Override
	protected int getKeyboardSortValue() {
		return 191;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "thai_keyboard";
	}

	@Override
	protected String getKeyboardIconResId() {
		return "thai";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "thai_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}
	
	@Override
	protected String getAdditionalIsLetterExceptions() {
		return "'";
	}
}