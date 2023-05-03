package com.anysoftkeyboard.test;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;
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
        SharedPreferences preferences = getSharedPreferences();
        getSharedPreferences().edit().putString(key, value).apply();
        TestRxSchedulers.foregroundFlushAllJobs();

        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, boolean value) {
        SharedPreferences preferences = getSharedPreferences();
        getSharedPreferences().edit().putBoolean(key, value).apply();
        TestRxSchedulers.foregroundFlushAllJobs();
        return preferences;
    }

    public static SharedPreferences setPrefsValue(String key, int value) {
        SharedPreferences preferences = getSharedPreferences();
        final SharedPreferences.Editor editor = getSharedPreferences().edit().putInt(key, value);
        editor.apply();
        TestRxSchedulers.foregroundFlushAllJobs();
        return preferences;
    }

    public static void clearPrefsValue(@StringRes int keyRes) {
        clearPrefsValue(getApplicationContext().getResources().getString(keyRes));
    }

    public static void clearPrefsValue(String key) {
        final SharedPreferences.Editor editor = getSharedPreferences().edit().remove(key);
        editor.apply();
        TestRxSchedulers.foregroundFlushAllJobs();
    }

    public static boolean getPrefValue(@StringRes int keyStringRes, boolean defaultValue) {
        final String key = getApplicationContext().getResources().getString(keyStringRes);
        return getSharedPreferences().getBoolean(key, defaultValue);
    }

    public static int getPrefValue(@StringRes int keyStringRes, int defaultValue) {
        final String key = getApplicationContext().getResources().getString(keyStringRes);
        return getSharedPreferences().getInt(key, defaultValue);
    }

    public static String getPrefValue(@StringRes int keyStringRes, String defaultValue) {
        final String key = getApplicationContext().getResources().getString(keyStringRes);
        return getSharedPreferences().getString(key, defaultValue);
    }

    @NonNull public static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(
                ApplicationProvider.getApplicationContext());
    }
}
