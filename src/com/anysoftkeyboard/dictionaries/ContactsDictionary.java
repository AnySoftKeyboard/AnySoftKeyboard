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

package com.anysoftkeyboard.dictionaries;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.ContactsContract.Contacts;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;

import java.util.HashMap;
import java.util.Map.Entry;

@TargetApi(5)
public class ContactsDictionary extends UserDictionaryBase {

    protected static final String TAG = "ASK CDict";

    private static final String[] PROJECTION = {Contacts._ID,
            Contacts.DISPLAY_NAME, Contacts.STARRED, Contacts.TIMES_CONTACTED};

    private static final int INDEX_NAME = 1;
    private static final int INDEX_STARRED = 2;
    private static final int INDEX_TIMES = 3;

    private ContentObserver mObserver;

    private int mContactsCount = 0;

    private long mContactsHash = 0;

    public ContactsDictionary(Context context) throws Exception {
        super("ContactsDictionary", context);
        // Perform a managed query. The Activity will handle closing and
        // re-querying the cursor
        // when needed.
        ContentResolver cres = mContext.getContentResolver();
        // registering
        Log.d(TAG, "Registering to contants changes at " + Contacts.CONTENT_URI);
        cres.registerContentObserver(Contacts.CONTENT_URI, true,
                mObserver = createContactContectObserver());
    }

    protected ContentObserver createContactContectObserver() {
        return new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                Log.d(TAG, "Contacts list modified (self: " + selfChange
                        + "). Reloading...");
                super.onChange(selfChange);
                loadDictionary();
            }

            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }
        };
    }

    @Override
    protected synchronized void loadDictionaryAsync() {
        Log.d(TAG, "Starting load of contact names...");
        Cursor cursor = null;
        try {
            // a bit less contacts for memory stress reduction
            cursor = mContext.getContentResolver().query(Contacts.CONTENT_URI,
                    PROJECTION, Contacts.IN_VISIBLE_GROUP + "=?",
                    new String[]{"1"}, null);
            if (cursor != null) {
                int newCount = 0;
                long newHash = 0;
                // first checking if something has changed
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        String name = cursor.getString(INDEX_NAME);
                        if (name != null) {
                            newHash += name.hashCode();
                            newCount++;
                        }
                        cursor.moveToNext();
                    }
                }

                Log.d(TAG, "I noticed " + newCount + " contacts.");
                if (newCount == mContactsCount && newHash == mContactsHash) {
                    Log.d(TAG, "No new data in the contacts lists, I'll skip.");
                    return;
                }

                Log.d(TAG,
                        "Contacts will be reloaded since count or hash changed. New count "
                                + newCount + " was(" + mContactsCount
                                + "), new hash " + newHash + " (was "
                                + mContactsHash + ").");
                mContactsCount = newCount;
                mContactsHash = newHash;

                clearDictionary();
                int loadedContacts = 0;
                final int maxWordLength = MAX_WORD_LENGTH;
                HashMap<String, Integer> names = new HashMap<String, Integer>(
                        mContactsCount);
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        String name = cursor.getString(INDEX_NAME);

                        if (name != null) {
                            int len = name.length();

                            // TODO: Better tokenization for non-Latin writing
                            // systems
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
                                    if (wordLen < maxWordLength && wordLen > 1) {
                                        final boolean isStarred = cursor
                                                .getInt(INDEX_STARRED) > 0;
                                        final int timesContacted = cursor
                                                .getInt(INDEX_TIMES);
                                        loadedContacts++;
                                        int freq = 1;
                                        if (isStarred)
                                            freq = 255;// WOW! important!
                                        else if (timesContacted > 100)
                                            freq = 128;
                                        else if (timesContacted > 10)
                                            freq = 32;
                                        else if (timesContacted > 1)
                                            freq = 16;

                                        if (names.containsKey(word)) {
                                            // this word is already in the list
                                            // should we update its freq?
                                            int oldFreq = names.get(word);
                                            // if a name is really popular, then
                                            // it should reflect that
                                            freq += oldFreq;
                                            if (AnyApplication.DEBUG)
                                                Log.d(TAG,
                                                        "The contact part "
                                                                + word
                                                                + " get get a better freq (was "
                                                                + oldFreq
                                                                + ", and can be "
                                                                + freq
                                                                + "). Updating.");
                                            names.put(word, freq);
                                        } else {
                                            if (AnyApplication.DEBUG)
                                                Log.d(TAG,
                                                        "Contact '"
                                                                + word
                                                                + "' will be added to contacts dictionary with freq "
                                                                + freq);
                                            names.put(word, freq);
                                        }
                                    }
                                }
                            }
                        }

                        cursor.moveToNext();
                    }
                }

                // actually adding the words
                for (Entry<String, Integer> wordFreq : names.entrySet()) {
                    addWordFromStorage(wordFreq.getKey(), wordFreq.getValue());
                }

                Log.i(TAG, "Loaded " + loadedContacts
                        + " words which were made up from your contacts list.");
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Contacts DB is having problems");
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    @Override
    protected synchronized void closeAllResources() {
        if (mObserver != null) {
            if (AnyApplication.DEBUG)
                Log.d(TAG, "Unregisterring from contacts change notifications.");
            mContext.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
    }

    @Override
    protected synchronized void AddWordToStorage(String word, int frequency) {
    }

    @Override
    public synchronized WordsCursor getWordsCursor() {
        return null;
    }

    @Override
    public synchronized void deleteWord(String word) {
    }
}
