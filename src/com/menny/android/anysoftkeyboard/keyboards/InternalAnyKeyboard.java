
package com.menny.android.anysoftkeyboard.keyboards;


import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;

public abstract class InternalAnyKeyboard extends AnyKeyboard 
{
	private static class KeyboardMetaData
	{
		public String PrefString;
		public int KeyboardNameId;
		public int IconResId;
		public String DefaultDictionaryLanguage;
		
		public KeyboardMetaData()
		{
			PrefString = null;
			KeyboardNameId = -1;
			IconResId = -1;
			DefaultDictionaryLanguage = "None";
		}
	}
	
	private static final String XML_META_DATA_TAG = "AnySoftKeyboardMetaData";
	private static final String XML_PREF_ID_ATTRIBUTE = "PrefString";
	private static final String XML_NAME_RES_ID_ATTRIBUTE = "KeyboardNameResId";
	private static final String XML_ICON_RES_ID_ATTRIBUTE = "KeyboardIconResId";
	private static final String XML_DICTIONARY_NAME_ATTRIBUTE = "DefaultDictionaryLanguage";
	
    private final KeyboardMetaData mKeyboardMetaData;
    
    protected InternalAnyKeyboard(AnyKeyboardContextProvider context,
    		int xmlLayoutResId) 
    {
        super(context, context.getApplicationContext(), xmlLayoutResId);
        mKeyboardMetaData = loadKeyboard(context.getApplicationContext(), xmlLayoutResId);
        
        Log.d("AnySoftKeyboard", "loadKeyboard result (not relevant in external keyboard): "+"" +
        		"PrefString:"+ ((mKeyboardMetaData.PrefString!=null)? mKeyboardMetaData.PrefString : "NULL")+
        		" KeyboardId:" + mKeyboardMetaData.KeyboardNameId +
        		" IconResId:" + mKeyboardMetaData.IconResId +
        		" DefaultDictionaryLanguage:" + ((mKeyboardMetaData.PrefString!=null)? mKeyboardMetaData.DefaultDictionaryLanguage : "NULL"));
        
        Log.i("AnySoftKeyboard", "Done creating keyboard: "+getKeyboardName()+", which is LTR:"+isLeftToRightLanguage());
    }

	private KeyboardMetaData loadKeyboard(Context applicationContext, int xmlLayoutResId) {
		KeyboardMetaData result = new KeyboardMetaData();
		XmlPullParser parser = applicationContext.getResources().getXml(xmlLayoutResId);
		
        boolean inMetaData = false;
        
        try {
            int event;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) 
            {
                if (event == XmlPullParser.START_TAG) {
                    String tag = parser.getName();
                    if (XML_META_DATA_TAG.equals(tag)) {
                    	inMetaData = true;
                    	Log.d("AnySoftKeyboard", "Starting parsing "+XML_META_DATA_TAG);
                    	AttributeSet attrs = Xml.asAttributeSet(parser);
                    	result.PrefString = attrs.getAttributeValue(null, XML_PREF_ID_ATTRIBUTE);
                    	result.KeyboardNameId = attrs.getAttributeResourceValue(null, XML_NAME_RES_ID_ATTRIBUTE, -1);
                    	result.IconResId = attrs.getAttributeResourceValue(null, XML_ICON_RES_ID_ATTRIBUTE, R.drawable.sym_keyboard_notification_icon);
                    	result.DefaultDictionaryLanguage = attrs.getAttributeValue(null, XML_DICTIONARY_NAME_ATTRIBUTE);
                    }
                }
                else if (event == XmlPullParser.END_TAG && inMetaData) {
                	Log.d("AnySoftKeyboard", "Finished parsing "+XML_META_DATA_TAG);
                	break;
                }
            }
        } catch (Exception e) {
            Log.e("AnySoftKeyboard", "Parse error:" + e);
            e.printStackTrace();
        }
        
        return result;
	}
    
	@Override
    public String getDefaultDictionaryLanguage()
    {
    	return mKeyboardMetaData.DefaultDictionaryLanguage;
    }
    
	@Override
	public int getKeyboardNameResId(){return mKeyboardMetaData.KeyboardNameId;};
	
    @Override
    public int getKeyboardIconResId()
    {
    	return mKeyboardMetaData.IconResId;
    }
    @Override
	public String getKeyboardPrefId() {
		return mKeyboardMetaData.PrefString;
	}
}
