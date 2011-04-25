package com.menny.android.anysoftkeyboard;

import com.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.devicespecific.DeviceSpecific;
import com.anysoftkeyboard.devicespecific.FactoryViewBase;
import com.anysoftkeyboard.ui.tutorials.TutorialsProvider;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;


public class AnyApplication extends Application {

	private static final String TAG = "ASK_APP";
	private static AnySoftKeyboardConfiguration msConfig;
	private static DeviceSpecific msDeviceSpecific;
	private static CloudBackupRequester msCloudBackuper;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (AnySoftKeyboardConfiguration.DEBUG) Log.d(TAG, "** Starting application in DEBUG mode.");

		msConfig = AnySoftKeyboardConfiguration.createInstance(this);
		
        LayoutInflater inflate =
            (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        FactoryViewBase factory = (FactoryViewBase)inflate.inflate(R.layout.device_specific, null);
        msDeviceSpecific = factory.createDeviceSpecific();
        factory = null;//GC! Please clean this view!
        
        msCloudBackuper = msDeviceSpecific.createCloudBackupRequester(getPackageName());
		
		TutorialsProvider.ShowTutorialsIfNeeded(this);
	}
	
	public static AnySoftKeyboardConfiguration getConfig()
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
