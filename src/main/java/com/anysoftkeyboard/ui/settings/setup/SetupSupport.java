package com.anysoftkeyboard.ui.settings.setup;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

public class SetupSupport {

    public static boolean isThisKeyboardSetAsDefaultIME(Context context) {
        final String defaultIME = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        return isThisKeyboardSetAsDefaultIME(defaultIME, context.getPackageName());
    }

    public static boolean isThisKeyboardSetAsDefaultIME(String defaultIME, String myPackageName) {
        if (TextUtils.isEmpty(defaultIME))
            return false;

        ComponentName defaultInputMethod = ComponentName.unflattenFromString(defaultIME);
        if (defaultInputMethod.getPackageName().equals(myPackageName)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isThisKeyboardEnabled(Context context) {
        final String enabledIMEList = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS);
        return isThisKeyboardEnabled(enabledIMEList, context.getPackageName());
    }

    public static boolean isThisKeyboardEnabled(String enabledIMEList, String myPackageName) {
        if (TextUtils.isEmpty(enabledIMEList))
            return false;

        String[] enabledIMEs = enabledIMEList.split(":");
        for(String enabledIMEId : enabledIMEs) {
            ComponentName enabledIME = ComponentName.unflattenFromString(enabledIMEId);
            if (enabledIME.getPackageName().equals(myPackageName)) {
                return true;
            }
        }

        return false;
    }
}
