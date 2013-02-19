package com.anysoftkeyboard.dictionaries;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

public class ExternalDictionaryFactory extends AddOnsFactory<DictionaryAddOnAndBuilder> {

	private static final String TAG = "ASK ExtDictFctry";
    
    private static final String XML_LANGUAGE_ATTRIBUTE = "locale";
    private static final String XML_ASSETS_ATTRIBUTE = "dictionaryAssertName";
    private static final String XML_RESOURCE_ATTRIBUTE = "dictionaryResourceId";
    private static final String XML_AUTO_TEXT_RESOURCE_ATTRIBUTE = "autoTextResourceId";
    private static final String XML_INITIAL_SUGGESTIONS_ARRAY_RESOURCE_ATTRIBUTE = "initialSuggestions";
    
    
    private static final ExternalDictionaryFactory msInstance;
    
    static {
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
    
    public synchronized DictionaryAddOnAndBuilder getAddOnByLocale(String locale, Context askContext) {
    	if (mBuildersByLocale.size() == 0)
    		loadAddOns(askContext);
    	
    	return mBuildersByLocale.get(locale);
    }

	@Override
	protected DictionaryAddOnAndBuilder createConcreateAddOn(Context askContext, Context context,
			String prefId, int nameId, String description, int sortIndex,
			AttributeSet attrs) {
		
		final String language = attrs.getAttributeValue(null, XML_LANGUAGE_ATTRIBUTE);
		final String assets = attrs.getAttributeValue(null, XML_ASSETS_ATTRIBUTE);
		final int dictionaryResourceId = attrs.getAttributeResourceValue(null, XML_RESOURCE_ATTRIBUTE, AddOn.INVALID_RES_ID);
		final int autoTextResId = attrs.getAttributeResourceValue(null, XML_AUTO_TEXT_RESOURCE_ATTRIBUTE, AddOn.INVALID_RES_ID);
		final int initialSuggestionsId = attrs.getAttributeResourceValue(null, XML_INITIAL_SUGGESTIONS_ARRAY_RESOURCE_ATTRIBUTE, AddOn.INVALID_RES_ID);
		//asserting
		if (TextUtils.isEmpty(prefId) ||(language == null) || (nameId == AddOn.INVALID_RES_ID) || ((assets == null) && (dictionaryResourceId == AddOn.INVALID_RES_ID)))
		{
			Log.e(TAG, "External dictionary does not include all mandatory details! Will not create dictionary.");
			return null;
		}
		else
		{
			final DictionaryAddOnAndBuilder creator;
			if (dictionaryResourceId == AddOn.INVALID_RES_ID)
				creator = new DictionaryAddOnAndBuilder(askContext, context, prefId, nameId, description, sortIndex, language, assets, initialSuggestionsId);
			else
				creator = new DictionaryAddOnAndBuilder(askContext, context, prefId, nameId, description, sortIndex, language, dictionaryResourceId, autoTextResId, initialSuggestionsId);
				
			return creator;
		}
	}
}
