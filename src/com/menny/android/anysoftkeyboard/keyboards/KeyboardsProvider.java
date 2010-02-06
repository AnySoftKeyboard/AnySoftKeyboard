package com.menny.android.anysoftkeyboard.keyboards;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

public class KeyboardsProvider
{
	public static abstract class KeyboardProvider extends ContentProvider {
	
		private static final String TAG = "ASK KeyboardProvider";
		
		public static final String[] rows = { KeyboardFactory.KEYBOARD_COL_KEY, KeyboardFactory.KEYBOARD_COL_VALUE};

		private static final UriMatcher uriMatcher;

		static {
			uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
			uriMatcher.addURI("*", "*", 4);
		}
		
		@Override
		public int delete(Uri arg0, String arg1, String[] arg2) {
			throw new SQLException("This operation is now allowed");
		}

		@Override
		public String getType(Uri _uri) {
			switch (uriMatcher.match(_uri)) {
			case 4:
				return "vnd.anysoftkeyboard.keyboard.cursor.dir/vnd.anysoftkeyboard.keyboard.dir";
			default:
				throw new IllegalArgumentException("Unsupported URI: " + _uri);
			}
		}

		@Override
		public Uri insert(Uri arg0, ContentValues arg1) {
			throw new SQLException("This operation is now allowed");			
		}

		@Override
		public boolean onCreate() {		
			return true;
		}

		@Override
		public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3,
				String arg4) {
			Log.d(TAG, "Returning keyboard data.");
			switch (uriMatcher.match(uri)) {
			case 4:
				MatrixCursor c = new MatrixCursor(rows);

				c.addRow(new Object[]{
						KeyboardFactory.KEYBOARD_KEY_NAME_RES_ID, 
						"android.resource://"+getPackageName()+"/string/"+getKeyboardNameResId()});
				
				c.addRow(new Object[]{
						KeyboardFactory.KEYBOARD_KEY_Icon_RES_ID, 
						"android.resource://"+getPackageName()+"/drawable/"+getKeyboardIconResId()});

				c.addRow(new Object[]{
						KeyboardFactory.KEYBOARD_KEY_LAYOUT_RES_ID, 
						"android.resource://"+getPackageName()+"/xml/"+getKeyboardLayoutId()});
				
				c.addRow(new Object[]{
						KeyboardFactory.KEYBOARD_KEY_LAYOUT_LANDSCAPE_RES_ID, 
						"android.resource://"+getPackageName()+"/xml/"+getKeyboardLandscapeLayoutId()});
				
				c.addRow(new Object[]{
						KeyboardFactory.KEYBOARD_KEY_PREF_ID, 
						getKeyboardEnabledPrefKey()});
				
				c.addRow(new Object[]{
						KeyboardFactory.KEYBOARD_KEY_SORT_ORDER, 
						getKeyboardSortValue()});
				Log.d(TAG, "cursor3");
				return c;
			}
			return null;
		}

		protected abstract String getKeyboardLayoutId();

		protected abstract String getKeyboardLandscapeLayoutId();
		
		protected abstract String getKeyboardSortValue();

		protected abstract String getKeyboardEnabledPrefKey();

		protected abstract String getKeyboardIconResId();

		protected abstract String getKeyboardNameResId();
		
		protected abstract String getPackageName();
		
		@Override
		public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
			throw new SQLException("This operation is now allowed");
		}
	}
	
	public static class EnglishKeyboardProvider extends KeyboardProvider {
	
		public static final String AUTHORITY = "com.anysoftkeyboard.keyboard.english";
		public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/items");
		
		@Override
		protected String getKeyboardLayoutId() {
			return "qwerty";
		}

		@Override
		protected String getKeyboardLandscapeLayoutId() {
			return "qwerty";
		}	
		
		@Override
		protected String getKeyboardSortValue() {
			return "1";
		}

		@Override
		protected String getKeyboardEnabledPrefKey() {
			return "eng_keyboard";
		}

		@Override
		protected String getKeyboardIconResId() {
			return "en";
		}

		@Override
		protected String getKeyboardNameResId() {
			return "eng_keyboard";
		}
		
		@Override
		protected String getPackageName()
		{
			return "com.menny.android.anysoftkeyboard";
		}
	}
}
