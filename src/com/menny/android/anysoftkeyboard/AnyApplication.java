package com.menny.android.anysoftkeyboard;

import com.anysoftkeyboard.Configuration;
import com.anysoftkeyboard.ConfigurationImpl;
import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.devicespecific.DeviceSpecific;
import com.anysoftkeyboard.devicespecific.FactoryViewBase;
import com.anysoftkeyboard.ui.tutorials.TutorialsProvider;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;


public class AnyApplication extends Application {

	public static final boolean DEBUG = true;
	
	private static final String TAG = "ASK_APP";
	private static Configuration msConfig;
	private static DeviceSpecific msDeviceSpecific;
	private static CloudBackupRequester msCloudBackuper;
	
	@Override
	public void onCreate() {
		if (DEBUG) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.penaltyDeath()
				.build());
		}
		super.onCreate();
		
		if (DEBUG) Log.d(TAG, "** Starting application in DEBUG mode.");
		
		msConfig = new ConfigurationImpl(this);
		
        LayoutInflater inflate =
            (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        FactoryViewBase factory = (FactoryViewBase)inflate.inflate(R.layout.device_specific, null);
        msDeviceSpecific = factory.createDeviceSpecific();
        factory = null;//GC! Please clean this view!
        
        msCloudBackuper = msDeviceSpecific.createCloudBackupRequester(getPackageName());
		
		TutorialsProvider.ShowTutorialsIfNeeded(this);
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
