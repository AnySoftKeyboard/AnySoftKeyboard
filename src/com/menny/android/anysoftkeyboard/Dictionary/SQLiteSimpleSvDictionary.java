package com.menny.android.anysoftkeyboard.Dictionary;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class SQLiteSimpleSvDictionary extends SQLiteUserDictionaryBase {

	protected SQLiteSimpleSvDictionary(AnyKeyboardContextProvider anyContext) throws Exception {
		super(anyContext);
	}
	
	@Override
	protected DictionarySQLiteConnection createStorage() {
		try {
			return new AssertsSQLiteConnection(mContext, "sv", "sv");
		} catch (Exception e) {
			e.printStackTrace();
			return new DictionarySQLiteConnection(mContext, "sv", "sv", "Word", "Frequency");
		}
	}
	
	
	@Override
	public synchronized void addWord(String word, int frequency) {
		//does nothing
	}

}
