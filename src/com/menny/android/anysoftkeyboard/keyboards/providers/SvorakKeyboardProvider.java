package com.menny.android.anysoftkeyboard.keyboards.providers;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardProvider;

import android.net.Uri;


public class SvorakKeyboardProvider extends KeyboardProvider {
    public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.svorak";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/items");

    @Override
    protected String getKeyboardLayoutId() {
        return "svorak";
    }

    @Override
    protected int getKeyboardSortValue() {
        return 112;
    }

    @Override
    protected String getKeyboardEnabledPrefKey() {
        return "svorak_keyboard";
    }

    @Override
    protected String getKeyboardNameResId() {
        return "svorak_keyboard";
    }

    @Override
    protected String getPackageName() {
        return "com.menny.android.anysoftkeyboard";
    }

    @Override
    protected String getDefaultDictionary() {
        return "Swedish";
    }
}