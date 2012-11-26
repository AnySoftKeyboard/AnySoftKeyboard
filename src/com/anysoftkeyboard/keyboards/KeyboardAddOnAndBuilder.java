package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnImpl;
import com.anysoftkeyboard.addons.IconHolder;
import com.anysoftkeyboard.addons.ScreenshotHolder;

public class KeyboardAddOnAndBuilder extends AddOnImpl implements IconHolder, ScreenshotHolder {

	public static final String KEYBOARD_PREF_PREFIX = "keyboard_";

	private static final String TAG = "ASK KBD-BUILDER";
	
	private final int mResId;
    private final int mLandscapeResId;
    private final int mIconResId;
    private final String mDefaultDictionary;
    private final int mQwertyTranslationId;
    private final String mAdditionalIsLetterExceptions;
    private final String mSentenceSeparators;
    private final boolean mKeyboardDefaultEnabled;
    private final int mScreenshotResId;
    
    public KeyboardAddOnAndBuilder(Context packageContext, String id, int nameResId,
            int layoutResId, int landscapeLayoutResId,
            String defaultDictionary, int iconResId,
            int physicalTranslationResId,
            String additionalIsLetterExceptions,
            String sentenceSeparators,
            String description,
            int keyboardIndex,
            boolean keyboardDefaultEnabled,
            int screenshotResId) {
		super(packageContext, KEYBOARD_PREF_PREFIX+id, nameResId, description, keyboardIndex);
		
		mResId = layoutResId;
        if (landscapeLayoutResId == AddOn.INVALID_RES_ID){
            mLandscapeResId = mResId;
        } else {
            mLandscapeResId = landscapeLayoutResId;
        }
		
        mDefaultDictionary = defaultDictionary;
        mIconResId = iconResId;
        mAdditionalIsLetterExceptions = additionalIsLetterExceptions;
        mSentenceSeparators = sentenceSeparators;
        mQwertyTranslationId = physicalTranslationResId;
        mKeyboardDefaultEnabled = keyboardDefaultEnabled;
        mScreenshotResId = screenshotResId;
	}
    
    public boolean getKeyboardDefaultEnabled() {
    	return mKeyboardDefaultEnabled;
    }
    
    public String getKeyboardLocale()
    {
    	return mDefaultDictionary;
    }
    
    public Drawable getIcon() {
    	try {
	    	if (mIconResId != INVALID_RES_ID) {
	    		return getPackageContext().getResources().getDrawable(mIconResId);
	    	} else {
	    		return null;
	    	}
    	} catch (Resources.NotFoundException n) {
    		Log.w(TAG, "Failed to load pack ICON! ResId: "+mIconResId);
    		return null;
    	}
    }
    
    public boolean hasScreenshot() {
    	return (mScreenshotResId != INVALID_RES_ID);
    }
    
    public Drawable getScreenshot() {
    	try {
	    	if (mScreenshotResId != INVALID_RES_ID) {
	    		return getPackageContext().getResources().getDrawable(mScreenshotResId);
	    	} else {
	    		return null;
	    	}
    	} catch(Resources.NotFoundException n) {
    		Log.w(TAG, "Failed to load pack screenshot! ResId: "+mScreenshotResId);
    		return null;
    	}
    }
    
    public AnyKeyboard createKeyboard(AnyKeyboardContextProvider askContext, int mode) {
        return new ExternalAnyKeyboard(askContext, getPackageContext(), mResId, mLandscapeResId, getId(), getNameResId(), mIconResId, mQwertyTranslationId, mDefaultDictionary, mAdditionalIsLetterExceptions, mSentenceSeparators, mode);
    }
}
