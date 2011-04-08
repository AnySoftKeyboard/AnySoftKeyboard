/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.AnySoftKeyboardConfiguration;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

public class ContactsDictionary extends UserDictionaryBase {
    
    protected static final String TAG = "ASK CDict";
    
    private static final String[] PROJECTION = {
        Contacts._ID,
        Contacts.DISPLAY_NAME
    };

    private static final int INDEX_NAME = 1;

    private ContentObserver mObserver;

	private int mContactsCount = 0;

	private long mContactsHash = 0;
    
    public ContactsDictionary(AnyKeyboardContextProvider context) throws Exception {
        super("ContactsDictionary",context);
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        ContentResolver cres = mContext.getContentResolver();

        cres.registerContentObserver(Contacts.CONTENT_URI, true, mObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean self) {
                if (AnySoftKeyboardConfiguration.DEBUG)Log.d(TAG, "Contacts list modified (self: "+self+"). Reloading...");
                //mRequiresReload = true;
                loadDictionaryAsync();
            }
        });
    }

    
    @Override
    protected void loadDictionaryAsync() {
    	try{
        Cursor cursor = mContext.getContentResolver()
                .query(Contacts.CONTENT_URI, PROJECTION, Contacts.IN_VISIBLE_GROUP+"="+1, null, null);
        if (cursor != null) {
            addWords(cursor);
        }
        } catch(IllegalStateException e) {
            Log.e(TAG, "Contacts DB is having problems");
        }
    }
    

    private void addWords(Cursor cursor) {
    	int newCount = 0;
    	long newHash = 0;
    	//first checking if something has changed
    	if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(INDEX_NAME);
                if(name != null){
                    newHash += name.hashCode();
                    newCount++;
                }
                cursor.moveToNext();
            }
    	}
    	
    	if (newCount == mContactsCount  && newHash == mContactsHash )
    	{
    	    cursor.close();
    	    return;
    	    
    	}
    		if (AnySoftKeyboardConfiguration.DEBUG) Log.d(TAG, "Contacts will be reloaded since count or hash changed. New count "+newCount+" was("+mContactsCount+"), new hash "+newHash+" (was "+mContactsHash+").");
    		mContactsCount = newCount;
    		mContactsHash = newHash;
    		
    		clearDictionary();
            int loadedContacts = 0;
            final int maxWordLength = MAX_WORD_LENGTH;
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String name = cursor.getString(INDEX_NAME);

                    if (name != null) {
                        int len = name.length();

                        // TODO: Better tokenization for non-Latin writing systems
                        for (int i = 0; i < len; i++) {
                            if (Character.isLetter(name.charAt(i))) {
                                int j;
                                for (j = i + 1; j < len; j++) {
                                    char c = name.charAt(j);

                                    if (!(c == '-' || c == '\'' ||
                                          Character.isLetter(c))) {
                                        break;
                                    }
                                }

                                String word = name.substring(i, j);
                                i = j - 1;

                                // Safeguard against adding really long words. Stack
                                // may overflow due to recursion
                                // Also don't add single letter words, possibly confuses
                                // capitalization of i.
                                final int wordLen = word.length();
                                if (wordLen < maxWordLength && wordLen > 1) {
                                	if (AnySoftKeyboardConfiguration.DEBUG)
                                		Log.d(TAG, "Contact '"+word+"' will be added to contacts dictionary.");
                                	loadedContacts++;
                                    super.addWord(word, 128);
                                }
                            }
                        }
                    }

                    cursor.moveToNext();
                }
            }
            
            Log.i(TAG, "Loaded "+loadedContacts+" contacts");
    	
        
        cursor.close();
    }

    
    @Override
    protected void closeAllResources() {
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
    }

    @Override
    protected void AddWordToStorage(String word, int frequency) {
        
    }

}
