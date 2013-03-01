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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;

public class KeyboardExtensionFactory extends AddOnsFactory<KeyboardExtension> {

    private static final KeyboardExtensionFactory msInstance;

    static {
        msInstance = new KeyboardExtensionFactory();
    }

    public static KeyboardExtension getCurrentKeyboardExtension(
            Context context, final int type) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        final String settingKey;
        final String defaultValue;
        switch (type) {
            case KeyboardExtension.TYPE_BOTTOM:
                settingKey = context
                        .getString(R.string.settings_key_ext_kbd_bottom_row_key);
                defaultValue = context
                        .getString(R.string.settings_default_ext_kbd_bottom_row_key);
                break;
            case KeyboardExtension.TYPE_TOP:
                settingKey = context
                        .getString(R.string.settings_key_ext_kbd_top_row_key);
                defaultValue = context
                        .getString(R.string.settings_default_top_row_key);
                break;
            case KeyboardExtension.TYPE_EXTENSION:
                settingKey = context
                        .getString(R.string.settings_key_ext_kbd_ext_ketboard_key);
                defaultValue = context
                        .getString(R.string.settings_default_ext_keyboard_key);
                break;
            case KeyboardExtension.TYPE_HIDDEN_BOTTOM:
                settingKey = context
                        .getString(R.string.settings_key_ext_kbd_hidden_bottom_row_key);
                defaultValue = "";
                break;
            default:
                throw new RuntimeException("No such extension keyboard type: "
                        + type);
        }

        String selectedKeyId = sharedPreferences.getString(settingKey,
                defaultValue);
        KeyboardExtension selectedKeyboard = null;
        ArrayList<KeyboardExtension> keys = msInstance.getAllAddOns(context);

        if (selectedKeyId != null) {
            for (KeyboardExtension aKey : keys) {
                if (aKey.getExtensionType() != type)
                    continue;
                if (aKey.getId().equals(selectedKeyId)) {
                    selectedKeyboard = aKey;
                    break;
                }
            }
        }

        if (selectedKeyboard == null) {
            // still can't find the keyboard. Taking default.
            for (KeyboardExtension aKey : keys) {
                if (aKey.getExtensionType() != type)
                    continue;
                selectedKeyboard = aKey;// this is to make sure I have at least
                // one keyboard
                break;
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(settingKey, selectedKeyboard.getId());
            editor.commit();
        }

        return selectedKeyboard;
    }

    public static ArrayList<KeyboardExtension> getAllAvailableExtensions(
            Context applicationContext, final int type) {
        ArrayList<KeyboardExtension> all = msInstance
                .getAllAddOns(applicationContext);
        ArrayList<KeyboardExtension> onlyAsked = new ArrayList<KeyboardExtension>();
        for (KeyboardExtension e : all) {
            if (e.getExtensionType() == type)
                onlyAsked.add(e);
        }

        return onlyAsked;
    }

    private static final String XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE = "extensionKeyboardResId";
    private static final String XML_EXT_KEYBOARD_TYPE_ATTRIBUTE = "extensionKeyboardType";

    private KeyboardExtensionFactory() {
        super("ASK_EKF", "com.anysoftkeyboard.plugin.EXTENSION_KEYBOARD",
                "com.anysoftkeyboard.plugindata.extensionkeyboard",
                "ExtensionKeyboards", "ExtensionKeyboard",
                R.xml.extension_keyboards, false);// At this point in time, I do
        // not allow external packs
    }

    @Override
    protected KeyboardExtension createConcreateAddOn(Context askContext,
                                                     Context context, String prefId, int nameResId, String description,
                                                     int sortIndex, AttributeSet attrs) {
        int keyboardResId = attrs.getAttributeResourceValue(null,
                XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE, -2);
        if (keyboardResId == -2)
            keyboardResId = attrs.getAttributeIntValue(null,
                    XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE, -2);
        int extensionType = attrs.getAttributeResourceValue(null,
                XML_EXT_KEYBOARD_TYPE_ATTRIBUTE, -2);
        if (extensionType != -2) {
            extensionType = context.getResources().getInteger(extensionType);
        } else {
            extensionType = attrs.getAttributeIntValue(null,
                    XML_EXT_KEYBOARD_TYPE_ATTRIBUTE, -2);
        }
        Log.d(TAG,
                String.format(
                        "Parsing Extension Keyboard! prefId %s, keyboardResId %d, type %d",
                        prefId, keyboardResId, extensionType));

        if ((keyboardResId == -2) || (extensionType == -2)) {
            String detailMessage = String.format(
                    "Missing details for creating Extension Keyboard! prefId %s\n"
                            + "keyboardResId: %d, type: %d", prefId,
                    keyboardResId, extensionType);

            throw new RuntimeException(detailMessage);
        }
        return new KeyboardExtension(askContext, context, prefId, nameResId,
                keyboardResId, extensionType, description, sortIndex);
    }

    @Override
    protected boolean isEventRequiresViewReset(Intent eventIntent,
                                               Context context) {
        // will reset ONLY if this is the active extension keyboard
        final int[] types = new int[]{KeyboardExtension.TYPE_BOTTOM,
                KeyboardExtension.TYPE_EXTENSION,
                KeyboardExtension.TYPE_HIDDEN_BOTTOM,
                KeyboardExtension.TYPE_TOP};
        for (int type : types) {
            KeyboardExtension selectedExtension = getCurrentKeyboardExtension(
                    context, type);
            if ((selectedExtension != null)
                    && (selectedExtension.getPackageContext().getPackageName()
                    .equals(eventIntent.getData()
                            .getSchemeSpecificPart()))) {
                Log.d(TAG,
                        "It seems that selected keyboard extension has been changed. I need to reload view!");
                return true;
            }
        }
        return false;
    }
}
