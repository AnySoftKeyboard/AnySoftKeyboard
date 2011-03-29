package com.menny.android.anysoftkeyboard;

import com.menny.android.anysoftkeyboard.backup.CloudBackupRequester;
import com.menny.android.anysoftkeyboard.tutorials.TutorialsProvider;

import android.app.Application;
import android.util.Log;


public class AnyApplication extends Application {

	private static AnySoftKeyboardConfiguration msConfig;

	@Override
	public void onCreate() {
		super.onCreate();
		
		if (AnySoftKeyboardConfiguration.DEBUG) Log.d("ASK", "** Starting application in DEBUG mode.");
		
		CloudBackupRequester.createRequesterInstance(getPackageName());
		
		msConfig = AnySoftKeyboardConfiguration.createInstance(this);


		TutorialsProvider.ShowTutorialsIfNeeded(this);
	}
	
	public static AnySoftKeyboardConfiguration getConfig()
	{
		return msConfig;
	}
	
}
