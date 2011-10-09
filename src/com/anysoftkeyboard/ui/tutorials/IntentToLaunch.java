package com.anysoftkeyboard.ui.tutorials;

import android.content.Intent;

class IntentToLaunch
{
	public final Intent IntentToStart;
	public final int NotificationIcon;
	public final int NotificationTitle;
	public final int NotificationText;
	
	public IntentToLaunch(Intent intent, int icon, int title, int text)
	{
		IntentToStart = intent;
		NotificationIcon = icon;
		NotificationTitle = title;
		NotificationText = text;
	}
}