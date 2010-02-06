package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class HebrewKeyboardProvider extends KeyboardProvider {	
	public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.hebrew";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
	
	@Override
	protected String getKeyboardLayoutId() {
		return "heb_qwerty";
	}

	@Override
	protected String getKeyboardLandscapeLayoutId() {
		return null;
	}	
	
	@Override
	protected int getKeyboardSortValue() {
		return 21;
	}

	@Override
	protected String getKeyboardEnabledPrefKey() {
		return "heb_keyboard";
	}

	@Override
	protected String getKeyboardIconResId() {
		return "he";
	}

	@Override
	protected String getKeyboardNameResId() {
		return "heb_keyboard";
	}
	
	@Override
	protected String getPackageName()
	{
		return "com.menny.android.anysoftkeyboard";
	}

	@Override
	protected String getDefaultDictionary() {
		return "Hebrew";
	}
	
	@Override
	protected String getHardKeyboardQwertyTranslation() {
		return "\u05e5\u05e3\u05e7\u05e8\u05d0\u05d8\u05d5\u05df\u05dd\u05e4\u05e9\u05d3\u05d2\u05db\u05e2\u05d9\u05d7\u05dc\u05da\u05d6\u05e1\u05d1\u05d4\u05e0\u05de\u05e6";
	}
	
	@Override
	protected String getAdditionalIsLetterExceptions() {
		//Hebrew also support "
		return "\"";
	}
}