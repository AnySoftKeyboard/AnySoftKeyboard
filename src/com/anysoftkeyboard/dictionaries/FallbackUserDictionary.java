package com.anysoftkeyboard.dictionaries;

import android.content.Context;

public class FallbackUserDictionary extends SQLiteUserDictionaryBase {
/*
	private static class FallBackSQLite extends DictionarySQLiteConnection
	{
		private static final String DB_NAME = "fallback.db";
		private static final String TABLE_NAME = "FALL_BACK_USER_DICTIONARY";
		private static final String WORD_COL = "Word";
		private static final String FREQ_COL = "Freq";
		private static final String LOCALE_COL = "locale";

		public FallBackSQLite(Context context, String currentLocale) {
			super(context, DB_NAME, TABLE_NAME, WORD_COL, FREQ_COL, LOCALE_COL, currentLocale);
		}
	}
*/
	private final String mLocale;
	
	public FallbackUserDictionary(Context context, String locale){
		super("FallbackUserDictionary", context);
		mLocale = locale;
	}

	@Override
	protected DictionarySQLiteConnection createStorage() {
		return new DictionarySQLiteConnection(super.mContext, mLocale);
	}
	
	@Override
	public void loadDictionary() {
		//NOT doing it async, why? because my parent (SafeUserDictionary) is doing it async
		loadDictionaryAsync();
	}
}
