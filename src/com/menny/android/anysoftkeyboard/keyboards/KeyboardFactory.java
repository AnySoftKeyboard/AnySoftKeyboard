package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;

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
	public static final String SVORAK_KEYBOARD = "svorak_keyboard";
	public static final String COLEMAK_KEYBOARD = "colemak_keyboard";
	public static final String HEBREW_KEYBOARD = "heb_keyboard";
	public static final String RU_KEYBOARD = "ru_keyboard";
	public static final String RU_PH_KEYBOARD = "ru_ph_keyboard";
	public static final String ARABIC_KEYBOARD = "arabic_keyboard";
	public static final String LAO_KEYBOARD ="lao_keyboard";
	public static final String BG_PH_KEYBOARD = "bg_ph_keyboard";
	public static final String BG_BDS_KEYBOARD = "bg_bds_keyboard";
	public static final String DANISH_KEYBOARD = "danish_keyboard";
	public static final String NORWEGIAN_KEYBOARD = "norwegian_keyboard";
	public static final String FINNISH_SWEDISH_KEYBOARD = "finnish_swedish_keyboard";
	public static final String SPANISH_KEYBOARD = "es_keyboard";
	public static final String HUNGARIAN_KEYBOARD = "hungarian_keyboard";
	public static final String CATALAN_KEYBOARD = "catalan_keyboard";
	public static final String CH_FR_KEYBOARD = "ch_fr_keyboard";
	public static final String DE_KEYBOARD = "ch_de_keyboard";
	public static final String BE_CYRILLIC_KEYBOARD = "be_cyrillic";
	public static final String BE_LATIN_KEYBOARD = "be_latin";
	public static final String PT_KEYBOARD = "pt_keyboard";
	public static final String THAI_KEYBOARD = "thai_keyboard";
	public static final String FR_CA_KEYBOARD = "fr_ca_keyboard";
	public static final String KA_KEYBOARD = "ka_keyboard";
	public static final String RU_KEYBOARD_4_ROWS = "ru_keyboard_4_row";
	public static final String UK_KEYBOARD = "uk_keyboard";
	public static final String UK_KEYBOARD_4_ROWS = "uk_keyboard_4_row";
	public static final String BEPO_KEYBOARD = "bepo_keyboard";
	
	
	static
	{
		ms_creators = new ArrayList<KeyboardCreator>();
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new EnglishKeyboard(contextProvider);} public String getKeyboardPrefId() {return ENGLISH_KEYBOARD;}});
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.azerty);} public String getKeyboardPrefId() {return AZERTY_KEYBOARD;}});
		//issue 31
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.dvorak);} public String getKeyboardPrefId() {return DVORAK_KEYBOARD;}});
		
		//issue 178: colemak
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.colemak);} public String getKeyboardPrefId() {return COLEMAK_KEYBOARD;}});
		
		//issue 78: bepo
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.bepo);} public String getKeyboardPrefId() {return BEPO_KEYBOARD;}});
		
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new HebrewKeyboard(contextProvider);} public String getKeyboardPrefId() {return HEBREW_KEYBOARD;}});
		//issue 59 - Regular Russian layout
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianKeyboard(contextProvider);} public String getKeyboardPrefId() {return RU_KEYBOARD;}});
		//issue 26 - Russian keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianPhoneticKeyboard(contextProvider);} public String getKeyboardPrefId() {return RU_PH_KEYBOARD;}});
		//Arabic keyboard - issue 16 - no ready yet.
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new ArabicKeyboard(contextProvider);} public String getKeyboardPrefId() {return ARABIC_KEYBOARD;}});
		//BG - issue 25
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianBDSKeyboard(contextProvider);} public String getKeyboardPrefId() {return BG_BDS_KEYBOARD;}});
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianPhoneticKeyboard(contextProvider);} public String getKeyboardPrefId() {return BG_PH_KEYBOARD;}});
		
		//Lao keyboard - issue 10
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LaoKeyboard(contextProvider);} public String getKeyboardPrefId() {return LAO_KEYBOARD;}});
		
		//Issue 122: Danish keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.dk_qwerty);} public String getKeyboardPrefId() {return DANISH_KEYBOARD;}});
		
		//Issue 166: Norwegian keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.no_qwerty);} public String getKeyboardPrefId() {return NORWEGIAN_KEYBOARD;}});
		
		//Issue 39: Finnish/Swedish keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.fin_swedish_qwerty);} public String getKeyboardPrefId() {return FINNISH_SWEDISH_KEYBOARD;}});
		// issue 218: svorak
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.svorak);} public String getKeyboardPrefId() {return SVORAK_KEYBOARD;}});
		
		// issue 208: hungarian
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.hu_qwertz);} public String getKeyboardPrefId() {return HUNGARIAN_KEYBOARD;}});
		
		//Issue 54: Spanish keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.es_qwerty);} public String getKeyboardPrefId() {return SPANISH_KEYBOARD;}});
		
		//Issue 42: Catalan keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.catalan);} public String getKeyboardPrefId() {return CATALAN_KEYBOARD;}});
		
		//Issue 37: Swiss keyboards
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.ch_fr_qwerty);} public String getKeyboardPrefId() {return CH_FR_KEYBOARD;}});
		//Issue 114: French Canadian
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.ca_fr_qwerty);} public String getKeyboardPrefId() {return FR_CA_KEYBOARD;}});
		//Issue 86: German keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.de_qwerty);} public String getKeyboardPrefId() {return DE_KEYBOARD;}});
		//issue 105
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BelarusianCyrillicKeyboard(contextProvider);} public String getKeyboardPrefId() {return BE_CYRILLIC_KEYBOARD;}}); 
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BelarusianLatinKeyboard(contextProvider);} public String getKeyboardPrefId() {return BE_LATIN_KEYBOARD;}});
		//issue 108
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.pt_qwerty);} public String getKeyboardPrefId() {return PT_KEYBOARD;}});
		
		//Thai keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new ThaiKeyboard(contextProvider);} public String getKeyboardPrefId() {return THAI_KEYBOARD;}});
		//Georgian keyboard - issue 109
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.ka_qwerty);} public String getKeyboardPrefId() {return KA_KEYBOARD;}});
		//Ukrainian keyboard - issue 154
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new UkrainianKeyboard(contextProvider);} public String getKeyboardPrefId() {return UK_KEYBOARD;}});
		
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.esperanto);} public String getKeyboardPrefId() {return "esperanto_keyboard";}});
	}
	
	public static KeyboardCreator[] createAlphaBetKeyboards(AnyKeyboardContextProvider contextProvider)
	{
		Log.i("AnySoftKeyboard", "Creating keyboards. I have "+ ms_creators.size()+" creators");
		//Thread.dumpStack();
		ArrayList<KeyboardCreator> keyboards = new ArrayList<KeyboardCreator>();
		
		//getting shared prefs to determine which to create.
		SharedPreferences sharedPreferences = contextProvider.getSharedPreferences();
		for(int keyboardIndex=0; keyboardIndex<ms_creators.size(); keyboardIndex++)
		{
			KeyboardCreator creator = ms_creators.get(keyboardIndex);
			//the first keyboard is defaulted to true
			boolean keyboardIsEnabled = sharedPreferences.getBoolean(creator.getKeyboardPrefId(), keyboardIndex == 0);
			
			if (keyboardIsEnabled)
			{
				keyboards.add(creator/*.createKeyboard(contextProvider)*/);
			}
		}
		
		// Fix: issue 219
		// Check if there is any keyboards created if not, lets create a default english keyboard
		if( keyboards.size() == 0 ) {
			SharedPreferences.Editor editor = sharedPreferences.edit( );
			KeyboardCreator creator = ms_creators.get( 0 );
			editor.putBoolean( creator.getKeyboardPrefId( ) , true );
			editor.commit( );
			keyboards.add( creator );
		}
		
		for(KeyboardCreator aKeyboard : keyboards)
			Log.d("AnySoftKeyboard", "Factory provided creator: "+aKeyboard.getKeyboardPrefId());
		
		keyboards.trimToSize();
		KeyboardCreator[] keyboardsArray = new KeyboardCreator[keyboards.size()];
		return keyboards.toArray(keyboardsArray);
	}

}
