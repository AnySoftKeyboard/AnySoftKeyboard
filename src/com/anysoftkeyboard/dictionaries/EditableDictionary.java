package com.anysoftkeyboard.dictionaries;

import android.database.Cursor;

public abstract class EditableDictionary extends Dictionary {

	protected EditableDictionary(String dictionaryName) {
		super(dictionaryName);
	}

	/**
     * Adds a word to the dictionary and makes it persistent.
     * @param word the word to add. If the word is capitalized, then the dictionary will
     * recognize it as a capitalized word when searched.
     * @param frequency the frequency of occurrence of the word. A frequency of 255 is considered
     * the highest.
     * @TODO use a higher or float range for frequency
     */
    public abstract void addWord(String word, int frequency);
    
    public abstract Cursor getWordsCursor();

	public abstract void deleteWord(String word);
}
