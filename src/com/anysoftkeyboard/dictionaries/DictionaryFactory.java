package com.anysoftkeyboard.dictionaries;

import android.util.Log;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.utils.Workarounds;

public class DictionaryFactory
{
    private static final String TAG = "ASK DictFctry";
    
    private static final DictionaryFactory msFactory;
    
    static
    {
    	if (!Workarounds.isEclair())
    		msFactory = new DictionaryFactory();
    	else
    	{
    		//it seems that Contacts Dictionary can be used from OS 2.0 or higher....
    		DictionaryFactory factory = null;
    		
			try {
				Class<?> theClass = Class.forName("com.anysoftkeyboard.dictionaries.DictionaryFactoryAPI5");
	    		factory = (DictionaryFactory)theClass.newInstance();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		msFactory = (factory == null)? new DictionaryFactory() : factory;
    	}
    }
    
    public static DictionaryFactory getInstance()
    {
    	return msFactory;
    }
    
    private AutoDictionary mAutoDictionary = null;
    private AddableDictionary mUserDictionary = null;

    protected DictionaryFactory()
    {
    }
    
    public synchronized AddableDictionary createUserDictionary(AnyKeyboardContextProvider context)
    {
        if (mUserDictionary != null){
            return mUserDictionary;
        }
        
        mUserDictionary = new SafeUserDictionary(context);
        return mUserDictionary;
    }
    
    public synchronized AddableDictionary createContactsDictionary(AnyKeyboardContextProvider context)
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
    
    
    public synchronized AutoDictionary createAutoDictionary(AnyKeyboardContextProvider context, AnySoftKeyboard ime, String currentAutoDictionaryLocale)
    {
    	if (mAutoDictionary != null && equalsString(mAutoDictionary.getLocale(), currentAutoDictionaryLocale))
    	{
    		return mAutoDictionary;
    	}
    	
    	Log.d(TAG, "Creating AutoDictionary for locale: "+currentAutoDictionaryLocale);
        mAutoDictionary = new AutoDictionary(context, ime, currentAutoDictionaryLocale);
        
        return mAutoDictionary;
    }
    
    private Dictionary makeDictionaryFromBuilder(
			DictionaryAddOnAndBuilder builder) {
		if (builder != null)
        {
			try {
				Dictionary dict = builder.createDictionary();
				dict.loadDictionary();
				return dict;
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        
		return null;
	}
    
    public synchronized Dictionary getDictionaryByLanguage(final String language, AnyKeyboardContextProvider context){
    	DictionaryAddOnAndBuilder builder = ExternalDictionaryFactory.getDictionaryBuilderByLocale(language, context.getApplicationContext());
        return makeDictionaryFromBuilder(builder);	
    }

	public synchronized Dictionary getDictionaryById(final String id, AnyKeyboardContextProvider context){
    	DictionaryAddOnAndBuilder builder = ExternalDictionaryFactory.getDictionaryBuilderById(id, context.getApplicationContext());
        return makeDictionaryFromBuilder(builder);	
    }

}
