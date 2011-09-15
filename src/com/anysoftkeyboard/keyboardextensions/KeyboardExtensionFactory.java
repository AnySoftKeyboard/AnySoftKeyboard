package com.anysoftkeyboard.keyboardextensions;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class KeyboardExtensionFactory extends AddOnsFactory<KeyboardExtension>
{
	
	private static final KeyboardExtensionFactory msInstance;
	
	static
	{
		msInstance = new KeyboardExtensionFactory();
	}
	
	public static KeyboardExtension getCurrentKeyboardExtension(AnyKeyboardContextProvider contextProvider, final int type)
	{
		 SharedPreferences sharedPreferences = contextProvider.getSharedPreferences();
		 final String settingKey;
         switch(type)
         {
         case KeyboardExtension.TYPE_BOTTOM:
        	 settingKey = contextProvider.getApplicationContext().getString(R.string.settings_key_ext_kbd_top_row_key);
        	 break;
         case KeyboardExtension.TYPE_TOP:
        	 settingKey = contextProvider.getApplicationContext().getString(R.string.settings_key_ext_kbd_bottom_row_key);
        	 break;
         case KeyboardExtension.TYPE_EXTENSION:
        	 settingKey = contextProvider.getApplicationContext().getString(R.string.settings_key_ext_kbd_ext_ketboard_key);
        	 break;
         case KeyboardExtension.TYPE_HIDDEN_BOTTOM:
        	 settingKey = contextProvider.getApplicationContext().getString(R.string.settings_key_ext_kbd_hidden_bottom_row_key);
        	 break;
    	 default:
    		 throw new RuntimeException("No such extension keyboard type: "+type);
         }
         
         String selectedKeyId = sharedPreferences.getString(settingKey, null);
         KeyboardExtension selectedKeyboard = null;
         ArrayList<KeyboardExtension> keys = msInstance.getAllAddOns(contextProvider.getApplicationContext());
         
         if (selectedKeyId != null) {
             for (KeyboardExtension aKey : keys) {
            	 if (aKey.getExtensionType() != type) continue;
                 if (aKey.getId().equals(selectedKeyId)) {
                	 selectedKeyboard = aKey;
                     break;
                 }
             }
         }

         if (selectedKeyboard == null) {
        	 //still can't find the keyboard. Taking default.
        	 for (KeyboardExtension aKey : keys) {
            	 if (aKey.getExtensionType() != type) continue;
            	 selectedKeyboard = aKey;
            	 break;
             }
             SharedPreferences.Editor editor = sharedPreferences.edit();
             editor.putString(settingKey, selectedKeyboard.getId());
             editor.commit();
         }

         return selectedKeyboard;
	}
	


	public static ArrayList<KeyboardExtension> getAllAvailableExtensions(Context applicationContext, final int type) {
		ArrayList<KeyboardExtension> all = msInstance.getAllAddOns(applicationContext);
		ArrayList<KeyboardExtension> onlyAsked = new ArrayList<KeyboardExtension>();
		for(KeyboardExtension e : all)
		{
			if (e.getExtensionType() == type)
				onlyAsked.add(e);
		}
		
		return onlyAsked;
	}

	private static final String XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE = "extensionKeyboardResId";
	private static final String XML_EXT_KEYBOARD_TYPE_ATTRIBUTE = "extensionKeyboardType";
	
	private KeyboardExtensionFactory() {
		super("ASK_EKF", "com.anysoftkeyboard.plugin.EXTENSION_KEYBOARD", "com.anysoftkeyboard.plugindata.extensionkeyboard", 
				"ExtensionKeyboards", "ExtensionKeyboard", 
				R.xml.extension_keyboards);
	}

	@Override
	protected KeyboardExtension createConcreateAddOn(Context context, String prefId, int nameResId,
			String description, int sortIndex, AttributeSet attrs) {
		int keyboardResId = attrs.getAttributeResourceValue(null, XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE, -2);
		if (keyboardResId == -2) keyboardResId = attrs.getAttributeIntValue(null, XML_EXT_KEYBOARD_RES_ID_ATTRIBUTE, -2);
		int extensionType = attrs.getAttributeResourceValue(null, XML_EXT_KEYBOARD_TYPE_ATTRIBUTE, -2);
		if (extensionType != -2)
		{
			extensionType = context.getResources().getInteger(extensionType);
		}
		else
		{
			extensionType = attrs.getAttributeIntValue(null, XML_EXT_KEYBOARD_TYPE_ATTRIBUTE, -2);
		}
		if (AnyApplication.DEBUG)
		{
			Log.d(TAG, String.format("Parsing Extension Keyboard! prefId %s, keyboardResId %d, type %d", 
					prefId, keyboardResId, extensionType));
		}
		if ((keyboardResId == -2) || (extensionType == -2))
		{
			String detailMessage = String.format("Missing details for creating Extension Keyboard! prefId %s\n"+
					"keyboardResId: %d, type: %d", 
					prefId, keyboardResId, extensionType);
			
			throw new RuntimeException(detailMessage);
		}
		return new KeyboardExtension(context, prefId, nameResId, keyboardResId, extensionType, description, sortIndex);
	}
}
