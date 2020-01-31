package com.anysoftkeyboard.keyboards;

import static com.menny.android.anysoftkeyboard.AnyApplication.prefs;

import android.content.Context;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.menny.android.anysoftkeyboard.R;

public class KeyboardPrefs {

    public static String getDefaultDomain(@NonNull Context appContext) {
        return getString(
                        appContext,
                        R.string.settings_key_default_domain_text,
                        R.string.settings_default_default_domain_text)
                .trim();
    }

    private static String getString(
            @NonNull Context appContext, @StringRes int prefKey, @StringRes int defaultValue) {
        return prefs(appContext).getString(prefKey, defaultValue).get();
    }

    private static boolean getBoolean(
            @NonNull Context appContext, @StringRes int prefKey, @BoolRes int defaultValue) {
        return prefs(appContext).getBoolean(prefKey, defaultValue).get();
    }

    public static boolean alwaysHideLanguageKey(@NonNull Context askContext) {
        return getBoolean(
                askContext,
                R.string.settings_key_always_hide_language_key,
                R.bool.settings_default_always_hide_language_key);
    }

    public static boolean disallowGenericRowOverride(@NonNull Context askContext) {
        return !getBoolean(
                askContext,
                R.string.settings_key_allow_layouts_to_provide_generic_rows,
                R.bool.settings_default_allow_layouts_to_provide_generic_rows);
    }
}
