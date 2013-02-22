package com.anysoftkeyboard.dictionaries;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.UserDictionary.Words;

import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;

public class DictionarySQLiteConnection extends SQLiteOpenHelper {
	private static final String TAG = "ASK DictSql";

	public static class DictionaryWord {
		private final String mWord;
		private final int mFrequency;

		public DictionaryWord(String word, int freq) {
			if (word == null) {
				Log.e(TAG, "Got a NULL word from dictionary! This is illegal!");
				word = "" + this.hashCode();
			}
			mWord = word;
			mFrequency = freq;
		}

		public String getWord() {
			return mWord;
		}

		public int getFrequency() {
			return mFrequency;
		}
	}

	private final static String DB_FILENAME = "fallback.db";
	private final static String TABLE_NAME = "FALL_BACK_USER_DICTIONARY";
	// protected final String mWordsColumnName;
	// protected final String mFrequencyColumnName;
	// protected final String mLocaleColumnName;
	protected final Context mContext;
	private final String mCurrentLocale;

	public DictionarySQLiteConnection(Context context, String currentLocale) {
		super(context, DB_FILENAME, null, 6);
		mContext = context;
		mCurrentLocale = currentLocale;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + Words._ID
				+ " INTEGER PRIMARY KEY," + Words.WORD + " TEXT,"
				+ Words.FREQUENCY + " INTEGER," + Words.LOCALE + " TEXT" + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Please note: don't use class level constants here, since they may
		// change.
		// if you upgrade from one version to another, make sure you use the
		// correct names!
		Log.d(TAG, "Upgrading DictionarySQLiteConnection from version "
				+ oldVersion + " to " + newVersion + "...");
		if (oldVersion < 4) {
			Log.d(TAG,
					"Upgrading DictionarySQLiteConnection to version 4: Adding locale column...");
			db.execSQL("ALTER TABLE FALL_BACK_USER_DICTIONARY ADD COLUMN locale TEXT;");
		}
		if (oldVersion < 5) {
			Log.d(TAG,
					"Upgrading DictionarySQLiteConnection to version 5: Adding _id column and populating...");
			db.execSQL("ALTER TABLE FALL_BACK_USER_DICTIONARY ADD COLUMN _id INTEGER;");
			db.execSQL("UPDATE FALL_BACK_USER_DICTIONARY SET _id=Id;");
		}
		if (oldVersion < 6) {
			Log.d(TAG,
					"Upgrading DictionarySQLiteConnection to version 6: Matching schema with Android's User-Dictionary table...");
			db.execSQL("ALTER TABLE FALL_BACK_USER_DICTIONARY RENAME TO tmp_FALL_BACK_USER_DICTIONARY;");

			db.execSQL("CREATE TABLE FALL_BACK_USER_DICTIONARY ("
					+ "_id INTEGER PRIMARY KEY," + "word TEXT,"
					+ "frequency INTEGER," + "locale TEXT" + ");");

			db.execSQL("INSERT INTO FALL_BACK_USER_DICTIONARY(_id, word, frequency, locale) SELECT _id, Word, Freq, locale FROM tmp_FALL_BACK_USER_DICTIONARY;");

			db.execSQL("DROP TABLE tmp_FALL_BACK_USER_DICTIONARY;");
		}
	}

	public synchronized void addWord(String word, int freq) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Words._ID, word.hashCode());// ensuring that any word is
												// inserted once
		values.put(Words.WORD, word);
		values.put(Words.FREQUENCY, freq);
		values.put(Words.LOCALE, mCurrentLocale);
		long res = db.insert(TABLE_NAME, null, values);
		if (res < 0) {
			Log.e(TAG, "Unable to insert '" + word
					+ "' to the fall-back dictionary! Result:" + res);
		} else {
			if (AnyApplication.DEBUG)
				Log.d(TAG, "Inserted '" + word
						+ "' to the fall-back dictionary. Id:" + res);
		}
		db.close();
	}

	public synchronized void deleteWord(String word) {
		SQLiteDatabase db = getWritableDatabase();

		db.delete(TABLE_NAME, Words.WORD + "=?", new String[] { word });
		db.close();
	}

	public synchronized WordsCursor getWordsCursor() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, new String[] { Words._ID, Words.WORD,
				Words.FREQUENCY }, "(" + Words.LOCALE + " IS NULL) or ("
				+ Words.LOCALE + "=?)", new String[] { mCurrentLocale }, null,
				null, null);
		return new WordsCursor.SqliteWordsCursor(db, c);
	}

	public synchronized List<DictionaryWord> getAllWords() {
		WordsCursor wordsCursor = getWordsCursor();
		try {
			Cursor c = wordsCursor.getCursor();
			if (c != null) {
				final int wordColumnIndex = c.getColumnIndex(Words.WORD);
				final int freqColumnIndex = c.getColumnIndex(Words.FREQUENCY);
				List<DictionaryWord> words;
				try {
					words = new ArrayList<DictionaryWord>(c.getCount());
					if (c.moveToFirst()) {
						while (!c.isAfterLast()) {
							String word = c.getString(wordColumnIndex);
							int freq = c.getInt(freqColumnIndex);
							words.add(new DictionaryWord(word, freq));
							c.moveToNext();
						}
					}
				} catch (IllegalStateException e) {
					// could be a memory issue
					// see
					// https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/2
					words = new ArrayList<DictionaryWord>(0);
				}

				return words;
			} else
				return new ArrayList<DictionaryWord>(0);
		} finally {
			wordsCursor.close();
		}
	}

	String getDatabaseFile() {
		return DB_FILENAME;
	}
}
