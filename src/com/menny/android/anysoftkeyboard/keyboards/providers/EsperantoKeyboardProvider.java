package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class EsperantoKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.esperanto";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "esperanto";
	}
	
	@Override
	protected int getKeyboardSortValue() {
		return 221;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "esperanto_keyboard";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "esperanto_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}
}