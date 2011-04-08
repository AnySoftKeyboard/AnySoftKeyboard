package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnyKeyboardContextProvider;

import android.util.Log;

public class DictionaryFactoryAPI5 extends DictionaryFactory
{
	private static final String TAG = "ASK DictFctry5";
    
	private AddableDictionary mContactsDictionary;
    
	@Override
	public synchronized AddableDictionary createContactsDictionary(AnyKeyboardContextProvider context)
    {
          if(mContactsDictionary != null){
              return mContactsDictionary;
          }
          try{
            mContactsDictionary = new ContactsDictionary(context);
            mContactsDictionary.loadDictionary();
        }
        catch(final Exception ex)
        {
            Log.w(TAG, "Failed to load 'ContactsDictionary'",ex); 
            mContactsDictionary = null;
        }
        return mContactsDictionary;
    }
	


	public void closeContactsDictionary() {
	    if(mContactsDictionary != null){
	        mContactsDictionary.close();
	        mContactsDictionary = null;
	    }
	}
	
	@Override
	public synchronized void close() {
		super.close();
        
        if(mContactsDictionary != null){
            mContactsDictionary.close();
        }
	}
}
