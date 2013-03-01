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

package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import com.anysoftkeyboard.dictionaries.DictionarySQLiteConnection.DictionaryWord;
import com.anysoftkeyboard.utils.Log;

import java.util.List;

public abstract class SQLiteUserDictionaryBase extends UserDictionaryBase {

    private DictionarySQLiteConnection mStorage;
    private boolean mInDatabaseFileRecovery = false;

    protected SQLiteUserDictionaryBase(String dictionaryName, Context context) {
        super(dictionaryName, context);
    }

    @Override
    protected synchronized void loadDictionaryAsync() {
        try {
            if (mStorage == null)
                mStorage = createStorage();
            // taking time for storage load.
            long loadStartTime = System.currentTimeMillis();
            List<DictionaryWord> words = mStorage.getAllWords();
            long loadEndTime = System.currentTimeMillis();
            Log.d(TAG, "SQLite dictionary loaded " + words.size()
                    + " words. Took " + (loadEndTime - loadStartTime) + " ms.");
            for (DictionaryWord word : words) {
                addWordFromStorage(word.getWord(), word.getFrequency());
            }
            long storeEndTime = System.currentTimeMillis();
            Log.d(TAG, "Stored " + words.size() + " words in dictionary. Took "
                    + (storeEndTime - loadEndTime) + " ms.");
            /*
			 * calling GC here, will stop the device for even longer time. //we
			 * just finished working with a lot of memory. //lets release it.
			 * System.gc();
			 */
        } catch (SQLiteException e) {
            e.printStackTrace();
            if (mInDatabaseFileRecovery)// this will make sure the recursion
                // happens once.
                throw e;
            mInDatabaseFileRecovery = true;
            if (mStorage != null) {
                String dbFile = mStorage.getDatabaseFile();
                Log.w(TAG,
                        "Caught an SQL exception while read database (message: '"
                                + e.getMessage()
                                + "'). I'll delete the database '" + dbFile
                                + "'...");
                mContext.deleteDatabase(dbFile);
                mStorage = null;// will re-create the storage.
                loadDictionaryAsync();
            }
        }
    }

    @Override
    public synchronized WordsCursor getWordsCursor() {
        Log.d(TAG, "getWordsCursor");
        if (mStorage == null) {
            Log.d(TAG, "getWordsCursor::createStorage");
            mStorage = createStorage();
        }
        return mStorage.getWordsCursor();
    }

    protected abstract DictionarySQLiteConnection createStorage();

    @Override
    protected synchronized void AddWordToStorage(String word, int frequency) {
        mStorage.addWord(word, frequency);
    }

    @Override
    public synchronized void deleteWord(String word) {
        mStorage.deleteWord(word);
        reloadDictionary();
    }

    @Override
    protected synchronized void closeAllResources() {
        if (mStorage != null)
            mStorage.close();
    }

}
