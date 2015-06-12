/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.dictionaries.sqlite;

import android.content.Context;
import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;

/**
 * Stores new words temporarily until they are promoted to the user dictionary
 * for longevity. Words in the auto dictionary are used to determine if it's ok
 * to accept a word that's not in the main or user dictionary. Using a new word
 * repeatedly will promote it to the user dictionary.
 */
public class AutoDictionary extends SQLiteUserDictionaryBase {

    protected static final String TAG = "ASK ADict";

    public enum AdditionType {
        Picked,
        Typed
    }
    // Weight added to a user picking a new word from the suggestion strip
    private static final int FREQUENCY_FOR_PICKED = 3;
    // Weight added to a user typing a new word that doesn't get corrected (or
    // is reverted)
    private static final int FREQUENCY_FOR_TYPED = 1;
    // A word that is frequently typed and gets promoted to the user dictionary,
    // uses this
    // frequency.
    private static final int AUTO_ADDED_WORDS_FREQUENCY = 178;

    /**
     * Sort by descending order of frequency.
     */
    public static final String DEFAULT_SORT_ORDER = WordsSQLiteConnection.Words.FREQUENCY + " DESC";

    public AutoDictionary(Context context, String locale) {
        super("Auto", context, locale);
    }

    @Override
    protected WordsSQLiteConnection createStorage(String locale) {
        /*I've renamed the db filename, since the previous one was in an incompatible format*/
        return new WordsSQLiteConnection(mContext, "auto_dict_2.db", locale);
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        return false;//words in the auto-dictionary are always invalid
    }

    /**
     * Adds the word to the auto-dictionary, if it was used enough times, it will be promoted to the user's dictionary
     * @param word the word to remember
     * @param type what type of addition was it
     * @return true if the word was promoted to user's dictionary.
     */
    public boolean addWord(WordComposer word, AdditionType type, AnySoftKeyboard callingIme) {
        synchronized (mResourceMonitor) {
            if (isClosed()) {
                Log.d(TAG, "Dictionary (type " + this.getClass().getName() + ") " + this.getDictionaryName() + " is closed! Can not add word.");
                return false;
            }
            final int length = word.length();
            // Don't add very short or very long words.
            if (length < 2 || length > MAX_WORD_LENGTH)
                return false;
            //ask can not be null! This should not happen (since the caller is ASK instance...)
            String wordToAdd = word.getTypedWord().toString();
            if (callingIme.getCurrentWord().isAutoCapitalized()) {
                // Remove caps before adding
                wordToAdd = Character.toLowerCase(wordToAdd.charAt(0)) + wordToAdd.substring(1);
            }
            int freq = getWordFrequency(wordToAdd);
            final int frequencyDelta = type.equals(AdditionType.Picked)? FREQUENCY_FOR_PICKED: FREQUENCY_FOR_TYPED;

            freq = freq < 0 ? frequencyDelta : freq + frequencyDelta;
            boolean added;
            if (freq >= AnyApplication.getConfig().getAutoDictionaryInsertionThreshold()) {
                Log.i(TAG, "Promoting the word " + word + " (freq " + freq
                        + ") to the user dictionary. It earned it.");
                added = callingIme.promoteToUserDictionary(wordToAdd, AUTO_ADDED_WORDS_FREQUENCY);
                deleteWord(wordToAdd);
            } else {
                super.addWord(wordToAdd, freq);
                added = false;//this means that the word was not promoted.
            }
            return added;
        }
    }

    @Override
    public boolean addWord(String word, int frequency) {
        throw new RuntimeException("You should not call addWord(String, int) in AutoDictionary! Please call addWord(WordComposer, AdditionType)");
    }
}
