package com.menny.android.anysoftkeyboard.Dictionary;

import java.util.HashMap;

import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary.Language;

public class DictionaryFactory 
{
	private static UserDictionaryBase msUserDictionary = null;
	private static final HashMap<Dictionary.Language, Dictionary> msDictionaries;
	
	static
	{
		msDictionaries = new HashMap<Dictionary.Language, Dictionary>();
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
	        catch(Exception ex)
	        {
	        	Log.w("AnySoftKeyboard", "Failed to load 'AndroidUserDictionary' (could be that the platform does not support it). Will use fall-back dictionary. Error:"+ex.getMessage());
	        	try {
					msUserDictionary = new FallbackUserDictionary(context);
					msUserDictionary.loadDictionary();
				} catch (Exception e) {
					Log.e("AnySoftKeyboard", "Failed to load failback user dictionary!");
					e.printStackTrace();
				}
	        }
		}
        return msUserDictionary;
	}
	
	
	public synchronized static Dictionary getDictionary(final Dictionary.Language language, AnyKeyboardContextProvider context)
	{
		if (msDictionaries.containsKey(language))
			return msDictionaries.get(language);
		
		Dictionary dict = null;
		
		//showing lengthy operation toast
		//context.showToastMessage(R.string.toast_lengthy_words_long_operation, false);		
		try
		{
			switch(language)
			{
			case English:
				dict = new SQLiteSimpleDictionary(context, "en", "en");
				break;
			case Hebrew:
				dict = new SQLiteSimpleDictionary(context, "he", "he");
				break;
			case French:
				dict = new SQLiteSimpleDictionary(context, "fr", "fr");
				break;
			case German:
				dict = new SQLiteSimpleDictionary(context, "de", "de");
				break;
			case Spanish:
				dict = new SQLiteSimpleDictionary(context, "es", "es");
				break;
			case Swedish:
				dict = new SQLiteSimpleDictionary(context, "sv", "sv");
				break;
			case Russian:
				dict = new SQLiteSimpleDictionary(context, "ru", "ru");
				break;
			case Finnish:
				dict = new SQLiteSimpleDictionary(context, "fi", "fi");
				break;
			case Dutch:
				dict = new SQLiteSimpleDictionary(context, "nl", "nl");
				break;
			default:
				return null;
			}
			final Dictionary dictToLoad = dict;
			Thread loader = new Thread()
			{
				public void run()
				{
					try {
						dictToLoad.loadDictionary();
					} catch (Exception e) {
						Log.e("AnySoftKeyboard", "Failed load dictionary for "+language+"! Will reset the map. Error:"+e.getMessage());
						e.printStackTrace();
						removeDictionary(language);
					}
				}				
			};
			loader.start();
			msDictionaries.put(language, dict);
		}
		catch(Exception ex)
		{
			Log.e("AnySoftKeyboard", "Failed to load main dictionary for: "+language);
			ex.printStackTrace();
		}
		
		return dict;
	}

	public synchronized static void removeDictionary(Language language) 
	{
		if (msDictionaries.containsKey(language))
		{
			Dictionary dict = msDictionaries.get(language);
			dict.close();
			msDictionaries.remove(language);
		}		
	}

	public synchronized static void close() {
		if (msUserDictionary != null)
			msUserDictionary.close();
		for(Dictionary dict : msDictionaries.values())
			dict.close();
		
		msUserDictionary = null;
		msDictionaries.clear();
	}


	public static void releaseAllDictionaries() 
	{
		close();
	}
	
	public synchronized static void releaseDictionary(Language language)
	{
		if (msDictionaries.containsKey(language))
		{
			Dictionary dict = msDictionaries.get(language);
			dict.close();
			msDictionaries.remove(language);
		}
	}
}
