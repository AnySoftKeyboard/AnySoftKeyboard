package com.menny.android.anysoftkeyboard.dictionary;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboard;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.Workarounds;

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
				Class<?> theClass = Class.forName("com.menny.android.anysoftkeyboard.dictionary.DictionaryFactoryAPI5");
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
    
    private AutoDictionary mAutoDictionary;
    private AddableDictionary mUserDictionary = null;
    private final List<Dictionary> mDictionaries;

    // Maps id to specific index in msDictionaries
    private final Map<String, Integer> mDictionariesById;
    // Maps language to specific index in msDictionaries
    private final Map<String, Integer> mDictionariesByLanguage;

    protected DictionaryFactory()
    {
        mDictionaries = new ArrayList<Dictionary>();
        mDictionariesById = new HashMap<String, Integer>();
        mDictionariesByLanguage = new HashMap<String, Integer>();
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
    
    public void closeContactsDictionary() {
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
    	
    	closeAutoDictionary();
    	
    	Log.d(TAG, "Creating AutoDictionary for locale: "+currentAutoDictionaryLocale);
        mAutoDictionary = new AutoDictionary(context, ime, currentAutoDictionaryLocale);
        
        return mAutoDictionary;
    }
    
	public void closeAutoDictionary() {
	    if(mAutoDictionary != null){
	        mAutoDictionary.close();
	        mAutoDictionary = null;
	    }
	}
    
    public synchronized Dictionary getDictionaryByLanguage(final String language, AnyKeyboardContextProvider context){
        return getDictionaryImpl(language, null, context);
    }
    public synchronized Dictionary getDictionaryById(final String id, AnyKeyboardContextProvider context){
        return getDictionaryImpl(null, id, context);
    }

    private synchronized Dictionary getDictionaryImpl(final String language, final String id, AnyKeyboardContextProvider context)
    {
        final String languageFormat = language == null ? "(null)" : language;
        final String idFormat = id == null ? "(null)" : id;

        if (language != null && mDictionariesByLanguage.containsKey(language)) {
            return mDictionaries.get(mDictionariesByLanguage.get(language));
        }
        if (id != null && mDictionariesById.containsKey(id)) {
            return mDictionaries.get(mDictionariesById.get(id));
        }

        Dictionary dict = null;

        try
        {
            if(id == null) {
                if ((language == null) || (language.length() == 0 || ("none".equalsIgnoreCase(language)))) {
                    return null;
                }
            }
            if(language == null) {
                if ((id == null) || (id.length() == 0 || ("none".equalsIgnoreCase(id)))) {
                    return null;
                }
            }

            if(id != null) {
                dict = locateDictionaryByIdInFactory(id, context);
            }
            else if(language != null) {
                dict = locateDictionaryByLanguageInFactory(language, context);
            }


            if (dict == null)
            {

                Log.d(TAG, MessageFormat.format("Could not locate dictionary for language {0} and id {1}. Maybe it was not loaded yet (installed recently?)",
                                new Object[]{languageFormat, idFormat}));
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
                            Log.e(TAG, MessageFormat.format(
                                    "Failed load dictionary for language {0} with id {1}! Will reset the map. Error:{2}",
                                    new Object[]{languageFormat, idFormat, e.getMessage()}));
                            e.printStackTrace();
                            if(id != null) {
                                removeDictionaryById(id);
                            }else {
                                removeDictionaryByLanguage(language);
                            }
                        }
                    }
                };
                //a little less...
                loader.setPriority(Thread.NORM_PRIORITY - 1);
                loader.start();

                if(id != null) {
                    addDictionaryById(id, dict);
                }else {
                    addDictionaryByLanguage(language, dict);
                }

            }
        }
        catch(final Exception ex)
        {
            Log.e(TAG, "Failed to load main dictionary for: "+language);
            ex.printStackTrace();
        }

        return dict;
    }


    private Dictionary locateDictionaryByLanguageInFactory(final String language,
            AnyKeyboardContextProvider context)
            throws Exception {
        Dictionary dict = null;

        if (language == null)
        	return dict;

        final ArrayList<DictionaryAddOnAndBuilder> allBuilders = ExternalDictionaryFactory.getAllAvailableExternalDictionaries(context.getApplicationContext());

        for(DictionaryAddOnAndBuilder builder : allBuilders)
        {
            if (AnySoftKeyboardConfiguration.DEBUG){
                Log.d(TAG, MessageFormat.format("Checking if builder ''{0}'' with locale ''{1}'' matches locale ''{2}''",
                        new Object[] {builder.getId(), builder.getLanguage(), language}));
            }
            if (builder.getLanguage().equalsIgnoreCase(language))
            {
                dict = builder.createDictionary();
                break;
            }
        }
        return dict;
    }

    private Dictionary locateDictionaryByIdInFactory(final String id,
            AnyKeyboardContextProvider context)
            throws Exception {
        Dictionary dict = null;

        if (id == null)
        	return dict;

        final ArrayList<DictionaryAddOnAndBuilder> allBuilders = ExternalDictionaryFactory.getAllAvailableExternalDictionaries(context.getApplicationContext());

        for(DictionaryAddOnAndBuilder builder : allBuilders)
        {
            if (AnySoftKeyboardConfiguration.DEBUG){
                Log.d(TAG, MessageFormat.format("Checking if builder ''{0}'' with locale ''{1}'' matches id ''{2}''",
                        new Object[] {builder.getId(), builder.getLanguage(), id}));
            }
            if (builder.getId().equalsIgnoreCase(id))
            {
                dict = builder.createDictionary();
                break;
            }
        }
        return dict;
    }

    public synchronized void addDictionaryByLanguage(String language, Dictionary dictionary)
    {
    	if(language == null || dictionary == null)
    		return;

    	// Add dictionary to msDictionaries, if necessary
        int position = mDictionaries.indexOf(dictionary);
        if(position < 0) {
        	mDictionaries.add(dictionary);
        	position = mDictionaries.size() - 1;
        }

        assert mDictionaries.get(position) == dictionary;
        // Overwrite/Create language->dictionary mapping
        mDictionariesByLanguage.put(language, position);
    }

    public synchronized void addDictionaryById(String id, Dictionary dictionary)
    {
    	if(id == null || dictionary == null)
    		return;

    	// Add dictionary to msDictionaries, if necessary
        int position = mDictionaries.indexOf(dictionary);
        if(position < 0) {
        	mDictionaries.add(dictionary);
        	position = mDictionaries.size() - 1;
        }

        assert mDictionaries.get(position) == dictionary;
        // Overwrite/Create id->dictionary mapping
        mDictionariesById.put(id, position);
    }

    public synchronized void removeDictionaryByLanguage(String language)
    {
    	if(language == null)
    		return;

        if (mDictionariesByLanguage.containsKey(language))
        {
            final int index = mDictionariesByLanguage.get(language);
            final Dictionary dict = mDictionaries.get(index);
            dict.close();
            mDictionaries.remove(index);
            mDictionariesById.remove(language);
            Collection<Integer> languageMappings = mDictionariesByLanguage.values();
            // Note that changes in this collection are mapped back to the map which
            // is what we want
            languageMappings.remove(index);
        }
    }

    public synchronized void removeDictionaryById(String id)
    {
    	if(id == null)
    		return;

        if (mDictionariesById.containsKey(id))
        {
            final int index = mDictionariesById.get(id);
            final Dictionary dict = mDictionaries.get(index);
            dict.close();
            mDictionaries.remove(index);
            mDictionariesById.remove(id);
            Collection<Integer> idMappings = mDictionariesById.values();
            // Note that changes in this collection are mapped back to the map which
            // is what we want
            idMappings.remove(index);
        }
    }

    public synchronized void close() {
        if (mUserDictionary != null) {
            mUserDictionary.close();
        }
        for(final Dictionary dict : mDictionaries) {
            dict.close();
        }

        mUserDictionary = null;
        mDictionaries.clear();
        mDictionariesById.clear();
        mDictionariesByLanguage.clear();
    }


    public void releaseAllDictionaries()
    {
        close();
    }


    public synchronized void onLowMemory(Dictionary currentlyUsedDictionary) {
        //I'll clear all dictionaries but the required.
        final Dictionary dictToKeep;
        final int dictToKeepIndex = mDictionaries.indexOf(currentlyUsedDictionary);
        if(dictToKeepIndex >= 0) {
            dictToKeep = mDictionaries.get(dictToKeepIndex);
            Log.d(TAG, "Going to keep "+dictToKeep.getDictionaryName()+" dictionary");
        }
        else
        {
        	dictToKeep = null;
        }

        String idMappingToDict = null;
        String languageMappingToDict = null;

        // We search first the id->dictionary mapping and
        // then language->dictionary mapping.
        Iterator<Entry<String, Integer>> idIterator = mDictionariesById.entrySet().iterator();
        while(idIterator.hasNext()) {
            Entry<String, Integer> value = idIterator.next();
            if(value.getValue() == dictToKeepIndex) {
                idMappingToDict = value.getKey();
                break;
            }
        }
       

        Iterator<Entry<String, Integer>> languageIterator = mDictionariesByLanguage.entrySet().iterator();
        while(languageIterator.hasNext()) {
            Entry<String, Integer> value = languageIterator.next();
            if(value.getValue() == dictToKeepIndex) {
                languageMappingToDict = value.getKey();
                break;
            }
        }


        assert (dictToKeep == null) || (idMappingToDict != null && languageMappingToDict != null);

        for(int dictToCloseIndex=0; dictToCloseIndex<mDictionaries.size(); dictToCloseIndex++)
        {
        	if (dictToCloseIndex == dictToKeepIndex) continue;
        	
        	final Dictionary dictToClose = mDictionaries.get(dictToCloseIndex);
        	Log.d(TAG, "Going to release "+dictToClose.getDictionaryName()+" dictionary");
        	dictToClose.close();
        }
        
        mDictionaries.clear();
        mDictionariesByLanguage.clear();
        mDictionariesById.clear();

        if (dictToKeep != null)
        {
            if(idMappingToDict != null){
            	addDictionaryById(idMappingToDict, currentlyUsedDictionary);
            }
            
            if(languageMappingToDict != null){
            	addDictionaryByLanguage(languageMappingToDict, currentlyUsedDictionary);
            }
        }
    }
}
