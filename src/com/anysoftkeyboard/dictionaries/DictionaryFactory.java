package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import android.util.Log;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;

public class DictionaryFactory
{
    private static final String TAG = "ASK DictFctry";
    
    private AutoDictionary mAutoDictionary = null;
    private String mUserDictionaryLocale = null;
    private EditableDictionary mUserDictionary = null;

    public DictionaryFactory()
    {
    }
    
    public synchronized EditableDictionary createUserDictionary(Context context, String locale)
    {
        if (mUserDictionary != null && equalsString(mUserDictionaryLocale, locale)){
        	Log.d(TAG, "Returning cached user-dictionary for locale "+mUserDictionaryLocale);
            return mUserDictionary;
        }
        Log.d(TAG, "Creating a new UserDictionart for locale "+locale);
        mUserDictionary = new SafeUserDictionary(context, locale);

        mUserDictionaryLocale = locale;
        return mUserDictionary;
    }
    
    public synchronized EditableDictionary createContactsDictionary(Context context)
    {
          return null;
    }
    
    public boolean equalsString(String a, String b){
        if(a == null && b == null){
            return true;
        }
        if(a == null || b == null){
            return false;
        }
        return a.equals(b);
    }
    
    
    public synchronized AutoDictionary createAutoDictionary(Context context, AnySoftKeyboard ime, String currentAutoDictionaryLocale)
    {
    	if (AnyApplication.getConfig().getAutoDictionaryInsertionThreshold() < 0)
    		return null;
    	
    	if (mAutoDictionary != null && equalsString(mAutoDictionary.getLocale(), currentAutoDictionaryLocale))
    	{
    		return mAutoDictionary;
    	}
    	
    	Log.d(TAG, "Creating AutoDictionary for locale: "+currentAutoDictionaryLocale);
        mAutoDictionary = new AutoDictionary(context, ime, currentAutoDictionaryLocale);
        mAutoDictionary.loadDictionary();
        
        return mAutoDictionary;
    }
}
