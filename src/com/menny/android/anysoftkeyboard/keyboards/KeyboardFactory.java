package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;

import com.menny.android.anysoftkeyboard.R;

import android.content.SharedPreferences;
import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;

public class KeyboardFactory 
{
	public interface KeyboardCreator
	{
		AnyKeyboard createKeyboard(AnyKeyboardContextProvider context);
		String getKeyboardPrefId();
	}
	
	private static final ArrayList<KeyboardCreator> ms_creators;
	
	private static final String ENGLISH_KEYBOARD = "eng_keyboard";
	private static final String AZERTY_KEYBOARD = "azerty_keyboard";
	private static final String DVORAK_KEYBOARD = "dvorak_keyboard";
	private static final String HEBREW_KEYBOARD = "heb_keyboard";
	private static final String RU_KEYBOARD = "ru_keyboard";
	private static final String RU_PH_KEYBOARD = "ru_ph_keyboard";
	//public static final String ARABIC_KEYBOARD = "arabic_keyboard";
	private static final String LAO_KEYBOARD ="lao_keyboard";
	private static final String BG_PH_KEYBOARD = "bg_ph_keyboard";
	private static final String BG_BDS_KEYBOARD = "bg_bds_keyboard";
	private static final String FINNISH_SWEDISH_KEYBOARD = "finnish_swedish_keyboard";
	private static final String SPANISH_KEYBOARD = "es_keyboard";
	private static final String CATALAN_KEYBOARD = "catalan_keyboard";
	private static final String CH_FR_KEYBOARD = "ch_fr_keyboard";
	private static final String DE_KEYBOARD = "ch_de_keyboard";//this is the preferences key. Do not change it.
	
	static
	{
		ms_creators = new ArrayList<KeyboardCreator>();
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new EnglishKeyboard(contextProvider);} public String getKeyboardPrefId() {return ENGLISH_KEYBOARD;}});
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.azerty, R.string.azerty_keyboard, Dictionary.Language.French);} public String getKeyboardPrefId() {return AZERTY_KEYBOARD;}});
		//issue 31
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.dvorak, R.string.dvorak_keyboard, Dictionary.Language.English);} public String getKeyboardPrefId() {return DVORAK_KEYBOARD;}});
		
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new HebrewKeyboard(contextProvider);} public String getKeyboardPrefId() {return HEBREW_KEYBOARD;}});
		//issue 59 - Regular Russian layout
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianKeyboard(contextProvider);} public String getKeyboardPrefId() {return RU_KEYBOARD;}});
		//issue 26 - Russian keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianPhoneticKeyboard(contextProvider);} public String getKeyboardPrefId() {return RU_PH_KEYBOARD;}});
		//Arabic keyboard - issue 16 - no ready yet.
		//keyboards.add(new ArabicKeyboard(contextProvider));
		//BG - issue 25
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianBDSKeyboard(contextProvider);} public String getKeyboardPrefId() {return BG_BDS_KEYBOARD;}});
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianPhoneticKeyboard(contextProvider);} public String getKeyboardPrefId() {return BG_PH_KEYBOARD;}});
		
		//Lao keyboard - issue 10
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LaoKeyboard(contextProvider);} public String getKeyboardPrefId() {return LAO_KEYBOARD;}});
		
		//Issue 39:  	 Finnish/Swedish keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.fin_swedish_qwerty, R.string.finnish_swedish_keyboard, Dictionary.Language.Swedish);} public String getKeyboardPrefId() {return FINNISH_SWEDISH_KEYBOARD;}});
		
		//Issue 54: Spanish keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.es_qwerty, R.string.es_keyboard, Dictionary.Language.Spanish);} public String getKeyboardPrefId() {return SPANISH_KEYBOARD;}});
		
		//Issue 42: Catalan keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.catalan, R.string.catalan_keyboard, Dictionary.Language.Spanish);} public String getKeyboardPrefId() {return CATALAN_KEYBOARD;}});
		
		//Issue 37: Swiss keyboards
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.ch_fr_qwerty, R.string.ch_fr_keyboard, Dictionary.Language.French);} public String getKeyboardPrefId() {return CH_FR_KEYBOARD;}});
		//Issue 86: German keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.de_qwerty, R.string.de_keyboard, Dictionary.Language.German);} public String getKeyboardPrefId() {return DE_KEYBOARD;}});		
	}
	
	public static ArrayList<AnyKeyboard> createAlphaBetKeyboards(AnyKeyboardContextProvider contextProvider)
	{
		Log.i("AnySoftKeyboard", "Creating keyboards. I have "+ ms_creators.size()+" creators");
		//Thread.dumpStack();
		
		ArrayList<AnyKeyboard> keyboards = new ArrayList<AnyKeyboard>();
		
		//getting shared prefs to determine which to create.
		SharedPreferences sharedPreferences = contextProvider.getSharedPreferences();
		for(int keyboardIndex=0; keyboardIndex<ms_creators.size(); keyboardIndex++)
		{
			KeyboardCreator creator = ms_creators.get(keyboardIndex);
			//the first keyboard is defaulted to true
			boolean keyboardIsEnabled = sharedPreferences.getBoolean(creator.getKeyboardPrefId(), keyboardIndex == 0);
			
			if (keyboardIsEnabled)
			{
				keyboards.add(creator.createKeyboard(contextProvider));
			}
		}
		
		for(AnyKeyboard aKeyboard : keyboards)
			Log.d("AnySoftKeyboard", "Factory created: "+aKeyboard.getKeyboardName());
		
		return keyboards;
	}

}
