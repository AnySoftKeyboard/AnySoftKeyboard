package com.menny.android.anysoftkeyboard.keyboards;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

public abstract class KeyboardProvider extends ContentProvider {
	
	private static final String TAG = "ASK KeyboardProvider";
	
	public static final String KEYBOARD_KEY_NAME_RES_ID = "KeyboardNameResId";
	public static final String KEYBOARD_KEY_ICON_RES_ID = "KeyboardIconResId";
	public static final String KEYBOARD_KEY_LAYOUT_RES_ID = "KeyboardLayoutResId";
	public static final String KEYBOARD_KEY_LAYOUT_LANDSCAPE_RES_ID = "KeyboardLandscapeLayoutResId";
	public static final String KEYBOARD_KEY_PREF_ID = "KeyboardPrefId";
	public static final String KEYBOARD_KEY_DICTIONARY = "KeyboardDefaultDictionary";
	public static final String KEYBOARD_KEY_ADDITIONAL_IS_LETTER_EXCEPTIONS = "IsLetterExceptions";
	public static final String KEYBOARD_KEY_HARD_QWERTY_TRANSLATION = "HardKeyboardQwertyTranslation";
	public static final String KEYBOARD_KEY_SORT_ORDER = "KeyboardSortOrder";
	
	public static final String[] rows = { 
		KEYBOARD_KEY_NAME_RES_ID,
		KEYBOARD_KEY_ICON_RES_ID,
		KEYBOARD_KEY_LAYOUT_RES_ID,
		KEYBOARD_KEY_LAYOUT_LANDSCAPE_RES_ID,
		KEYBOARD_KEY_PREF_ID,
		KEYBOARD_KEY_DICTIONARY,
		KEYBOARD_KEY_ADDITIONAL_IS_LETTER_EXCEPTIONS,
		KEYBOARD_KEY_HARD_QWERTY_TRANSLATION,
		KEYBOARD_KEY_SORT_ORDER
	};

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
		Log.d(TAG, "Returning keyboard data:"+this.getClass().getName());
		switch (uriMatcher.match(uri)) {
		case 4:
			MatrixCursor c = new MatrixCursor(rows);

			c.addRow(new Object[]{
					getPackageName()+":string/"+getKeyboardNameResId(),
					getPackageName()+":drawable/"+getKeyboardIconResId(),
					getPackageName()+":xml/"+getKeyboardLayoutId(),
					getPackageName()+":xml/"+getKeyboardLandscapeLayoutId(),
					getKeyboardEnabledPrefKey(),
					getDefaultDictionary(),
					getAdditionalIsLetterExceptions(),
					getHardKeyboardQwertyTranslation(),
					getKeyboardSortValue()});
			return c;
		}
		return null;
	}

	protected abstract String getPackageName();
	
	protected abstract String getKeyboardEnabledPrefKey();

	protected abstract String getKeyboardLayoutId();

	protected String getKeyboardLandscapeLayoutId()
	{
		return null;
	}
	
	protected String getKeyboardIconResId()
	{
		return null;
	}

	protected abstract String getKeyboardNameResId();

	protected abstract int getKeyboardSortValue();
	
	protected String getDefaultDictionary()
	{
		return null;
	}

	protected String getAdditionalIsLetterExceptions()
	{
		return null;
	}	
	
	protected String getHardKeyboardQwertyTranslation()
	{
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		throw new SQLException("This operation is now allowed");
	}
}

