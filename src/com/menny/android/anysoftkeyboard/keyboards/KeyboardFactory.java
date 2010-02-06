package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;

import com.menny.android.anysoftkeyboard.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.AZERTYKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.BepoKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.ColemakKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.DVORAKKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.EnglishKeyboardProvider;

public class KeyboardFactory 
{
	public interface KeyboardCreator
	{
		AnyKeyboard createKeyboard(AnyKeyboardContextProvider context);
		String getKeyboardPrefId();
	}
	
	private static class KeyboardCreatorImpl implements KeyboardCreator
	{
		private final String mPrefId;
		private final int mNameId;
		private final int mResId;
		private final int mLandscapeResId;
		private final int mIconResId;
		private final String mDefaultDictionary;
		private final int mSortValue;
		
		public KeyboardCreatorImpl(Context context, String prefId, String resNameId, String resId, String resLandscapeId, String defaultDictionary, String resIconId, int sortOrderValue)
		{
			mPrefId = prefId;
			mNameId = context.getResources().getIdentifier(resNameId, null, null);
			mResId = context.getResources().getIdentifier(resId, null, null);
			if ((resLandscapeId == null) || (resLandscapeId.length() == 0))
				mLandscapeResId = mResId;
			else
				mLandscapeResId = context.getResources().getIdentifier(resLandscapeId, null, null);
			mDefaultDictionary = defaultDictionary;
			mIconResId = context.getResources().getIdentifier(resIconId, null, null);
			
			mSortValue = sortOrderValue;
			Log.d("ASK KeyboardCreatorImpl", "Creator for "+prefId+" res: "+resId+" is actually:"+ mResId+" LandscapeRes: "+resLandscapeId+" is actually:"+ mLandscapeResId+" dictionary: "+mDefaultDictionary);
		}
		
		public AnyKeyboard createKeyboard(AnyKeyboardContextProvider context) {
			return new ExternalAnyKeyboard(context, mResId, mLandscapeResId, mPrefId, mNameId, mIconResId, mDefaultDictionary);
		}

		public String getKeyboardPrefId() {
			return mPrefId;
		}
		
		public int getSortOrederValue() {
			return mSortValue;
		}
	}
	
	private static final ArrayList<KeyboardCreator> ms_creators;
	
	public static final String SVORAK_KEYBOARD = "svorak_keyboard";
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
	
	
	static
	{
		ms_creators = new ArrayList<KeyboardCreator>();
		
		// Terminal-friendly keyboard
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.terminal);} public String getKeyboardPrefId() {return "terminal_keyboard";}});

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
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.hu_qwertz, R.xml.hu_landscape );} public String getKeyboardPrefId() {return HUNGARIAN_KEYBOARD;}});
		ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.hu_qwertz_4_row, R.xml.hu_landscape );} public String getKeyboardPrefId() {return "hungarian_keyboard_4_row";}});
		
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
		ContentResolver rc = contextProvider.getApplicationContext().getContentResolver();
		
		Uri[] externalKeyboardsUris = getExternalKeyboardsUri(contextProvider.getApplicationContext());
		
		for(Uri anExternalKeyboardUri : externalKeyboardsUris)
		{
			extractExternalKeyboardFromUri(contextProvider, keyboards, sharedPreferences, rc, anExternalKeyboardUri);
		}
		
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

	private static Uri[] getExternalKeyboardsUri(Context applicationContext) {
		return new Uri[]
			{
				EnglishKeyboardProvider.CONTENT_URI,
				AZERTYKeyboardProvider.CONTENT_URI,
				DVORAKKeyboardProvider.CONTENT_URI,
				ColemakKeyboardProvider.CONTENT_URI,
				BepoKeyboardProvider.CONTENT_URI
			};
	}

	private static void extractExternalKeyboardFromUri(
			AnyKeyboardContextProvider contextProvider,
			ArrayList<KeyboardCreator> keyboards,
			SharedPreferences sharedPreferences, ContentResolver rc,
			final Uri externalKeyboardUri) {
		Cursor c = rc.query(externalKeyboardUri, null, null, null, null);
		
		if (c.moveToFirst())
		{
			String prefId = null;
			String nameId = null;
			String resId = null;
			String landscapeResId = null;
			String iconResId = "com.menny.android.anysoftkeyboard:drawable/sym_keyboard_notification_icon";
			String defaultDictionary = "None";
			int sortValue = Integer.MAX_VALUE;
			
			do
			{
				final int prefIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_PREF_ID);
				final int nameIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_NAME_RES_ID);
				final int resIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_LAYOUT_RES_ID);
				final int landscapeRedIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_LAYOUT_LANDSCAPE_RES_ID);
				final int iconIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_ICON_RES_ID);
				final int defaultDictionaryIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_DICTIONARY);
				final int sortValueIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_SORT_ORDER);
				
				if (prefIdIndex >= 0)
					prefId = c.getString(prefIdIndex);
				if (nameIdIndex >= 0)
					nameId = c.getString(nameIdIndex);
				if (resIdIndex >= 0)
					resId = c.getString(resIdIndex);
				if (landscapeRedIdIndex >= 0)
					landscapeResId = c.getString(landscapeRedIdIndex);
				if (iconIdIndex >= 0)
					iconResId = c.getString(iconIdIndex);
				if (defaultDictionaryIndex >= 0)
					defaultDictionary = c.getString(defaultDictionaryIndex);
				if (sortValueIndex >= 0)
					sortValue = c.getInt(sortValueIndex);
				
				
			}while(c.moveToNext());
			
			//asserting
			if ((prefId == null) ||
				(nameId == null) ||
				(resId == null))
			{
				Log.e("ASK Keyboards", "External Keyboard does not include all mandatory details! Will not create keyboard in URI:"+externalKeyboardUri);
			}
			else
			{
				Log.d("ASK KeyboardFactory", "External keyboard details: prefId:"+prefId+" nameId:"+nameId+" resId:"+resId+" landscapeResId:"+landscapeResId+" iconResId:"+iconResId+" defaultDictionary:"+defaultDictionary+" sortValue:"+sortValue);
				boolean keyboardIsEnabled = sharedPreferences.getBoolean(prefId, keyboards.size() == 0);
				if (keyboardIsEnabled)
				{
					KeyboardCreator creator = new KeyboardCreatorImpl(contextProvider.getApplicationContext(), 
							prefId, nameId, resId, landscapeResId, defaultDictionary, iconResId,
							sortValue);
					
					Log.d("ASK Keyboards", "External keyboard "+prefId+" will have a creator. URI:"+externalKeyboardUri);
					keyboards.add(creator);
				}
			}
		}
		else
		{
			Log.d("ASK Keyboards", "No keyboards were located in ContentProviders in URI:"+externalKeyboardUri);
		}
	}
}
