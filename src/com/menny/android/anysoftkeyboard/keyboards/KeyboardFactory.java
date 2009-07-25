package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;
import java.util.HashMap;

import com.menny.android.anysoftkeyboard.R;

import android.content.SharedPreferences;
import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class KeyboardFactory 
{
	public interface KeyboardCreator
	{
		AnyKeyboard createKeyboard(AnyKeyboardContextProvider context);
		String getKeyboardPrefId();
	}
	
	private static final ArrayList<KeyboardCreator> ms_creators;
	
	public static final String ENGLISH_KEYBOARD = "eng_keyboard";
	public static final String AZERTY_KEYBOARD = "azerty_keyboard";
	public static final String DVORAK_KEYBOARD = "dvorak_keyboard";
	public static final String HEBREW_KEYBOARD = "heb_keyboard";
	public static final String RU_PH_KEYBOARD = "ru_ph_keyboard";
	//public static final String ARABIC_KEYBOARD = "arabic_keyboard";
	public static final String LAO_KEYBOARD ="lao_keyboard";
	public static final String BG_PH_KEYBOARD = "bg_ph_keyboard";
	public static final String BG_BDS_KEYBOARD = "bg_bds_keyboard";
	public static final String FINNISH_SWEDISH_KEYBOARD = "finnish_swedish_keyboard";
	public static final String SPANISH_KEYBOARD = "es_keyboard";
	public static final String CATALAN_KEYBOARD = "catalan_keyboard";
	public static final String CH_FR_KEYBOARD = "ch_fr_keyboard";
	public static final String CH_DE_KEYBOARD = "ch_de_keyboard";
	
	static
	{
		ms_creators = new ArrayList<KeyboardCreator>();
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new EnglishKeyboard(contextProvider, ENGLISH_KEYBOARD);} public String getKeyboardPrefId() {return ENGLISH_KEYBOARD;}});
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.azerty, R.string.azerty_keyboard, AZERTY_KEYBOARD);} public String getKeyboardPrefId() {return AZERTY_KEYBOARD;}});
		//issue 31
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.dvorak, R.string.dvorak_keyboard, DVORAK_KEYBOARD);} public String getKeyboardPrefId() {return DVORAK_KEYBOARD;}});
		
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new HebrewKeyboard(contextProvider, HEBREW_KEYBOARD);} public String getKeyboardPrefId() {return HEBREW_KEYBOARD;}});
		//issue 26 - Russian keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianPhoneticKeyboard(contextProvider, RU_PH_KEYBOARD);} public String getKeyboardPrefId() {return RU_PH_KEYBOARD;}});
		//Arabic keyboard - issue 16 - no ready yet.
		//keyboards.add(new ArabicKeyboard(contextProvider));
		//BG - issue 25
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianBDSKeyboard(contextProvider, BG_BDS_KEYBOARD);} public String getKeyboardPrefId() {return BG_BDS_KEYBOARD;}});
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianPhoneticKeyboard(contextProvider, BG_PH_KEYBOARD);} public String getKeyboardPrefId() {return BG_PH_KEYBOARD;}});
		
		//Lao keyboard - issue 10
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LaoKeyboard(contextProvider, LAO_KEYBOARD);} public String getKeyboardPrefId() {return LAO_KEYBOARD;}});
		
		//Issue 39:  	 Finnish/Swedish keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.fin_swedish_qwerty, R.string.finnish_swedish_keyboard, FINNISH_SWEDISH_KEYBOARD);} public String getKeyboardPrefId() {return FINNISH_SWEDISH_KEYBOARD;}});
		
		//Issue 54: Spanish keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.es_qwerty, R.string.es_keyboard, SPANISH_KEYBOARD);} public String getKeyboardPrefId() {return SPANISH_KEYBOARD;}});
		
		//Issue 42: Catalan keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.catalan, R.string.catalan_keyboard, CATALAN_KEYBOARD);} public String getKeyboardPrefId() {return CATALAN_KEYBOARD;}});
		
		//Issue 37: Swiss keyboards
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.ch_fr_qwerty, R.string.ch_fr_keyboard, CH_FR_KEYBOARD);} public String getKeyboardPrefId() {return CH_FR_KEYBOARD;}});
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.ch_de_qwerty, R.string.ch_fr_keyboard, CH_DE_KEYBOARD);} public String getKeyboardPrefId() {return CH_DE_KEYBOARD;}});		
	}
	
	public static synchronized ArrayList<AnyKeyboard> createAlphaBetKeyboards(AnyKeyboardContextProvider contextProvider)
	{
		Log.d("AnySoftKeyboard", "No keyboards exist! Creating what needed. I have "+ ms_creators.size()+" creators");
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
		
		return keyboards;
		
//		if (!checkIfKeyboardExists(ENGLISH_KEYBOARD))
//			ms_keyboards.add(0, new EnglishKeyboard(contextProvider));
//		//issue 36: AZERTY keyboard
//		if (!checkIfKeyboardExists(AZERTY_KEYBOARD))
//			ms_keyboards.add(1, new LatinKeyboard(contextProvider, R.xml.azerty, R.string.azerty_keyboard, AZERTY_KEYBOARD));
//		//issue 31
//		if (!checkIfKeyboardExists(DVORAK_KEYBOARD))
//			ms_keyboards.add(2, new LatinKeyboard(contextProvider, R.xml.dvorak, R.string.dvorak_keyboard, DVORAK_KEYBOARD));
//		if (!checkIfKeyboardExists(HEBREW_KEYBOARD))
//			ms_keyboards.add(3, new HebrewKeyboard(contextProvider));
//		//issue 26 - Russian keyboard
//		if (!checkIfKeyboardExists(RU_PH_KEYBOARD))
//			ms_keyboards.add(4, new RussianPhoneticKeyboard(contextProvider));
//		//Arabic keyboard - issue 16 - no ready yet.
//		//keyboards.add(new ArabicKeyboard(contextProvider));
//		//BG - issue 25
//		if (!checkIfKeyboardExists(BG_BDS_KEYBOARD))
//			ms_keyboards.add(5, new BulgarianBDSKeyboard(contextProvider));
//		if (!checkIfKeyboardExists(BG_PH_KEYBOARD))
//			ms_keyboards.add(0, new BulgarianPhoneticKeyboard(contextProvider));
//		
//		//Lao keyboard - issue 10
//		if (!checkIfKeyboardExists(LAO_KEYBOARD))
//			ms_keyboards.add(0, new LaoKeyboard(contextProvider));
//		
//		//Issue 39:  	 Finnish/Swedish keyboard
//		if (!checkIfKeyboardExists(FINNISH_SWEDISH_KEYBOARD))
//			ms_keyboards.add(0, new LatinKeyboard(contextProvider, R.xml.fin_swedish_qwerty, R.string.finnish_swedish_keyboard, FINNISH_SWEDISH_KEYBOARD));
//		
//		//Issue 54: Spanish keyboard
//		if (!checkIfKeyboardExists(SPANISH_KEYBOARD))
//			ms_keyboards.add(0, new LatinKeyboard(contextProvider, R.xml.es_qwerty, R.string.es_keyboard, SPANISH_KEYBOARD));
//		
//		//Issue 42: Catalan keyboard
//		if (!checkIfKeyboardExists(CATALAN_KEYBOARD))
//			ms_keyboards.add(0, new LatinKeyboard(contextProvider, R.xml.catalan, R.string.catalan_keyboard, CATALAN_KEYBOARD));
//		
//		//Issue 37: Swiss keyboards
//		if (!checkIfKeyboardExists(CH_FR_KEYBOARD))
//			ms_keyboards.add(0, new LatinKeyboard(contextProvider, R.xml.ch_fr_qwerty, R.string.ch_fr_keyboard, CH_FR_KEYBOARD));
//		if (!checkIfKeyboardExists(CH_DE_KEYBOARD))
//			ms_keyboards.add(0, new LatinKeyboard(contextProvider, R.xml.ch_de_qwerty, R.string.ch_fr_keyboard, CH_DE_KEYBOARD));
//		
//		Log.i("AnySoftKeyboard", "KeyboardFactory created "+ms_keyboards.size()+" keyboards.");
//        return ms_keyboards;
	}

}
