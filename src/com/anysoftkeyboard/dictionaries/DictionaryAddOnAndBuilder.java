package com.anysoftkeyboard.dictionaries;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.anysoftkeyboard.addons.AddOnImpl;

public class DictionaryAddOnAndBuilder extends AddOnImpl {

	private static final String DICTIONARY_PREF_PREFIX = "dictionary_";
	
	private static final int INVALID_RES_ID = 0;
	
	private final String mLanguage;
	private final String mAssetsFilename;
	private final int mDictionaryResId;
	private final int mAutoTextResId;
	private final int mInitialSuggestionsResId;
	
	private DictionaryAddOnAndBuilder(Context packageContext, String id,
			int nameResId, String description, int sortIndex, String dictionaryLanguage, 
			String assetsFilename, int dictResId, int autoTextResId, int initialSuggestionsResId) {
		super(packageContext, DICTIONARY_PREF_PREFIX + id, nameResId, description, sortIndex);
		mLanguage = dictionaryLanguage;
		mAssetsFilename = assetsFilename;
		mDictionaryResId = dictResId;
		mAutoTextResId = autoTextResId;
		mInitialSuggestionsResId = initialSuggestionsResId;
	}
	
	public DictionaryAddOnAndBuilder(Context packageContext, String id,
			int nameResId, String description, int sortIndex, String dictionaryLanguage, String assetsFilename, int initialSuggestionsResId) {
		this(packageContext, id, nameResId, description, sortIndex, dictionaryLanguage, assetsFilename, INVALID_RES_ID, INVALID_RES_ID, initialSuggestionsResId);
	}
	
	public DictionaryAddOnAndBuilder(Context packageContext, String id,
			int nameResId, String description, int sortIndex, String dictionaryLanguage, int dictionaryResId, int autoTextResId, int initialSuggestionsResId) {
		this(packageContext, id, nameResId, description, sortIndex, dictionaryLanguage, null, dictionaryResId, autoTextResId, initialSuggestionsResId);
	}

	public String getLanguage()
	{
		return mLanguage;
	}
	
	public int getAutoTextResId()
	{
		return mAutoTextResId;
	}
	
	public Dictionary createDictionary() throws Exception
	{
		if (mDictionaryResId == INVALID_RES_ID)
			return new BinaryDictionary(getName(), getPackageContext().getAssets().openFd(mAssetsFilename));
		else
			return new ResourceBinaryDictionary(getName(), getPackageContext(), mDictionaryResId);
	}
	
	public AutoText createAutoText()
	{
		if (mAutoTextResId == INVALID_RES_ID)
			return null;
		else
			return new AutoText(getPackageContext().getResources(), mAutoTextResId);
	}
	
	public List<CharSequence> createInitialSuggestions()
	{
		if (mInitialSuggestionsResId == INVALID_RES_ID)
		{
			return null;
		}
		else
		{
			String[] initialSuggestions = getPackageContext().getResources().getStringArray(mInitialSuggestionsResId);
			if (initialSuggestions != null)
			{
				List<CharSequence> suggestionsList= new ArrayList<CharSequence>(initialSuggestions.length);
				for(String suggestion : initialSuggestions)
					suggestionsList.add(suggestion);
				
				return suggestionsList;
			}
			else
			{
				return null;
			}
		}
	}
}
