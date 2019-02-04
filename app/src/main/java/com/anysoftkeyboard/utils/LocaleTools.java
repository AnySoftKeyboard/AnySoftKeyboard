package com.anysoftkeyboard.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.anysoftkeyboard.base.utils.Logger;

import java.util.Locale;

public class LocaleTools {
    private static final String TAG = "ASK_LocaleTools";

    public static void applyLocaleToContext(@NonNull Context context, @Nullable String localeString) {
        final Locale forceLocale = LocaleTools.getLocaleForLocaleString(localeString);

        final Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(forceLocale);
        } else {
            //noinspection deprecation
            configuration.locale = forceLocale;
        }
        context.getResources().updateConfiguration(configuration, null);
    }

    @NonNull
    public static Locale getLocaleForLocaleString(@Nullable String localeString) {
        if ("System".equals(localeString) || TextUtils.isEmpty(localeString)) {
            return Locale.getDefault();
        } else {
            try {
                final Locale parsedLocale;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    parsedLocale = Locale.forLanguageTag(localeString);
                } else {
                    //first, we'll try to be nice citizens
                    for (Locale locale : Locale.getAvailableLocales()) {
                        if (localeString.equals(locale.getLanguage())) {
                            return locale;
                        }
                    }
                    //couldn't find it. Trying to force it.
                    parsedLocale = new Locale(localeString);
                }

                if (parsedLocale == null || TextUtils.isEmpty(parsedLocale.getLanguage())) {
                    return Locale.getDefault();
                } else {
                    return parsedLocale;
                }
            } catch (Exception e) {
                Logger.w(TAG, e, "Failed to parse locale value '%s'!", localeString);
                return Locale.getDefault();
            }
        }
    }
}
