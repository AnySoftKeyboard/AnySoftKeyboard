package com.menny.android.anysoftkeyboard.Dictionary;

import java.util.List;

import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.Dictionary.DictionarySQLiteConnection.DictionaryWord;

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
		
		List<DictionaryWord> words = mStorage.getAllWords();
		Log.d("AnySoftKeyboard", "SQLite dictionary loaded "+words.size()+" words.");
		for(DictionaryWord word : words)
		{
			addWordFromStorage(word.getWord(), word.getFrequency());
		}
		/*calling GC here, will stop the device for even longer time.
		//we just finished working with a lot of memory.
		//lets release it.
		System.gc();
		*/
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
