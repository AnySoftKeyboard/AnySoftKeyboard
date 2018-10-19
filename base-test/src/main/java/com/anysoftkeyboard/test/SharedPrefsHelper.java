package com.anysoftkeyboard.test;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v4.content.SharedPreferencesCompat;

public class SharedPrefsHelper {
    public static SharedPreferences setPrefsValue(@StringRes int keyRes, String value) {
        return setPrefsValue(getApplicationContext().getResources().getString(keyRes), value);
    }

    public static SharedPreferences setPrefsValue(@StringRes int keyRes, boolean value) {
        return setPrefsValue(getApplicationContext().getResources().getString(keyRes), value);
    }

    public static SharedPreferences setPrefsValue(@StringRes int keyRes, int value) {
        return setPrefsValue(getApplicationContext().getResources().getString(keyRes), value);
    }

    public static SharedPreferences setPrefsValue(String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = preferences.edit().putString(key, value);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);

        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = preferences.edit().putBoolean(key, value);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = preferences.edit().putInt(key, value);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        return preferences;
    }

    public static void clearPrefsValue(@StringRes int keyRes) {
        clearPrefsValue(getApplicationContext().getResources().getString(keyRes));
    }

    public static void clearPrefsValue(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = preferences.edit().remove(key);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    public static boolean getPrefValue(@StringRes int keyStringRes, boolean defaultValue) {
        final String key = getApplicationContext().getResources().getString(keyStringRes);
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(key, defaultValue);
    }

    public static int getPrefValue(@StringRes int keyStringRes, int defaultValue) {
        final String key = getApplicationContext().getResources().getString(keyStringRes);
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(key, defaultValue);
    }

    public static String getPrefValue(@StringRes int keyStringRes, String defaultValue) {
        final String key = getApplicationContext().getResources().getString(keyStringRes);
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(key, defaultValue);
    }
}
