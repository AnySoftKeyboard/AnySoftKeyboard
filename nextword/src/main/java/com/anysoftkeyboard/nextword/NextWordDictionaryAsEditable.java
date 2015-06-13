package com.anysoftkeyboard.nextword;

import android.content.Context;

import com.anysoftkeyboard.base.dictionaries.EditableDictionary;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.base.dictionaries.WordsCursor;

public class NextWordDictionaryAsEditable extends EditableDictionary {

    private final NextWordDictionary mNextWordDictionary;

    public NextWordDictionaryAsEditable(Context context, String locale) {
        super("NextWordDictionaryAsEditable_"+locale);
        mNextWordDictionary = new NextWordDictionary(context, locale);
    }

    @Override
    public boolean addWord(String word, int frequency) {
        return false;
    }

    @Override
    public WordsCursor getWordsCursor() {
        return null;
    }

    @Override
    public void deleteWord(String word) {

    }

    @Override
    public void getWords(WordComposer composer, WordCallback callback) {

    }

    @Override
    public boolean isValidWord(CharSequence word) {
        return false;
    }

    @Override
    protected void closeAllResources() {

    }

    @Override
    protected void loadAllResources() {

    }
}
