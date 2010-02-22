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
    private static final HashMap<String, Dictionary> msDictionaries;

    static
    {
        msDictionaries = new HashMap<String, Dictionary>();
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
        if (msDictionaries.containsKey(language)) {
            return msDictionaries.get(language);
        }

        Dictionary dict = null;

        try
        {
        	ArrayList<DictionaryBuilder> allBuilders = ExternalDictionaryFactory.getAllCreators(context.getApplicationContext());
        	
        	if ((language == null) || (language.length() == 0 || ("none".equalsIgnoreCase(language))))
        		return null;
        	
        	for(DictionaryBuilder builder : allBuilders)
        	{
        		if (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
        			Log.d("DictionaryFactory", "Checking if builder '"+builder.getDictionaryKey()+"' is '"+language+"'...");
        		if (builder.getDictionaryKey().equalsIgnoreCase(language))
        		{
        			dict = builder.createDictionary();
        			break;
        		}
        	}
//            if (language.equalsIgnoreCase("English")) {
//                dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("en_binary.mp3"));
//            } else if (language.equalsIgnoreCase("Hebrew")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("he_binary.mp3"));
//            } else if (language.equalsIgnoreCase("French")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("fr_binary.mp3"));
//            } else if (language.equalsIgnoreCase("German")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("de_binary.mp3"));
//            } else if (language.equalsIgnoreCase("Spanish")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("es_binary.mp3"));
//            } else if (language.equalsIgnoreCase("Swedish")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("sv_binary.mp3"));
//            } else if (language.equalsIgnoreCase("Russian")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("ru_binary.mp3"));
//            } else if (language.equalsIgnoreCase("Finnish")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("fi_binary.mp3"));
//            } else if (language.equalsIgnoreCase("Dutch")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("nl_binary.mp3"));
//            } else if (language.equalsIgnoreCase("Slovenian")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("sl_binary.mp3"));
//            } else if (language.equalsIgnoreCase("Portuguese")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("pt_binary.mp3"));
//            } else if (language.equalsIgnoreCase("Bulgarian")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("bg_binary.mp3"));
//            } else if (language.equalsIgnoreCase("Ukrainian")) {
//            	dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd("uk_binary.mp3"));
//            } else {
//                return null;
//            }
        	if (dict == null)
        	{
        		Log.w("DictionaryFactory", "Could not locate dictionary for "+language);
        	}
        	else
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
//
//    private enum LanguageStrings
//    {
//        None,
//        English,
//        Hebrew,
//        French,
//        German,
//        Spanish,
//        Russian,
//        Arabic,
//        Lao,
//        Swedish, 
//        Finnish, 
//        Dutch,
//        Slovenian,
//        Portuguese,
//        Bulgarian,
//        Thai,
//        Ukrainian
//    }
//    public static List<String> getKnownDictionariesNames() {
//        final ArrayList<String> list = new ArrayList<String>();
//        for(final LanguageStrings lang : LanguageStrings.values()) {
//            list.add(lang.toString());
//        }
//
//        return list;
//    }
}
