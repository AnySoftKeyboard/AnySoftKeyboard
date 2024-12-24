package com.anysoftkeyboard.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.base.utils.Logger;
import java.util.Locale;

public class LocaleTools {
  private static final String TAG = "ASKLocaleTools";

  public static void applyLocaleToContext(@NonNull Context context, @Nullable String localeString) {
    final Locale forceLocale = LocaleTools.getLocaleForLocaleString(localeString);

    final Configuration configuration = context.getResources().getConfiguration();
    configuration.setLocale(forceLocale);
    context.getResources().updateConfiguration(configuration, null);
  }

  @NonNull
  public static Locale getLocaleForLocaleString(@Nullable String localeString) {
    if ("System".equals(localeString) || TextUtils.isEmpty(localeString)) {
      return Locale.getDefault();
    } else {
      try {
        final Locale parsedLocale;
        parsedLocale = Locale.forLanguageTag(localeString);

        if (TextUtils.isEmpty(parsedLocale.getLanguage())) {
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
