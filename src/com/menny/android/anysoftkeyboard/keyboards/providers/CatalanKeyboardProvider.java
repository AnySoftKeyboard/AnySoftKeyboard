package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;


public class CatalanKeyboardProvider extends KeyboardProvider {	
    public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.catalan";
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");

    @Override
    protected String getKeyboardLayoutId() {
        return "catalan";
    }

    @Override
    protected int getKeyboardSortValue() {
        return 132;
    }

    @Override
    protected String getKeyboardEnabledPrefKey() {
        return "catalan_keyboard";
    }

    @Override
    protected String getKeyboardNameResId() {
        return "catalan_keyboard";
    }

    @Override
    protected String getPackageName()
    {
        return "com.menny.android.anysoftkeyboard";
    }

    @Override
    protected String getDefaultDictionary() {
        return "Spanish";
    }
}