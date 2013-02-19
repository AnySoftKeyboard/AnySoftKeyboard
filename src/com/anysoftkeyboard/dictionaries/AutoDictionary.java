/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.anysoftkeyboard.dictionaries;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;

/**
 * Stores new words temporarily until they are promoted to the user dictionary
 * for longevity. Words in the auto dictionary are used to determine if it's ok
 * to accept a word that's not in the main or user dictionary. Using a new word
 * repeatedly will promote it to the user dictionary.
 */
public class AutoDictionary extends UserDictionaryBase {

	protected static final String TAG = "ASK ADict";

	// Weight added to a user picking a new word from the suggestion strip
	public static final int FREQUENCY_FOR_PICKED = 3;
	// Weight added to a user typing a new word that doesn't get corrected (or
	// is reverted)
	public static final int FREQUENCY_FOR_TYPED = 1;
	// A word that is frequently typed and gets promoted to the user dictionary,
	// uses this
	// frequency.
	public static final int FREQUENCY_FOR_AUTO_ADD = 178;
	/*
	 * this is not interesting // If the user touches a typed word 2 times or
	 * more, it will become valid. private static final int VALIDITY_THRESHOLD =
	 * FREQUENCY_FOR_PICKED;
	 */
	// If the user touches a typed word 4 times or more, it will be added to the
	// user dict.

	private AnySoftKeyboard mIme;
	// Locale for which this auto dictionary is storing words
	private final String mLocale;

	private HashMap<String, Integer> mPendingWrites = new HashMap<String, Integer>();
	private final Object mPendingWritesLock = new Object();

	private static final String DATABASE_NAME = "auto_dict.db";
	private static final int DATABASE_VERSION = 1;

	private static final String COLUMN_ID = BaseColumns._ID;
	private static final String COLUMN_WORD = "word";
	private static final String COLUMN_FREQUENCY = "freq";
	private static final String COLUMN_LOCALE = "locale";

	/** Sort by descending order of frequency. */
	public static final String DEFAULT_SORT_ORDER = COLUMN_FREQUENCY + " DESC";

	/** Name of the words table in the auto_dict.db */
	private static final String AUTODICT_TABLE_NAME = "words";

	private static HashMap<String, String> sDictProjectionMap;

	static {
		sDictProjectionMap = new HashMap<String, String>();
		sDictProjectionMap.put(COLUMN_ID, COLUMN_ID);
		sDictProjectionMap.put(COLUMN_WORD, COLUMN_WORD);
		sDictProjectionMap.put(COLUMN_FREQUENCY, COLUMN_FREQUENCY);
		sDictProjectionMap.put(COLUMN_LOCALE, COLUMN_LOCALE);
	}

	// Why static? Because ALL locale are using the same connection
	private static DatabaseHelper msGlobalDbHelper = null;
	private static Object msDbLocker = new Object();

	private static DatabaseHelper getDatabaseHelper(Context context) {
		synchronized (msDbLocker) {
			if (msGlobalDbHelper == null) {
				msGlobalDbHelper = new DatabaseHelper(context);
			}
			return msGlobalDbHelper;
		}
	}

	public AutoDictionary(Context context, AnySoftKeyboard ime, String locale) {
		super("Auto", context);
		mIme = ime;
		mLocale = locale;
	}

	@Override
	public boolean isValidWord(CharSequence word) {
		final int frequency = getWordFrequency(word);
		return frequency >= 1;// which means it has been seen before
	}

	@Override
	public void close() {
		flushPendingWrites();
		// Don't close the database as locale changes will require it to be
		// reopened anyway
		// Also, the database is written to somewhat frequently, so it needs to
		// be kept alive
		// throughout the life of the process.
		// mOpenHelper.close();
		super.close();
	}

	@Override
	protected void loadDictionaryAsync() {
		// Load the words that correspond to the current input locale
		WordsCursor wordsCursor = getWordsCursor();
		Cursor cursor = wordsCursor.getCursor();
		try {
			if (cursor.moveToFirst()) {
				int wordIndex = cursor.getColumnIndex(COLUMN_WORD);
				int frequencyIndex = cursor.getColumnIndex(COLUMN_FREQUENCY);
				while (!cursor.isAfterLast()) {
					String word = cursor.getString(wordIndex);
					int frequency = cursor.getInt(frequencyIndex);
					// Safeguard against adding really long words. Stack may
					// overflow due
					// to recursive lookup
					if (word.length() < MAX_WORD_LENGTH) {
						addWordFromStorage(word, frequency);
					}
					cursor.moveToNext();
				}
			}
		} finally {
			//NOTE: I'm closing ONLY the cursor! Not the WordsCursor (which also closes the DB).
			//this will help fighting race conditions when loading this locale, and a background
			//task is writing pendings of the previous locale.
			cursor.close();
		}
	}

	@Override
	public boolean addWord(String word, int addFrequency) {
		final int length = word.length();
		// Don't add very short or very long words.
		if (length < 2 || length > MAX_WORD_LENGTH)
			return false;
		if (mIme.getCurrentWord().isAutoCapitalized()) {
			// Remove caps before adding
			word = Character.toLowerCase(word.charAt(0)) + word.substring(1);
		}
		int freq = getWordFrequency(word);
		freq = freq < 0 ? addFrequency : freq + addFrequency;
		super.addWord(word, freq);

		boolean added = false;
		if (freq >= AnyApplication.getConfig()
				.getAutoDictionaryInsertionThreshold()) {
			Log.i(TAG, "Promoting the word " + word + " (freq " + freq
					+ ") to the user dictionary. It earned it.");
			added = mIme.promoteToUserDictionary(word, FREQUENCY_FOR_AUTO_ADD);
			freq = 0;
		}

		synchronized (mPendingWritesLock) {
			// Write a null frequency if it is to be deleted from the db
			mPendingWrites.put(word, freq == 0 ? null : Integer.valueOf(freq));
		}

		return added;
	}

	public void flushPendingWrites() {
		synchronized (mPendingWritesLock) {
			// Nothing pending? Return
			DatabaseHelper helper = getDatabaseHelper(mContext);
			if (mPendingWrites.isEmpty() || helper == null)
				return;
			// Create a background thread to write the pending entries
			new UpdateDbTask(mContext, helper, getDictionaryName(),
					mPendingWrites, mLocale).execute();
			// Create a new map for writing new entries into while the old one
			// is written to db
			mPendingWrites = new HashMap<String, Integer>();
		}
	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + AUTODICT_TABLE_NAME + " (" + COLUMN_ID
					+ " INTEGER PRIMARY KEY," + COLUMN_WORD + " TEXT,"
					+ COLUMN_FREQUENCY + " INTEGER," + COLUMN_LOCALE + " TEXT"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("AutoDictionary", "Upgrading database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + AUTODICT_TABLE_NAME);
			onCreate(db);
		}
	}

	private WordsCursor query(String selection, String[] selectionArgs) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(AUTODICT_TABLE_NAME);
		qb.setProjectionMap(sDictProjectionMap);

		// Get the database and run the query
		SQLiteDatabase db = getDatabaseHelper(mContext).getReadableDatabase();
		Cursor c = qb.query(db, null, selection, selectionArgs, null, null,
				DEFAULT_SORT_ORDER);
		return new WordsCursor.SqliteWordsCursor(db, c);
	}

	/**
	 * Async task to write pending words to the database so that it stays in
	 * sync with the in-memory trie.
	 */
	private static class UpdateDbTask extends AsyncTask<Void, Void, Void> {
		private final Context mAppContext;
		private final HashMap<String, Integer> mMap;
		private final DatabaseHelper mDbHelper;
		private final String mLocale;
		private final String mDatabaseFilename;

		public UpdateDbTask(Context context, DatabaseHelper openHelper,
				String databaseFilename,
				HashMap<String, Integer> pendingWrites, String locale) {
			mAppContext = context.getApplicationContext();
			mDatabaseFilename = databaseFilename;
			mMap = pendingWrites;
			mLocale = locale;
			mDbHelper = openHelper;
		}

		@Override
		protected Void doInBackground(Void... v) {
			try// issue 952
			{
				flushToDB();
			} catch (SQLiteException e) {
				Log.w(TAG,
						"Could not access the auto-dictionary database!! Error: "
								+ e.getMessage());
				e.printStackTrace();
				try// issue 952
				{
					mAppContext.deleteDatabase(mDatabaseFilename);
					flushToDB();
				} catch (SQLiteException e2) {
					Log.w(TAG,
							"Could not delete the auto-dictionary database (failing DB)!! Error: "
									+ e2.getMessage());
					e.printStackTrace();

					if (AnyApplication.DEBUG)
						throw e2;

					return null;
				}
			}
			return null;
		}

		public void flushToDB() {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			// Write all the entries to the db
			Set<Entry<String, Integer>> mEntries = mMap.entrySet();
			for (Entry<String, Integer> entry : mEntries) {
				Integer freq = entry.getValue();
				db.delete(AUTODICT_TABLE_NAME, COLUMN_WORD + "=? AND "
						+ COLUMN_LOCALE + "=?", new String[] { entry.getKey(),
						mLocale });
				// note: any word with NULL is a deleted word (see "addWord"
				// function)
				if (freq != null) {
					db.insert(AUTODICT_TABLE_NAME, null,
							getContentValues(entry.getKey(), freq, mLocale));
				}
			}
		}

		private ContentValues getContentValues(String word, int frequency,
				String locale) {
			ContentValues values = new ContentValues(4);
			values.put(COLUMN_WORD, word);
			values.put(COLUMN_FREQUENCY, frequency);
			values.put(COLUMN_LOCALE, locale);
			return values;
		}
	}

	public String getLocale() {
		return mLocale;
	}

	@Override
	protected void closeAllResources() {
		/*
		 * NO NEED TO CLOSE THIS CONNECTION HERE. Only at process end.
		 * (msOpenHelper != null) msOpenHelper.close();
		 */
	}

	@Override
	protected void AddWordToStorage(String word, int frequency) {
	}

	@Override
	public WordsCursor getWordsCursor() {
		if (TextUtils.isEmpty(mLocale))
			return query(null, null);
		else
			return query(COLUMN_LOCALE + "=?", new String[] { mLocale });
	}

	@Override
	public void deleteWord(String word) {
	}
}
