package com.anysoftkeyboard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;

public class PackagesChangedReceiver extends BroadcastReceiver {

	private static final String TAG = "ASK PkgChanged";
	
	private final StringBuffer mSB = new StringBuffer();
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null || intent.getData() == null || context == null)
			return;

		if (AnyApplication.DEBUG)
		{
			mSB.setLength(0);
			String text = mSB.append("Package '").append(intent.getData()).append("' have been changed.").toString();
			Log.d(TAG, text);
		}
		
		AddOnsFactory.onPackageChanged(intent);
	}
}
