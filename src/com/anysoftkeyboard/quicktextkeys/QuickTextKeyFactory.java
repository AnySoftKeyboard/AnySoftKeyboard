package com.anysoftkeyboard.quicktextkeys;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.R;

public class QuickTextKeyFactory extends AddOnsFactory<QuickTextKey>
{
	
	private static final QuickTextKeyFactory msInstance;
	
	static
	{
		msInstance = new QuickTextKeyFactory();
	}
	
	public static QuickTextKey getCurrentQuickTextKey(AnyKeyboardContextProvider contextProvider)
	{
		 SharedPreferences sharedPreferences = contextProvider.getSharedPreferences();
         String settingKey = contextProvider.getApplicationContext().getString(R.string.settings_key_active_quick_text_key);
         
         String selectedKeyId = sharedPreferences.getString(settingKey, null);
         QuickTextKey selectedKey = null;
         ArrayList<QuickTextKey> keys = msInstance.getAllAddOns(contextProvider.getApplicationContext());
         if (selectedKeyId != null) {
        	 //Find the builder in the array by id. Mayne would've been better off with a HashSet
             for (QuickTextKey aKey : keys) {
                 if (aKey.getId().equals(selectedKeyId)) {
                     selectedKey = aKey;
                     break;
                 }
             }
         }

         if (selectedKey == null) {
        	 //Haven't found a builder or no preference is stored, so we use the default one
        	 selectedKey = keys.get(0);

             SharedPreferences.Editor editor = sharedPreferences.edit();
             editor.putString(settingKey, selectedKey.getId());
             editor.commit();
         }

         return selectedKey;
	}
	


	public static ArrayList<QuickTextKey> getAllAvailableQuickKeys(Context applicationContext) {
		return msInstance.getAllAddOns(applicationContext);
	}

	private static final String XML_POPUP_KEYBOARD_RES_ID_ATTRIBUTE = "popupKeyboard";
	private static final String XML_POPUP_LIST_TEXT_RES_ID_ATTRIBUTE = "popupListText";
	private static final String XML_POPUP_LIST_OUTPUT_RES_ID_ATTRIBUTE = "popupListOutput";
	private static final String XML_POPUP_LIST_ICONS_RES_ID_ATTRIBUTE = "popupListIcons";
	private static final String XML_ICON_RES_ID_ATTRIBUTE = "keyIcon";
	private static final String XML_KEY_LABEL_RES_ID_ATTRIBUTE = "keyLabel";
	private static final String XML_KEY_OUTPUT_TEXT_RES_ID_ATTRIBUTE = "keyOutputText";
	private static final String XML_ICON_PREVIEW_RES_ID_ATTRIBUTE = "iconPreview";
	
	private QuickTextKeyFactory() {
		super("ASK_QKF", "com.anysoftkeyboard.plugin.QUICK_TEXT_KEY", "com.anysoftkeyboard.plugindata.quicktextkeys", 
				"QuickTextKeys", "QuickTextKey", 
				R.xml.quick_text_keys);
	}

	@Override
	protected QuickTextKey createConcreateAddOn(Context context, String prefId, int nameResId,
			String description, int sortIndex, AttributeSet attrs) {
		final int popupKeyboardResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_KEYBOARD_RES_ID_ATTRIBUTE, -1);
		final int popupListTextResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_LIST_TEXT_RES_ID_ATTRIBUTE, -1);
		final int popupListOutputResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_LIST_OUTPUT_RES_ID_ATTRIBUTE, -1);
		final int popupListIconsResId = attrs.getAttributeResourceValue(null,
				XML_POPUP_LIST_ICONS_RES_ID_ATTRIBUTE, -1);
		final int iconResId = attrs.getAttributeResourceValue(null,
				XML_ICON_RES_ID_ATTRIBUTE, -1); //Maybe should make a default icon
		final int keyLabelResId = attrs.getAttributeResourceValue(null,
				XML_KEY_LABEL_RES_ID_ATTRIBUTE, -1);
		final int keyOutputTextResId = attrs.getAttributeResourceValue(null,
				XML_KEY_OUTPUT_TEXT_RES_ID_ATTRIBUTE, -1);
		final int keyIconPreviewResId = attrs.getAttributeResourceValue(null,
				XML_ICON_PREVIEW_RES_ID_ATTRIBUTE, -1);
		
		if (
				((popupKeyboardResId == -1) && ((popupListTextResId == -1) || (popupListOutputResId == -1))) ||
				((iconResId == -1) && (keyLabelResId == -1))||
				(keyOutputTextResId == -1))
		{
			String detailMessage = String.format("Missing details for creating QuickTextKey! prefId %s\n"+
					"popupKeyboardResId: %d, popupListTextResId: %d, popupListOutputResId: %d, (iconResId: %d, keyLabelResId: %d), keyOutputTextResId: %d", 
					prefId, popupKeyboardResId, popupListTextResId, popupListOutputResId, iconResId, keyLabelResId, keyOutputTextResId);
			
			throw new RuntimeException(detailMessage);
		}
		return new QuickTextKey(context, prefId, nameResId, popupKeyboardResId,
				popupListTextResId, popupListOutputResId, popupListIconsResId, iconResId,
				keyLabelResId, keyOutputTextResId, keyIconPreviewResId, description, sortIndex);
	}
}
