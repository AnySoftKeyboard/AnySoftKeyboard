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

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteException;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.BTreeDictionary;

public abstract class SQLiteUserDictionaryBase extends BTreeDictionary {
    private static final String TAG = "SQLiteUserDictionaryBase";

    private volatile WordsSQLiteConnection mStorage;
    private final String mLocale;

    protected SQLiteUserDictionaryBase(String dictionaryName, Context context, String locale) {
        super(dictionaryName, context);
        mLocale = locale;
        Logger.d(TAG, "Created instance of %s for locale %s.", dictionaryName, locale);
    }

    public String getLocale() {
        return mLocale;
    }

    @Override
    protected void readWordsFromActualStorage(WordReadListener listener) {
        try {
            if (mStorage == null)
                mStorage = createStorage(mLocale);

            mStorage.loadWords(listener);
        } catch (SQLiteException e) {
            e.printStackTrace();
            final String dbFile = mStorage.getDbFilename();
            try {
                mStorage.close();
            } catch (SQLiteException swallow) {
            }
            Logger.w(TAG, "Caught an SQL exception while read database (message: '" + e.getMessage() + "'). I'll delete the database '" + dbFile + "'...");
            try {
                mContext.deleteDatabase(dbFile);
            } catch (Exception okToFailEx) {
                Logger.w(TAG, "Failed to delete database file " + dbFile + "!");
                okToFailEx.printStackTrace();
            }
            mStorage = null;// will re-create the storage.
            mStorage = createStorage(mLocale);
            //if this function will throw an exception again, well the hell with it.
            mStorage.loadWords(listener);
        }
    }

    protected WordsSQLiteConnection createStorage(String locale) {
        return new WordsSQLiteConnection(mContext, getDictionaryName() + ".db", locale);
    }

    @Override
    protected final void addWordToStorage(String word, int frequency) {
        if (mStorage != null)
            mStorage.addWord(word, frequency);
    }

    @Override
    protected final void deleteWordFromStorage(String word) {
        if (mStorage != null)
            mStorage.deleteWord(word);
    }

    @Override
    protected final void registerObserver(ContentObserver dictionaryContentObserver, ContentResolver contentResolver) {
        //nothing to do here, the storage is internal and cannot be changed from the outside.
    }

    @Override
    protected void closeStorage() {
        if (mStorage != null)
            mStorage.close();
        mStorage = null;
    }
}
