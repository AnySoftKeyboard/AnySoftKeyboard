package com.anysoftkeyboard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;

public class PackagesChangedReceiver extends BroadcastReceiver {

	private static final String TAG = "ASK PkgChanged";
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null || intent.getData() == null || context == null)
			return;

		if (AnyApplication.DEBUG)Log.d(TAG, new StringBuffer("Packages (").append(intent.getData())
				.append(") have been changed.").toString());
		
		//DictionaryFactory.getInstance().releaseAllDictionaries();
		AddOnsFactory.onPackageChanged(intent);
	}
}
