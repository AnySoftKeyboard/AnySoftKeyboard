package com.anysoftkeyboard.dictionaries;

import android.content.Context;

import com.anysoftkeyboard.addons.AddOnImpl;

public class DictionaryAddOnAndBuilder extends AddOnImpl {

	private static final String DICTIONARY_PREF_PREFIX = "dictionary_";
	
	private static final int INVALID_RES_ID = -1;
	
	private final String mLanguage;
	private final String mAssetsFilename;
	private final int mDictionaryResId;
	
	
	
	private DictionaryAddOnAndBuilder(Context packageContext, String id,
			int nameResId, String description, int sortIndex, String dictionaryLanguage, String assetsFilename, int dictResId) {
		super(packageContext, DICTIONARY_PREF_PREFIX + id, nameResId, description, sortIndex);
		mLanguage = dictionaryLanguage;
		mAssetsFilename = assetsFilename;
		mDictionaryResId = dictResId;
	}
	
	public DictionaryAddOnAndBuilder(Context packageContext, String id,
			int nameResId, String description, int sortIndex, String dictionaryLanguage, String assetsFilename) {
		this(packageContext, id, nameResId, description, sortIndex, dictionaryLanguage, assetsFilename, INVALID_RES_ID);
	}
	
	public DictionaryAddOnAndBuilder(Context packageContext, String id,
			int nameResId, String description, int sortIndex, String dictionaryLanguage, int dictionaryResId) {
		this(packageContext, id, nameResId, description, sortIndex, dictionaryLanguage, null, dictionaryResId);
	}

	public String getLanguage()
	{
		return mLanguage;
	}
	
	public Dictionary createDictionary() throws Exception
	{
		if (mDictionaryResId == INVALID_RES_ID)
			return new BinaryDictionary(getName(), getPackageContext().getAssets().openFd(mAssetsFilename));
		else
			return new ResourceBinaryDictionary(getName(), getPackageContext(), new int[]{mDictionaryResId});
	}
}
