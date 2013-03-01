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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WordsCursor {
    private final Cursor mCursor;

    protected WordsCursor(Cursor cursor) {
        mCursor = cursor;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void close() {
        if (!mCursor.isClosed()) mCursor.close();
    }

    public static class SqliteWordsCursor extends WordsCursor {
        private final SQLiteDatabase mDb;

        SqliteWordsCursor(SQLiteDatabase db, Cursor cursor) {
            super(cursor);
            mDb = db;
        }

        @Override
        public void close() {
            super.close();
            if (mDb.isOpen())
                mDb.close();
        }
    }
}