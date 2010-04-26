package com.menny.android.anysoftkeyboard.dictionary2;

import java.io.IOException;
import java.util.ArrayList;
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

import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.R;

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

        Dictionary createDictionary() throws Exception;
        String getId();
        String getLanguage();
        //String getDictionaryName();
        String getDescription();
        Context getPackageContext();
    }

    private static class BinaryDictionaryBuilderImpl implements DictionaryBuilder
    {
        private final String mLanguage;
        private final String mId;
        private final int mNameId;
        private final String mDescription;
        private final String mAssetsFilename;
        private final Context mPackageContext;

        public BinaryDictionaryBuilderImpl(Context context, String id, String language, int nameId, String assetsFilename,
                String description)
        {
        	mId = id;
            mLanguage = language;
            mNameId = nameId;
            mDescription = description;
            mAssetsFilename = assetsFilename;
            mPackageContext = context;
            Log.d("ASK BinaryDictionaryBuilderImpl", "Creator for "+mLanguage+" with id "+mId+" assets:"+mAssetsFilename+" package:"+mPackageContext.getPackageName());
        }

        public Dictionary createDictionary() throws Exception{
            return new BinaryDictionary(mPackageContext.getAssets().openFd(mAssetsFilename));
        }

        public String getDictionaryName() {return mPackageContext.getString(mNameId);}
        public String getDescription() {return mDescription;}

        public String getDictionaryKey() {
            return mLanguage;
        }

        public Context getPackageContext()
        {
        	return mPackageContext;
        }

		public String getId() {
			return mId;
		}

		public String getLanguage() {
			// TODO Auto-generated method stub
			return null;
		}
    }

    private static ArrayList<DictionaryBuilder> ms_creators = null;

    private static final String XML_DICTIONARIES_TAG = "Dictionaries";
    private static final String XML_DICTIONARY_TAG = "Dictionary";

    private static final String XML_ID_ATTRIBUTE = "id";
    private static final String XML_LANGUAGE_ATTRIBUTE = "language";
    private static final String XML_NAME_RES_ID_ATTRIBUTE = "nameResId";
    //private static final String XML_TYPE_ATTRIBUTE = "type";
    private static final String XML_ASSETS_ATTRIBUTE = "dictionaryAssertName";
    private static final String XML_DESCRIPTION_ATTRIBUTE = "description";

    private static ArrayList<DictionaryBuilder> getDictionaryBuildersFromResId(Context context,
            int keyboardsResId) {
        final XmlPullParser allKeyboards = context.getResources().getXml(keyboardsResId);
        return parseDictionaryBuildersFromXml(context, allKeyboards);
    }

    private static ArrayList<DictionaryBuilder> getDictionaryBuildersFromActivityInfo(
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

                        final String id = attrs.getAttributeValue(null, XML_ID_ATTRIBUTE);
                        final String language = attrs.getAttributeValue(null, XML_LANGUAGE_ATTRIBUTE);
                        final int nameId = attrs.getAttributeResourceValue(null, XML_NAME_RES_ID_ATTRIBUTE, -1);
                        final String assets = attrs.getAttributeValue(null, XML_ASSETS_ATTRIBUTE);
                        final String description = attrs.getAttributeValue(null, XML_DESCRIPTION_ATTRIBUTE);

                        //asserting
                        if ((id == null) || (id.length() == 0) ||(language == null) || (nameId == -1) || (assets == null))
                        {
                            Log.e(TAG, "External dictionary does not include all mandatory details! Will not create dictionary.");
                        }
                        else
                        {
                            if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) {
                                Log.d(TAG, "External dictionary details: language:"+language+" id:"+id+" nameId:"+nameId+" assets:"+assets);
                            }
                            final DictionaryBuilder creator = new BinaryDictionaryBuilderImpl(context, id, language, nameId, assets, description);

                            if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) {
                                Log.d(TAG, "External dictionary "+language+" will have a creator.");
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
        } catch (final IOException e) {
            Log.e(TAG, "IO error:" + e);
            e.printStackTrace();
        } catch (final XmlPullParserException e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }

        return dictionaries;
            }

    private static ArrayList<DictionaryBuilder> getAllExternalDictionaryBuilders(Context context) {

        final List<ResolveInfo> broadcastReceivers =
            context.getPackageManager() .queryBroadcastReceivers( new
                    Intent(DictionaryBuilder.RECEIVER_INTERFACE),
                    PackageManager.GET_META_DATA);

        if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) {
            Log.d(TAG, "Number of potential external dictionary packages found: "
                    +
                    broadcastReceivers.size());
        }

        final ArrayList<DictionaryBuilder> externalDictionaryBuilders = new ArrayList<DictionaryBuilder>();
        for (final ResolveInfo receiver : broadcastReceivers) {

            if (receiver.activityInfo == null) {
                Log.e(TAG,
                        "BroadcastReceiver has null ActivityInfo. Receiver's label is " +
                        receiver.loadLabel(context.getPackageManager()));

                // Skip to next receiver
                continue;
            }

            try {
                final Context externalPackageContext = context.createPackageContext(
                        receiver.activityInfo.packageName,
                        PackageManager.GET_META_DATA);
                final ArrayList<DictionaryBuilder> packageKeyboardCreators = getDictionaryBuildersFromActivityInfo(
                        externalPackageContext,
                        receiver.activityInfo);

                externalDictionaryBuilders.addAll(packageKeyboardCreators);

            } catch(final NameNotFoundException e) {
                Log.e(TAG, "Did not find package: " + receiver.activityInfo.packageName);
            }
        }

        if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) {
            Log.d(TAG, "Number of external dictionary builders successfully parsed: "
                    + externalDictionaryBuilders.size());
        }

        return externalDictionaryBuilders;

    }

    public synchronized static void resetBuildersCache()
    {
    	ms_creators = null;
    }

    public synchronized static ArrayList<DictionaryBuilder> getAllBuilders(Context context) {

        if (ms_creators == null)
        {
            final ArrayList<DictionaryBuilder> dictionaries = new ArrayList<DictionaryBuilder>();

            dictionaries.addAll(getAllExternalDictionaryBuilders(context));
            dictionaries.addAll(getDictionaryBuildersFromResId(context, R.xml.dictionaries));

            ms_creators = dictionaries;
        }

        return ms_creators;
    }
}
