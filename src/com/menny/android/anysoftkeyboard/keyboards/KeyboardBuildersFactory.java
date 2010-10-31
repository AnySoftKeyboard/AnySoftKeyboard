package com.menny.android.anysoftkeyboard.keyboards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.R;

public class KeyboardBuildersFactory {

    private static final String TAG = "ASK KBD creator factory";

    public interface KeyboardBuilder
    {
        /**
         * This is the interface name that a broadcast receiver implementing an
         * external keyboard should say that it supports -- that is, this is the
         * action it uses for its intent filter.
         */
        public static final String RECEIVER_INTERFACE = "com.menny.android.anysoftkeyboard.KEYBOARD";

        /**
         * Name under which an external keyboard broadcast receiver component
         * publishes information about itself.
         */
        public static final String RECEIVER_META_DATA = "com.menny.android.anysoftkeyboard.keyboards";

        AnyKeyboard createKeyboard(AnyKeyboardContextProvider context, int mode);
        String getId();
        int getKeyboardIndex();
        int getKeyboardNameResId();
        String getDescription();
        Context getPackageContext();
    }

    public static class KeyboardBuilderImpl implements KeyboardBuilder
    {
        private final String mId;
        private final int mNameId;
        private final int mResId;
        private final int mLandscapeResId;
        private final int mIconResId;
        private final int mKeyboardIndex;
        private final String mDefaultDictionary;
        private final int mQwertyTranslationId;
        private final String mAdditionalIsLetterExceptions;
        private final String mDescription;
        private final Context mPackageContext;

        public KeyboardBuilderImpl(Context packageContext, String id, int nameId,
                int layoutResId, int landscapeLayoutResId,
                String defaultDictionary, int iconResId,
                int physicalTranslationResId,
                String additionalIsLetterExceptions,
                String description,
                int keyboardIndex)
        {
            mId = "keyboard_"+id;
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
            mDescription = description;
            mPackageContext = packageContext;
            mKeyboardIndex = keyboardIndex;
            if (AnySoftKeyboardConfiguration.DEBUG)Log.d("ASK KeyboardCreatorImpl", "Creator for "+mId+" package:"+mPackageContext.getPackageName()+" res: "+ mResId+" LandscapeRes: "+ mLandscapeResId+" dictionary: "+mDefaultDictionary+" qwerty:" + mQwertyTranslationId);
        }

        public AnyKeyboard createKeyboard(AnyKeyboardContextProvider askContext, int mode) {
            if (AnySoftKeyboardConfiguration.DEBUG)Log.d(TAG, "Creating external keyboard '"+mId+"' in mode "+mode);
            return new ExternalAnyKeyboard(askContext, mPackageContext, mResId, mLandscapeResId, getId(), mNameId, mIconResId, mQwertyTranslationId, mDefaultDictionary, mAdditionalIsLetterExceptions, mode);
        }

        public Context getPackageContext() {return mPackageContext;}

        public int getKeyboardNameResId() {return mNameId;}
        public String getDescription() {return mDescription;}

        public String getId() {
            return mId;
        }

        public int getKeyboardIndex() {
        	return mKeyboardIndex;
        }
    }

    private static ArrayList<KeyboardBuilder> ms_creators = null;

    private static final String XML_KEYBOARDS_TAG = "Keyboards";
    private static final String XML_KEYBOARD_TAG = "Keyboard";

    private static final String XML_PREF_ID_ATTRIBUTE = "id";
    private static final String XML_NAME_RES_ID_ATTRIBUTE = "nameResId";
    private static final String XML_LAYOUT_RES_ID_ATTRIBUTE = "layoutResId";
    private static final String XML_LANDSCAPE_LAYOUT_RES_ID_ATTRIBUTE = "landscapeResId";
    private static final String XML_ICON_RES_ID_ATTRIBUTE = "iconResId";
    private static final String XML_DICTIONARY_NAME_ATTRIBUTE = "defaultDictionaryLocale";
    private static final String XML_ADDITIONAL_IS_LETTER_EXCEPTIONS_ATTRIBUTE = "additionalIsLetterExceptions";
    private static final String XML_PHYSICAL_TRANSLATION_RES_ID_ATTRIBUTE = "physicalKeyboardMappingResId";
    private static final String XML_DESCRIPTION_ATTRIBUTE = "description";
    private static final String XML_INDEX_ATTRIBUTE = "index";


    public synchronized static void resetBuildersCache()
    {
        ms_creators = null;
    }
    
    public synchronized static void onPackageSetupChanged(String packageName)
    {
    	
    }

    public synchronized static ArrayList<KeyboardBuilder> getAllBuilders(final Context context) {

        if (ms_creators == null)
        {
            final ArrayList<KeyboardBuilder> keyboards = new ArrayList<KeyboardBuilder>();
            keyboards.addAll(getKeyboardCreatorsFromResId(context, R.xml.keyboards));
            keyboards.addAll(getAllExternalKeyboardCreators(context));
            ms_creators = keyboards;

            //sorting the keyboards according to the requested
            //sort order (from minimum to maximum)
            Collections.sort(ms_creators, new Comparator<KeyboardBuilder>()
                    {
		                public int compare(KeyboardBuilder k1, KeyboardBuilder k2)
		                {
		                	Context c1 = k1.getPackageContext();
		                	Context c2 = k2.getPackageContext();
		                	if (c1 == null)
		                		c1 = context;
		                	if (c2 == null)
		                		c2 = context;

		                	String key1 = c1.getPackageName()+String.format("%08d%n", k1.getKeyboardIndex());
		                	String key2 = c2.getPackageName()+String.format("%08d%n", k2.getKeyboardIndex());

		                	int value = key2.compareToIgnoreCase(key1);

		                	//Log.d(TAG, "Collections.sort: "+key1+" vs "+key2+" = "+value);

		                	return value;
		                }
                    });
        }

        return ms_creators;
    }

    private static ArrayList<KeyboardBuilder> getAllExternalKeyboardCreators(Context context){

        final List<ResolveInfo> broadcastReceivers = context.getPackageManager()
	        .queryBroadcastReceivers(
	                new Intent(KeyboardBuilder.RECEIVER_INTERFACE),
	                PackageManager.GET_META_DATA);

        final ArrayList<KeyboardBuilder> externalKeyboardCreators = new ArrayList<KeyboardBuilder>();
        for(final ResolveInfo receiver : broadcastReceivers){
            // If activityInfo is null, we are probably dealing with a service.
            if (receiver.activityInfo == null) {
                Log.e(TAG,
                        "BroadcastReceiver has null ActivityInfo. Receiver's label is "
                        + receiver.loadLabel(context.getPackageManager()));
                Log.e(TAG,
                "Is the external keyboard a service instead of BroadcastReceiver?");
                // Skip to next receiver
                continue;
            }

            try {
                final Context externalPackageContext = context.createPackageContext(
                        receiver.activityInfo.packageName, PackageManager.GET_META_DATA);
                final ArrayList<KeyboardBuilder> packageKeyboardCreators = getKeyboardCreatorsFromActivityInfo(externalPackageContext,
                        receiver.activityInfo);
                
                externalKeyboardCreators.addAll(packageKeyboardCreators);
            } catch (final NameNotFoundException e) {
                Log.e(TAG, "Did not find package: " + receiver.activityInfo.packageName);
            }

        }

        return externalKeyboardCreators;
    }

    private static ArrayList<KeyboardBuilder> getKeyboardCreatorsFromResId(Context context,
            int keyboardsResId) {
        final XmlPullParser allKeyboards = context.getResources().getXml(keyboardsResId);
        return parseKeyboardCreatorsFromXml(context, allKeyboards);
    }

    private static ArrayList<KeyboardBuilder> getKeyboardCreatorsFromActivityInfo(Context context,
            ActivityInfo ai) {
        final XmlPullParser allKeyboards = ai.loadXmlMetaData(context.getPackageManager(),
                KeyboardBuilder.RECEIVER_META_DATA);
        return parseKeyboardCreatorsFromXml(context, allKeyboards);
    }

    private static ArrayList<KeyboardBuilder> parseKeyboardCreatorsFromXml(Context context,
            XmlPullParser allKeyboards) {
        final ArrayList<KeyboardBuilder> keyboards = new ArrayList<KeyboardBuilder>();
        try {
            int event;
            boolean inKeyboards = false;
            while ((event = allKeyboards.next()) != XmlPullParser.END_DOCUMENT) {
                final String tag = allKeyboards.getName();
                if (event == XmlPullParser.START_TAG) {
                    if (XML_KEYBOARDS_TAG.equals(tag)) {
                        inKeyboards = true;
                    } else if (inKeyboards && XML_KEYBOARD_TAG.equals(tag)) {
                        final AttributeSet attrs = Xml.asAttributeSet(allKeyboards);

                        final String prefId = attrs.getAttributeValue(null, XML_PREF_ID_ATTRIBUTE);
                        final int nameId = attrs.getAttributeResourceValue(null,
                                XML_NAME_RES_ID_ATTRIBUTE, -1);
                        final int layoutResId = attrs.getAttributeResourceValue(null,
                                XML_LAYOUT_RES_ID_ATTRIBUTE, -1);
                        final int landscapeLayoutResId = attrs.getAttributeResourceValue(null,
                                XML_LANDSCAPE_LAYOUT_RES_ID_ATTRIBUTE, -1);
                        final int iconResId = attrs.getAttributeResourceValue(null,
                                XML_ICON_RES_ID_ATTRIBUTE,
                                R.drawable.sym_keyboard_notification_icon);
                        final String defaultDictionary = attrs.getAttributeValue(null,
                                XML_DICTIONARY_NAME_ATTRIBUTE);
                        final String additionalIsLetterExceptions = attrs.getAttributeValue(null,
                                XML_ADDITIONAL_IS_LETTER_EXCEPTIONS_ATTRIBUTE);
                        final int physicalTranslationResId = attrs.getAttributeResourceValue(null,
                                XML_PHYSICAL_TRANSLATION_RES_ID_ATTRIBUTE, -1);

                        final int descriptionInt = attrs.getAttributeResourceValue(null,
                                XML_DESCRIPTION_ATTRIBUTE,-1);
                        //NOTE, to be compatibel we need this. because the most of descriptions are
                        //without @string/<desc>
                        String description;
                        if(descriptionInt != -1){
                            description = context.getResources().getString(descriptionInt);
                        } else {
                            description =  attrs.getAttributeValue(null,
                                    XML_DESCRIPTION_ATTRIBUTE); 
                        }

                        final int keyboardIndex = attrs.getAttributeResourceValue(null,
                        		XML_INDEX_ATTRIBUTE, 1);

                        // asserting
                        if ((prefId == null) || (nameId == -1) || (layoutResId == -1)) {
                            Log
                            .e(TAG,
                            "External Keyboard does not include all mandatory details! Will not create keyboard.");
                        } else {
                            if (AnySoftKeyboardConfiguration.DEBUG) {
                                Log.d(TAG,
                                        "External keyboard details: prefId:" + prefId + " nameId:"
                                        + nameId + " resId:" + layoutResId
                                        + " landscapeResId:" + landscapeLayoutResId
                                        + " iconResId:" + iconResId + " defaultDictionary:"
                                        + defaultDictionary);
                            }
                            final KeyboardBuilder creator = new KeyboardBuilderImpl(context,
                                    prefId, nameId, layoutResId, landscapeLayoutResId,
                                    defaultDictionary, iconResId, physicalTranslationResId,
                                    additionalIsLetterExceptions, description, keyboardIndex);

                            keyboards.add(creator);
                        }

                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if (XML_KEYBOARDS_TAG.equals(tag)) {
                        inKeyboards = false;
                        break;
                    }
                }
            }
        } catch (final IOException e) {
            Log.e(TAG, "IO error:" + e);
            e.printStackTrace();
        } catch (final XmlPullParserException e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }

        return keyboards;
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
