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
import com.anysoftkeyboard.dictionaries.BTreeDictionary;
import com.anysoftkeyboard.dictionaries.WordsCursor;
import com.anysoftkeyboard.utils.Log;

public abstract class SQLiteUserDictionaryBase extends BTreeDictionary {

    private WordsSQLiteConnection mStorage;
    private final String mLocale;

    protected SQLiteUserDictionaryBase(String dictionaryName, Context context, String locale) {
        super(dictionaryName, context);
        mLocale = locale;
    }

    public String getLocale() {
        return mLocale;
    }

    @Override
    public final WordsCursor getWordsCursor() {
        try {
            if (mStorage == null)
                mStorage = createStorage(mLocale);
            
            return mStorage.getWordsCursor();
        } catch (SQLiteException e) {
            e.printStackTrace();
            final String dbFile = mStorage.getDatabaseName();
            try {
                mStorage.close();
            } catch (SQLiteException swallow) {}
            Log.w(TAG, "Caught an SQL exception while read database (message: '" + e.getMessage() + "'). I'll delete the database '" + dbFile + "'...");
            mContext.deleteDatabase(dbFile);
            mStorage = null;// will re-create the storage.
            mStorage = createStorage(mLocale);
            //if this function will throw an exception again, well the hell with it.
            return mStorage.getWordsCursor();
        }
    }

    protected abstract WordsSQLiteConnection createStorage(String locale);

    @Override
    protected final void AddWordToStorage(String word, int frequency) {
        mStorage.addWord(word, frequency);
    }

    @Override
    protected final void deleteWordFromStorage(String word) {
        mStorage.deleteWord(word);
    }

    @Override
    protected final void registerObserver(ContentObserver dictionaryContentObserver, ContentResolver contentResolver) {
        //nothing to do here, the storage is internal and cannot be changed from the outside.
    }
}
