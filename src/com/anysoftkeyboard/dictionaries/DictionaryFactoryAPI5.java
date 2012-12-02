package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyApplication;

public class DictionaryFactoryAPI5 extends DictionaryFactory {
	private static final String TAG = "ASK DictFctry5";

	private EditableDictionary mContactsDictionary;

	@Override
	public synchronized EditableDictionary createContactsDictionary(
			Context context) {
		if (mContactsDictionary != null) {
			if (AnyApplication.DEBUG)
				Log.d(TAG, "Returning cached contacts dictionary.");
			return mContactsDictionary;
		}
		try {
			Log.d(TAG, "Creating device's contacts suggestions dictionary...");
			mContactsDictionary = new ContactsDictionary(context);
			mContactsDictionary.loadDictionary();
		} catch (Exception ex) {
			Log.w(TAG, "Failed to load 'ContactsDictionary'", ex);
			mContactsDictionary = null;
		}
		return mContactsDictionary;
	}
}
