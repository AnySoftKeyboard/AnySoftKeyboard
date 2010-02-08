package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;


public class GermanKeyboardProvider extends KeyboardProvider {	
    public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.german";
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");

    @Override
    protected String getKeyboardLayoutId() {
        return "de_qwerty";
    }

    @Override
    protected int getKeyboardSortValue() {
        return 161;
    }

    @Override
    protected String getKeyboardEnabledPrefKey() {
        return "ch_de_keyboard";
    }

    @Override
    protected String getKeyboardNameResId() {
        return "de_keyboard";
    }

    @Override
    protected String getPackageName()
    {
        return "com.menny.android.anysoftkeyboard";
    }

    @Override
    protected String getDefaultDictionary() {
        return "German";
    }
}