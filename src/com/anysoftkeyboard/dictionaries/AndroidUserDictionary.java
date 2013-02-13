package com.anysoftkeyboard.dictionaries;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.UserDictionary.Words;
import android.text.TextUtils;
import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyApplication;

public class AndroidUserDictionary extends UserDictionaryBase {

	private final class UserDictionaryContentObserver extends
			ContentObserver {
		private UserDictionaryContentObserver() {
			super(null);
		}

		@Override
		public void onChange(boolean self) {
			mRequiresReload = true;
		}
	}

	private static final String[] PROJECTION = { Words._ID, Words.WORD,
			Words.FREQUENCY };

	private static final int INDEX_WORD = 1;
	private static final int INDEX_FREQUENCY = 2;

	private ContentObserver mObserver;
	private final String mLocale;

	public AndroidUserDictionary(Context context, String locale) {
		super("AndroidUserDictionary", context);
		mLocale = locale;
	}

	protected void closeAllResources() {
		closeObserver();
	}

	private void closeObserver() {
		if (mObserver != null) {
			mContext.getContentResolver().unregisterContentObserver(mObserver);
			mObserver = null;
		}
	}

	@Override
	public void loadDictionary() {
		// NOT doing it async, why? because my parent (SafeUserDictionary) is
		// doing it async
		loadDictionaryAsync();
	}

	protected void loadDictionaryAsync() {
		WordsCursor cursor = getWordsCursor();

		addWords(cursor.getCursor());

		cursor.close();

		// Perform a managed query. The Activity will handle closing and
		// requerying the cursor
		// when needed.
		closeObserver();
		ContentResolver cres = mContext.getContentResolver();
		mObserver = new UserDictionaryContentObserver();
		cres.registerContentObserver(Words.CONTENT_URI, true, mObserver);
	}

	public WordsCursor getWordsCursor() {
		Cursor cursor = TextUtils.isEmpty(mLocale) ? mContext
				.getContentResolver().query(Words.CONTENT_URI, PROJECTION,
						null, null, null) : mContext.getContentResolver()
				.query(Words.CONTENT_URI,
						PROJECTION,
						"(" + Words.LOCALE + " IS NULL) or (" + Words.LOCALE
								+ "=?)", new String[] { mLocale }, null);

		if (cursor == null)
			throw new RuntimeException("No built-in Android dictionary!");
		
		return new WordsCursor(cursor);
	}

	private void addWords(Cursor cursor) {
		if (cursor.moveToFirst()) {
			Log.d(TAG, "About to load " + cursor.getCount()
					+ " rows from Android's User Dictionary of locale "
					+ mLocale + "...");
			while (!cursor.isAfterLast()) {
				String word = cursor.getString(INDEX_WORD);
				int frequency = cursor.getInt(INDEX_FREQUENCY);
				// Safeguard against adding really long words. Stack may
				// overflow due
				// to recursion
				if (word.length() < MAX_WORD_LENGTH) {
					addWordFromStorage(word, frequency);
				}
				cursor.moveToNext();
				if (AnyApplication.DEBUG) {
					if (cursor.getPosition() % 25 == 0) {
						Log.d(TAG, "Read " + cursor.getPosition() + " out of "
								+ cursor.getCount() + " words.");
					}
				}
			}
		}
	}

	protected void AddWordToStorage(String word, int frequency) {
		if (TextUtils.isEmpty(word)) {
			return;
		}

		if (frequency < 1)
			frequency = 1;
		if (frequency > 255)
			frequency = 255;

		ContentValues values = new ContentValues(4);
		values.put(Words.WORD, word);
		values.put(Words.FREQUENCY, frequency);
		values.put(Words.LOCALE, mLocale);
		values.put(Words.APP_ID, 0); // TODO: Get App UID

		Uri result = mContext.getContentResolver().insert(Words.CONTENT_URI,
				values);
		Log.i(TAG, "Added the word '" + word + "' at locale " + mLocale
				+ " into Android's user dictionary. Result " + result);
		// Words.addWord(mContext, word, frequency, Words.LOCALE_TYPE_CURRENT);
	}

	@Override
	public void deleteWord(String word) {
		mContext.getContentResolver().delete(Words.CONTENT_URI,
				Words.WORD + "=?", new String[] { word });
		reloadDictionary();
	}
}
