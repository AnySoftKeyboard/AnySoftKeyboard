package com.anysoftkeyboard.theme;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.R;

public class KeyboardThemeFactory extends AddOnsFactory<KeyboardTheme>
{
	
	private static final KeyboardThemeFactory msInstance;
	
	static
	{
		msInstance = new KeyboardThemeFactory();
	}
	
	public static KeyboardTheme getCurrentKeyboardTheme(Context appContext)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
		 String settingKey = appContext.getString(R.string.settings_key_keyboard_theme_key);
         
         String selectedThemeId = sharedPreferences.getString(settingKey, appContext.getString(R.string.settings_default_keyboard_theme_key));
         KeyboardTheme selectedTheme = null;
         ArrayList<KeyboardTheme> themes = msInstance.getAllAddOns(appContext);
         if (selectedThemeId != null) {
        	 //Find the builder in the array by id. Mayne would've been better off with a HashSet
             for (KeyboardTheme aTheme : themes) {
                 if (aTheme.getId().equals(selectedThemeId)) {
                	 selectedTheme = aTheme;
                     break;
                 }
             }
         }

         if (selectedTheme == null) {
        	 //Haven't found a builder or no preference is stored, so we use the default one
        	 selectedTheme = themes.get(0);

             SharedPreferences.Editor editor = sharedPreferences.edit();
             editor.putString(settingKey, selectedTheme.getId());
             editor.commit();
         }

         return selectedTheme;
	}
	


	public static ArrayList<KeyboardTheme> getAllAvailableQuickKeys(Context applicationContext) {
		return msInstance.getAllAddOns(applicationContext);
	}

	private static final String XML_POPUP_KEYBOARD_THEME_RES_ID_ATTRIBUTE = "themeRes";
	private static final String XML_POPUP_KEYBOARD_POPUP_THEME_RES_ID_ATTRIBUTE = "popupThemeRes";
	private static final String XML_POPUP_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE = "iconsThemeRes";
	private static final String XML_POPUP_KEYBOARD_THEME_SCREENSHOT_RES_ID_ATTRIBUTE = "themeScreenshot";
	
	private KeyboardThemeFactory() {
		super("ASK_KT", "com.anysoftkeyboard.plugin.KEYBOARD_THEME", "com.anysoftkeyboard.plugindata.keyboardtheme", 
				"KeyboardThemes", "KeyboardTheme", 
				R.xml.keyboard_themes, true);
	}

	@Override
	protected KeyboardTheme createConcreateAddOn(Context context, String prefId, int nameResId,
			String description, int sortIndex, AttributeSet attrs) {
		final int keyboardThemeResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_KEYBOARD_THEME_RES_ID_ATTRIBUTE, 0);
		final int popupKeyboardThemeResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_KEYBOARD_POPUP_THEME_RES_ID_ATTRIBUTE, 0);
		final int keyboardThemeScreenshotResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_KEYBOARD_THEME_SCREENSHOT_RES_ID_ATTRIBUTE, 0);
		final int iconsThemeResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE, 0);
		
		if (keyboardThemeResId == -1)
		{
			String detailMessage = String.format("Missing details for creating Keyboard theme! prefId %s, "+
					"keyboardThemeResId: %d, keyboardThemeScreenshotResId: %d", 
					prefId, keyboardThemeResId, keyboardThemeScreenshotResId);
			
			throw new RuntimeException(detailMessage);
		}
		return new KeyboardTheme(context, prefId, nameResId, 
				keyboardThemeResId, popupKeyboardThemeResId, iconsThemeResId,
				keyboardThemeScreenshotResId, description, sortIndex);
	}



	public static KeyboardTheme getFallbackTheme(Context appContext) {
		final String defaultThemeId = appContext.getString(R.string.settings_default_keyboard_theme_key);
		ArrayList<KeyboardTheme> themes = msInstance.getAllAddOns(appContext);
        if (defaultThemeId != null) {
       	 //Find the builder in the array by id. Mayne would've been better off with a HashSet
            for (KeyboardTheme aTheme : themes) {
                if (aTheme.getId().equals(defaultThemeId)) {
                	return aTheme;
                }
            }
        }
        
        return getCurrentKeyboardTheme(appContext);
	}
	
	@Override
	protected boolean isEventRequiresViewReset(Intent eventIntent) {
		return true;
	}
}
