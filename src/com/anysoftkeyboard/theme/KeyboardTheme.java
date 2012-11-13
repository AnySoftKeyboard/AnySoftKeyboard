package com.anysoftkeyboard.theme;

import com.anysoftkeyboard.addons.AddOnImpl;
import com.anysoftkeyboard.addons.ScreenshotHolder;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class KeyboardTheme extends AddOnImpl implements ScreenshotHolder {
	
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
    
    public boolean hasScreenshot() {
    	return (mThemeScreenshotResId != INVALID_RES_ID);
    }
    
    public Drawable getScreenshot() {
    	if (mThemeScreenshotResId != INVALID_RES_ID) {
    		return getPackageContext().getResources().getDrawable(mThemeScreenshotResId);
    	} else {
    		return null;
    	}
    }
	
	public int getIconsThemeResId() {
		return mIconsThemeResId;
	}
}