package com.menny.android.anysoftkeyboard;

import android.app.Application;


public class AnyApplication extends Application {

	private static AnySoftKeyboardConfiguration msConfig;

	@Override
	public void onCreate() {
		super.onCreate();
		
		msConfig = AnySoftKeyboardConfiguration.createInstance(this);
	}
	
	public static AnySoftKeyboardConfiguration getConfig()
	{
		return msConfig;
	}
	
}
