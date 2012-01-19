package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.util.Log;

public class DictionaryFactoryAPI5 extends DictionaryFactory
{
	private static final String TAG = "ASK DictFctry5";
    
	private AddableDictionary mContactsDictionary;
    
	@Override
	public synchronized AddableDictionary createContactsDictionary(Context context)
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
}
