package com.anysoftkeyboard.theme;

import com.anysoftkeyboard.addons.AddOnImpl;

import android.content.Context;

public class KeyboardTheme extends AddOnImpl {
	
	private final int mThemeResId;
	private final int mThemeScreenshotResId;
	
	public KeyboardTheme(Context packageContext, String id, int nameResId, 
			int themeResId, int themeScreenshotResId,
			String description, int sortIndex) {
		super(packageContext, id, nameResId, description, sortIndex);
		
		mThemeResId = themeResId;
		mThemeScreenshotResId = themeScreenshotResId;
	}
	
	public int getThemeResId() {
		return mThemeResId;
	}
	
	public int getThemeScreenshotResId() {
		return mThemeScreenshotResId;
	}
}