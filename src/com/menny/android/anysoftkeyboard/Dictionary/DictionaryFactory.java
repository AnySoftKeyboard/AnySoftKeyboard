package com.menny.android.anysoftkeyboard.Dictionary;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        //showing lengthy operation toast
        // context.showToastMessage(R.string.toast_lengthy_words_long_operation,
        // false);

        // Binary dictionary
        // dict = new
        // BinaryDictionary(context.getApplicationContext().getAssets().openFd(
        // "finnish.mp3"));
        // Problems with extensions?
        try
        {
            if (language.equalsIgnoreCase("English")) {
                dict = new SQLiteSimpleDictionary(context, "en", "en");
            } else if (language.equalsIgnoreCase("Hebrew")) {
                dict = new SQLiteSimpleDictionary(context, "he", "he");
            } else if (language.equalsIgnoreCase("French")) {
                dict = new SQLiteSimpleDictionary(context, "fr", "fr");
            } else if (language.equalsIgnoreCase("German")) {
                dict = new SQLiteSimpleDictionary(context, "de", "de");
            } else if (language.equalsIgnoreCase("Spanish")) {
                dict = new SQLiteSimpleDictionary(context, "es", "es");
            } else if (language.equalsIgnoreCase("Swedish")) {
                dict = new SQLiteSimpleDictionary(context, "sv", "sv");
            } else if (language.equalsIgnoreCase("Russian")) {
                dict = new SQLiteSimpleDictionary(context, "ru", "ru");
            } else if (language.equalsIgnoreCase("Finnish")) {
                // dict = new SQLiteSimpleDictionary(context, "fi", "fi");
                // TODO: investigate filename extension
                dict = new BinaryDictionary(context.getApplicationContext().getAssets().openFd(
                "fiBinary.mp3"));
            } else if (language.equalsIgnoreCase("Dutch")) {
                dict = new SQLiteSimpleDictionary(context, "nl", "nl");
            } else if (language.equalsIgnoreCase("Slovenian")) {
                dict = new SQLiteSimpleDictionary(context, "sl", "sl");
            } else if (language.equalsIgnoreCase("Portuguese")) {
                dict = new SQLiteSimpleDictionary(context, "pt", "pt");
            } else if (language.equalsIgnoreCase("Bulgarian")) {
                dict = new SQLiteSimpleDictionary(context, "bg", "bg");
            } else if (language.equalsIgnoreCase("Ukrainian")) {
                dict = new SQLiteSimpleDictionary(context, "uk", "uk");
            } else {
                return null;
            }

            final Dictionary dictToLoad = dict;
            final Thread loader = new Thread()
            {
                @Override
                public void run()
                {
                    try {
                        dictToLoad.loadDictionary();
                    } catch (final Exception e) {
                        Log.e("AnySoftKeyboard", "Failed load dictionary for "+language+"! Will reset the map. Error:"+e.getMessage());
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
        catch(final Exception ex)
        {
            Log.e("AnySoftKeyboard", "Failed to load main dictionary for: "+language);
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

    private enum LanguageStrings
    {
        None,
        English,
        Hebrew,
        French,
        German,
        Spanish,
        Russian,
        Arabic,
        Lao,
        Swedish, 
        Finnish, 
        Dutch,
        Slovenian,
        Portuguese,
        Bulgarian,
        Thai,
        Ukrainian
    }
    public static List<String> getKnownDictionariesNames() {
        final ArrayList<String> list = new ArrayList<String>();
        for(final LanguageStrings lang : LanguageStrings.values()) {
            list.add(lang.toString());
        }

        return list;
    }
}
