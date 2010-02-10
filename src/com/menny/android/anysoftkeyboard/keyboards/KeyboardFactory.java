package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.providers.AZERTYKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.ArabicKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.BepoKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.CatalanKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.ColemakKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.DVORAKKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.DanishKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.EnglishKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.GermanKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.NorwegianKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.SpanishKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.SvorakKeyboardProvider;
import com.menny.android.anysoftkeyboard.keyboards.providers.TerminalKeyboardProvider;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KeyboardFactory 
{
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

    private static final ArrayList<KeyboardCreator> ms_creators;

    public static final String HEBREW_KEYBOARD = "heb_keyboard";
    public static final String RU_KEYBOARD = "ru_keyboard";
    public static final String RU_PH_KEYBOARD = "ru_ph_keyboard";
    public static final String BG_PH_KEYBOARD = "bg_ph_keyboard";
    public static final String BG_BDS_KEYBOARD = "bg_bds_keyboard";
    public static final String HUNGARIAN_KEYBOARD = "hungarian_keyboard";    
    public static final String CH_FR_KEYBOARD = "ch_fr_keyboard";
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

        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new HebrewKeyboard(contextProvider);} public String getKeyboardPrefId() {return HEBREW_KEYBOARD;} public int getSortOrderValue() {return 31;}});
        //issue 59 - Regular Russian layout
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianKeyboard(contextProvider);} public String getKeyboardPrefId() {return RU_KEYBOARD;} public int getSortOrderValue() {return 41;}});
        //issue 26 - Russian keyboard
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianPhoneticKeyboard(contextProvider);} public String getKeyboardPrefId() {return RU_PH_KEYBOARD;} public int getSortOrderValue() {return 42;}});
        //BG - issue 25
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianBDSKeyboard(contextProvider);} public String getKeyboardPrefId() {return BG_BDS_KEYBOARD;} public int getSortOrderValue() {return 61;}});
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianPhoneticKeyboard(contextProvider);} public String getKeyboardPrefId() {return BG_PH_KEYBOARD;} public int getSortOrderValue() {return 62;}});

        // issue 208: hungarian
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.hu_qwertz, R.xml.hu_landscape );} public String getKeyboardPrefId() {return HUNGARIAN_KEYBOARD;} public int getSortOrderValue() {return 121;}});
        ms_creators.add(new KeyboardCreator() {
            public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {
                return new LatinKeyboard(contextProvider, R.xml.hu_qwertz_4_row, R.xml.hu_landscape);
            }

            public String getKeyboardPrefId() {
                return "hungarian_keyboard_4_row";
            }

            public int getSortOrderValue() {
                return 122;
            }
        });		

        //Issue 37: Swiss keyboards
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.ch_fr_qwerty);} public String getKeyboardPrefId() {return CH_FR_KEYBOARD;} public int getSortOrderValue() {return 141;}});
        //Issue 114: French Canadian
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.ca_fr_qwerty);} public String getKeyboardPrefId() {return FR_CA_KEYBOARD;} public int getSortOrderValue() {return 151;}});

        //issue 105
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BelarusianCyrillicKeyboard(contextProvider);} public String getKeyboardPrefId() {return BE_CYRILLIC_KEYBOARD;} public int getSortOrderValue() {return 171;}}); 
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BelarusianLatinKeyboard(contextProvider);} public String getKeyboardPrefId() {return BE_LATIN_KEYBOARD;} public int getSortOrderValue() {return 172;}});
        //issue 108
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.pt_qwerty);} public String getKeyboardPrefId() {return PT_KEYBOARD;} public int getSortOrderValue() {return 181;}});

        //Thai keyboard
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new ThaiKeyboard(contextProvider);} public String getKeyboardPrefId() {return THAI_KEYBOARD;} public int getSortOrderValue() {return 191;}});
        //Georgian keyboard - issue 109
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.ka_qwerty);} public String getKeyboardPrefId() {return KA_KEYBOARD;} public int getSortOrderValue() {return 201;}});
        //Ukrainian keyboard - issue 154
        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new UkrainianKeyboard(contextProvider);} public String getKeyboardPrefId() {return UK_KEYBOARD;} public int getSortOrderValue() {return 211;}});

        ms_creators.add(new KeyboardCreator(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new LatinKeyboard(contextProvider, R.xml.esperanto);} public String getKeyboardPrefId() {return "esperanto_keyboard";} public int getSortOrderValue() {return 221;}});
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
        //keyboardProviders.add(HebrewKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(ArabicKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(DanishKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(NorwegianKeyboardProvider.CONTENT_URI);
        //keyboardProviders.add(FinnishKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(CatalanKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(SvorakKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(GermanKeyboardProvider.CONTENT_URI);
    	keyboardProviders.add(SpanishKeyboardProvider.CONTENT_URI);               
    	
    	return keyboardProviders.toArray(new Uri[]{});
    }

    public static KeyboardCreator[] createAlphaBetKeyboards(AnyKeyboardContextProvider contextProvider)
    {
        Log.i("AnySoftKeyboard", "Creating keyboards. I have "+ ms_creators.size()+" creators");
        //Thread.dumpStack();
        final ArrayList<KeyboardCreator> keyboards = new ArrayList<KeyboardCreator>();

        //getting shared prefs to determine which to create.
        final SharedPreferences sharedPreferences = contextProvider.getSharedPreferences();
        final ContentResolver rc = contextProvider.getApplicationContext().getContentResolver();

        final Uri[] externalKeyboardsUris = getExternalKeyboardsUri(contextProvider.getApplicationContext());

        for(final Uri anExternalKeyboardUri : externalKeyboardsUris)
        {
            extractExternalKeyboardFromUri(contextProvider, keyboards, sharedPreferences, rc, anExternalKeyboardUri);
        }

        for(int keyboardIndex=0; keyboardIndex<ms_creators.size(); keyboardIndex++)
        {
            final KeyboardCreator creator = ms_creators.get(keyboardIndex);
            //the first keyboard is defaulted to true
            final boolean keyboardIsEnabled = sharedPreferences.getBoolean(creator.getKeyboardPrefId(), keyboardIndex == 0);

            if (keyboardIsEnabled)
            {
                keyboards.add(creator/*.createKeyboard(contextProvider)*/);
            }
        }

        //sorting the keyboards according to the requested
        //sort order (from minimum to maximum)
        Collections.sort(keyboards, new Comparator<KeyboardCreator>()
                {
            public int compare(KeyboardCreator k1, KeyboardCreator k2) 
            {
                return k1.getSortOrderValue() - k2.getSortOrderValue();
            }
                });

        // Fix: issue 219
        // Check if there is any keyboards created if not, lets create a default english keyboard
        if( keyboards.size() == 0 ) {
            final SharedPreferences.Editor editor = sharedPreferences.edit( );
            final KeyboardCreator creator = ms_creators.get( 0 );
            editor.putBoolean( creator.getKeyboardPrefId( ) , true );
            editor.commit( );
            keyboards.add( creator );
        }

        for(final KeyboardCreator aKeyboard : keyboards) {
            Log.d("AnySoftKeyboard", "Factory provided creator: "+aKeyboard.getKeyboardPrefId());
        }

        keyboards.trimToSize();
        final KeyboardCreator[] keyboardsArray = new KeyboardCreator[keyboards.size()];
        return keyboards.toArray(keyboardsArray);
    }

    private static void extractExternalKeyboardFromUri(
            AnyKeyboardContextProvider contextProvider,
            ArrayList<KeyboardCreator> keyboards,
            SharedPreferences sharedPreferences, ContentResolver rc,
            final Uri externalKeyboardUri) {
        final Cursor c = rc.query(externalKeyboardUri, null, null, null, null);

        if (c.moveToFirst())
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

            do
            {
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
                final boolean keyboardIsEnabled = sharedPreferences.getBoolean(prefId, keyboards.size() == 0);
                if (keyboardIsEnabled)
                {
                    final KeyboardCreator creator = new KeyboardCreatorImpl(contextProvider.getApplicationContext(), 
                            prefId, nameId, resId, landscapeResId, defaultDictionary, iconResId,
                            qwertyTranslation,
                            additionalIsLetterExceptions,
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
    
    public static class KeyboardsResolverActivity extends Activity
    {
    	private String mKeyboardProviderUri;
    	private final Object mMonitor = new Object();
    	
    	@Override
    	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    		super.onActivityResult(requestCode, resultCode, data);
    		if (resultCode == RESULT_OK)
    		{
    			mKeyboardProviderUri = data.getStringExtra("keyboardContentProviderUri");
    			Log.i("ASK KeyboardsResolverActivity", "Got result: "+mKeyboardProviderUri);
    		}
    		else
    		{
    			Log.w("ASK KeyboardsResolverActivity", "Got ERROR result: "+resultCode);
    		}
    		mMonitor.notifyAll();
    	}
    	
    	public String getKeyboardProviderUri(ResolveInfo info)
    	{
    		Intent intent = new Intent();
            intent.setClassName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
            Log.d("ASK KeyboardsResolverActivity", "Located external activity "+intent.getComponent().toString());
    		try {
    			startActivityForResult(intent, 1);
				mMonitor.wait(5000);
			} catch (Exception e) {
				e.printStackTrace();
				Log.w("ASK KeyboardsResolverActivity", "Failed receiving keyboard provider URI from external activity.");
			}
			
			return mKeyboardProviderUri;
    	}
    }
}
