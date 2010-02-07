package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class LaoKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.lao";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "lao_qwerty";
	}

	@Override
	protected String getKeyboardLandscapeLayoutId() {
		return null;
	}	
	
	@Override
	protected int getKeyboardSortValue() {
		return 81;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "lao_keyboard";
	}

	@Override
	protected String getKeyboardIconResId() {
		return "lao";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "lao_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}
}