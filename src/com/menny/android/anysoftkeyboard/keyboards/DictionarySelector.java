package com.menny.android.anysoftkeyboard.keyboards;

public interface DictionarySelector {
	String getDictionaryLanguage();
}

class DefaultDictionarySelector implements DictionarySelector
{
	private final String mDefaultLanguage;
	public DefaultDictionarySelector(String language)
	{
		mDefaultLanguage = language;
	}
	public String getDictionaryLanguage() {
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