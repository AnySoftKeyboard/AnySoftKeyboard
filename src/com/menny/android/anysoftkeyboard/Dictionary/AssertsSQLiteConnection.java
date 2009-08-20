package com.menny.android.anysoftkeyboard.Dictionary;

import java.io.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

class AssertsSQLiteConnection extends DictionarySQLiteConnection {

	//The Android's default system path of your application database.
    private static final String DB_PATH = "/data/data/com.menny.android.anysoftkeyboard/databases/";

    private final String mDbName;
    // The revision is the version of the database in the assets.
    // It is taken from the metadata table of the table (wordsTableName+"_metadata").
    //If the version in this member does not match the version in the database already installed on the 
    //device, then the database on the device will be deleted, and the database from the assets will be used.
    private final int mDatabaseRevision;
    
    private SQLiteDatabase mDataBase; 
    
	protected AssertsSQLiteConnection(Context conext, String dbName, String wordsTableName, int databaseRevision) throws IOException {
		super(conext, dbName, wordsTableName, "Word", "Frequency");
		mDbName = dbName;
		mDatabaseRevision = databaseRevision;
		
		createDataBase();
	}
	
	/**
    * Creates a empty database on the system and rewrites it with your own database.
    * */
	private void createDataBase() throws IOException
	{
		boolean dbExist = checkDataBase();
		if(dbExist){
			Log.v("AnySoftKeyboard", "AssertsSQLiteConnection:createDataBase: Database exists.");
			//do nothing - database already exist
		}else{
			/*
			Log.d("AnySoftKeyboard", "AssertsSQLiteConnection:createDataBase: Database does not exist. Creating empty database file for "+mDbName);
			//By calling this method and empty database will be created into the default system path
		    //of your application so we are gonna be able to overwrite that database with our database.
		   	SQLiteDatabase newDB = super.getReadableDatabase();
		   	//now, i need to close it.
		   	newDB.close();
			*/
		   	copyDataBase();
		}
	}
	
	@Override
	public synchronized void close() {
		if(mDataBase != null)
			mDataBase.close();
		
		mDataBase = null;
		super.close();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
	}


   /**
    * Check if the database already exist to avoid re-copying the file each time you open the application.
    * @return true if it exists, false if it doesn't
    */
	private boolean checkDataBase(){
		SQLiteDatabase checkDB = null;
		boolean validDatabase = false;
		try{
			String myPath = DB_PATH + mDbName;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
			//OK. If we got here, then the database exists!
			//Lets check that it is from the correct version
			Cursor c = checkDB.query(mTableName+"_metadata", new String[]{"key", "value"}, "key like '%revision%'", null, null, null, null);
			try
			{
				if ((c != null) && (c.moveToFirst())) 
				{
					while ((!c.isAfterLast()) && (!validDatabase)) 
	                {
	                    int revision = c.getInt(1);
	                    Log.v("AnySoftKeyboard", "Found revision "+revision+" looking for "+mDatabaseRevision);
	                    if (revision == mDatabaseRevision) {
	                    	Log.d("AnySoftKeyboard", mDbName+"."+mTableName+" is of the correct revision.");
	                    	validDatabase = true;
	                    }
	                    c.moveToNext();
	                }
	            }
			}
			finally
			{
				if (c != null)
					 c.close();
			}
		}
		catch(SQLiteException e)
		{
			if(checkDB != null)
			{
				validDatabase = false;
				//database exists, but not of the correct revision
				Log.i("AnySoftKeyboard", "Failed to get metadata from "+mDbName+"."+mTableName+". Error: "+ e.getMessage());
			}
			else
			{
				//database does't exist yet.
				validDatabase = false;
				Log.i("AnySoftKeyboard", "The table "+mDbName+"."+mTableName+" does not exist in the device. Error: "+ e.getMessage());
			}
		}
		finally
		{
			if(checkDB != null)		
				checkDB.close();
		}
		
		return validDatabase;
	}

   /**
    * Copies your database from your local assets-folder to the just created empty database in the
    * system folder, from where it can be accessed and handled.
    * This is done by transfering bytestream.
    * */
   private void copyDataBase() throws IOException{

   	//Open your local db as the input stream
   	InputStream myInput = super.mContext.getAssets().open(mDbName);

   	// Path to the just created empty db
   	String outFileName = DB_PATH + mDbName;

   	Log.d("AnySoftKeyboard", "***** AssertsSQLiteConnection: About to copy DB from assets to '"+outFileName+"'. Size: "+myInput.available());
   	//Open the empty db as the output stream
   	File databaseFile = new File(outFileName);
   	if (!databaseFile.exists())
   		databaseFile.createNewFile();
   	
   	OutputStream myOutput = new FileOutputStream(outFileName, false);

   	//transfer bytes from the inputfile to the outputfile
   	byte[] buffer = new byte[1024];
   	int length;
   	while ((length = myInput.read(buffer))>0){
   		myOutput.write(buffer, 0, length);
   	}

   	//Close the streams
   	myOutput.flush();
   	myOutput.close();
   	myInput.close();
   	Log.d("AnySoftKeyboard", "***** AssertsSQLiteConnection: DB was copied!");
   }
   
   @Override
   public synchronized SQLiteDatabase getReadableDatabase() {
	   if (mDataBase != null)
		   return mDataBase;
	   
		String myPath = DB_PATH + mDbName;
		mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		return mDataBase;
   }
}
