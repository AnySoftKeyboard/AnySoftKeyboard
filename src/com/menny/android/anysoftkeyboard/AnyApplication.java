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
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import com.anysoftkeyboard.Configuration;
import com.anysoftkeyboard.ConfigurationImpl;
import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.backup.CloudBackupRequesterDiagram;
import com.anysoftkeyboard.devicespecific.DeviceSpecific;
import com.anysoftkeyboard.ui.tutorials.TutorialsProvider;
import com.anysoftkeyboard.utils.Log;
import net.evendanan.frankenrobot.Diagram;
import net.evendanan.frankenrobot.FrankenRobot;
import net.evendanan.frankenrobot.Lab;


public class AnyApplication extends Application implements OnSharedPreferenceChangeListener {

    public static final boolean DEBUG = false;
    //public static final boolean BLEEDING_EDGE = true;

    private static final String TAG = "ASK_APP";
    private static Configuration msConfig;
    private static FrankenRobot msFrank;
    private static DeviceSpecific msDeviceSpecific;
    private static CloudBackupRequester msCloudBackuper;

    @Override
    public void onCreate() {
//		if (DEBUG) {
//			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//				.detectAll()
//				.penaltyLog()
//				.build());
//			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//				.detectAll()
//				.penaltyLog()
//				.penaltyDeath()
//				.build());
//		}
        super.onCreate();

        if (DEBUG) Log.d(TAG, "** Starting application in DEBUG mode.");

        msConfig = new ConfigurationImpl(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        msFrank = Lab.build(getApplicationContext(), R.array.frankenrobot_interfaces_mapping);
        msDeviceSpecific = msFrank.embody(new Diagram<DeviceSpecific>() {
        });
        Log.i(TAG, "Loaded DeviceSpecific " + msDeviceSpecific.getApiLevel() + " concrete class " + msDeviceSpecific.getClass().getName());

        msCloudBackuper = msFrank.embody(new CloudBackupRequesterDiagram(getApplicationContext()));

        TutorialsProvider.showDragonsIfNeeded(getApplicationContext());
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ((ConfigurationImpl) msConfig).onSharedPreferenceChanged(sharedPreferences, key);
        //should we disable the Settings App? com.menny.android.anysoftkeyboard.LauncherSettingsActivity
        if (key.equals(getString(R.string.settings_key_show_settings_app))) {
            PackageManager pm = getPackageManager();
            boolean showApp = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.settings_default_show_settings_app));
            pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), com.menny.android.anysoftkeyboard.LauncherSettingsActivity.class),
                    showApp ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }


    public static Configuration getConfig() {
        return msConfig;
    }

    public static DeviceSpecific getDeviceSpecific() {
        return msDeviceSpecific;
    }

    public static void requestBackupToCloud() {
        if (msCloudBackuper != null)
            msCloudBackuper.notifyBackupManager();
    }

    public static FrankenRobot getFrankenRobot() {
        return msFrank;
    }

}
