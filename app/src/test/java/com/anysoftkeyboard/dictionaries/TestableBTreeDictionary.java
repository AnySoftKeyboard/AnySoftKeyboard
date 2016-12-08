package com.anysoftkeyboard.dictionaries;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.base.dictionaries.LoadedWord;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TestableBTreeDictionary extends BTreeDictionary {
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

    @NonNull
    @Override
    protected List<LoadedWord> readWordsFromActualStorage() {
        List<LoadedWord> loadedWords = new ArrayList<>();
        for (Object[] row : STORAGE) {
            loadedWords.add(new LoadedWord((String) row[1], (int) row[2]));
        }

        return loadedWords;
    }

    @Override
    protected void addWordToStorage(String word, int frequency) {
        wordRequestedToAddedToStorage = word;
        wordFrequencyRequestedToAddedToStorage = frequency;
    }

    @Override
    protected void closeStorage() {
        storageIsClosed = true;
    }
}
