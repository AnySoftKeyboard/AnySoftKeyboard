package com.anysoftkeyboard.test;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v4.content.SharedPreferencesCompat;

import org.robolectric.RuntimeEnvironment;

public class SharedPrefsHelper {
    public static SharedPreferences setPrefsValue(@StringRes int keyRes, String value) {
        return setPrefsValue(RuntimeEnvironment.application.getResources().getString(keyRes), value);
    }

    public static SharedPreferences setPrefsValue(@StringRes int keyRes, boolean value) {
        return setPrefsValue(RuntimeEnvironment.application.getResources().getString(keyRes), value);
    }

    public static SharedPreferences setPrefsValue(@StringRes int keyRes, int value) {
        return setPrefsValue(RuntimeEnvironment.application.getResources().getString(keyRes), value);
    }

    public static SharedPreferences setPrefsValue(String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        final SharedPreferences.Editor editor = preferences.edit().putString(key, value);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);

        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        final SharedPreferences.Editor editor = preferences.edit().putBoolean(key, value);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        final SharedPreferences.Editor editor = preferences.edit().putInt(key, value);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        return preferences;
    }

    public static void clearPrefsValue(@StringRes int keyRes) {
        clearPrefsValue(RuntimeEnvironment.application.getResources().getString(keyRes));
    }

    public static void clearPrefsValue(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        final SharedPreferences.Editor editor = preferences.edit().remove(key);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    public static boolean getPrefValue(@StringRes int keyStringRes, boolean defaultValue) {
        final String key = RuntimeEnvironment.application.getResources().getString(keyStringRes);
        return PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application).getBoolean(key, defaultValue);
    }

    public static int getPrefValue(@StringRes int keyStringRes, int defaultValue) {
        final String key = RuntimeEnvironment.application.getResources().getString(keyStringRes);
        return PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application).getInt(key, defaultValue);
    }

    public static String getPrefValue(@StringRes int keyStringRes, String defaultValue) {
        final String key = RuntimeEnvironment.application.getResources().getString(keyStringRes);
        return PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application).getString(key, defaultValue);
    }
}
