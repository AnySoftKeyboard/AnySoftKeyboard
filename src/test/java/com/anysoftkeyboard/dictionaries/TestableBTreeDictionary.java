package com.anysoftkeyboard.dictionaries;

import android.content.ContentResolver;
import android.content.Context;
import android.database.AbstractCursor;
import android.database.ContentObserver;

import com.anysoftkeyboard.base.dictionaries.WordsCursor;
import com.anysoftkeyboard.dictionaries.sqlite.WordsSQLiteConnection;

import java.lang.reflect.Field;

public class TestableBTreeDictionary extends BTreeDictionary{
    public static final Object[][] STORAGE = {
            {1, "hello", 255, "en"},
            {2, "AnySoftKeyboard", 255, "en"},
            {3, "phone", 200, "en"},
            {4, "thing", 200, "en"},
            {5, "she", 180, "en"},
            {6, "are", 179, "en"},
            {7, "Menny", 50, "en"},
            {8, "laptop", 40, "en"},
            {9, "gmail.com", 30, "en"},
            {10, "Android", 29, "en"},
    };

    public String wordRequestedToBeDeletedFromStorage = null;
    public String wordRequestedToAddedToStorage = null;
    public int wordFrequencyRequestedToAddedToStorage = -1;
    public boolean storageIsClosed = false;

    private Field mRootsField;

    protected TestableBTreeDictionary(String dictionaryName, Context context) throws NoSuchFieldException {
        super(dictionaryName, context);
        mRootsField = BTreeDictionary.class.getDeclaredField("mRoots");
        mRootsField.setAccessible(true);
    }

    public NodeArray getRoot() throws IllegalAccessException {
        return (NodeArray) mRootsField.get(this);
    }

    @Override
    protected void deleteWordFromStorage(String word) {
        wordRequestedToBeDeletedFromStorage = word;
    }

    @Override
    protected void registerObserver(ContentObserver dictionaryContentObserver, ContentResolver contentResolver) {

    }

    @Override
    public WordsCursor getWordsCursor() {
        storageIsClosed = false;
        return new WordsCursor(new AbstractCursor() {

            @Override
            public int getCount() {
                return STORAGE.length;
            }

            @Override
            public String[] getColumnNames() {
                return new String[]
                        {       WordsSQLiteConnection.Words._ID,
                                WordsSQLiteConnection.Words.WORD,
                                WordsSQLiteConnection.Words.FREQUENCY,
                                WordsSQLiteConnection.Words.LOCALE
                        };
            }

            @Override
            public String getString(int column) {
                return (String)STORAGE[getPosition()][column];
            }

            @Override
            public short getShort(int column) {
                return (Short)STORAGE[getPosition()][column];
            }

            @Override
            public int getInt(int column) {
                return (Integer)STORAGE[getPosition()][column];
            }

            @Override
            public long getLong(int column) {
                return (Long)STORAGE[getPosition()][column];
            }

            @Override
            public float getFloat(int column) {
                return (Float)STORAGE[getPosition()][column];
            }

            @Override
            public double getDouble(int column) {
                return (Double)STORAGE[getPosition()][column];
            }

            @Override
            public boolean isNull(int column) {
                return STORAGE[getPosition()][column] == null;
            }
        });
    }

    @Override
    protected void AddWordToStorage(String word, int frequency) {
        wordRequestedToAddedToStorage = word;
        wordFrequencyRequestedToAddedToStorage = frequency;
    }

    @Override
    protected void closeStorage() {
        storageIsClosed = true;
    }
}
