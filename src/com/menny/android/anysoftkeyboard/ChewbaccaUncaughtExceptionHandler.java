package com.menny.android.anysoftkeyboard;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;


import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

class ChewbaccaUncaughtExceptionHandler implements UncaughtExceptionHandler {
	private static final String TAG = "ASK CHEWBACCA";

	private final UncaughtExceptionHandler mOsDefaultHandler;

	private final Context mApp;
	
	public ChewbaccaUncaughtExceptionHandler(Context app, UncaughtExceptionHandler previous)
	{
		mApp = app;
		mOsDefaultHandler = previous;
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e(TAG, "Caught an unhandled exception!!! ", ex);
		if (AnySoftKeyboardConfiguration.DEBUG)
		{
			Notification notification = new Notification();

			Intent notificationIntent = new Intent();
			notificationIntent = new Intent(mApp, SendBugReportUiActivity.class);
			
			String appName = mApp.getText(R.string.ime_name).toString();
			try {
				PackageInfo info = mApp.getPackageManager().getPackageInfo(mApp.getPackageName(), 0);
				appName = appName + " v"+info.versionName+" release "+info.versionCode;
			} catch (NameNotFoundException e) {
				appName = "NA";
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String logText = "Hi. It seems that we have crashed.... Here are some details:\n"+
				"****** GMT Time: "+(new Date()).toGMTString()+"\n"+
				"****** Application name: "+appName+"\n"+
				"******************************\n"+
				"****** Exception type: "+ex.getClass().getName()+"\n"+
				"****** Exception message: "+ex.getMessage()+"\n"+
				"****** Trace trace:\n"+getStackTrace(ex)+"\n"+
				"******************************\n"+
				"****** Logcat:\n"+getLogcat();
			Log.e(TAG, "About to send a bug report:\n"+logText);
	        
			notificationIntent.putExtra(SendBugReportUiActivity.CRASH_REPORT_TEXT, logText);
			
			PendingIntent contentIntent = PendingIntent.getActivity(mApp, 0,
					notificationIntent, 0);

			notification.setLatestEventInfo(mApp, 
					mApp.getText(R.string.ime_name), 
					"Caught an unhandled exception!",
					contentIntent);
			notification.icon = R.drawable.icon_8_key;
			notification.flags = Notification.FLAG_AUTO_CANCEL + Notification.FLAG_ONLY_ALERT_ONCE;
			notification.defaults = Notification.DEFAULT_LIGHTS + Notification.DEFAULT_VIBRATE;
			// notifying
			NotificationManager notificationManager = (NotificationManager)mApp.getSystemService(Context.NOTIFICATION_SERVICE);
			
			notificationManager.notify(1, notification);
		}
		//and sending to the OS
		if (mOsDefaultHandler != null)
		{
			Log.i(TAG, "Sending the exception to OS exception handler...");
			mOsDefaultHandler.uncaughtException(thread, ex);
		}
		
		System.exit(0);
	}

	private String getLogcat() {
		return "Not supported at the moment";
	}

	private String getStackTrace(Throwable ex) {
		StackTraceElement[] stackTrace = ex.getStackTrace();
		StringBuilder sb = new StringBuilder();
		
		for(StackTraceElement element : stackTrace)
		{
			sb.append(element.toString());
			sb.append('\n');
		}
		
		if (ex.getCause() == null)
			return sb.toString();
		else
		{
			ex = ex.getCause();
			String cause = getStackTrace(ex);
			sb.append("*** Cause: "+ex.getClass().getName());
			sb.append('\n');
			sb.append("** Message: "+ex.getMessage());
			sb.append('\n');
			sb.append("** Stack track: "+cause);
			sb.append('\n');
			return sb.toString();
		}
	}

}
