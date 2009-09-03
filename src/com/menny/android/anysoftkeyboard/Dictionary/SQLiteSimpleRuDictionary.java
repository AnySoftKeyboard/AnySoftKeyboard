package com.menny.android.anysoftkeyboard.Dictionary;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class SQLiteSimpleRuDictionary extends SQLiteUserDictionaryBase {

	protected SQLiteSimpleRuDictionary(AnyKeyboardContextProvider anyContext) throws Exception {
		super(anyContext);
	}
	
	@Override
	protected DictionarySQLiteConnection createStorage() {
		try {
			return new AssertsSQLiteConnection(mContext, "ru", "ru");
		} catch (Exception e) {
			e.printStackTrace();
			return new DictionarySQLiteConnection(mContext, "ru", "ru", "Word", "Frequency");
		}
	}
	
	
	@Override
	public synchronized void addWord(String word, int frequency) {
		//does nothing
	}

}
