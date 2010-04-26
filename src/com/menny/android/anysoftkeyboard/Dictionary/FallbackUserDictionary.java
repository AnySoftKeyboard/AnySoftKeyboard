package com.menny.android.anysoftkeyboard.Dictionary;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;


public class FallbackUserDictionary extends SQLiteUserDictionaryBase {

	private class FallBackSQLite extends DictionarySQLiteConnection
	{
		private static final String DB_NAME = "fallback.db";
		private static final String TABLE_NAME = "FALL_BACK_USER_DICTIONARY";
		private static final String WORD_COL = "Word";
		private static final String FREQ_COL = "Freq";

		public FallBackSQLite(Context context) {
			super(context, DB_NAME, TABLE_NAME, WORD_COL, FREQ_COL);
		}

		@Override
		public List<DictionaryWord> getAllWords() {
			List<DictionaryWord> words = super.getAllWords();
			if (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
			{
				for(DictionaryWord word : words)
				{
						Log.d("AnySoftKeyboard", "FallBackSQLite dictionary loaded: "+word.getWord());
				}
			}
			return words;
		}
	}

	public FallbackUserDictionary(AnyKeyboardContextProvider context) throws Exception {
		super(context);
	}

	@Override
	protected DictionarySQLiteConnection createStorage() {
		return new FallBackSQLite(super.mContext);
	}
}
