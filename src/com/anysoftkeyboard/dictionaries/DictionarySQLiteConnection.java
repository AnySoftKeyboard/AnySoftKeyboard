package com.anysoftkeyboard.dictionaries;

import java.util.ArrayList;
import java.util.List;

import com.menny.android.anysoftkeyboard.AnyApplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DictionarySQLiteConnection extends SQLiteOpenHelper
{
	private static final String TAG = "ASK DictSql";
	
	public static class DictionaryWord
	{
		private final String mWord;
		private final int mFrequency;

		public DictionaryWord(String word, int freq)
		{
			if (word == null)
			{
				Log.e(TAG, "Got a NULL word from dictionary! This is illegal!");
				word = "" + this.hashCode();
			}
			mWord = word;
			mFrequency = freq;
		}

		public String getWord() {return mWord;}
		public int getFrequency() {return mFrequency;}
	}

	private final String mDBFile;
	protected final String mTableName;
	protected final String mWordsColumnName;
	protected final String mFrequencyColumnName;
	protected final String mLocaleColumnName;
	protected final Context mContext;
	private final String mCurrentLocale;

	public DictionarySQLiteConnection(Context context, String dbName, String tableName, String wordsColumnName, String frequencyColumnName, String localeColumnName, String currentLocale) {
		super(context, dbName, null, 5);
		mDBFile = dbName;
		mContext = context;
		mTableName = tableName;
		mWordsColumnName = wordsColumnName;
		mFrequencyColumnName =frequencyColumnName;
		mLocaleColumnName = localeColumnName;
		mCurrentLocale = currentLocale;
	}

	@Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + mTableName + " ("
                + "_id INTEGER PRIMARY KEY,"
                + mWordsColumnName+" TEXT,"
                + mFrequencyColumnName+" INTEGER,"
                + mLocaleColumnName+" TEXT"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4)
        {
        	db.execSQL("ALTER TABLE "+mTableName+" ADD COLUMN "+mLocaleColumnName+" TEXT;");
        }
        if (oldVersion < 5)
        {
        	db.execSQL("ALTER TABLE "+mTableName+" ADD COLUMN _id TEXT;");
        	db.execSQL("UPDATE "+mTableName+" SET _id=Id;");
        }
    }

    public synchronized void addWord(String word, int freq)
    {
    	SQLiteDatabase db = getWritableDatabase();

    	ContentValues values = new ContentValues();
    	values.put("_id", word.hashCode());//ensuring that any word is inserted once
    	values.put(mWordsColumnName, word);
    	values.put(mFrequencyColumnName, freq);
    	values.put(mLocaleColumnName, mCurrentLocale);
		long res = db.insert(mTableName, null, values);
		if (res < 0)
		{
			Log.e(TAG, "Unable to insert '"+word+"' to the fall-back dictionary! Result:"+res);
		}
		else
		{
		    if (AnyApplication.DEBUG)Log.d(TAG, "Inserted '"+word+"' to the fall-back dictionary. Id:"+res);
		}
		
		db.close();
    }
    
    public synchronized void deleteWord(String word)
    {
    	SQLiteDatabase db = getWritableDatabase();

    	db.delete(mTableName, mWordsColumnName+"=?", new String[]{word});
    	
		db.close();
    }
    
    public Cursor getWordsCursor(){
    	SQLiteDatabase db = getReadableDatabase();
    	Cursor c = db.query(mTableName, new String[]{mWordsColumnName, mFrequencyColumnName}, 
	    		"("+mLocaleColumnName+" IS NULL) or ("+mLocaleColumnName+"=?)", new String[] { mCurrentLocale },
	    		null,null,null);
    	db.close();
    	return c;
    }

    public synchronized List<DictionaryWord> getAllWords()
    {
    	Cursor c = getWordsCursor();

    	if (c != null)
    	{
    		List<DictionaryWord> words = new ArrayList<DictionaryWord>(c.getCount());
        	if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    String word = c.getString(0);
                    int freq = c.getInt(1);
                    words.add(new DictionaryWord(word.toLowerCase(), freq));
                    c.moveToNext();
                }
            }
        	c.close();
        	return words;
    	}
    	else
    		return new ArrayList<DictionaryWord>(0);
    }
    
    String getDatabaseFile()
    {
    	return mDBFile;
    }
}
