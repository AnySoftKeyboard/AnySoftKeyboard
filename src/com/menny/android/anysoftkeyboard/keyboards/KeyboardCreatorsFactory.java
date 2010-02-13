package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.AZERTYKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.ArabicKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.BepoKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.CatalanKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.ColemakKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.DVORAKKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.DanishKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.EnglishKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.EsperantoKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.FinnishKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.FrenchCanadaKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.GeorgianKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.GermanKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.Hungarian4RowsKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.HungarianKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.NorwegianKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.PtKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.SpanishKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.SvorakKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.SwissKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.TerminalKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.ThaiKeyboardProvider;

public class KeyboardCreatorsFactory {
	
	public interface KeyboardCreator
    {
        AnyKeyboard createKeyboard(AnyKeyboardContextProvider context);
        String getKeyboardPrefId();
        int getSortOrderValue();
    }

    private static class KeyboardCreatorImpl implements KeyboardCreator
    {
        private final String mPrefId;
        private final int mNameId;
        private final int mResId;
        private final int mLandscapeResId;
        private final int mIconResId;
        private final String mDefaultDictionary;
        private final String mQwertyTranslation;
        private final String mAdditionalIsLetterExceptions;
        private final int mSortValue;

        public KeyboardCreatorImpl(Context context, String prefId, String resNameId, String resId, String resLandscapeId, String defaultDictionary, String resIconId, String qwertyTranslation, String additionalIsLetterExceptions, int sortOrderValue)
        {
            mPrefId = prefId;
            mNameId = context.getResources().getIdentifier(resNameId, null, null);
            mResId = context.getResources().getIdentifier(resId, null, null);
            if ((resLandscapeId == null) || (resLandscapeId.length() == 0)) {
                mLandscapeResId = mResId;
            } else {
                mLandscapeResId = context.getResources().getIdentifier(resLandscapeId, null, null);
            }
            mDefaultDictionary = defaultDictionary;
            mIconResId = context.getResources().getIdentifier(resIconId, null, null);

            mAdditionalIsLetterExceptions = additionalIsLetterExceptions;

            mQwertyTranslation = qwertyTranslation;

            mSortValue = sortOrderValue;
            Log.d("ASK KeyboardCreatorImpl", "Creator for "+prefId+" res: "+resId+" is actually:"+ mResId+" LandscapeRes: "+resLandscapeId+" is actually:"+ mLandscapeResId+" dictionary: "+mDefaultDictionary);
        }

        public AnyKeyboard createKeyboard(AnyKeyboardContextProvider context) {
            return new ExternalAnyKeyboard(context, mResId, mLandscapeResId, mPrefId, mNameId, mIconResId, mQwertyTranslation, mDefaultDictionary, mAdditionalIsLetterExceptions);
        }

        public String getKeyboardPrefId() {
            return mPrefId;
        }

        public int getSortOrderValue() {
            return mSortValue;
        }
    }

    private static final ArrayList<KeyboardCreator> ms_internalCreators;
	
    static
    {
        ms_internalCreators = new ArrayList<KeyboardCreator>();

        ms_internalCreators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new HebrewKeyboard(contextProvider);} public String getKeyboardPrefId() {return "heb_keyboard";} public int getSortOrderValue() {return 31;}});
        //issue 59 - Regular Russian layout
        ms_internalCreators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianKeyboard(contextProvider);} public String getKeyboardPrefId() {return "ru_keyboard";} public int getSortOrderValue() {return 41;}});
        //issue 26 - Russian keyboard
        ms_internalCreators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianPhoneticKeyboard(contextProvider);} public String getKeyboardPrefId() {return "ru_ph_keyboard";} public int getSortOrderValue() {return 42;}});
        //BG - issue 25
        ms_internalCreators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianBDSKeyboard(contextProvider);} public String getKeyboardPrefId() {return "bg_bds_keyboard";} public int getSortOrderValue() {return 61;}});
        ms_internalCreators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianPhoneticKeyboard(contextProvider);} public String getKeyboardPrefId() {return "bg_ph_keyboard";} public int getSortOrderValue() {return 62;}});

        //issue 105
        ms_internalCreators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BelarusianCyrillicKeyboard(contextProvider);} public String getKeyboardPrefId() {return "be_cyrillic";} public int getSortOrderValue() {return 171;}}); 
        ms_internalCreators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BelarusianLatinKeyboard(contextProvider);} public String getKeyboardPrefId() {return "be_latin";} public int getSortOrderValue() {return 172;}});

        //Ukrainian keyboard - issue 154
        ms_internalCreators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new UkrainianKeyboard(contextProvider);} public String getKeyboardPrefId() {return "uk_keyboard";} public int getSortOrderValue() {return 211;}});
    }
    
    private static ArrayList<KeyboardCreator> ms_creators = null;

	public synchronized static ArrayList<KeyboardCreator> getAllCreators(AnyKeyboardContextProvider contextProvider) {

		if (ms_creators == null)
		{
			ArrayList<KeyboardCreator> keyboards = new ArrayList<KeyboardCreator>(ms_internalCreators);
			
			final ContentResolver rc = contextProvider.getApplicationContext().getContentResolver();
	
	        final Uri[] externalKeyboardsUris = getExternalKeyboardsUri(contextProvider.getApplicationContext());
	
	        for(final Uri anExternalKeyboardUri : externalKeyboardsUris)
	        {
	        	Log.d("ASK Keyboard Creators", "Extracting keyboard from:"+anExternalKeyboardUri);
	            extractExternalKeyboardFromUri(contextProvider, keyboards, rc, anExternalKeyboardUri);
	        }
	        ms_creators = keyboards;
		}
		
        return ms_creators;
	}
    
	private static void extractExternalKeyboardFromUri(
            AnyKeyboardContextProvider contextProvider,
            ArrayList<KeyboardCreator> keyboards, ContentResolver rc,
            final Uri externalKeyboardUri) {
        final Cursor c = rc.query(externalKeyboardUri, null, null, null, null);

        if ((c != null) && (c.moveToFirst()))
        {
            do
            {
            	String prefId = null;
                String nameId = null;
                String resId = null;
                String landscapeResId = null;
                String iconResId = "com.menny.android.anysoftkeyboard:drawable/sym_keyboard_notification_icon";
                String defaultDictionary = "None";
                String additionalIsLetterExceptions = null;
                int sortValue = Integer.MAX_VALUE;
                String qwertyTranslation = null;
                
                final int prefIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_PREF_ID);
                final int nameIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_NAME_RES_ID);
                final int resIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_LAYOUT_RES_ID);
                final int landscapeRedIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_LAYOUT_LANDSCAPE_RES_ID);
                final int iconIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_ICON_RES_ID);
                final int defaultDictionaryIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_DICTIONARY);
                final int additionalIsLetterExceptionsIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_ADDITIONAL_IS_LETTER_EXCEPTIONS);
                final int qwertyTranslationIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_HARD_QWERTY_TRANSLATION);
                final int sortValueIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_SORT_ORDER);

                if (prefIdIndex >= 0) {
                    prefId = c.getString(prefIdIndex);
                }
                if (nameIdIndex >= 0) {
                    nameId = c.getString(nameIdIndex);
                }
                if (resIdIndex >= 0) {
                    resId = c.getString(resIdIndex);
                }
                if (landscapeRedIdIndex >= 0) {
                    landscapeResId = c.getString(landscapeRedIdIndex);
                }
                if (iconIdIndex >= 0) {
                    iconResId = c.getString(iconIdIndex);
                }
                if (defaultDictionaryIndex >= 0) {
                    defaultDictionary = c.getString(defaultDictionaryIndex);
                }
                if (additionalIsLetterExceptionsIndex >= 0) {
                    additionalIsLetterExceptions = c.getString(additionalIsLetterExceptionsIndex);
                }
                if (qwertyTranslationIndex >= 0) {
                    qwertyTranslation = c.getString(qwertyTranslationIndex);
                }
                if (sortValueIndex >= 0) {
                    sortValue = c.getInt(sortValueIndex);
                }
                
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
                    KeyboardCreator creator = new KeyboardCreatorImpl(contextProvider.getApplicationContext(), 
                                prefId, nameId, resId, landscapeResId, defaultDictionary, iconResId,
                                qwertyTranslation,
                                additionalIsLetterExceptions,
                                sortValue);

                    Log.d("ASK Keyboards", "External keyboard "+prefId+" will have a creator. URI:"+externalKeyboardUri);
                    keyboards.add(creator);
                }
            }while(c.moveToNext());
        }
        else
        {
            Log.w("ASK Keyboards", "** No keyboards were located in ContentProviders in URI:"+externalKeyboardUri);
        }
    }
    
	private static Uri[] getExternalKeyboardsUri(Context applicationContext) {
    	Intent keyboards = new Intent("com.menny.android.anysoftkeyboard.KEYBOARD");
    	List<ResolveInfo> keyboardActivities = applicationContext.getPackageManager().queryIntentActivities(keyboards,PackageManager.MATCH_DEFAULT_ONLY);
    	List<Uri> keyboardProviders = new ArrayList<Uri>();
    	Log.d("ASK Factory", "Located "+keyboardActivities.size()+" external keyboards activities.");
    	for(ResolveInfo info : keyboardActivities)
    	{
    		KeyboardsResolverActivity resolver = new KeyboardsResolverActivity();
    		String uri = resolver.getKeyboardProviderUri(info);
    		if (uri != null)
    			keyboardProviders.add(Uri.parse(uri));            
    	}
    	
    	//adding internal URI - this is for now.
    	keyboardProviders.add(EnglishKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(AZERTYKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(DVORAKKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(ColemakKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(BepoKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(TerminalKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(ArabicKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(DanishKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(NorwegianKeyboardProvider.CONTENT_URI);
        keyboardProviders.add(FinnishKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(CatalanKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(SvorakKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(GermanKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(SpanishKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(HungarianKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(Hungarian4RowsKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(SwissKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(FrenchCanadaKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(PtKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(ThaiKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(GeorgianKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(EsperantoKeyboardProvider.CONTENT_URI);

    	return keyboardProviders.toArray(new Uri[]{});
    }
}
