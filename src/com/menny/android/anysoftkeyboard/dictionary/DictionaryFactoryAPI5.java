package com.menny.android.anysoftkeyboard.dictionary;

import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class DictionaryFactoryAPI5 extends DictionaryFactory
{
	private static final String TAG = "ASK DictFctry5";
    
	private UserDictionaryBase contactsDictionary;
    
	@Override
	public synchronized UserDictionaryBase createContactsDictionary(AnyKeyboardContextProvider context)
    {
          if(contactsDictionary != null){
              return contactsDictionary;
          }
          try{
            contactsDictionary = new ContactsDictionary(context);
            contactsDictionary.loadDictionary();
        }
        catch(final Exception ex)
        {
            Log.w(TAG, "Failed to load 'ContactsDictionary'",ex); 
            contactsDictionary = null;
        }
        return contactsDictionary;
    }
	


	public void closeContactsDictionary() {
	    if(contactsDictionary != null){
	        contactsDictionary.close();
	        contactsDictionary = null;
	    }
	}
	
	@Override
	public synchronized void close() {
		super.close();
        
        if(contactsDictionary != null){
            contactsDictionary.close();
        }
	}
}
