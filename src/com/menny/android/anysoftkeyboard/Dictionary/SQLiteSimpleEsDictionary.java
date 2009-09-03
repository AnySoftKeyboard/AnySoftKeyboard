package com.menny.android.anysoftkeyboard.Dictionary;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class SQLiteSimpleEsDictionary extends SQLiteUserDictionaryBase {

	protected SQLiteSimpleEsDictionary(AnyKeyboardContextProvider anyContext) throws Exception {
		super(anyContext);
	}
	
	@Override
	protected DictionarySQLiteConnection createStorage() {
		try {
			return new AssertsSQLiteConnection(mContext, "es", "es");
		} catch (Exception e) {
			e.printStackTrace();
			return new DictionarySQLiteConnection(mContext, "es", "es", "Word", "Frequency");
		}
	}
	
	
	@Override
	public synchronized void addWord(String word, int frequency) {
		//does nothing
	}

}
