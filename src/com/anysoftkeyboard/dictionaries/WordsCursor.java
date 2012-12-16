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