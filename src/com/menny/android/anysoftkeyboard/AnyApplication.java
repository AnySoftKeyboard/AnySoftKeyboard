package com.menny.android.anysoftkeyboard;

import com.anysoftkeyboard.Configuration;
import com.anysoftkeyboard.ConfigurationImpl;
import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.devicespecific.DeviceSpecific;
import com.anysoftkeyboard.devicespecific.FactoryViewBase;
import com.anysoftkeyboard.ui.tutorials.TutorialsProvider;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;


public class AnyApplication extends Application implements OnSharedPreferenceChangeListener {

	public static final boolean DEBUG = true;
	public static final boolean BLEEDING_EDGE = true;
	
	private static final String TAG = "ASK_APP";
	private static Configuration msConfig;
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

        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        FactoryViewBase factory = (FactoryViewBase)inflate.inflate(R.layout.device_specific, null);
        msDeviceSpecific = factory.createDeviceSpecific();
        factory = null;//GC! Please clean this view!
        
        msCloudBackuper = msDeviceSpecific.createCloudBackupRequester(getApplicationContext());
		
		TutorialsProvider.showDragonsIfNeeded(getApplicationContext());
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		((ConfigurationImpl) msConfig).onSharedPreferenceChanged(sharedPreferences, key);
		//should we disable the Settings App? com.menny.android.anysoftkeyboard.LauncherSettingsActivity
		if (key.equals(getString(R.string.settings_key_show_settings_app)))
		{
			PackageManager pm = getPackageManager();
			boolean showApp = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.settings_default_show_settings_app));
			pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), com.menny.android.anysoftkeyboard.LauncherSettingsActivity.class), 
					showApp? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
					PackageManager.DONT_KILL_APP);
		}
	}
	
	
	
	public static Configuration getConfig()
	{
		return msConfig;
	}
	
	public static DeviceSpecific getDeviceSpecific()
	{
		return msDeviceSpecific;
	}
	
	public static void requestBackupToCloud()
	{
		if (msCloudBackuper != null)
			msCloudBackuper.notifyBackupManager();
	}
	
}
