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

import com.menny.android.anysoftkeyboard.AnyApplication;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

public class ContactsDictionary extends UserDictionaryBase {
    
    protected static final String TAG = "ASK CDict";
    
    private static final String[] PROJECTION = {
        Contacts._ID,
        Contacts.DISPLAY_NAME,
        Contacts.STARRED,
        Contacts.TIMES_CONTACTED
    };

    private static final int INDEX_NAME = 1;
    private static final int INDEX_STARRED = 2;
    private static final int INDEX_TIMES = 3;

    private ContentObserver mObserver;

	private int mContactsCount = 0;

	private long mContactsHash = 0;
    
    public ContactsDictionary(Context context) throws Exception {
        super("ContactsDictionary",context);
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        ContentResolver cres = mContext.getContentResolver();

        cres.registerContentObserver(Contacts.CONTENT_URI, true, mObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean self) {
                if (AnyApplication.DEBUG)Log.d(TAG, "Contacts list modified (self: "+self+"). Reloading...");
                new AsyncTask<Void, Void, Void>()
            	{
            		@Override
            		protected Void doInBackground(Void... params) {
            			loadDictionaryAsync();
            			return null;
            		}
            	}.execute();
            }
        });
    }

    
    @Override
    protected void loadDictionaryAsync() {
    	Log.d(TAG, "Starting load of contact names...");
    	try{
	        Cursor cursor = getWordsCursor();
	        if (cursor != null) {
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
	        	
	        	Log.d(TAG, "I noticed "+newCount+" contacts.");
	        	if (newCount == mContactsCount  && newHash == mContactsHash )
	        	{
	        	    return;
	        	}
	        	
	    		Log.d(TAG, "Contacts will be reloaded since count or hash changed. New count "+newCount+" was("+mContactsCount+"), new hash "+newHash+" (was "+mContactsHash+").");
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
	                                	final boolean isStarred = cursor.getInt(INDEX_STARRED) > 0;
	                                	final int timesContacted = cursor.getInt(INDEX_TIMES);
	                                	loadedContacts++;
	                                	int freq = 1;
	                                	if (isStarred) freq = 255;//WOW! important!
	                                	else if (timesContacted > 100)
	                                		freq = 128;
	                                	else if (timesContacted > 10)
	                                		freq = 32;
	                                	else if (timesContacted > 1)
	                                		freq = 16;
	                                	
	                                	if (AnyApplication.DEBUG)
		                                		Log.d(TAG, "Contact '"+word+"' will be added to contacts dictionary with freq "+freq);
		                                addWordFromStorage(word, freq);
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
        } catch(IllegalStateException e) {
            Log.e(TAG, "Contacts DB is having problems");
        }
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
    
    @Override
    public Cursor getWordsCursor() {
    	return mContext.getContentResolver().query(Contacts.CONTENT_URI, PROJECTION, Contacts.IN_VISIBLE_GROUP+"="+1, null, null);
    }

}
