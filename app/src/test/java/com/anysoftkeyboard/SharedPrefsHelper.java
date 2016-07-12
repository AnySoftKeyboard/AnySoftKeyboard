package com.anysoftkeyboard;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.robolectric.RuntimeEnvironment;

public class SharedPrefsHelper {
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
