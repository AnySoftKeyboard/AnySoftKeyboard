package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.util.Log;

public class DictionaryFactoryAPI5 extends DictionaryFactory {
	private static final String TAG = "ASK DictFctry5";

	@Override
	public synchronized EditableDictionary createContactsDictionary(
			Context context) {
		try {
			Log.d(TAG, "Creating device's contacts suggestions dictionary...");
			ContactsDictionary cdict = createConcreteContactsDictionary(context);
			cdict.loadDictionary();
			return cdict;
		} catch (Exception ex) {
			Log.w(TAG, "Failed to load 'ContactsDictionary'", ex);
			return null;
		}
	}

	protected ContactsDictionary createConcreteContactsDictionary(
			Context context) throws Exception {
		return new ContactsDictionary(context);
	}
}
