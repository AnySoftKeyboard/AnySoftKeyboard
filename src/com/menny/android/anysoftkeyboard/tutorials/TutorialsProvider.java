package com.menny.android.anysoftkeyboard.tutorials;

import java.util.ArrayList;

import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.settings.MainSettings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class TutorialsProvider 
{
	private static final String TAG = "ASK Turorial";

	private static final int BASE_NOTIFICATION_ID = 1024;

	private static ArrayList<Intent> msActivitiesToShow = new ArrayList<Intent>();
	
	public static void ShowTutorialsIfNeeded(Context context)
	{
		Log.i(TAG, "TutorialsProvider::ShowTutorialsIfNeeded called");
		if (AnySoftKeyboardConfiguration.DEBUG && AnySoftKeyboardConfiguration.getInstance().getShowVersionNotification())
		{
			Log.i(TAG, "TESTERS VERSION added");

			Intent i = new Intent(context, TutorialActivity.class);
			i.putExtra(TutorialActivity.LAYOUT_RESOURCE_ID, R.layout.testers_version);
			i.putExtra(TutorialActivity.NAME_RESOURCE_ID, R.string.testers_version);
			
			msActivitiesToShow.add(i);
		}
		
		if (AnySoftKeyboardConfiguration.getInstance().getShowVersionNotification() && (AnySoftKeyboardConfiguration.DEBUG || firstTimeVersionLoaded(context)))
		{
			Log.i(TAG, "changelog added");
			
			Intent i = new Intent(context, ChangeLogActivity.class);
			
			msActivitiesToShow.add(i);
		}
		
		showNotificationIcon(context);
	}
	
	public static void onServiceDestroy()
	{
		msActivitiesToShow.clear();
	}

	private static boolean firstTimeVersionLoaded(Context context) {
		SharedPreferences sp = context.getSharedPreferences("tutorials", 0);//private
		final int lastTutorialVersion = sp.getInt("tutorial_version", 0);
		final int packageVersion = getPackageVersion(context);
		
		Editor e = sp.edit();
		e.putInt("tutorial_version", packageVersion);
		e.commit();
		
		return packageVersion != lastTutorialVersion;
	}

	private static int getPackageVersion(Context context) {
		try {
			PackageInfo pi = MainSettings.getPackageInfo(context);
			return pi.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	public synchronized static void showNotificationIcon(Context context) {
		if (msActivitiesToShow.size() > 0)
		{
			Notification notification = new Notification(R.drawable.notification_icon, context.getText(R.string.notification_text), System.currentTimeMillis());
            
            Intent notificationIntent = msActivitiesToShow.remove(0);
            
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            
            notification.setLatestEventInfo(context,
                            context.getText(R.string.ime_name), context.getText(R.string.notification_text),
                            contentIntent);
            notification.defaults = 0;// no sound, vibrate, etc.
            //Cancel on click
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            //shows the number on the icon
            if (msActivitiesToShow.size() > 1)
            	notification.number = msActivitiesToShow.size();
            // notifying
            //need different id for each notification, so we can cancel easily
            ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(BASE_NOTIFICATION_ID+msActivitiesToShow.size(), notification);
		}
	}
}
