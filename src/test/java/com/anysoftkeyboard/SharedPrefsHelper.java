package com.anysoftkeyboard;

import android.content.SharedPreferences;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowPreferenceManager;

public class SharedPrefsHelper {
    public static SharedPreferences setPrefsValue(String key, String value) {
        SharedPreferences preferences = ShadowPreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        preferences.edit().putString(key, value).commit();
        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, boolean value) {
        SharedPreferences preferences = ShadowPreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        preferences.edit().putBoolean(key, value).commit();
        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, int value) {
        SharedPreferences preferences = ShadowPreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        preferences.edit().putInt(key, value).commit();
        return preferences;
    }
}
