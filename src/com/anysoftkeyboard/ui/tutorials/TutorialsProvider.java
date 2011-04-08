package com.anysoftkeyboard.ui.tutorials;

import java.util.ArrayList;

import com.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.anysoftkeyboard.ui.settings.MainSettings;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.Secure;
import android.util.Log;

public class TutorialsProvider 
{
	private static final String ASK_HAS_BEEN_ENABLED_BEFORE = "ask_has_been_enabled_before";

	private static final String TAG = "ASK Turorial";

	private static final int BASE_NOTIFICATION_ID = 1024;

	private static ArrayList<Intent> msActivitiesToShow = new ArrayList<Intent>();
	
	/**
	 * Search array for an entry BEGINNING with key.
	 * 
	 * @param array the array to search over
	 * @param key the string to search for
	 * @return true if the key was found in the array
	 */
	private static boolean linearSearch( String listOfIme, final String key )
	{
		String[] arrayOfIme = listOfIme.split(":");
		if (AnySoftKeyboardConfiguration.DEBUG)
			Log.d(TAG, "Currently these are the IME enabled in the OS: "+listOfIme);
		
		for(final String ime : arrayOfIme)
		{
			if (AnySoftKeyboardConfiguration.DEBUG)
				Log.d(TAG, "Is '"+ime+"' equals '"+key+"'?");
			//checking "startsWith" since the OS list is something like this:
			//com.android.inputmethod.latin/.LatinIME:com.menny.android.anysoftkeyboard/.SoftKeyboard
			if (ime.startsWith(key)) return true;
		}
		
		if (AnySoftKeyboardConfiguration.DEBUG)
			Log.d(TAG, "'"+key+"' was not found in the list of IMEs!");
		return false;
	}
	
	public static void ShowTutorialsIfNeeded(Context context)
	{
		Log.i(TAG, "TutorialsProvider::ShowTutorialsIfNeeded called");
		
		if (AnySoftKeyboardConfiguration.DEBUG && AnyApplication.getConfig().getShowVersionNotification())
		{
			Log.i(TAG, "TESTERS VERSION added");

			Intent i = new Intent(context, TestersNoticeActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			msActivitiesToShow.add(i);
		}
		
		if (!linearSearch( Secure.getString(context.getContentResolver(), Secure.ENABLED_INPUT_METHODS),
				context.getPackageName() ) )
		{
			//ASK is not enabled, but installed. Has the user forgot how to turn it on?
			if (!hasAnySoftKeyboardEnabled(context))
			{
				//this is the first time the application is loaded.
				Log.i(TAG, "Welcome added");

				Intent i = new Intent(context, WelcomeHowToNoticeActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				msActivitiesToShow.add(i);
			}
		}
		else
		{
			SharedPreferences sp = context.getSharedPreferences("tutorials", 0);//private
			
			Editor e = sp.edit();
			e.putBoolean(ASK_HAS_BEEN_ENABLED_BEFORE, true);
			e.commit();
		}
		
		
		if (AnyApplication.getConfig().getShowVersionNotification() && firstTimeVersionLoaded(context))
		{
			Log.i(TAG, "changelog added");
			
			Intent i = new Intent(context, ChangeLogActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			msActivitiesToShow.add(i);
		}
		
		showNotificationIcon(context);
	}
	
	public static void onServiceDestroy()
	{
		msActivitiesToShow.clear();
	}

	private static boolean hasAnySoftKeyboardEnabled(Context context) {
		SharedPreferences sp = context.getSharedPreferences("tutorials", 0);//private
		final boolean hasBeenLoaded = sp.getBoolean(ASK_HAS_BEEN_ENABLED_BEFORE, false);
		
		return hasBeenLoaded;
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
            
            Intent notificationIntent = msActivitiesToShow.get(0);
            
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
            
            //removes this notification
            msActivitiesToShow.remove(0);
		}
	}
}
