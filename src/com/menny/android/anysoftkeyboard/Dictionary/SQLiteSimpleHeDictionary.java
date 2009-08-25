package com.menny.android.anysoftkeyboard.Dictionary;

import java.io.IOException;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class SQLiteSimpleHeDictionary extends SQLiteUserDictionaryBase {

	protected SQLiteSimpleHeDictionary(AnyKeyboardContextProvider anyContext) throws Exception {
		super(anyContext);
	}
	
	@Override
	protected DictionarySQLiteConnection createStorage() throws IOException {
		return new AssertsSQLiteConnection(mContext, "he", "he");
	}
	
	
	@Override
	public synchronized void addWord(String word, int frequency) {
		//does nothing
	}

}
