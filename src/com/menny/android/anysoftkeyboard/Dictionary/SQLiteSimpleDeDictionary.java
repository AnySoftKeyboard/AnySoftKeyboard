package com.menny.android.anysoftkeyboard.Dictionary;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class SQLiteSimpleDeDictionary extends SQLiteUserDictionaryBase {

	protected SQLiteSimpleDeDictionary(AnyKeyboardContextProvider anyContext) throws Exception {
		super(anyContext);
	}
	
	@Override
	protected DictionarySQLiteConnection createStorage() {
		try {
			return new AssertsSQLiteConnection(mContext, "de", "de");
		} catch (Exception e) {
			e.printStackTrace();
			return new DictionarySQLiteConnection(mContext, "de", "de", "Word", "Frequency");
		}
	}
	
	@Override
	public synchronized void addWord(String word, int frequency) {
		//does nothing
	}

}
