//package com.menny.android.anysoftkeyboard.Dictionary;
//
//import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
//
//public class SQLiteSimpleDictionary extends SQLiteUserDictionaryBase {
//
//	private final String mDbName;
//	private final String mTableName;
//	
//	public SQLiteSimpleDictionary(AnyKeyboardContextProvider anyContext, String dbName, String tableName) throws Exception {
//		super(anyContext);
//		mDbName = dbName;
//		mTableName = tableName;
//	}
//	
//	@Override
//	protected DictionarySQLiteConnection createStorage() {
//		try {
//			return new AssertsSQLiteConnection(mContext, mDbName, mTableName);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new DictionarySQLiteConnection(mContext, mDbName, mTableName, "Word", "Frequency");
//		}
//	}
//	
//	@Override
//	public synchronized void addWord(String word, int frequency) {
//		//does nothing
//	}
//
//	@Override
//	public String toString() {
//		return "SQLiteSimpleDictionary("+mDbName+"."+mTableName+")";
//	}
//}
