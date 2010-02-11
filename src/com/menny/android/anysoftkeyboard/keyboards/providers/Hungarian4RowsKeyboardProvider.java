package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;


public class Hungarian4RowsKeyboardProvider extends KeyboardProvider {	
    public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.hungarian.qwertz4rows";
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");

    @Override
    protected String getKeyboardLayoutId() {
        return "hu_qwertz_4_row";
    }
    
    @Override
    protected String getKeyboardLandscapeLayoutId() {
    	return "hu_landscape";
    }

    @Override
    protected int getKeyboardSortValue() {
        return 122;
    }

    @Override
    protected String getKeyboardEnabledPrefKey() {
        return "hungarian_keyboard_4_row";
    }

    @Override
    protected String getKeyboardNameResId() {
        return "hungarian_keyboard";
    }

    @Override
    protected String getPackageName()
    {
        return "com.menny.android.anysoftkeyboard";
    }

    @Override
    protected String getDefaultDictionary() {
        return "Hungarian";
    }
}