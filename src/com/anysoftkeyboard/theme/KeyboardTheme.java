package com.anysoftkeyboard.theme;

import com.anysoftkeyboard.addons.AddOnImpl;

import android.content.Context;

public class KeyboardTheme extends AddOnImpl {
	
	private final int mThemeResId;
	private final int mPopupThemeResId;
	private final int mIconsThemeResId;
	private final int mThemeScreenshotResId;
	
	public KeyboardTheme(Context packageContext, String id, int nameResId, 
			int themeResId, int popupThemeResId, int iconsThemeResId,
			int themeScreenshotResId,
			String description, int sortIndex) {
		super(packageContext, id, nameResId, description, sortIndex);
		
		mThemeResId = themeResId;
		mPopupThemeResId = popupThemeResId == -1 ? mThemeResId : popupThemeResId;
		mIconsThemeResId = iconsThemeResId;
		mThemeScreenshotResId = themeScreenshotResId;
	}
	
	public int getThemeResId() {
		return mThemeResId;
	}
	
	public int getPopupThemeResId() {
		return mPopupThemeResId;
	}
	
	public int getThemeScreenshotResId() {
		return mThemeScreenshotResId;
	}
	
	public int getIconsThemeResId() {
		return mIconsThemeResId;
	}
}