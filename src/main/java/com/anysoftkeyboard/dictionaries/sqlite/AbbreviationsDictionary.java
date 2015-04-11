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
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.anysoftkeyboard.WordComposer;
import com.anysoftkeyboard.dictionaries.Dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbbreviationsDictionary extends SQLiteUserDictionaryBase {

    private static final int ABBR_MAX_WORD_LENGTH = 2048;

    private final Map<CharSequence, List<String>> mAbbreviationsMap = new HashMap<>();

    public AbbreviationsDictionary(Context context, String locale) {
        super("AbbreviationsDictionary", context, locale);
    }

    @Override
    protected WordsSQLiteConnection createStorage(String locale) {
        return new WordsSQLiteConnection(mContext, "abbreviations.db", locale);
    }

    @Override
    protected int getMaxWordLength() {
        return ABBR_MAX_WORD_LENGTH;
    }

    @Override
    public void getWords(WordComposer codes, WordCallback callback) {
        if (isClosed() || isLoading()) return;

        String word = codes.getTypedWord().toString();
        reportExplodedWords(callback, word);

        if (codes.isFirstCharCapitalized()) {
            String nonCapitalizedWord = toLowerCase(word.charAt(0))+(word.length() > 1? word.substring(1) : "");
            reportExplodedWords(callback, nonCapitalizedWord);
        }
    }

    private void reportExplodedWords(WordCallback callback, String word) {
        List<String> explodedStringsList = mAbbreviationsMap.get(word);
        if (explodedStringsList != null) {
            for(String explodedString : explodedStringsList)
                callback.addWord(explodedString.toCharArray(), 0, explodedString.length(), MAX_WORD_FREQUENCY, this);
        }
    }

    @Override
    protected void addWordFromStorage(String word, int frequency) {
        //not double storing the words in memory, so I'm not calling the super method
        String key = getAbbreviation(word, frequency);
        String value = getExplodedSentence(word, frequency);
        if (mAbbreviationsMap.containsKey(key)) {
            mAbbreviationsMap.get(key).add(value);
        } else {
            List<String> list = new ArrayList<>(1);
            list.add(value);
            mAbbreviationsMap.put(key, list);
        }
    }

    public static String getAbbreviation(@NonNull String word, int frequency) {
        return word.substring(0, frequency);
    }

    public static String getExplodedSentence(@NonNull String word, int frequency) {
        return word.substring(frequency);
    }
}
