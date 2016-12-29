/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.theme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.SharedPreferencesCompat;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;
import java.util.Locale;

public class KeyboardThemeFactory extends AddOnsFactory<KeyboardTheme> {

    private static final KeyboardThemeFactory msInstance;
    private static final String XML_KEYBOARD_THEME_RES_ID_ATTRIBUTE = "themeRes";
    private static final String XML_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE = "iconsThemeRes";
    private static final String XML_POPUP_KEYBOARD_THEME_RES_ID_ATTRIBUTE = "popupThemeRes";
    private static final String XML_POPUP_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE = "popupIconsThemeRes";

    static {
        msInstance = new KeyboardThemeFactory();
    }
    private KeyboardThemeFactory() {
        super("ASK_KT", "com.anysoftkeyboard.plugin.KEYBOARD_THEME", "com.anysoftkeyboard.plugindata.keyboardtheme",
                "KeyboardThemes", "KeyboardTheme",
                R.xml.keyboard_themes, true);
    }

    public static KeyboardTheme getCurrentKeyboardTheme(Context appContext) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String settingKey = appContext.getString(R.string.settings_key_keyboard_theme_key);

        String selectedThemeId = sharedPreferences.getString(settingKey, appContext.getString(R.string.settings_default_keyboard_theme_key));
        KeyboardTheme selectedTheme = null;
        List<KeyboardTheme> themes = msInstance.getAllAddOns(appContext);
        //Find the builder in the array by id. Mayne would've been better off with a HashSet
        for (KeyboardTheme aTheme : themes) {
            if (aTheme.getId().equals(selectedThemeId)) {
                selectedTheme = aTheme;
                break;
            }
        }

        if (selectedTheme == null) {
            //Haven't found a builder or no preference is stored, so we use the default one
            selectedTheme = themes.get(0);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(settingKey, selectedTheme.getId());
            SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        }

        return selectedTheme;
    }

    public static List<KeyboardTheme> getAllAvailableThemes(Context applicationContext) {
        return msInstance.getAllAddOns(applicationContext);
    }

    public static KeyboardTheme getFallbackTheme(Context appContext) {
        final String defaultThemeId = appContext.getString(R.string.settings_default_keyboard_theme_key);
        List<KeyboardTheme> themes = msInstance.getAllAddOns(appContext);
        //Find the builder in the array by id. Maybe would've been better off with a HashSet
        for (KeyboardTheme aTheme : themes) {
            if (aTheme.getId().equals(defaultThemeId)) {
                return aTheme;
            }
        }

        return getCurrentKeyboardTheme(appContext.getApplicationContext());
    }

    @Override
    protected KeyboardTheme createConcreteAddOn(Context askContext, Context context, String prefId, int nameResId, String description, int sortIndex, AttributeSet attrs) {
        final int keyboardThemeResId = attrs.getAttributeResourceValue(null,
                XML_KEYBOARD_THEME_RES_ID_ATTRIBUTE, 0);
        final int popupKeyboardThemeResId = attrs.getAttributeResourceValue(null,
                XML_POPUP_KEYBOARD_THEME_RES_ID_ATTRIBUTE, 0);
        final int iconsThemeResId = attrs.getAttributeResourceValue(null,
                XML_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE, 0);
        final int popupKeyboardIconThemeResId = attrs.getAttributeResourceValue(null,
                XML_POPUP_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE, 0);

        if (keyboardThemeResId == -1) {
            String detailMessage = String.format(Locale.US, "Missing details for creating Keyboard theme! prefId %s, keyboardThemeResId: %d",
                    prefId, keyboardThemeResId);

            throw new RuntimeException(detailMessage);
        }
        return new KeyboardTheme(askContext, context, prefId, nameResId,
                keyboardThemeResId, popupKeyboardThemeResId, iconsThemeResId, popupKeyboardIconThemeResId,
                description, sortIndex);
    }

    @Override
    protected boolean isEventRequiresViewReset(Intent eventIntent, Context context) {
        //will reset ONLY if this is the active theme
        KeyboardTheme selectedTheme = getCurrentKeyboardTheme(context.getApplicationContext());
        if ((selectedTheme != null) && (selectedTheme.getPackageName().equals(eventIntent.getData().getSchemeSpecificPart()))) {
            Logger.d(TAG, "It seems that selected keyboard theme has been changed. I need to reload view!");
            return true;
        }
        return false;
    }
}
