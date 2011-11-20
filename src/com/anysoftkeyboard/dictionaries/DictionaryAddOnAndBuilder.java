package com.anysoftkeyboard.dictionaries;

import android.content.Context;

import com.anysoftkeyboard.addons.AddOnImpl;

public class DictionaryAddOnAndBuilder extends AddOnImpl {

	private static final String DICTIONARY_PREF_PREFIX = "dictionary_";
	
	private static final int INVALID_RES_ID = -1;
	
	private final String mLanguage;
	private final String mAssetsFilename;
	private final int mDictionaryResId;
	private final int mAutoTextResId;
	
	
	private DictionaryAddOnAndBuilder(Context packageContext, String id,
			int nameResId, String description, int sortIndex, String dictionaryLanguage, 
			String assetsFilename, int dictResId, int autoTextResId) {
		super(packageContext, DICTIONARY_PREF_PREFIX + id, nameResId, description, sortIndex);
		mLanguage = dictionaryLanguage;
		mAssetsFilename = assetsFilename;
		mDictionaryResId = dictResId;
		mAutoTextResId = autoTextResId;
	}
	
	public DictionaryAddOnAndBuilder(Context packageContext, String id,
			int nameResId, String description, int sortIndex, String dictionaryLanguage, String assetsFilename) {
		this(packageContext, id, nameResId, description, sortIndex, dictionaryLanguage, assetsFilename, INVALID_RES_ID, INVALID_RES_ID);
	}
	
	public DictionaryAddOnAndBuilder(Context packageContext, String id,
			int nameResId, String description, int sortIndex, String dictionaryLanguage, int dictionaryResId, int autoTextResId) {
		this(packageContext, id, nameResId, description, sortIndex, dictionaryLanguage, null, dictionaryResId, autoTextResId);
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
			return new ResourceBinaryDictionary(getName(), getPackageContext(), new int[]{mDictionaryResId});
	}
	
	public AutoText createAutoText()
	{
		if (mAutoTextResId == INVALID_RES_ID)
			return null;
		else
			return new AutoText(getPackageContext().getResources(), mAutoTextResId);
	}
}
