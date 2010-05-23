package com.menny.android.anysoftkeyboard;

import com.menny.android.anysoftkeyboard.dictionary.DictionaryFactory;
import com.menny.android.anysoftkeyboard.dictionary.ExternalDictionaryFactory;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardBuildersFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PackagesChangedReceiver extends BroadcastReceiver {

	private static final String TAG = "ASKPackagesChangedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent == null || intent.getData() == null || context == null)
			return;


		boolean isDictionary = (context.getPackageManager().queryBroadcastReceivers(
				new Intent(ExternalDictionaryFactory.DictionaryBuilder.RECEIVER_INTERFACE), 0).size() > 0);
		boolean isKeyboard = (context.getPackageManager().queryBroadcastReceivers(
				new Intent(KeyboardBuildersFactory.KeyboardBuilder.RECEIVER_INTERFACE), 0).size() > 0);

		if(isKeyboard){
			Log.i(TAG, "Rebuilding Keyboards since (keyboard) packages have been changed.");
			KeyboardSwitcher keyboardSwitcher = KeyboardSwitcher.getInstance();
			if(keyboardSwitcher != null){
				KeyboardBuildersFactory.resetBuildersCache();
				KeyboardSwitcher.getInstance().makeKeyboards(true);
			}
		}

		if(isDictionary){
			Log.i(TAG, "Refreshing dictionaries since (dictionary) packages have been changed.");

			DictionaryFactory.releaseAllDictionaries();
			AnySoftKeyboard softKeyboard = AnySoftKeyboard.getInstance();
			if(softKeyboard != null) {
				softKeyboard.setMainDictionaryForCurrentKeyboard();
			}
		}
	}

}
