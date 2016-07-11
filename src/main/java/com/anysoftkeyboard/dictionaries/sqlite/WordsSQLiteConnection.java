/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.dictionaries.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.anysoftkeyboard.base.dictionaries.WordsCursor;
import com.anysoftkeyboard.utils.Logger;

public class WordsSQLiteConnection extends SQLiteOpenHelper {
    private static final String TAG = "ASK SqliteCnnt";
    private final static String TABLE_NAME = "WORDS";//was FALL_BACK_USER_DICTIONARY;
    protected final Context mContext;
    private final String mCurrentLocale;
    private final String mDbName;

    public WordsSQLiteConnection(Context context, String DbFilename, String currentLocale) {
        super(context, DbFilename, null, 7);
        mContext = context;
        mCurrentLocale = currentLocale;
        mDbName = DbFilename;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        synchronized (mDbName) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + Words._ID + " INTEGER PRIMARY KEY," + Words.WORD + " TEXT," + Words.FREQUENCY + " INTEGER," + Words.LOCALE + " TEXT" + ");");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        synchronized (mDbName) {
            // Please note: don't use class level constants here, since they may
            // change.
            // if you upgrade from one version to another, make sure you use the
            // correct names!
            Logger.d(TAG, "Upgrading WordsSQLiteConnection from version " + oldVersion + " to " + newVersion + "...");
            if (oldVersion < 4) {
                Logger.d(TAG, "Upgrading WordsSQLiteConnection to version 4: Adding locale column...");
                db.execSQL("ALTER TABLE FALL_BACK_USER_DICTIONARY ADD COLUMN locale TEXT;");
            }
            if (oldVersion < 5) {
                Logger.d(TAG, "Upgrading WordsSQLiteConnection to version 5: Adding _id column and populating...");
                db.execSQL("ALTER TABLE FALL_BACK_USER_DICTIONARY ADD COLUMN _id INTEGER;");
                db.execSQL("UPDATE FALL_BACK_USER_DICTIONARY SET _id=Id;");
            }
            if (oldVersion < 6) {
                Logger.d(TAG, "Upgrading WordsSQLiteConnection to version 6: Matching schema with Android's User-Dictionary table...");
                db.execSQL("ALTER TABLE FALL_BACK_USER_DICTIONARY RENAME TO tmp_FALL_BACK_USER_DICTIONARY;");

                db.execSQL("CREATE TABLE FALL_BACK_USER_DICTIONARY (" + "_id INTEGER PRIMARY KEY,word TEXT," + "frequency INTEGER,locale TEXT);");

                db.execSQL("INSERT INTO FALL_BACK_USER_DICTIONARY(_id, word, frequency, locale) SELECT _id, Word, Freq, locale FROM tmp_FALL_BACK_USER_DICTIONARY;");

                db.execSQL("DROP TABLE tmp_FALL_BACK_USER_DICTIONARY;");
            }
            if (oldVersion < 7) {
                Logger.d(TAG, "Renaming the table's name to a generic one...");
                db.execSQL("ALTER TABLE FALL_BACK_USER_DICTIONARY RENAME TO WORDS;");
            }
        }
    }

    public void addWord(String word, int freq) {
        synchronized (mDbName) {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(Words._ID, word.hashCode());// ensuring that any word is inserted once
            values.put(Words.WORD, word);
            values.put(Words.FREQUENCY, freq);
            values.put(Words.LOCALE, mCurrentLocale);
            long res = db.insert(TABLE_NAME, null, values);
            if (res < 0) {
                Logger.e(TAG, "Unable to insert '" + word + "' to SQLite storage (" + mCurrentLocale + "@" + mDbName + ")! Result:" + res);
            }
            db.close();
        }
    }

    public void deleteWord(String word) {
        synchronized (mDbName) {
            SQLiteDatabase db = getWritableDatabase();

            db.delete(TABLE_NAME, Words.WORD + "=?", new String[]{word});
            db.close();
        }
    }

    public WordsCursor getWordsCursor() {
        synchronized (mDbName) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor c;
            if (TextUtils.isEmpty(mCurrentLocale)) {
                //some language packs will not provide locale, and Android _may_ crash here
                c = db.query(TABLE_NAME, new String[]{Words._ID, Words.WORD, Words.FREQUENCY}, "(" + Words.LOCALE + " IS NULL)", null, null, null, null);
            } else {
                c = db.query(TABLE_NAME, new String[]{Words._ID, Words.WORD, Words.FREQUENCY}, "(" + Words.LOCALE + " IS NULL) or (" + Words.LOCALE + "=?)", new String[]{mCurrentLocale}, null, null, null);
            }

            return new WordsCursor.SqliteWordsCursor(db, c);
        }
    }

    /**
     * This is a compatibility function: SQLiteOpenHelper.getDatabaseName exists only in API14
     */
    public String getDbFilename() {
        return mDbName;
    }

    public static final class Words {
        public static final java.lang.String _ID = "_id";
        public static final java.lang.String WORD = "word";
        public static final java.lang.String FREQUENCY = "frequency";
        public static final java.lang.String LOCALE = "locale";
    }
}
