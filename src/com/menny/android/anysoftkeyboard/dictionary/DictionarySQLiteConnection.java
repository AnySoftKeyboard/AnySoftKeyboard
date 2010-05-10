package com.menny.android.anysoftkeyboard.dictionary;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DictionarySQLiteConnection extends SQLiteOpenHelper
{
	public static class DictionaryWord
	{
		private final String mWord;
		private final int mFrequency;

		public DictionaryWord(String word, int freq)
		{
			if (word == null)
			{
				Log.e("AnySoftKeyboard", "Got a NULL word from dictionary! This is illegal!");
				word = "" + this.hashCode();
			}
			mWord = word;
			mFrequency = freq;
		}

		public String getWord() {return mWord;}
		public int getFrequency() {return mFrequency;}
	}

	protected final String mTableName;
	protected final String mWordsColumnName;
	protected final String mFrequencyColumnName;
	protected final Context mContext;

	public DictionarySQLiteConnection(Context context, String dbName, String tableName, String wordsColumnName, String frequencyColumnName) {
		super(context, dbName, null, 3);
		mContext = context;
		mTableName = tableName;
		mWordsColumnName = wordsColumnName;
		mFrequencyColumnName =frequencyColumnName;
	}

	@Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + mTableName + " ("
                + "Id INTEGER PRIMARY KEY,"
                + mWordsColumnName+" TEXT,"
                + mFrequencyColumnName+" INTEGER"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+mTableName);
        onCreate(db);
    }

    public void addWord(String word, int freq)
    {
    	SQLiteDatabase db = super.getWritableDatabase();

    	ContentValues values = new ContentValues();
    	values.put("Id", word.hashCode());//ensuring that any word is inserted once
    	values.put(mWordsColumnName, word);
    	values.put(mFrequencyColumnName, freq);
		long res = db.insert(mTableName, null, values);
		if (res < 0)
		{
			Log.e("AnySoftKeyboard", "Unable to insert '"+word+"' to the fall-back dictionary! Result:"+res);
		}
		else
		{
			Log.d("AnySoftKeyboard", "Inserted '"+word+"' to the fall-back dictionary. Id:"+res);
		}
    }

    public List<DictionaryWord> getAllWords()
    {
    	//starting with a big storage
    	List<DictionaryWord> words = new ArrayList<DictionaryWord>(5000);
    	SQLiteDatabase db = getReadableDatabase();
    	Cursor c = db.query(mTableName, new String[]{mWordsColumnName, mFrequencyColumnName}, null, null, null, null, null);

    	if (c != null)
    	{
        	if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    String word = c.getString(0);
                    int freq = c.getInt(1);
                    words.add(new DictionaryWord(word.toLowerCase(), freq));
                    c.moveToNext();
                }
            }
        	c.close();
    	}

    	return words;
    }
}
