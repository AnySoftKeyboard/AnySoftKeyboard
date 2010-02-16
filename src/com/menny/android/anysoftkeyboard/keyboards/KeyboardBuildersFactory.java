package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.R;

public class KeyboardBuildersFactory {
	
	public interface KeyboardBuilder
    {
        AnyKeyboard createKeyboard(AnyKeyboardContextProvider context);
        String getKeyboardPrefId();
        int getSortOrderValue();
        int getKeyboardNameResId();
        String getDescription();
    }

    private static class KeyboardBuilderImpl implements KeyboardBuilder
    {
        private final String mPrefId;
        private final int mNameId;
        private final int mResId;
        private final int mLandscapeResId;
        private final int mIconResId;
        private final String mDefaultDictionary;
        private final int mQwertyTranslationId;
        private final String mAdditionalIsLetterExceptions;
        private final int mSortValue;
        private final String mDescription;

        public KeyboardBuilderImpl(Context context, String prefId, int nameId,
				int layoutResId, int landscapeLayoutResId,
				String defaultDictionary, int iconResId,
				int physicalTranslationResId,
				String additionalIsLetterExceptions, int sortValue,
				String description) 
        {
        	mPrefId = prefId;
            mNameId = nameId;
            mResId = layoutResId;
            if (landscapeLayoutResId == -1){
                mLandscapeResId = mResId;
            } else {
                mLandscapeResId = landscapeLayoutResId;
            }
            mDefaultDictionary = defaultDictionary;
            mIconResId = iconResId;
            mAdditionalIsLetterExceptions = additionalIsLetterExceptions;
            mQwertyTranslationId = physicalTranslationResId;
            mSortValue = sortValue;
            mDescription = description;
            Log.d("ASK KeyboardCreatorImpl", "Creator for "+mPrefId+" res: "+ mResId+" LandscapeRes: "+ mLandscapeResId+" dictionary: "+mDefaultDictionary);
		}

		public AnyKeyboard createKeyboard(AnyKeyboardContextProvider context) {
            return new ExternalAnyKeyboard(context, mResId, mLandscapeResId, mPrefId, mNameId, mIconResId, mQwertyTranslationId, mDefaultDictionary, mAdditionalIsLetterExceptions);
        }

		public int getKeyboardNameResId() {return mNameId;}
		public String getDescription() {return mDescription;}
        
        public String getKeyboardPrefId() {
            return mPrefId;
        }

        public int getSortOrderValue() {
            return mSortValue;
        }
    }

    private static final ArrayList<KeyboardBuilder> ms_internalCreators;
	
    static
    {
        ms_internalCreators = new ArrayList<KeyboardBuilder>();

        //issue 59 - Regular Russian layout
        ms_internalCreators.add(new KeyboardBuilder(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianKeyboard(contextProvider);} public String getKeyboardPrefId() {return "ru_keyboard";} public int getSortOrderValue() {return 41;} public int getKeyboardNameResId(){return R.string.ru_keyboard;} public String getDescription() {return "Created with dbriskin";}});
        //issue 26 - Russian keyboard
        ms_internalCreators.add(new KeyboardBuilder(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new RussianPhoneticKeyboard(contextProvider);} public String getKeyboardPrefId() {return "ru_ph_keyboard";} public int getSortOrderValue() {return 42;} public int getKeyboardNameResId(){return R.string.ru_ph_keyboard;} public String getDescription() {return "Created with dbriskin";}});
        //BG - issue 25
        ms_internalCreators.add(new KeyboardBuilder(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianBDSKeyboard(contextProvider);} public String getKeyboardPrefId() {return "bg_bds_keyboard";} public int getSortOrderValue() {return 61;} public int getKeyboardNameResId(){return R.string.bg_bds_keyboard;} public String getDescription() {return "Created with atg2d";}});
        ms_internalCreators.add(new KeyboardBuilder(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BulgarianPhoneticKeyboard(contextProvider);} public String getKeyboardPrefId() {return "bg_ph_keyboard";} public int getSortOrderValue() {return 62;} public int getKeyboardNameResId(){return R.string.bg_ph_keyboard;} public String getDescription() {return "Created with atg2d";}});

        //issue 105
        ms_internalCreators.add(new KeyboardBuilder(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BelarusianCyrillicKeyboard(contextProvider);} public String getKeyboardPrefId() {return "be_cyrillic";} public int getSortOrderValue() {return 171;} public int getKeyboardNameResId(){return R.string.be_cyrillic_keyboard;} public String getDescription() {return "Created with alex73";}}); 
        ms_internalCreators.add(new KeyboardBuilder(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new BelarusianLatinKeyboard(contextProvider);} public String getKeyboardPrefId() {return "be_latin";} public int getSortOrderValue() {return 172;} public int getKeyboardNameResId(){return R.string.be_latin_keyboard;} public String getDescription() {return "test1";}});

        //Ukrainian keyboard - issue 154
        ms_internalCreators.add(new KeyboardBuilder(){public AnyKeyboard createKeyboard(AnyKeyboardContextProvider contextProvider) {return new UkrainianKeyboard(contextProvider);} public String getKeyboardPrefId() {return "uk_keyboard";} public int getSortOrderValue() {return 211;} public int getKeyboardNameResId(){return R.string.uk_keyboard;} public String getDescription() {return "Created with rlavriv";}});
    }
    
    private static ArrayList<KeyboardBuilder> ms_creators = null;
    
    private static final String XML_KEYBOARDS_TAG = "Keyboards";
    private static final String XML_KEYBOARD_TAG = "Keyboard";

	private static final String XML_PREF_ID_ATTRIBUTE = "enabledPrefStringId";
	private static final String XML_NAME_RES_ID_ATTRIBUTE = "nameResId";
	private static final String XML_LAYOUT_RES_ID_ATTRIBUTE = "layoutResId";
	private static final String XML_LANDSCAPE_LAYOUT_RES_ID_ATTRIBUTE = "landscapeResId";
	private static final String XML_ICON_RES_ID_ATTRIBUTE = "iconResId";
	private static final String XML_DICTIONARY_NAME_ATTRIBUTE = "defaultDictionaryName";
	private static final String XML_ADDITIONAL_IS_LETTER_EXCEPTIONS_ATTRIBUTE = "additionalIsLetterExceptions";
	private static final String XML_SORT_OREDER_VALUE_ATTRIBUTE = "sortOrder";
	private static final String XML_PHYSICAL_TRANSLATION_RES_ID_ATTRIBUTE = "physicalKeyboardMappingResId";
	private static final String XML_DESCRIPTION_ATTRIBUTE = "description";
    
    private static ArrayList<KeyboardBuilder> getKeyboardCreatorsFromResId(Context context, int keyboardsResId)
    {
    	ArrayList<KeyboardBuilder> keyboards = new ArrayList<KeyboardBuilder>();
    	XmlPullParser allKeyboards = context.getResources().getXml(keyboardsResId);
    	try {
            int event;
            boolean inKeyboards = false;
            while ((event = allKeyboards.next()) != XmlPullParser.END_DOCUMENT) 
            {
            	String tag = allKeyboards.getName();
                if (event == XmlPullParser.START_TAG) {
                    if (XML_KEYBOARDS_TAG.equals(tag)) {
                    	inKeyboards = true;
                    	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d("ASK Keyboards creator factory", "Starting parsing "+XML_KEYBOARDS_TAG);
                    }
                    else if (inKeyboards && XML_KEYBOARD_TAG.equals(tag))
                    {
                    	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d("ASK Keyboards creator factory", "Starting parsing "+XML_KEYBOARD_TAG);
                    	AttributeSet attrs = Xml.asAttributeSet(allKeyboards);
                    	
                    	final String prefId = attrs.getAttributeValue(null, XML_PREF_ID_ATTRIBUTE);
                        final int nameId = attrs.getAttributeResourceValue(null, XML_NAME_RES_ID_ATTRIBUTE, -1);
                        final int layoutResId = attrs.getAttributeResourceValue(null, XML_LAYOUT_RES_ID_ATTRIBUTE, -1);
                        final int landscapeLayoutResId = attrs.getAttributeResourceValue(null, XML_LANDSCAPE_LAYOUT_RES_ID_ATTRIBUTE, -1);
                        final int iconResId = attrs.getAttributeResourceValue(null, XML_ICON_RES_ID_ATTRIBUTE, R.drawable.sym_keyboard_notification_icon);
                        final String defaultDictionary = attrs.getAttributeValue(null, XML_DICTIONARY_NAME_ATTRIBUTE);
                        final String additionalIsLetterExceptions = attrs.getAttributeValue(null, XML_ADDITIONAL_IS_LETTER_EXCEPTIONS_ATTRIBUTE);
                        final int sortValue = attrs.getAttributeIntValue(null, XML_SORT_OREDER_VALUE_ATTRIBUTE, Integer.MAX_VALUE);
                        final int physicalTranslationResId = attrs.getAttributeResourceValue(null, XML_PHYSICAL_TRANSLATION_RES_ID_ATTRIBUTE, -1);
                        final String description = attrs.getAttributeValue(null, XML_DESCRIPTION_ATTRIBUTE);
                        
                        //asserting
                        if ((prefId == null) || (nameId == -1) || (layoutResId == -1))
                        {
                            Log.e("ASK Keyboards creator factory", "External Keyboard does not include all mandatory details! Will not create keyboard.");
                        }
                        else
                        {
                        	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d("ASK Keyboards creator factory", "External keyboard details: prefId:"+prefId+" nameId:"+nameId+" resId:"+layoutResId+" landscapeResId:"+landscapeLayoutResId+" iconResId:"+iconResId+" defaultDictionary:"+defaultDictionary+" sortValue:"+sortValue);
                            KeyboardBuilder creator = new KeyboardBuilderImpl(context, prefId, nameId, layoutResId, landscapeLayoutResId, defaultDictionary, iconResId,
                            		physicalTranslationResId, additionalIsLetterExceptions, sortValue, description);

                            if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d("ASK Keyboards creator factory", "External keyboard "+prefId+" will have a creator.");
                            keyboards.add(creator);
                        }
                        
                    }
                }
                else if (event == XmlPullParser.END_TAG) {
                	if (XML_KEYBOARDS_TAG.equals(tag)) {
                    	inKeyboards = false;
                    	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d("ASK Keyboards creator factory", "Finished parsing "+XML_KEYBOARDS_TAG);
                    	break;
                    } 
                	else if (inKeyboards && XML_KEYBOARD_TAG.equals(tag))
                    {
                		if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d("ASK Keyboards creator factory", "Finished parsing "+XML_KEYBOARD_TAG);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("AnySoftKeyboard", "Parse error:" + e);
            e.printStackTrace();
        }
        
        return keyboards;
    }

	public synchronized static ArrayList<KeyboardBuilder> getAllCreators(Context context) {

		if (ms_creators == null)
		{
			ArrayList<KeyboardBuilder> keyboards = new ArrayList<KeyboardBuilder>(ms_internalCreators);
			
//			final ContentResolver rc = contextProvider.getApplicationContext().getContentResolver();
//	
//	        final Uri[] externalKeyboardsUris = getExternalKeyboardsUri(contextProvider.getApplicationContext());
//	
//	        for(final Uri anExternalKeyboardUri : externalKeyboardsUris)
//	        {
//	        	Log.d("ASK Keyboard Creators", "Extracting keyboard from:"+anExternalKeyboardUri);
//	            extractExternalKeyboardFromUri(contextProvider, keyboards, rc, anExternalKeyboardUri);
//	        }
			keyboards.addAll(getKeyboardCreatorsFromResId(context, R.xml.keyboards));
	        ms_creators = keyboards;
	        
	      //sorting the keyboards according to the requested
	        //sort order (from minimum to maximum)
	        Collections.sort(ms_creators, new Comparator<KeyboardBuilder>()
	                {
			            public int compare(KeyboardBuilder k1, KeyboardBuilder k2) 
			            {
			                return k1.getSortOrderValue() - k2.getSortOrderValue();
			            }
	                });
		}
		
        return ms_creators;
	}
    
//	private static void extractExternalKeyboardFromUri(
//            AnyKeyboardContextProvider contextProvider,
//            ArrayList<KeyboardCreator> keyboards, ContentResolver rc,
//            final Uri externalKeyboardUri) {
//        final Cursor c = rc.query(externalKeyboardUri, null, null, null, null);
//
//        if ((c != null) && (c.moveToFirst()))
//        {
//            do
//            {
//            	String prefId = null;
//                String nameId = null;
//                String resId = null;
//                String landscapeResId = null;
//                String iconResId = "com.menny.android.anysoftkeyboard:drawable/sym_keyboard_notification_icon";
//                String defaultDictionary = "None";
//                String additionalIsLetterExceptions = null;
//                int sortValue = Integer.MAX_VALUE;
//                String qwertyTranslation = null;
//                
//                final int prefIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_PREF_ID);
//                final int nameIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_NAME_RES_ID);
//                final int resIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_LAYOUT_RES_ID);
//                final int landscapeRedIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_LAYOUT_LANDSCAPE_RES_ID);
//                final int iconIdIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_ICON_RES_ID);
//                final int defaultDictionaryIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_DICTIONARY);
//                final int additionalIsLetterExceptionsIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_ADDITIONAL_IS_LETTER_EXCEPTIONS);
//                final int qwertyTranslationIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_HARD_QWERTY_TRANSLATION);
//                final int sortValueIndex = c.getColumnIndex(KeyboardProvider.KEYBOARD_KEY_SORT_ORDER);
//
//                if (prefIdIndex >= 0) {
//                    prefId = c.getString(prefIdIndex);
//                }
//                if (nameIdIndex >= 0) {
//                    nameId = c.getString(nameIdIndex);
//                }
//                if (resIdIndex >= 0) {
//                    resId = c.getString(resIdIndex);
//                }
//                if (landscapeRedIdIndex >= 0) {
//                    landscapeResId = c.getString(landscapeRedIdIndex);
//                }
//                if (iconIdIndex >= 0) {
//                    iconResId = c.getString(iconIdIndex);
//                }
//                if (defaultDictionaryIndex >= 0) {
//                    defaultDictionary = c.getString(defaultDictionaryIndex);
//                }
//                if (additionalIsLetterExceptionsIndex >= 0) {
//                    additionalIsLetterExceptions = c.getString(additionalIsLetterExceptionsIndex);
//                }
//                if (qwertyTranslationIndex >= 0) {
//                    qwertyTranslation = c.getString(qwertyTranslationIndex);
//                }
//                if (sortValueIndex >= 0) {
//                    sortValue = c.getInt(sortValueIndex);
//                }
//                
//                //asserting
//                if ((prefId == null) ||
//                        (nameId == null) ||
//                        (resId == null))
//                {
//                    Log.e("ASK Keyboards", "External Keyboard does not include all mandatory details! Will not create keyboard in URI:"+externalKeyboardUri);
//                }
//                else
//                {
//                    Log.d("ASK KeyboardFactory", "External keyboard details: prefId:"+prefId+" nameId:"+nameId+" resId:"+resId+" landscapeResId:"+landscapeResId+" iconResId:"+iconResId+" defaultDictionary:"+defaultDictionary+" sortValue:"+sortValue);
//                    KeyboardCreator creator = new KeyboardCreatorImpl(contextProvider.getApplicationContext(), 
//                                prefId, nameId, resId, landscapeResId, defaultDictionary, iconResId,
//                                qwertyTranslation,
//                                additionalIsLetterExceptions,
//                                sortValue);
//
//                    Log.d("ASK Keyboards", "External keyboard "+prefId+" will have a creator. URI:"+externalKeyboardUri);
//                    keyboards.add(creator);
//                }
//            }while(c.moveToNext());
//        }
//        else
//        {
//            Log.w("ASK Keyboards", "** No keyboards were located in ContentProviders in URI:"+externalKeyboardUri);
//        }
//    }
    
//	private static Uri[] getExternalKeyboardsUri(Context applicationContext) {
//    	Intent keyboards = new Intent("com.menny.android.anysoftkeyboard.KEYBOARD");
//    	List<ResolveInfo> keyboardActivities = applicationContext.getPackageManager().queryIntentActivities(keyboards,PackageManager.MATCH_DEFAULT_ONLY);
//    	List<Uri> keyboardProviders = new ArrayList<Uri>();
//    	Log.d("ASK Factory", "Located "+keyboardActivities.size()+" external keyboards activities.");
//    	for(ResolveInfo info : keyboardActivities)
//    	{
//    		KeyboardsResolverActivity resolver = new KeyboardsResolverActivity();
//    		String uri = resolver.getKeyboardProviderUri(info);
//    		if (uri != null)
//    			keyboardProviders.add(Uri.parse(uri));            
//    	}
//    	
//    	//adding internal URI - this is for now.
//    	keyboardProviders.add(EnglishKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(AZERTYKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(DVORAKKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(ColemakKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(BepoKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(TerminalKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(ArabicKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(DanishKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(NorwegianKeyboardProvider.CONTENT_URI);
//        keyboardProviders.add(FinnishKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(CatalanKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(SvorakKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(GermanKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(SpanishKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(HungarianKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(Hungarian4RowsKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(SwissKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(FrenchCanadaKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(PtKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(ThaiKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(GeorgianKeyboardProvider.CONTENT_URI);
//    	keyboardProviders.add(EsperantoKeyboardProvider.CONTENT_URI);
//
//    	return keyboardProviders.toArray(new Uri[]{});
//    }
}
