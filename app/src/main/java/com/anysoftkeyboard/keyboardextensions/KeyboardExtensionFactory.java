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

package com.anysoftkeyboard.keyboardextensions;

import static com.anysoftkeyboard.keyboardextensions.KeyboardExtension.TYPE_BOTTOM;
import static com.anysoftkeyboard.keyboardextensions.KeyboardExtension.TYPE_EXTENSION;
import static com.anysoftkeyboard.keyboardextensions.KeyboardExtension.TYPE_TOP;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.R;

import java.util.Locale;

public class KeyboardExtensionFactory extends AddOnsFactory.SingleAddOnsFactory<KeyboardExtension> {

    protected static final String BASE_PREF_ID_PREFIX = "ext_kbd_enabled_";

    public static final String BOTTOM_ROW_PREF_ID_PREFIX = BASE_PREF_ID_PREFIX + TYPE_BOTTOM + "_";
    public static final String TOP_ROW_PREF_ID_PREFIX = BASE_PREF_ID_PREFIX + TYPE_TOP + "_";
    public static final String EXT_PREF_ID_PREFIX = BASE_PREF_ID_PREFIX + TYPE_EXTENSION + "_";

    private static final String XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE = "extensionKeyboardResId";
    private static final String XML_EXT_KEYBOARD_TYPE_ATTRIBUTE = "extensionKeyboardType";

    @KeyboardExtension.KeyboardExtensionType
    private final int mExtensionType;

    public KeyboardExtensionFactory(@NonNull Context context, @StringRes int defaultAddOnId, String prefIdPrefix, int extensionType) {
        super(context, "ASK_EKF", "com.anysoftkeyboard.plugin.EXTENSION_KEYBOARD",
                "com.anysoftkeyboard.plugindata.extensionkeyboard",
                "ExtensionKeyboards", "ExtensionKeyboard", prefIdPrefix,
                R.xml.extension_keyboards, defaultAddOnId, true);
        mExtensionType = extensionType;
    }

    @Override
    protected KeyboardExtension createConcreteAddOn(Context askContext, Context context, int apiVersion, CharSequence prefId, CharSequence name, CharSequence description, boolean isHidden,
            int sortIndex, AttributeSet attrs) {
        int keyboardResId = attrs.getAttributeResourceValue(null, XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        if (keyboardResId == AddOn.INVALID_RES_ID) {
            keyboardResId = attrs.getAttributeIntValue(null, XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        }
        @KeyboardExtension.KeyboardExtensionType
        int extensionType = attrs.getAttributeResourceValue(null, XML_EXT_KEYBOARD_TYPE_ATTRIBUTE, AddOn.INVALID_RES_ID);
        //noinspection WrongConstant
        if (extensionType != AddOn.INVALID_RES_ID) {
            extensionType = KeyboardExtension.ensureValidType(context.getResources().getInteger(extensionType));
        } else {
            //noinspection WrongConstant
            extensionType = attrs.getAttributeIntValue(null, XML_EXT_KEYBOARD_TYPE_ATTRIBUTE, AddOn.INVALID_RES_ID);
        }
        Logger.d(mTag, "Parsing Extension Keyboard! prefId %s, keyboardResId %d, type %d", prefId, keyboardResId, extensionType);

        //noinspection WrongConstant
        if (extensionType == AddOn.INVALID_RES_ID) {
            throw new RuntimeException(String.format(Locale.US, "Missing details for creating Extension Keyboard! prefId %s keyboardResId: %d, type: %d", prefId, keyboardResId, extensionType));
        } else {
            if (extensionType == mExtensionType) {
                return new KeyboardExtension(askContext, context, apiVersion, prefId, name, keyboardResId, extensionType, description, isHidden, sortIndex);
            } else {
                return null;
            }
        }
    }

    @KeyboardExtension.KeyboardExtensionType
    public int getExtensionType() {
        return mExtensionType;
    }
}
