package com.menny.android.anysoftkeyboard.Dictionary;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.R;

public class ExternalDictionaryFactory {

	private static final String TAG = "ExternalDictionaryFactory";
	public interface DictionaryBuilder
    {
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
    
    private static ArrayList<DictionaryBuilder> getDictionaryBuildersFromResId(Context context, int dictionariesResId)
    {
    	ArrayList<DictionaryBuilder> dictionaries = new ArrayList<DictionaryBuilder>();
    	XmlPullParser xmlParser = context.getResources().getXml(dictionariesResId);
    	try {
            int event;
            boolean inDictionaries = false;
            while ((event = xmlParser.next()) != XmlPullParser.END_DOCUMENT) 
            {
            	String tag = xmlParser.getName();
                if (event == XmlPullParser.START_TAG) {
                    if (XML_DICTIONARIES_TAG.equals(tag)) {
                    	inDictionaries = true;
                    	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Starting parsing "+XML_DICTIONARIES_TAG);
                    }
                    else if (inDictionaries && XML_DICTIONARY_TAG.equals(tag))
                    {
                    	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Starting parsing "+XML_DICTIONARY_TAG);
                    	AttributeSet attrs = Xml.asAttributeSet(xmlParser);
                    	
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
                        	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "External dictionary details: key:"+key+" nameId:"+nameId+" assets:"+assets);
                            DictionaryBuilder creator = new BinaryDictionaryBuilderImpl(context, key, nameId, assets, description);

                            if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "External dictionary "+key+" will have a creator.");
                            dictionaries.add(creator);
                        }
                        
                    }
                }
                else if (event == XmlPullParser.END_TAG) {
                	if (XML_DICTIONARIES_TAG.equals(tag)) {
                    	inDictionaries = false;
                    	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Finished parsing "+XML_DICTIONARIES_TAG);
                    	break;
                    } 
                	else if (inDictionaries && XML_DICTIONARY_TAG.equals(tag))
                    {
                		if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Finished parsing "+XML_DICTIONARY_TAG);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }
        
        return dictionaries;
    }

	public synchronized static ArrayList<DictionaryBuilder> getAllCreators(Context context) {

		if (ms_creators == null)
		{
			ArrayList<DictionaryBuilder> dictionaries = new ArrayList<DictionaryBuilder>();
		
			dictionaries.addAll(getDictionaryBuildersFromResId(context, R.xml.dictionaries));
	        ms_creators = dictionaries;
		}
		
        return ms_creators;
	}
}
