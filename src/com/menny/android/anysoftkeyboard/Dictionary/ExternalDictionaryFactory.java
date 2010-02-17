package com.menny.android.anysoftkeyboard.Dictionary;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.R;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import java.util.ArrayList;

public class ExternalDictionaryFactory {

    private static final String TAG = "ExternalDictionaryFactory";
    public interface DictionaryBuilder
    {
        /**
         * This is the interface name that a broadcast receiver implementing an
         * external dictionary should say that it supports -- that is, this is
         * the action it uses for its intent filter.
         */
        public static final String RECEIVER_INTERFACE = "com.menny.android.anysoftkeyboard.DICTIONARY";

        /**
         * Name under which an external dictionary broadcast receiver component
         * publishes information about itself.
         */
        public static final String RECEIVER_META_DATA = "com.menny.android.anysoftkeyboard.dictionaries";

        Dictionary createDictionary(AnyKeyboardContextProvider context) throws Exception;
        String getDictionaryKey();
        int getDictionaryNameResId();
        String getDescription();
    }

    private static class BinaryDictionaryBuilderImpl implements DictionaryBuilder
    {
        private final String mKey;
        private final int mNameId;
        private final String mDescription;
        private final String mAssetsFilename;

        public BinaryDictionaryBuilderImpl(Context context, String key, int nameId, String assetsFilename,
                String description) 
        {
            mKey = key;
            mNameId = nameId;            
            mDescription = description;
            mAssetsFilename = assetsFilename;
            Log.d("ASK BinaryDictionaryBuilderImpl", "Creator for "+mKey+" assets:"+mAssetsFilename);
        }

        public Dictionary createDictionary(AnyKeyboardContextProvider context) throws Exception{
            return new BinaryDictionary(context.getApplicationContext().getAssets().openFd(mAssetsFilename));
        }

        public int getDictionaryNameResId() {return mNameId;}
        public String getDescription() {return mDescription;}

        public String getDictionaryKey() {
            return mKey;
        }
    }

    private static ArrayList<DictionaryBuilder> ms_creators = null;

    private static final String XML_DICTIONARIES_TAG = "Dictionaries";
    private static final String XML_DICTIONARY_TAG = "Dictionary";

    private static final String XML_KEY_ATTRIBUTE = "key";
    private static final String XML_NAME_RES_ID_ATTRIBUTE = "nameResId";
    //private static final String XML_TYPE_ATTRIBUTE = "type";
    private static final String XML_ASSETS_ATTRIBUTE = "dictionaryAssertName";
    private static final String XML_DESCRIPTION_ATTRIBUTE = "description";

    private static ArrayList<DictionaryBuilder> getKeyboardCreatorsFromResId(Context context,
            int keyboardsResId) {
        final XmlPullParser allKeyboards = context.getResources().getXml(keyboardsResId);
        return parseDictionaryBuildersFromXml(context, allKeyboards);
    }

    private static ArrayList<DictionaryBuilder> getKeyboardCreatorsFromActivityInfo(
            Context context, ActivityInfo ai) {
        final XmlPullParser allKeyboards = ai.loadXmlMetaData(context.getPackageManager(),
                DictionaryBuilder.RECEIVER_META_DATA);
        return parseDictionaryBuildersFromXml(context, allKeyboards);
    }

    private static ArrayList<DictionaryBuilder> parseDictionaryBuildersFromXml(Context context,
            XmlPullParser xmlParser)
            {
        final ArrayList<DictionaryBuilder> dictionaries = new ArrayList<DictionaryBuilder>();
        try {
            int event;
            boolean inDictionaries = false;
            while ((event = xmlParser.next()) != XmlPullParser.END_DOCUMENT) 
            {
                final String tag = xmlParser.getName();
                if (event == XmlPullParser.START_TAG) {
                    if (XML_DICTIONARIES_TAG.equals(tag)) {
                        inDictionaries = true;
                        if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) {
                            Log.d(TAG, "Starting parsing "+XML_DICTIONARIES_TAG);
                        }
                    }
                    else if (inDictionaries && XML_DICTIONARY_TAG.equals(tag))
                    {
                        if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) {
                            Log.d(TAG, "Starting parsing "+XML_DICTIONARY_TAG);
                        }
                        final AttributeSet attrs = Xml.asAttributeSet(xmlParser);

                        final String key = attrs.getAttributeValue(null, XML_KEY_ATTRIBUTE);
                        final int nameId = attrs.getAttributeResourceValue(null, XML_NAME_RES_ID_ATTRIBUTE, -1);
                        final String assets = attrs.getAttributeValue(null, XML_ASSETS_ATTRIBUTE);
                        final String description = attrs.getAttributeValue(null, XML_DESCRIPTION_ATTRIBUTE);

                        //asserting
                        if ((key == null) || (nameId == -1) || (assets == null))
                        {
                            Log.e(TAG, "External dictionary does not include all mandatory details! Will not create dictionary.");
                        }
                        else
                        {
                            if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) {
                                Log.d(TAG, "External dictionary details: key:"+key+" nameId:"+nameId+" assets:"+assets);
                            }
                            final DictionaryBuilder creator = new BinaryDictionaryBuilderImpl(context, key, nameId, assets, description);

                            if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) {
                                Log.d(TAG, "External dictionary "+key+" will have a creator.");
                            }
                            dictionaries.add(creator);
                        }

                    }
                }
                else if (event == XmlPullParser.END_TAG) {
                    if (XML_DICTIONARIES_TAG.equals(tag)) {
                        inDictionaries = false;
                        if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) {
                            Log.d(TAG, "Finished parsing "+XML_DICTIONARIES_TAG);
                        }
                        break;
                    } 
                    else if (inDictionaries && XML_DICTIONARY_TAG.equals(tag))
                    {
                        if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) {
                            Log.d(TAG, "Finished parsing "+XML_DICTIONARY_TAG);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }

        return dictionaries;
            }

    private static ArrayList<DictionaryBuilder> getAllExternalDictionaryBuilders(Context context) {

        // model from keyboards, remember to use dictionary intent
        /*
         * final List<ResolveInfo> broadcastReceivers =
         * context.getPackageManager() .queryBroadcastReceivers( new
         * Intent(AnyKeyboard.RECEIVER_INTERFACE),
         * PackageManager.GET_META_DATA); Log.d("ASK Keyboards creator factory",
         * "Number of potential external keyboard packages found: " +
         * broadcastReceivers.size()); final ArrayList<KeyboardBuilder>
         * externalKeyboardCreators = new ArrayList<KeyboardBuilder>();
         * for(final ResolveInfo receiver : broadcastReceivers){ if
         * (receiver.activityInfo == null) {
         * Log.e("ASK Keyboards creator factory",
         * "BroadcastReceiver has null ActivityInfo. Receiver's label is " +
         * receiver.loadLabel(context.getPackageManager())); // Skip to next
         * receiver continue; } try { final Context externalPackageContext =
         * context.createPackageContext( receiver.activityInfo.packageName,
         * PackageManager.GET_META_DATA); final ArrayList<KeyboardBuilder>
         * packageKeyboardCreators =
         * getKeyboardCreatorsFromActivityInfo(externalPackageContext,
         * receiver.activityInfo);
         * externalKeyboardCreators.addAll(packageKeyboardCreators); } catch
         * (final NameNotFoundException e) {
         * Log.e("ASK Keyboards creator factory", "Did not find package: " +
         * receiver.activityInfo.packageName); } }
         * Log.d("ASK Keyboards creator factory",
         * "Number of external keyboard creators successfully parsed: " +
         * externalKeyboardCreators.size()); return externalKeyboardCreators;
         */
        return new ArrayList<DictionaryBuilder>();
    }

    public synchronized static ArrayList<DictionaryBuilder> getAllCreators(Context context) {

        if (ms_creators == null)
        {
            final ArrayList<DictionaryBuilder> dictionaries = new ArrayList<DictionaryBuilder>();

            dictionaries.addAll(getKeyboardCreatorsFromResId(context, R.xml.dictionaries));
            dictionaries.addAll(getAllExternalDictionaryBuilders(context));
            ms_creators = dictionaries;
        }

        return ms_creators;
    }
}
