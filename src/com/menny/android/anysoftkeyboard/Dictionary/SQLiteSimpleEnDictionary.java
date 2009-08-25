package com.menny.android.anysoftkeyboard.Dictionary;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class SQLiteSimpleEnDictionary extends SQLiteUserDictionaryBase {

	protected SQLiteSimpleEnDictionary(AnyKeyboardContextProvider anyContext) throws Exception {
		super(anyContext);
	}
	
	@Override
	protected DictionarySQLiteConnection createStorage() {
		try {
			return new AssertsSQLiteConnection(mContext, "en", "en");
		} catch (Exception e) {
			e.printStackTrace();
			return new DictionarySQLiteConnection(mContext, "en", "en", "Word", "Frequency");
		}
	}
	
	
	@Override
	public synchronized void addWord(String word, int frequency) {
		//does nothing
	}

}
