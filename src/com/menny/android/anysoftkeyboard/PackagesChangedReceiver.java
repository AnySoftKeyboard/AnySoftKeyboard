package com.menny.android.anysoftkeyboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.menny.android.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.dictionary.DictionaryFactory;

public class PackagesChangedReceiver extends BroadcastReceiver {

	private static final String TAG = "ASK PkgChanged";
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null || intent.getData() == null || context == null)
			return;

		if (AnySoftKeyboardConfiguration.DEBUG)Log.d(TAG, new StringBuffer("Packages (").append(intent.getData())
				.append(") have been changed.").toString());
		
		DictionaryFactory.getInstance().releaseAllDictionaries();
		AddOnsFactory.onPackageChanged(intent);
	}
}
