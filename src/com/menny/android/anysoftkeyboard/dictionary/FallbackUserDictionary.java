package com.menny.android.anysoftkeyboard.dictionary;

import android.content.Context;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

class FallbackUserDictionary extends SQLiteUserDictionaryBase {

	private static class FallBackSQLite extends DictionarySQLiteConnection
	{
		private static final String DB_NAME = "fallback.db";
		private static final String TABLE_NAME = "FALL_BACK_USER_DICTIONARY";
		private static final String WORD_COL = "Word";
		private static final String FREQ_COL = "Freq";

		public FallBackSQLite(Context context) {
			super(context, DB_NAME, TABLE_NAME, WORD_COL, FREQ_COL);
		}
	}

	public FallbackUserDictionary(AnyKeyboardContextProvider context){
		super("FallbackUserDictionary", context);
	}

	@Override
	protected DictionarySQLiteConnection createStorage() {
		return new FallBackSQLite(super.mContext);
	}
	
	@Override
	public void loadDictionary() {
		//NOT doing it async
		loadDictionaryAsync();
	}
}
