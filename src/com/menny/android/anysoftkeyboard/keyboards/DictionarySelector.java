package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.Dictionary.Dictionary.Language;

public interface DictionarySelector {
	Language getDictionaryLanguage();
}

class DefaultDictionarySelector implements DictionarySelector
{
	private final Language mDefaultLanguage;
	public DefaultDictionarySelector(Language language)
	{
		mDefaultLanguage = language;
	}
	public Language getDictionaryLanguage() {
		return mDefaultLanguage;
	}	
}

//class LocaleSensitiveDictionarySelector implements DictionarySelector
//{
//	private final Map<String, Language> mLanguagesMap;
//	private final Language mDefaultLanguage;
//	private final Configuration mConfig;
//	
//	public LocaleSensitiveDictionarySelector(Map<String, Language> languagesMap, Language defaultLanguage)
//	{
//		mDefaultLanguage = defaultLanguage;
//		mLanguagesMap = languagesMap;
//	}
//	public Language getDictionaryLanguage() {
//		return mDefaultLanguage;
//	}	
//}