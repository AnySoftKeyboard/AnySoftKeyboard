package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;

public class SpanishKeyboardProvider extends KeyboardProvider {	
    public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.spanish";
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");

    @Override
    protected String getKeyboardLayoutId() {
        return "es_qwerty";
    }

    @Override
    protected int getKeyboardSortValue() {
        return 131;
    }

    @Override
    protected String getKeyboardEnabledPrefKey() {
        return "es_keyboard";
    }

    @Override
    protected String getKeyboardNameResId() {
        return "es_keyboard";
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