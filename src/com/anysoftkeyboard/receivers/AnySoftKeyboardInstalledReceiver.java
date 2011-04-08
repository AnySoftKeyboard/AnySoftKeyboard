package com.anysoftkeyboard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * This receiver is (hopefully) called after AnySoftKeyboard installed from the Market
 * see:
 * http://code.google.com/mobile/analytics/docs/android/#referrals
 * http://stackoverflow.com/questions/4093150/get-referrer-after-installing-app-from-android-market
 * 
 * This is actually used so the Application will be created, and so the Tutorial provider will be
 * called, and help (if needed) users to activate the keyboard. 
 */
public class AnySoftKeyboardInstalledReceiver extends BroadcastReceiver {

	private static final String TAG = "ASK Installed";
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.i(TAG, "Thank you for installing AnySoftKeyboard! We hope you'll like it.");
	}
}
