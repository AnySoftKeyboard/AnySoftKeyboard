package com.anysoftkeyboard.test;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.StringRes;
import com.anysoftkeyboard.rx.TestRxSchedulers;

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
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putString(key, value).apply();
        TestRxSchedulers.foregroundFlushAllJobs();

        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, boolean value) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putBoolean(key, value).apply();
        TestRxSchedulers.foregroundFlushAllJobs();
        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, int value) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = preferences.edit().putInt(key, value);
        editor.apply();
        TestRxSchedulers.foregroundFlushAllJobs();
        return preferences;
    }

    public static void clearPrefsValue(@StringRes int keyRes) {
        clearPrefsValue(getApplicationContext().getResources().getString(keyRes));
    }

    public static void clearPrefsValue(String key) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = preferences.edit().remove(key);
        editor.apply();
        TestRxSchedulers.foregroundFlushAllJobs();
    }

    public static boolean getPrefValue(@StringRes int keyStringRes, boolean defaultValue) {
        final String key = getApplicationContext().getResources().getString(keyStringRes);
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(key, defaultValue);
    }

    public static int getPrefValue(@StringRes int keyStringRes, int defaultValue) {
        final String key = getApplicationContext().getResources().getString(keyStringRes);
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt(key, defaultValue);
    }

    public static String getPrefValue(@StringRes int keyStringRes, String defaultValue) {
        final String key = getApplicationContext().getResources().getString(keyStringRes);
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getString(key, defaultValue);
    }
}
