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

import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;

/**
 * Stores new words temporarily until they are promoted to the user dictionary
 * for longevity. Words in the auto dictionary are used to determine if it's ok
 * to accept a word that's not in the main or user dictionary. Using a new word
 * repeatedly will promote it to the user dictionary.
 */
public class AutoDictionary extends SQLiteUserDictionaryBase {

    protected static final String TAG = "ASK ADict";

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

    @Override
    public boolean addWord(String word, int frequencyDelta) {
        synchronized (mResourceMonitor) {
            if (isClosed()) {
                return false;
            }
            final int length = word.length();
            // Don't add very short or very long words.
            if (length < 2 || length > MAX_WORD_LENGTH)
                return false;
            //ask can not be null! This should not happen (since the caller is ASK instance...)
            int freq = getWordFrequency(word);

            freq = freq < 0 ? frequencyDelta : freq + frequencyDelta;
            if (freq >= AnyApplication.getConfig().getAutoDictionaryInsertionThreshold()) {
                Logger.i(TAG, "Promoting the word '%s' to the user dictionary. It earned it.", word);
                //no need for this word in this dictionary any longer
                deleteWord(word);
                return true;
            } else {
                //this means that the word was not promoted.
                super.addWord(word, freq);
                return false;
            }
        }
    }

}
