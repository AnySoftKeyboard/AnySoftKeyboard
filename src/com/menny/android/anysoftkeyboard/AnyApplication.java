package com.menny.android.anysoftkeyboard;

import com.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.devicespecific.DeviceSpecific;
import com.anysoftkeyboard.ui.tutorials.TutorialsProvider;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;


public class AnyApplication extends Application {

	private static final String TAG = "ASK_APP";
	private static AnySoftKeyboardConfiguration msConfig;
	private static DeviceSpecific msDeviceSpecific;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (AnySoftKeyboardConfiguration.DEBUG) Log.d(TAG, "** Starting application in DEBUG mode.");

		msConfig = AnySoftKeyboardConfiguration.createInstance(this);
		
        LayoutInflater inflate =
            (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        msDeviceSpecific = (DeviceSpecific)inflate.inflate(R.layout.device_specific, null);
        
		CloudBackupRequester.createRequesterInstance(getPackageName());
		
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
	
}
