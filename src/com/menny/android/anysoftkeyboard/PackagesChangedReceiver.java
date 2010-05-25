package com.menny.android.anysoftkeyboard;

import com.menny.android.anysoftkeyboard.dictionary.DictionaryFactory;
import com.menny.android.anysoftkeyboard.dictionary.ExternalDictionaryFactory;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardBuildersFactory;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class PackagesChangedReceiver extends BroadcastReceiver {

	private static final String TAG = "ASKPackagesChangedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null || intent.getData() == null || context == null)
			return;

		if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)
				&& intent.getExtras().getBoolean(Intent.EXTRA_REPLACING)) {
			// Event should be followed by a install so we do nothing at
			// this point
			Log.d(TAG, "Replacing app; we wait for ADD intent");
			return;
		}

		Intent dictionaryIntent = new Intent(
				ExternalDictionaryFactory.DictionaryBuilder.RECEIVER_INTERFACE);
		Intent keyboardIntent = new Intent(
				KeyboardBuildersFactory.KeyboardBuilder.RECEIVER_INTERFACE);

		dictionaryIntent.setPackage(intent.getPackage());
		keyboardIntent.setPackage(intent.getPackage());
		boolean isDictionary = (context.getPackageManager()
				.queryBroadcastReceivers(dictionaryIntent,
						PackageManager.GET_RECEIVERS).size() > 0);
		boolean isKeyboard = (context.getPackageManager()
				.queryBroadcastReceivers(keyboardIntent,
						PackageManager.GET_RECEIVERS).size() > 0);
		isDictionary = isDictionary
				|| context.getPackageName().equals(intent.getPackage());

		isKeyboard = isKeyboard
				|| context.getPackageName().equals(intent.getPackage());

		Log.d(TAG, new StringBuffer("Packages (").append(intent.getData())
				.append(") have been changed. Is dictionary ").append(
						isDictionary).append(", isKeyboard ")
				.append(isKeyboard).toString());

		if (isKeyboard || isDictionary) {
			Log
					.i(TAG,
							"Rebuilding Keyboards since (keyboard) packages have been changed.");
			KeyboardSwitcher keyboardSwitcher = KeyboardSwitcher.getInstance();
			if (keyboardSwitcher != null) {
				KeyboardBuildersFactory.resetBuildersCache();
				KeyboardSwitcher.getInstance().makeKeyboards(true);
			}
		}

		if (isDictionary || isDictionary) {
			Log
					.i(TAG,
							"Refreshing dictionaries since (dictionary) packages have been changed.");

			DictionaryFactory.releaseAllDictionaries();
			AnySoftKeyboard softKeyboard = AnySoftKeyboard.getInstance();
			if (softKeyboard != null) {
				softKeyboard.setMainDictionaryForCurrentKeyboard();
			}
		}
	}
}
