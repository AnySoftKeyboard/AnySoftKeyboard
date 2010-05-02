package com.menny.android.anysoftkeyboard.tutorials;

import java.util.ArrayList;

import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TutorialsProvider 
{
	private static final String TAG = "ASK Turorial";

	private static final int BASE_NOTIFICATION_ID = 1024;

	private static ArrayList<TutorialActivityData> msActivitiesToShow = new ArrayList<TutorialActivityData>();
	
	public static void ShowTutorialsIfNeeded(Context context)
	{
		Log.i(TAG, "TutorialsProvider::ShowTutorialsIfNeeded called");
		if (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
		{
			Log.i(TAG, "TESTERS VERSION added");
			TutorialActivityData data = new TutorialActivityData(R.string.testers_version, R.layout.testers_version);
			msActivitiesToShow.add(data);
		}
		
		if (AnySoftKeyboardConfiguration.getInstance().getDEBUG() || firstTimeVersionLoaded(context))
		{
			Log.i(TAG, "changelog added");
			TutorialActivityData data = new TutorialActivityData(R.string.changelog, R.layout.changelog);
			msActivitiesToShow.add(data);
		}
		
		showNotificationIcon(context);
	}
	
	public static void onServiceDestroy()
	{
		msActivitiesToShow.clear();
	}

	private static boolean firstTimeVersionLoaded(Context context) {
		// TODO Auto-generated method stub
		return false;
	}

	public synchronized static void showNotificationIcon(Context context) {
		if (msActivitiesToShow.size() > 0)
		{
			Notification notification = new Notification(R.drawable.notification_icon, context.getText(R.string.notification_text), System.currentTimeMillis());
            
            Intent notificationIntent = new Intent(context, TutorialActivity.class);
            
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
	
	public static synchronized TutorialActivityData dequeueTutorial()
	{
		if (msActivitiesToShow.size() > 0)
		{
			return msActivitiesToShow.remove(0);
		}
		else
		{
			return null;
		}
	}
	
	public static class TutorialActivityData
	{
		public final int NameResourceId;
		public final int LayoutResourceId;
		
		public TutorialActivityData(int nameId, int layoutId)
		{
			NameResourceId = nameId;
			LayoutResourceId = layoutId;
		}
	}
}
