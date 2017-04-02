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
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.R;

import java.util.Locale;

public class KeyboardThemeFactory extends AddOnsFactory.SingleAddOnsFactory<KeyboardTheme> {

    private static final String XML_KEYBOARD_THEME_RES_ID_ATTRIBUTE = "themeRes";
    private static final String XML_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE = "iconsThemeRes";
    private static final String XML_POPUP_KEYBOARD_THEME_RES_ID_ATTRIBUTE = "popupThemeRes";
    private static final String XML_POPUP_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE = "popupIconsThemeRes";
    public static final String PREF_ID_PREFIX = "theme_";
    private final CharSequence mFallbackThemeId;

    public KeyboardThemeFactory(@NonNull Context context) {
        super(context, "ASK_KT", "com.anysoftkeyboard.plugin.KEYBOARD_THEME", "com.anysoftkeyboard.plugindata.keyboardtheme",
                "KeyboardThemes", "KeyboardTheme", PREF_ID_PREFIX,
                R.xml.keyboard_themes, R.string.settings_default_keyboard_theme_key, true);
        mFallbackThemeId = mContext.getText(R.string.fallback_keyboard_theme_id);
    }

    public KeyboardTheme getFallbackTheme() {
        return getAddOnById(mFallbackThemeId);
    }

    @Override
    protected KeyboardTheme createConcreteAddOn(Context askContext, Context context, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden, int sortIndex, AttributeSet attrs) {
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
        return new KeyboardTheme(askContext, context, prefId, name,
                keyboardThemeResId, popupKeyboardThemeResId, iconsThemeResId, popupKeyboardIconThemeResId,
                isHidden, description, sortIndex);
    }

    @Override
    protected boolean isEventRequiresViewReset(Intent eventIntent) {
        //will reset ONLY if this is the active theme
        KeyboardTheme selectedTheme = getEnabledAddOn();
        if ((selectedTheme != null) && (selectedTheme.getPackageName().equals(eventIntent.getData().getSchemeSpecificPart()))) {
            Logger.d(mTag, "It seems that selected keyboard theme has been changed. I need to reload view!");
            return true;
        }
        return false;
    }
}
