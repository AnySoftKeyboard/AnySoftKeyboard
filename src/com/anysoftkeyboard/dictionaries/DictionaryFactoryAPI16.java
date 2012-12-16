package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyApplication;

public class DictionaryFactoryAPI16 extends DictionaryFactory {
	private static final String TAG = "ASK DictFctry16";

	protected ContactsDictionary createConcreteContactsDictionary(
			Context context) throws Exception {
		if (AnyApplication.DEBUG) Log.d(TAG, "Actually using API16 for contacts.");
		return new ContactsDictionaryAPI16(context);
	}
}
