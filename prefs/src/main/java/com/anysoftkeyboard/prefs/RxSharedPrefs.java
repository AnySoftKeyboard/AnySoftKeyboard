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
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.BoolRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.SharedPreferencesCompat;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;

import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;

public class RxSharedPrefs {
    private static final String TAG = "ASK_Cfg";

    static final String CONFIGURATION_VERSION = "configurationVersion";
    static final int CONFIGURATION_LEVEL_VALUE = 11;

    @NonNull
    private final Resources mResources;
    @NonNull
    private final RxSharedPreferences mRxSharedPreferences;

    public RxSharedPrefs(Context context, SharedPreferences sp) {
        mResources = context.getResources();

        upgradeSettingsValues(sp);

        mRxSharedPreferences = RxSharedPreferences.create(sp);
    }

    public static PrefsProvider createSharedPrefsProvider(Context context) {
        return new SharedPrefsProvider(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public Preference<Boolean> getBoolean(@StringRes int prefKey, @BoolRes int defaultValue) {
        return mRxSharedPreferences.getBoolean(mResources.getString(prefKey), mResources.getBoolean(defaultValue));
    }

    public Preference<Integer> getInteger(@StringRes int prefKey, @IntegerRes int defaultValue) {
        return mRxSharedPreferences.getInteger(mResources.getString(prefKey), mResources.getInteger(defaultValue));
    }

    public Preference<String> getString(@StringRes int prefKey, @StringRes int defaultValue) {
        return mRxSharedPreferences.getString(mResources.getString(prefKey), mResources.getString(defaultValue));
    }

    public <T> Observable<T> getParsedString(@StringRes int prefKey, @StringRes int defaultValue, StringParser<T> parser) {
        final String defaultStringValue = mResources.getString(defaultValue);
        return mRxSharedPreferences.getString(mResources.getString(prefKey), defaultStringValue)
                .asObservable().map(parser::parse).onErrorReturnItem(parser.parse(defaultStringValue));
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    public Preference<Set<String>> getStringSet(@StringRes int stringSetKeyResId) {
        return mRxSharedPreferences.getStringSet(mResources.getString(stringSetKeyResId));
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

    public interface StringParser<T> {
        T parse(String value);
    }

    @VisibleForTesting
    static class SharedPrefsProvider implements PrefsProvider {

        private final SharedPreferences mSharedPreferences;

        public SharedPrefsProvider(SharedPreferences sharedPreferences) {
            mSharedPreferences = sharedPreferences;
        }

        @Override
        public String providerId() {
            return "SharedPreferences";
        }

        @Override
        public PrefsRoot getPrefsRoot() {
            PrefsRoot root = new PrefsRoot(1);
            for (Map.Entry<String, ?> entry : mSharedPreferences.getAll().entrySet()) {
                final String typeOfPref = getTypeOf(entry.getValue());
                if (typeOfPref != null && entry.getValue() != null) {
                    final PrefItem prefEntry = root.createChild();
                    prefEntry.addValue("type", typeOfPref);
                    prefEntry.addValue(entry.getKey(), entry.getValue().toString());
                }
            }
            return root;
        }

        @Nullable
        private static String getTypeOf(Object value) {
            if (value == null) return null;

            if (value instanceof Integer) return "int";
            if (value instanceof String) return "string";
            if (value instanceof Boolean) return "bool";

            return null;
        }

        @Override
        public void storePrefsRoot(PrefsRoot prefsRoot) throws Exception {
            final Editor editor = mSharedPreferences.edit();
            //first, clear anything currently in prefs
            for (Map.Entry<String, ?> entry : mSharedPreferences.getAll().entrySet()) {
                editor.remove(entry.getKey());
            }

            for (PrefItem prefItem : prefsRoot.getChildren()) {
                StoreToSharedPrefsFunction<?> convertFunction = null;
                String storedKey = null;
                String storedValue = null;
                for (Map.Entry<String, String> value : prefItem.getValues()) {
                    switch (value.getKey()) {
                        case "type":
                            convertFunction = getConvertFunctionFor(value.getValue());
                            break;
                        default:
                            storedKey = value.getKey();
                            storedValue = value.getValue();
                            break;
                    }

                    if (convertFunction != null && storedValue != null && storedKey != null) {
                        convertFunction.storeToEditor(editor, storedKey, storedValue);
                    }
                }
            }
            editor.commit();
            //upgrading anything that needs to be fixed
            upgradeSettingsValues(mSharedPreferences);
        }

        private static StoreToSharedPrefsFunction<?> getConvertFunctionFor(@Nullable String valueType) {
            if (valueType == null) return SharedPrefsProvider::storeStringToEditor;

            switch (valueType) {
                case "int":
                    return SharedPrefsProvider::storeIntToEditor;
                case "bool":
                    return SharedPrefsProvider::storeBooleanToEditor;
                default:
                    return SharedPrefsProvider::storeStringToEditor;
            }
        }

        private static void storeBooleanToEditor(Editor editor, String key, String value) {
            editor.putBoolean(key, Boolean.valueOf(value));
        }

        private static void storeIntToEditor(Editor editor, String key, String value) {
            editor.putInt(key, Integer.parseInt(value));
        }

        private static void storeStringToEditor(Editor editor, String key, Object value) {
            editor.putString(key, value == null ? null : value.toString());
        }

        private interface StoreToSharedPrefsFunction<T> {
            void storeToEditor(Editor editor, String key, String value);
        }
    }
}