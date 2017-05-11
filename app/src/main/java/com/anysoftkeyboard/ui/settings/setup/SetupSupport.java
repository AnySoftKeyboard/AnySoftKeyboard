package com.anysoftkeyboard.ui.settings.setup;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;

import java.util.List;
import java.util.Locale;

public class SetupSupport {

    public static boolean isThisKeyboardSetAsDefaultIME(Context context) {
        final String defaultIME = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        return isThisKeyboardSetAsDefaultIME(defaultIME, context.getPackageName());
    }

    @VisibleForTesting
    /*package*/ static boolean isThisKeyboardSetAsDefaultIME(String defaultIME, String myPackageName) {
        if (TextUtils.isEmpty(defaultIME))
            return false;

        ComponentName defaultInputMethod = ComponentName.unflattenFromString(defaultIME);
        if (defaultInputMethod.getPackageName().equals(myPackageName)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isThisKeyboardEnabled(@NonNull Context context) {
        final String enabledIMEList = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS);
        return isThisKeyboardEnabled(enabledIMEList, context.getPackageName());
    }

    @VisibleForTesting
    /*package*/ static boolean isThisKeyboardEnabled(String enabledIMEList, String myPackageName) {
        if (TextUtils.isEmpty(enabledIMEList))
            return false;

        String[] enabledIMEs = enabledIMEList.split(":");
        for (String enabledIMEId : enabledIMEs) {
            ComponentName enabledIME = ComponentName.unflattenFromString(enabledIMEId);
            if (enabledIME != null && enabledIME.getPackageName().equals(myPackageName)) {
                return true;
            }
        }

        return false;
    }

    /*package*/
    static boolean hasLanguagePackForCurrentLocale(@NonNull List<KeyboardAddOnAndBuilder> availableLanguagePacks) {
        for (KeyboardAddOnAndBuilder availableLanguagePack : availableLanguagePacks) {
            final String language = availableLanguagePack.getKeyboardLocale();
            if (TextUtils.isEmpty(language)) continue;

            if (Locale.getDefault().getLanguage().equals(new Locale(language).getLanguage())) {
                return true;
            }
        }

        return false;
    }
}
