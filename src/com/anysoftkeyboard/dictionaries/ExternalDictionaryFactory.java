package com.anysoftkeyboard.dictionaries;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.R;

public class ExternalDictionaryFactory extends AddOnsFactory<DictionaryAddOnAndBuilder> {

	private static final String TAG = "ASK ExtDictFctry";
    
    private static final String XML_LANGUAGE_ATTRIBUTE = "locale";
    private static final String XML_ASSETS_ATTRIBUTE = "dictionaryAssertName";
    private static final String XML_RESOURCE_ATTRIBUTE = "dictionaryResourceId";
    
    
    private static final ExternalDictionaryFactory msInstance;
    static
    {
    	msInstance = new ExternalDictionaryFactory();
    }
    
    public static ArrayList<DictionaryAddOnAndBuilder> getAllAvailableExternalDictionaries(Context askContext)
    {
    	return msInstance.getAllAddOns(askContext);
    }
    
    public static DictionaryAddOnAndBuilder getDictionaryBuilderById(String id, Context askContext)
    {
    	return msInstance.getAddOnById(id, askContext);
    }
    
    public static DictionaryAddOnAndBuilder getDictionaryBuilderByLocale(String locale, Context askContext)
    {
    	return msInstance.getAddOnByLocale(locale, askContext);
    }
    
    private final HashMap<String, DictionaryAddOnAndBuilder> mBuildersByLocale = new HashMap<String, DictionaryAddOnAndBuilder>();
    
    private ExternalDictionaryFactory() {
		super(TAG, "com.menny.android.anysoftkeyboard.DICTIONARY", "com.menny.android.anysoftkeyboard.dictionaries", 
				"Dictionaries", "Dictionary",
				R.xml.dictionaries, true);
	}
    
    @Override
    protected synchronized void clearAddOnList() {
    	super.clearAddOnList();
    	mBuildersByLocale.clear();
    }
    
    @Override
    protected void buildOtherDataBasedOnNewAddOns(
    		ArrayList<DictionaryAddOnAndBuilder> newAddOns) {
    	super.buildOtherDataBasedOnNewAddOns(newAddOns);
    	for(DictionaryAddOnAndBuilder addOn : newAddOns)
    		mBuildersByLocale.put(addOn.getLanguage(), addOn);
    }
    
    public synchronized DictionaryAddOnAndBuilder getAddOnByLocale(String locale, Context askContext)
    {
    	if (mBuildersByLocale.size() == 0)
    		loadAddOns(askContext);
    	
    	return mBuildersByLocale.get(locale);
    }

	@Override
	protected DictionaryAddOnAndBuilder createConcreateAddOn(Context context,
			String prefId, int nameId, String description, int sortIndex,
			AttributeSet attrs) {
		
		final String language = attrs.getAttributeValue(null, XML_LANGUAGE_ATTRIBUTE);
		final String assets = attrs.getAttributeValue(null, XML_ASSETS_ATTRIBUTE);
		final int dictionaryResourceId = attrs.getAttributeResourceValue(null, XML_RESOURCE_ATTRIBUTE, -1);

		//asserting
		if (TextUtils.isEmpty(prefId) ||(language == null) || (nameId == -1) || ((assets == null) && (dictionaryResourceId == -1)))
		{
			Log.e(TAG, "External dictionary does not include all mandatory details! Will not create dictionary.");
			return null;
		}
		else
		{
			final DictionaryAddOnAndBuilder creator;
			if (dictionaryResourceId == -1)
				creator = new DictionaryAddOnAndBuilder(context, prefId, nameId, description, sortIndex, language, assets);
			else
				creator = new DictionaryAddOnAndBuilder(context, prefId, nameId, description, sortIndex, language, dictionaryResourceId);
				
			return creator;
		}
	}
}
