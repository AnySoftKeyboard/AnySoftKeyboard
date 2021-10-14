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

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import androidx.annotation.BoolRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import java.io.File;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public class RxSharedPrefs {
    static final String CONFIGURATION_VERSION = "configurationVersion";
    static final int CONFIGURATION_LEVEL_VALUE = 12;
    private static final String TAG = "ASK_Cfg";

    @VisibleForTesting static final String AUTO_APPLY_PREFS_FILENAME = "STARTUP_PREFS_APPLY.xml";
    private static final String AUTO_APPLIED_PREFS_FILENAME_TEMPLATE_DATETIME_PLACEMENT =
            "[DATETIME]";

    @VisibleForTesting
    static final String AUTO_APPLIED_PREFS_FILENAME_TEMPLATE_PREFIX = "STARTUP_PREFS_APPLIED_";

    private static final String AUTO_APPLIED_PREFS_FILENAME_TEMPLATE =
            AUTO_APPLIED_PREFS_FILENAME_TEMPLATE_PREFIX
                    + AUTO_APPLIED_PREFS_FILENAME_TEMPLATE_DATETIME_PLACEMENT
                    + ".xml";
    @NonNull private final Resources mResources;
    @NonNull private final RxSharedPreferences mRxSharedPreferences;

    public RxSharedPrefs(
            @NonNull Context context,
            @NonNull SharedPreferences sp,
            @NonNull Consumer<File> restorer) {
        mResources = context.getResources();

        applyAutoPrefsFile(context, restorer);

        upgradeSettingsValues(sp);

        mRxSharedPreferences = RxSharedPreferences.create(sp);
    }

    private static void upgradeSettingsValues(@NonNull SharedPreferences sp) {
        Logger.d(TAG, "Checking if configuration upgrade is needed.");
        // please note: the default value should be the last version.
        // upgrading should only be done when actually need to be done.
        final int configurationVersion =
                sp.getInt(CONFIGURATION_VERSION, CONFIGURATION_LEVEL_VALUE);

        if (configurationVersion < 12) {
            // this means the user has used the app before this version, hence, might have used the
            // default android dictionary
            final Map<String, ?> allValues = sp.getAll();
            if (!allValues.containsKey("settings_key_always_use_fallback_user_dictionary")) {
                // if the key was not set, it means the user used the default value for v11
                // which is use-android-built-in-dictionary.
                // so, we'll need to set it to that value, so the new default will not change that.
                final Editor editor = sp.edit();
                editor.putBoolean(
                        "settings_key_always_use_fallback_user_dictionary",
                        false /*the previous default*/);
                editor.apply();
            }

            if (allValues.containsKey("vibrate_on_key_press_duration")) {
                try {
                    int previousVibrationValue =
                            Integer.parseInt(sp.getString("vibrate_on_key_press_duration", "0"));
                    final Editor editor = sp.edit();
                    editor.putInt(
                            "settings_key_vibrate_on_key_press_duration_int",
                            previousVibrationValue);
                    editor.remove("vibrate_on_key_press_duration");
                    editor.apply();
                } catch (Exception e) {
                    Logger.w(
                            TAG,
                            "Failed to parse vibrate_on_key_press_duration prefs value. Going with default value");
                }
            }
        }

        if (configurationVersion < 11) {
            // converting quick-text-key
            // settings_key_active_quick_text_key value -> quick_text_[value]
            final Editor editor = sp.edit();
            final Map<String, ?> allValues = sp.getAll();

            // QUICK-TEXT
            if (allValues.containsKey("settings_key_ordered_active_quick_text_keys")) {
                String orderedIds =
                        allValues.get("settings_key_ordered_active_quick_text_keys").toString();
                // order
                editor.putString("quick_text_AddOnsFactory_order_key", orderedIds);
                // enabled
                String[] addonIds = orderedIds.split(",", -1);
                for (String addonId : addonIds) {
                    editor.putBoolean("quick_text_" + addonId, true);
                }
            }

            // THEME
            if (allValues.containsKey("settings_key_keyboard_theme_key")) {
                String themeId = allValues.get("settings_key_keyboard_theme_key").toString();
                // enabled
                editor.putBoolean("theme_" + themeId, true);
            }

            // bottom row
            if (allValues.containsKey("settings_key_ext_kbd_bottom_row_key")) {
                String id = allValues.get("settings_key_ext_kbd_bottom_row_key").toString();
                // enabled
                editor.putBoolean("ext_kbd_enabled_1_" + id, true);
            }

            // top row
            if (allValues.containsKey("settings_key_ext_kbd_top_row_key")) {
                String id = allValues.get("settings_key_ext_kbd_top_row_key").toString();
                // enabled
                editor.putBoolean("ext_kbd_enabled_2_" + id, true);
            }

            // ext keyboard
            if (allValues.containsKey("settings_key_ext_kbd_ext_ketboard_key")) {
                String id = allValues.get("settings_key_ext_kbd_ext_ketboard_key").toString();
                // enabled
                editor.putBoolean("ext_kbd_enabled_3_" + id, true);
            }

            editor.apply();
        }

        // saving config level
        Editor e = sp.edit();
        e.putInt(CONFIGURATION_VERSION, CONFIGURATION_LEVEL_VALUE);
        e.apply();
    }

    private void applyAutoPrefsFile(@NonNull Context context, @NonNull Consumer<File> restorer) {
        final String autoApplyLogTag = TAG + "_auto_apply_pref";

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            final File autoApplyFile =
                    new File(context.getExternalFilesDir(null), AUTO_APPLY_PREFS_FILENAME);
            if (autoApplyFile.isFile()) {
                Logger.i(
                        autoApplyLogTag,
                        "Applying prefs file '%s'...",
                        autoApplyFile.getAbsolutePath());
                try {
                    restorer.accept(autoApplyFile);
                    Logger.i(
                            autoApplyLogTag,
                            "Prefs from file '%s' were applied!",
                            autoApplyFile.getAbsolutePath());
                    final CharSequence appliedTime =
                            DateFormat.format("yyyy-MM-dd__HH_mm_ss_zzz", Calendar.getInstance());
                    final File appliedFile =
                            new File(
                                    autoApplyFile.getParent(),
                                    AUTO_APPLIED_PREFS_FILENAME_TEMPLATE.replace(
                                            AUTO_APPLIED_PREFS_FILENAME_TEMPLATE_DATETIME_PLACEMENT,
                                            appliedTime));
                    Logger.i(
                            autoApplyLogTag,
                            "Renaming applied prefs file from '%s' to '%s'...",
                            autoApplyFile.getAbsolutePath(),
                            appliedFile.getAbsolutePath());
                    if (!autoApplyFile.renameTo(appliedFile.getAbsoluteFile())) {
                        Logger.w(autoApplyLogTag, "Failed to rename prefs file!");
                    }
                } catch (Exception e) {
                    Log.w(autoApplyLogTag, e);
                    Logger.w(
                            autoApplyLogTag,
                            e,
                            "Failed to restore prefs from the file '%s'.",
                            autoApplyFile.getAbsolutePath());
                }
            } else {
                Logger.i(
                        autoApplyLogTag,
                        "The file '%s' does not exists.",
                        autoApplyFile.getAbsolutePath());
            }
        } else {
            Logger.i(
                    autoApplyLogTag,
                    "Does not have WRITE_EXTERNAL_STORAGE to perform applyAutoPrefsFile.");
        }
    }

    public Preference<Boolean> getBoolean(@StringRes int prefKey, @BoolRes int defaultValue) {
        return mRxSharedPreferences.getBoolean(
                mResources.getString(prefKey), mResources.getBoolean(defaultValue));
    }

    public Preference<Integer> getInteger(@StringRes int prefKey, @IntegerRes int defaultValue) {
        return getInteger(mResources.getString(prefKey), defaultValue);
    }

    public Preference<Integer> getInteger(String prefKey, @IntegerRes int defaultValue) {
        return mRxSharedPreferences.getInteger(prefKey, mResources.getInteger(defaultValue));
    }

    public Preference<String> getString(@StringRes int prefKey, @StringRes int defaultValue) {
        return getString(mResources.getString(prefKey), defaultValue);
    }

    public Preference<String> getString(String prefKey, @StringRes int defaultValue) {
        return mRxSharedPreferences.getString(prefKey, mResources.getString(defaultValue));
    }

    public <T> Observable<T> getParsedString(
            @StringRes int prefKey, @StringRes int defaultValue, StringParser<T> parser) {
        final String defaultStringValue = mResources.getString(defaultValue);
        return mRxSharedPreferences
                .getString(mResources.getString(prefKey), defaultStringValue)
                .asObservable()
                .map(parser::parse)
                .onErrorReturnItem(parser.parse(defaultStringValue));
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    public Preference<Set<String>> getStringSet(@StringRes int stringSetKeyResId) {
        return mRxSharedPreferences.getStringSet(mResources.getString(stringSetKeyResId));
    }

    public interface StringParser<T> {
        T parse(String value);
    }

    public static class SharedPrefsProvider implements PrefsProvider {

        private final SharedPreferences mSharedPreferences;

        public SharedPrefsProvider(SharedPreferences sharedPreferences) {
            mSharedPreferences = sharedPreferences;
        }

        @Nullable
        private static String getTypeOf(Object value) {
            if (value == null) return null;

            if (value instanceof Integer) return "int";
            if (value instanceof String) return "string";
            if (value instanceof Boolean) return "bool";

            return null;
        }

        private static StoreToSharedPrefsFunction<?> getConvertFunctionFor(
                @Nullable String valueType) {
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

        @Override
        public void storePrefsRoot(PrefsRoot prefsRoot) {
            final Editor editor = mSharedPreferences.edit();
            // first, clear anything currently in prefs
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
            editor.apply();
            // upgrading anything that needs to be fixed
            upgradeSettingsValues(mSharedPreferences);
        }

        private interface StoreToSharedPrefsFunction<T> {
            void storeToEditor(Editor editor, String key, String value);
        }
    }
}
