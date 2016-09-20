package com.anysoftkeyboard;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;

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
        preferences.edit().putString(key, value).commit();
        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        preferences.edit().putBoolean(key, value).commit();
        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        preferences.edit().putInt(key, value).commit();
        return preferences;
    }
}
