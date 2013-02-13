package com.anysoftkeyboard.dictionaries;

import android.content.Context;

public class FallbackUserDictionary extends SQLiteUserDictionaryBase {
	
	private final String mLocale;
	
	public FallbackUserDictionary(Context context, String locale){
		super("FallbackUserDictionary", context);
		mLocale = locale;
	}

	@Override
	protected DictionarySQLiteConnection createStorage() {
		return new DictionarySQLiteConnection(super.mContext, mLocale);
	}
	
	@Override
	public void loadDictionary() {
		//NOT doing it async, why? because my parent (SafeUserDictionary) is doing it async
		loadDictionaryAsync();
	}
}
