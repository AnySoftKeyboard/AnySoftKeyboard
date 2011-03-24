package com.menny.android.anysoftkeyboard.dictionary;

import android.content.Context;

import com.menny.android.anysoftkeyboard.addons.AddOnImpl;

public class DictionaryAddOnAndBuilder extends AddOnImpl {

	private static final String DICTIONARY_PREF_PREFIX = "dictionary_";
	
	private final String mLanguage;
	private final String mAssetsFilename;
	
	public DictionaryAddOnAndBuilder(Context packageContext, String id,
			int nameResId, String description, int sortIndex, String dictionaryLanguage, String assetsFilename) {
		super(packageContext, DICTIONARY_PREF_PREFIX + id, nameResId, description, sortIndex);
		mLanguage = dictionaryLanguage;
		mAssetsFilename = assetsFilename;
	}

	public String getLanguage()
	{
		return mLanguage;
	}
	
	public Dictionary createDictionary() throws Exception
	{
		return new BinaryDictionary(getName(), getPackageContext().getAssets().openFd(mAssetsFilename));
	}
}
