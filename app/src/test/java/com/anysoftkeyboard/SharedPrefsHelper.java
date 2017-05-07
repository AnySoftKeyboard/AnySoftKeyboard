package com.anysoftkeyboard;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v4.content.SharedPreferencesCompat;

import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;

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

    public static void clearPrefsValue(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        final SharedPreferences.Editor editor = preferences.edit().remove(key);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
    }

    public static void ensureKeyboardAtIndexEnabled(int keyboardIndex, boolean enabled) {
        final KeyboardFactory keyboardFactory = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application);
        final KeyboardAddOnAndBuilder addOn = keyboardFactory.getAllAddOns().get(keyboardIndex);
        keyboardFactory.setAddOnEnabled(addOn.getId(), enabled);
    }
}
