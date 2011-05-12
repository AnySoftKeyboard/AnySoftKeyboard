package com.anysoftkeyboard.dictionaries;

import java.util.List;

import android.util.Log;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.dictionaries.DictionarySQLiteConnection.DictionaryWord;
import com.menny.android.anysoftkeyboard.AnyApplication;

public abstract class SQLiteUserDictionaryBase extends UserDictionaryBase {

	private DictionarySQLiteConnection mStorage;

	protected SQLiteUserDictionaryBase(String dictionaryName, AnyKeyboardContextProvider anyContext) {
		super(dictionaryName, anyContext);
	}

	@Override
	protected void loadDictionaryAsync()
	{
		if (mStorage == null)
			mStorage = createStorage();
		//taking time for storage load.
		long loadStartTime = System.currentTimeMillis();
		List<DictionaryWord> words = mStorage.getAllWords();
		long loadEndTime = System.currentTimeMillis();
		if (AnyApplication.DEBUG)Log.d(TAG, "SQLite dictionary loaded "+words.size()+" words. Took "+(loadEndTime-loadStartTime)+" ms.");
		for(DictionaryWord word : words)
		{
			addWordFromStorage(word.getWord(), word.getFrequency());
		}
		long storeEndTime = System.currentTimeMillis();
		if (AnyApplication.DEBUG)Log.d(TAG, "Stored "+words.size()+" words in dictionary. Took "+(storeEndTime-loadEndTime)+" ms.");
		/*calling GC here, will stop the device for even longer time.
		//we just finished working with a lot of memory.
		//lets release it.
		System.gc();
		*/
	}

	protected abstract DictionarySQLiteConnection createStorage();

	@Override
	protected void AddWordToStorage(String word, int frequency) {
		mStorage.addWord(word, frequency);
	}

	@Override
	protected void closeAllResources() {
		if (mStorage != null)
			mStorage.close();
	}

}
