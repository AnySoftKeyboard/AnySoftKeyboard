package com.menny.android.anysoftkeyboard.dictionary;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.UserDictionary.Words;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

class AndroidUserDictionary extends UserDictionaryBase {

	private static final String[] PROJECTION = {
        Words._ID,
        Words.WORD,
        Words.FREQUENCY
    };

	private static final int INDEX_WORD = 1;
    private static final int INDEX_FREQUENCY = 2;

	private ContentObserver mObserver;

    public AndroidUserDictionary(AnyKeyboardContextProvider context) throws Exception
    {
    	super("AndroidUserDictionary", context);

        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        ContentResolver cres = mContext.getContentResolver();

        cres.registerContentObserver(Words.CONTENT_URI, true, mObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean self) {
                mRequiresReload = true;
            }
        });
    }

	protected void closeAllResources() {
		if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
	}

	protected void loadAllWords() {
		Cursor cursor = mContext.getContentResolver().query(Words.CONTENT_URI, PROJECTION, null, null, null);
                		/*"(locale IS NULL) or (locale=?)",
                        new String[] { Locale.getDefault().toString() }, null);*/
        addWords(cursor);
	}

	private void addWords(Cursor cursor) {
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String word = cursor.getString(INDEX_WORD);
                int frequency = cursor.getInt(INDEX_FREQUENCY);
                // Safeguard against adding really long words. Stack may overflow due
                // to recursion
                if (word.length() < MAX_WORD_LENGTH) {
                	addWordFromStorage(word, frequency);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
    }

	protected void AddWordToStorage(String word, int frequency) {
		Words.addWord(mContext, word, frequency, Words.LOCALE_TYPE_CURRENT);
	}
}
