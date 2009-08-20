package com.menny.android.anysoftkeyboard.Dictionary;

import java.util.List;

import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public abstract class SQLiteUserDictionaryBase extends UserDictionaryBase {

	private DictionarySQLiteConnection mStorage;
	
	protected SQLiteUserDictionaryBase(AnyKeyboardContextProvider anyContext) throws Exception {
		super(anyContext);
	}

	@Override
	protected void loadAllWords() throws Exception 
	{
		if (mStorage == null)
			mStorage = createStorage();
		
		List<String> words = mStorage.getAllWords();
		Log.d("AnySoftKeyboard", "SQLite dictionary loaded "+words.size()+" words.");
		for(String word : words)
		{
			addWordFromStorage(word, 128);
		}
	}

	protected abstract DictionarySQLiteConnection createStorage() throws Exception;

	@Override
	protected void AddWordToStorage(String word, int frequency) {
		mStorage.addWord(word, frequency);
	}

	@Override
	protected void closeAllResources() {
		mStorage.close();
	}

}
