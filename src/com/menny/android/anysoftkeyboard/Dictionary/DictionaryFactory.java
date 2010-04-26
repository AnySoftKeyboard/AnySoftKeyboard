package com.menny.android.anysoftkeyboard.Dictionary;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.Dictionary.ExternalDictionaryFactory.DictionaryBuilder;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class DictionaryFactory 
{
    private static UserDictionaryBase msUserDictionary = null;
    private static final HashMap<String, Dictionary> msDictionariesByLocale;

    static
    {
    	msDictionariesByLocale = new HashMap<String, Dictionary>();
    }

    public synchronized static UserDictionaryBase createUserDictionary(AnyKeyboardContextProvider context)
    {
        if (msUserDictionary == null)
        {
            try
            {
                msUserDictionary = new AndroidUserDictionary(context);
                msUserDictionary.loadDictionary();
            }
            catch(final Exception ex)
            {
                Log.w("AnySoftKeyboard", "Failed to load 'AndroidUserDictionary' (could be that the platform does not support it). Will use fall-back dictionary. Error:"+ex.getMessage());
                try {
                    msUserDictionary = new FallbackUserDictionary(context);
                    msUserDictionary.loadDictionary();
                } catch (final Exception e) {
                    Log.e("AnySoftKeyboard", "Failed to load failback user dictionary!");
                    e.printStackTrace();
                }
            }
        }
        return msUserDictionary;
    }


    public synchronized static Dictionary getDictionary(final String language, AnyKeyboardContextProvider context)
    {
        if (msDictionariesByLocale.containsKey(language)) {
            return msDictionariesByLocale.get(language);
        }

        Dictionary dict = null;

        try
        {
        	if ((language == null) || (language.length() == 0 || ("none".equalsIgnoreCase(language))))
        		return null;
        	
        	dict = locateDictionaryInFactory(language, context);
        	if (dict == null)
        	{
        		Log.d("DictionaryFactory", "Could not locate dictionary for "+language+". Maybe it was not loaded yet (installed recently?)");
        		ExternalDictionaryFactory.resetBuildersCache();
        		//trying again
        		dict = locateDictionaryInFactory(language, context);
        		if (dict == null)
        			Log.w("DictionaryFactory", "Could not locate dictionary for "+language);
        	}
        	//checking again, cause it may have loaded the second try.
        	if (dict != null)
        	{
                final Dictionary dictToLoad = dict;
                final Thread loader = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try {
                            dictToLoad.loadDictionary();
                        } catch (final Exception e) {
                            Log.e("DictionaryFactory", "Failed load dictionary for "+language+"! Will reset the map. Error:"+e.getMessage());
                            e.printStackTrace();
                            removeDictionary(language);
                        }
                    }				
                };
                //a little less...
                loader.setPriority(Thread.NORM_PRIORITY - 1);
                loader.start();
                msDictionaries.put(language, dict);
        	}
        }
        catch(final Exception ex)
        {
            Log.e("DictionaryFactory", "Failed to load main dictionary for: "+language);
            ex.printStackTrace();
        }

        return dict;
    }


	private static Dictionary locateDictionaryInFactory(final String locale,
			AnyKeyboardContextProvider context)
			throws Exception {
		Dictionary dict = null;
		final ArrayList<DictionaryBuilder> allBuilders = ExternalDictionaryFactory.getAllBuilders(context.getApplicationContext());
		
		for(DictionaryBuilder builder : allBuilders)
		{
			if (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
				Log.d("DictionaryFactory", "Checking if builder '"+builder.getDictionaryLocale()+"' is '"+locale+"'...");
			if (builder.getDictionaryLocale().equalsIgnoreCase(locale))
			{
				dict = builder.createDictionary();
				break;
			}
		}
		return dict;
	}

    public synchronized static void removeDictionary(String language) 
    {
        if (msDictionaries.containsKey(language))
        {
            final Dictionary dict = msDictionaries.get(language);
            dict.close();
            msDictionaries.remove(language);
        }		
    }

    public synchronized static void close() {
        if (msUserDictionary != null) {
            msUserDictionary.close();
        }
        for(final Dictionary dict : msDictionaries.values()) {
            dict.close();
        }

        msUserDictionary = null;
        msDictionaries.clear();
    }


    public static void releaseAllDictionaries() 
    {
        close();
    }

    public synchronized static void releaseDictionary(String language)
    {
        if (msDictionaries.containsKey(language))
        {
            final Dictionary dict = msDictionaries.get(language);
            dict.close();
            msDictionaries.remove(language);
        }
    }


    public synchronized static void onLowMemory(String currentlyUsedDictionary) {
        //I'll clear all dictionaries but the required.
        Dictionary dictToKeep = null;
        for(final Entry<String, Dictionary> dict : msDictionaries.entrySet())
        {
            if (dict.getKey().equals(currentlyUsedDictionary))
            {
                dictToKeep = dict.getValue();
            }
            else
            {
                Log.i("AnySoftKeyboard", "DictionaryFacotry::onLowMemory: Removing "+dict.getKey());
                dict.getValue().close();
            }
        }

        msDictionaries.clear();
        if (dictToKeep != null)
        {
            msDictionaries.put(currentlyUsedDictionary, dictToKeep);
        }
    }
}
