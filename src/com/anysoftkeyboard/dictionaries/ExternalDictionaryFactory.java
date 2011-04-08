package com.anysoftkeyboard.dictionaries;

import java.util.ArrayList;

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
    
    
    private static final ExternalDictionaryFactory msInstance;
    static
    {
    	msInstance = new ExternalDictionaryFactory();
    }
    
    public static ArrayList<DictionaryAddOnAndBuilder> getAllAvailableExternalDictionaries(Context askContext)
    {
    	return msInstance.getAllAddOns(askContext);
    }
    
    private ExternalDictionaryFactory() {
		super(TAG, "com.menny.android.anysoftkeyboard.DICTIONARY", "com.menny.android.anysoftkeyboard.dictionaries", 
				"Dictionaries", "Dictionary",
				R.xml.dictionaries);
	}

	@Override
	protected DictionaryAddOnAndBuilder createConcreateAddOn(Context context,
			String prefId, int nameId, String description, int sortIndex,
			AttributeSet attrs) {
		
		final String language = attrs.getAttributeValue(null, XML_LANGUAGE_ATTRIBUTE);
		final String assets = attrs.getAttributeValue(null, XML_ASSETS_ATTRIBUTE);

		//asserting
		if (TextUtils.isEmpty(prefId) ||(language == null) || (nameId == -1) || (assets == null))
		{
			Log.e(TAG, "External dictionary does not include all mandatory details! Will not create dictionary.");
			return null;
		}
		else
		{
			final DictionaryAddOnAndBuilder creator = new DictionaryAddOnAndBuilder(context, prefId, nameId, description, sortIndex, language, assets);
			return creator;
		}
	}
}
