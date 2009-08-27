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
		//taking time for storage load.
		long loadStartTime = System.currentTimeMillis();
		List<DictionaryWord> words = mStorage.getAllWords();
		long loadEndTime = System.currentTimeMillis();
		Log.d("AnySoftKeyboard", "SQLite dictionary loaded "+words.size()+" words. Took "+(loadEndTime-loadStartTime)+" ms.");
		for(DictionaryWord word : words)
		{
			addWordFromStorage(word.getWord(), word.getFrequency());
		}
		long storeEndTime = System.currentTimeMillis();
		Log.d("AnySoftKeyboard", "Stored "+words.size()+" words in dictionary. Took "+(storeEndTime-loadEndTime)+" ms.");
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
