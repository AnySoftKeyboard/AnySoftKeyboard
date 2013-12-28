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

package com.anysoftkeyboard.dictionaries.content;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.ContactsContract.Contacts;

import com.anysoftkeyboard.dictionaries.BTreeDictionary;
import com.anysoftkeyboard.dictionaries.WordsCursor;

@TargetApi(7)
public class ContactsDictionary extends BTreeDictionary {

    /**
     * A contact is a valid word in a language, and it usually very frequent.
     */
    private static final int MINIMUM_CONTACT_WORD_FREQUENCY = 64;

    private static final class ContactsWordsCursor extends WordsCursor {

        public ContactsWordsCursor(Cursor cursor) {
            super(cursor);
        }

        @Override
        public int getCurrentWordFrequency() {
            //in contacts, the frequency is a bit tricky:
            //stared contacts are really high
            Cursor cursor = getCursor();
            final boolean isStarred = cursor.getInt(INDEX_STARRED) > 0;
            if (isStarred)
                return MAX_WORD_FREQUENCY;// WOW! important!
            //times contacted will be our frequency
            final int frequencyContacted = cursor.getInt(INDEX_TIMES);
            //A contact is a valid word in a language, and it usually very frequent.
            final int minimumAdjustedFrequencyContacted = Math.max(MINIMUM_CONTACT_WORD_FREQUENCY, frequencyContacted);
            //but no more than the max allowed
            return Math.min(minimumAdjustedFrequencyContacted, MAX_WORD_FREQUENCY);
        }
    }

    protected static final String TAG = "ASK CDict";

    private static final String[] PROJECTION = {Contacts._ID, Contacts.DISPLAY_NAME, Contacts.STARRED, Contacts.TIMES_CONTACTED};

    private static final int INDEX_STARRED = 2;
    private static final int INDEX_TIMES = 3;

    public ContactsDictionary(Context context) {
        super("ContactsDictionary", context);
    }

    @Override
    protected void registerObserver(ContentObserver dictionaryContentObserver, ContentResolver contentResolver) {
        contentResolver.registerContentObserver(Contacts.CONTENT_URI, true, dictionaryContentObserver);
    }

    @Override
    public WordsCursor getWordsCursor() {
        Cursor cursor = mContext.getContentResolver().query(Contacts.CONTENT_URI,
                PROJECTION, Contacts.IN_VISIBLE_GROUP + "=?",
                new String[]{"1"}, null);
        return new ContactsWordsCursor(cursor);
    }

    @Override
    protected void addWordFromStorage(String name, int frequency) {
        //the word in Contacts is actually the full name,
        //so, let's break it to individual words.
        int len = name.length();

        // TODO: Better tokenization for non-Latin writing systems
        for (int i = 0; i < len; i++) {
            if (Character.isLetter(name.charAt(i))) {
                int j;
                for (j = i + 1; j < len; j++) {
                    char c = name.charAt(j);

                    if (!(c == '-' || c == '\'' || Character
                            .isLetter(c))) {
                        break;
                    }
                }

                String word = name.substring(i, j);
                i = j - 1;

                // Safeguard against adding really long
                // words. Stack
                // may overflow due to recursion
                // Also don't add single letter words,
                // possibly confuses
                // capitalization of i.
                final int wordLen = word.length();
                if (wordLen < MAX_WORD_LENGTH && wordLen > 1) {
                    int oldFrequency = getWordFrequency(word);
                    if (oldFrequency < frequency)//I had it better!
                        super.addWordFromStorage(word, frequency);
                }
            }
        }
    }

    @Override
    protected void deleteWordFromStorage(String word) {
        //not going to support deletion of contacts!
    }

    @Override
    protected void AddWordToStorage(String word, int frequency) {
        //not going to support addition of contacts!
    }

    @Override
    protected void closeStorage() {
        /*nothing to close here*/
    }
}
