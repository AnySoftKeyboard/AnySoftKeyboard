package com.anysoftkeyboard.theme;

import com.anysoftkeyboard.addons.AddOnImpl;
import com.anysoftkeyboard.addons.ScreenshotHolder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class KeyboardTheme extends AddOnImpl implements ScreenshotHolder {
	
	private static final String TAG = "ASK KBD-THEME";
	private final int mThemeResId;
	private final int mPopupThemeResId;
	private final int mIconsThemeResId;
	private final int mThemeScreenshotResId;
	
	public KeyboardTheme(Context askContext, Context packageContext, String id, int nameResId, 
			int themeResId, int popupThemeResId, int iconsThemeResId,
			int themeScreenshotResId,
			String description, int sortIndex) {
		super(askContext, packageContext, id, nameResId, description, sortIndex);
		
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
    	try {
	    	if (mThemeScreenshotResId != INVALID_RES_ID) {
	    		return getPackageContext().getResources().getDrawable(mThemeScreenshotResId);
	    	} else {
	    		return null;
	    	}
    	} catch(Resources.NotFoundException n) {
    		Log.w(TAG, "Failed to load pack Screenshot! ResId:"+mThemeScreenshotResId);
    		return null;
    	}
    }
	
	public int getIconsThemeResId() {
		return mIconsThemeResId;
	}
}