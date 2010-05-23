package com.menny.android.anysoftkeyboard;

import com.menny.android.anysoftkeyboard.dictionary.DictionaryFactory;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardBuildersFactory;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardFactory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PackagesChangedReceiver extends BroadcastReceiver {

	private static final String TAG = "ASKPackagesChangedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Rebuilding Keyboards since packages have been changed.");
			KeyboardSwitcher keyboardSwitcher = KeyboardSwitcher.getInstance();
			if(keyboardSwitcher != null){
				KeyboardBuildersFactory.resetBuildersCache();
				KeyboardSwitcher.getInstance().makeKeyboards(true);
			}

			Log.i(TAG, "Refreshing dictionaries since packages have been changed.");

			DictionaryFactory.releaseAllDictionaries();
			AnySoftKeyboard softKeyboard = AnySoftKeyboard.getInstance();
			if(softKeyboard != null) {
				softKeyboard.setMainDictionaryForCurrentKeyboard();
			}
	}

}
