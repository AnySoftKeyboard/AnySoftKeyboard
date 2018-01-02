/*
 * Copyright (c) 2018 Menny Even-Danan
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

package com.anysoftkeyboard.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.SharedPreferencesCompat;

import com.anysoftkeyboard.base.utils.Logger;
import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;

import java.util.Map;

public class RxSharedPrefs {
    private static final String TAG = "ASK_Cfg";

    static final String CONFIGURATION_VERSION = "configurationVersion";
    static final int CONFIGURATION_LEVEL_VALUE = 11;

    @NonNull
    private final Resources mResources;
    @NonNull
    private final RxSharedPreferences mRxSharedPreferences;

    public RxSharedPrefs(Context context) {
        mResources = context.getResources();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        upgradeSettingsValues(sp);

        mRxSharedPreferences = RxSharedPreferences.create(sp);
    }

    public Preference<Boolean> getBoolean(@StringRes int prefKey, @BoolRes int defaultValue) {
        return mRxSharedPreferences.getBoolean(mResources.getString(prefKey), mResources.getBoolean(defaultValue));
    }

    public Preference<String> getString(@StringRes int prefKey, @StringRes int defaultValue) {
        return mRxSharedPreferences.getString(mResources.getString(prefKey), mResources.getString(defaultValue));
    }

    private static void upgradeSettingsValues(SharedPreferences sp) {
        Logger.d(TAG, "Checking if configuration upgrade is needed.");
        //please note: the default value should be the last version.
        //upgrading should only be done when actually need to be done.
        final int configurationVersion = sp.getInt(CONFIGURATION_VERSION, CONFIGURATION_LEVEL_VALUE);

        if (configurationVersion < 11) {
            //converting quick-text-key
            //settings_key_active_quick_text_key value -> quick_text_[value]
            final Editor editor = sp.edit();
            final Map<String, ?> allValues = sp.getAll();

            //QUICK-TEXT
            if (allValues.containsKey("settings_key_ordered_active_quick_text_keys")) {
                String orderedIds = allValues.get("settings_key_ordered_active_quick_text_keys").toString();
                //order
                editor.putString("quick_text_AddOnsFactory_order_key", orderedIds);
                //enabled
                String[] addonIds = orderedIds.split(",");
                for (String addonId : addonIds) {
                    editor.putBoolean("quick_text_" + addonId, true);
                }
            }

            //THEME
            if (allValues.containsKey("settings_key_keyboard_theme_key")) {
                String themeId = allValues.get("settings_key_keyboard_theme_key").toString();
                //enabled
                editor.putBoolean("theme_" + themeId, true);
            }

            //bottom row
            if (allValues.containsKey("settings_key_ext_kbd_bottom_row_key")) {
                String id = allValues.get("settings_key_ext_kbd_bottom_row_key").toString();
                //enabled
                editor.putBoolean("ext_kbd_enabled_1_" + id, true);
            }

            //top row
            if (allValues.containsKey("settings_key_ext_kbd_top_row_key")) {
                String id = allValues.get("settings_key_ext_kbd_top_row_key").toString();
                //enabled
                editor.putBoolean("ext_kbd_enabled_2_" + id, true);
            }

            //ext keyboard
            if (allValues.containsKey("settings_key_ext_kbd_ext_ketboard_key")) {
                String id = allValues.get("settings_key_ext_kbd_ext_ketboard_key").toString();
                //enabled
                editor.putBoolean("ext_kbd_enabled_3_" + id, true);
            }

            SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        }

        //saving config level
        Editor e = sp.edit();
        e.putInt(CONFIGURATION_VERSION, CONFIGURATION_LEVEL_VALUE);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(e);
    }
}