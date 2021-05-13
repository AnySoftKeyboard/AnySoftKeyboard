package com.anysoftkeyboard.dictionaries;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.support.v4.util.Pair;
import java.util.ArrayList;
import java.util.Collection;

public class InMemoryDictionary extends BTreeDictionary {

    private final ArrayList<Pair<String, Integer>> mWords;

    public InMemoryDictionary(
            String dictionaryName,
            Context context,
            Collection<Pair<String, Integer>> words,
            boolean includeTypedWord) {
        super(dictionaryName, context, includeTypedWord);
        mWords = new ArrayList<>(words);
    }

    @Override
    protected void readWordsFromActualStorage(WordReadListener wordReadListener) {
        for (Pair<String, Integer> word : mWords) {
            if (!wordReadListener.onWordRead(word.first, word.second)) break;
        }
    }

    @Override
    protected void deleteWordFromStorage(String word) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void registerObserver(
            ContentObserver dictionaryContentObserver, ContentResolver contentResolver) {}

    @Override
    protected void addWordToStorage(String word, int frequency) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void closeStorage() {
        mWords.clear();
    }
}
