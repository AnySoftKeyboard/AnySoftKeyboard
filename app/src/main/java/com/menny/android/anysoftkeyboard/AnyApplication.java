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

package com.menny.android.anysoftkeyboard;


import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.AskPrefs;
import com.anysoftkeyboard.AskPrefsImpl;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.backup.CloudBackupRequesterDiagram;
import com.anysoftkeyboard.devicespecific.DeviceSpecific;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV11;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV14;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV19;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV3;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV8;
import com.anysoftkeyboard.devicespecific.StrictModeAble;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.anysoftkeyboard.ui.tutorials.TutorialsProvider;
import com.anysoftkeyboard.utils.LogCatLogProvider;
import com.anysoftkeyboard.utils.Logger;
import com.anysoftkeyboard.utils.NullLogProvider;

import net.evendanan.frankenrobot.FrankenRobot;
import net.evendanan.frankenrobot.Lab;


public class AnyApplication extends Application implements OnSharedPreferenceChangeListener {

    private static final String TAG = "ASK_APP";
    private static AskPrefs msConfig;
    private static FrankenRobot msFrank;
    private static DeviceSpecific msDeviceSpecific;
    private static CloudBackupRequester msCloudBackupRequester;
    private KeyboardFactory mKeyboardFactory;
    private ExternalDictionaryFactory mExternalDictionaryFactory;
    private KeyboardExtensionFactory mBottomRowFactory;
    private KeyboardExtensionFactory mTopRowFactory;
    private KeyboardExtensionFactory mExtensionKeyboardFactory;
    private KeyboardThemeFactory mKeyboardThemeFactory;
    private QuickTextKeyFactory mQuickTextKeyFactory;

    public static AskPrefs getConfig() {
        return msConfig;
    }

    public static DeviceSpecific getDeviceSpecific() {
        return msDeviceSpecific;
    }

    public static void requestBackupToCloud() {
        if (msCloudBackupRequester != null)
            msCloudBackupRequester.notifyBackupManager();
    }

    public static FrankenRobot getFrankenRobot() {
        return msFrank;
    }

    public static KeyboardFactory getKeyboardFactory(Context context) {
        return ((AnyApplication) context.getApplicationContext()).mKeyboardFactory;
    }

    public static KeyboardExtensionFactory getTopRowFactory(Context context) {
        return ((AnyApplication) context.getApplicationContext()).mTopRowFactory;
    }

    public static KeyboardExtensionFactory getBottomRowFactory(Context context) {
        return ((AnyApplication) context.getApplicationContext()).mBottomRowFactory;
    }

    public static KeyboardExtensionFactory getKeyboardExtensionFactory(Context context) {
        return ((AnyApplication) context.getApplicationContext()).mExtensionKeyboardFactory;
    }

    public static ExternalDictionaryFactory getExternalDictionaryFactory(Context context) {
        return ((AnyApplication) context.getApplicationContext()).mExternalDictionaryFactory;
    }

    public static KeyboardThemeFactory getKeyboardThemeFactory(Context context) {
        return ((AnyApplication) context.getApplicationContext()).mKeyboardThemeFactory;
    }

    public static QuickTextKeyFactory getQuickTextKeyFactory(Context context) {
        return ((AnyApplication) context.getApplicationContext()).mQuickTextKeyFactory;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupCrashHandler();
        Logger.d(TAG, "** Starting application in DEBUG mode.");
        msFrank = Lab.build(getApplicationContext(), R.array.frankenrobot_interfaces_mapping);
        if (BuildConfig.DEBUG) {
            StrictModeAble strictMode = msFrank.embody(StrictModeAble.class);
            if (strictMode != null)//it should be created only in the API18.
                strictMode.setupStrictMode();
        }

        msConfig = new AskPrefsImpl(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);


        msDeviceSpecific = createDeviceSpecificImplementation();
        Logger.i(TAG, "Loaded DeviceSpecific " + msDeviceSpecific.getApiLevel() + " concrete class " + msDeviceSpecific.getClass().getName());

        msCloudBackupRequester = msFrank.embody(new CloudBackupRequesterDiagram(getApplicationContext()));

        mKeyboardFactory = new KeyboardFactory(this);
        mExternalDictionaryFactory = new ExternalDictionaryFactory(this);
        mBottomRowFactory = new KeyboardExtensionFactory(this, R.string.settings_default_ext_kbd_bottom_row_key, KeyboardExtensionFactory.BOTTOM_ROW_PREF_ID_PREFIX, KeyboardExtension.TYPE_BOTTOM);
        mTopRowFactory = new KeyboardExtensionFactory(this, R.string.settings_default_top_row_key, KeyboardExtensionFactory.TOP_ROW_PREF_ID_PREFIX, KeyboardExtension.TYPE_TOP);
        mExtensionKeyboardFactory = new KeyboardExtensionFactory(this, R.string.settings_default_ext_keyboard_key, KeyboardExtensionFactory.EXT_PREF_ID_PREFIX, KeyboardExtension.TYPE_EXTENSION);
        mKeyboardThemeFactory = new KeyboardThemeFactory(this);
        mQuickTextKeyFactory = new QuickTextKeyFactory(this);

        TutorialsProvider.showDragonsIfNeeded(getApplicationContext());
    }

    private DeviceSpecific createDeviceSpecificImplementation() {
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel <= 7) return new DeviceSpecificV3();
        if (apiLevel <= 10) return new DeviceSpecificV8();
        if (apiLevel <= 13) return new DeviceSpecificV11();
        if (apiLevel <= 18) return new DeviceSpecificV14();
        return new DeviceSpecificV19();
    }

    protected void setupCrashHandler() {
        if (BuildConfig.DEBUG) {
            Logger.setLogProvider(new LogCatLogProvider());
            Thread.setDefaultUncaughtExceptionHandler(new ChewbaccaUncaughtExceptionHandler(getBaseContext(), null));
        } else {
            Logger.setLogProvider(new NullLogProvider());
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ((AskPrefsImpl) msConfig).onSharedPreferenceChanged(sharedPreferences, key);
        //should we disable the Settings App? com.menny.android.anysoftkeyboard.LauncherSettingsActivity
        if (key.equals(getString(R.string.settings_key_show_settings_app))) {
            PackageManager pm = getPackageManager();
            boolean showApp = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.settings_default_show_settings_app));
            pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), com.menny.android.anysoftkeyboard.LauncherSettingsActivity.class),
                    showApp ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    public void onPackageChanged(final Intent eventIntent, final AnySoftKeyboard ask) {
        AddOnsFactory.onExternalPackChanged(eventIntent, ask,
                mTopRowFactory, mBottomRowFactory, mExtensionKeyboardFactory,
                mExternalDictionaryFactory, mKeyboardFactory,
                mKeyboardThemeFactory, mQuickTextKeyFactory);
    }
}
