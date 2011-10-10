package com.anysoftkeyboard.keyboards;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;


public class KeyboardFactory extends AddOnsFactory<KeyboardAddOnAndBuilder>
{
	private static final String TAG = "ASK_KF";
	
	private static final String XML_LAYOUT_RES_ID_ATTRIBUTE = "layoutResId";
	private static final String XML_LANDSCAPE_LAYOUT_RES_ID_ATTRIBUTE = "landscapeResId";
	private static final String XML_ICON_RES_ID_ATTRIBUTE = "iconResId";
	private static final String XML_DICTIONARY_NAME_ATTRIBUTE = "defaultDictionaryLocale";
	private static final String XML_ADDITIONAL_IS_LETTER_EXCEPTIONS_ATTRIBUTE = "additionalIsLetterExceptions";
	private static final String XML_SENTENCE_SEPARATOR_CHARACTERS_ATTRIBUTE = "sentenceSeparators";
	private static final String DEFAULT_SENTENCE_SEPARATORS = ".,!?)";
	private static final String XML_PHYSICAL_TRANSLATION_RES_ID_ATTRIBUTE = "physicalKeyboardMappingResId";
	private static final String XML_DEFAULT_ATTRIBUTE = "defaultEnabled";
	
	private static final KeyboardFactory msInstance;
	static
	{
		msInstance = new KeyboardFactory();
	}
	
	public static ArrayList<KeyboardAddOnAndBuilder> getAllAvailableKeyboards(Context askContext)
	{
		return msInstance.getAllAddOns(askContext);
	}
	
	public static ArrayList<KeyboardAddOnAndBuilder> getEnabledKeyboards(AnyKeyboardContextProvider contextProvider)
	{
		final ArrayList<KeyboardAddOnAndBuilder> allAddOns = msInstance.getAllAddOns(contextProvider.getApplicationContext());
        Log.i(TAG, "Creating enabled addons list. I have a total of "+ allAddOns.size()+" addons");

        //getting shared prefs to determine which to create.
        final SharedPreferences sharedPreferences = contextProvider.getSharedPreferences();
        
        final ArrayList<KeyboardAddOnAndBuilder> enabledAddOns = new ArrayList<KeyboardAddOnAndBuilder>();
        for(int addOnIndex=0; addOnIndex<allAddOns.size(); addOnIndex++)
        {
            final KeyboardAddOnAndBuilder addOn = allAddOns.get(addOnIndex);

            final boolean addOnEnabled = sharedPreferences.getBoolean(addOn.getId(), addOn.getKeyboardDefaultEnabled());

            if (addOnEnabled)
            {
            	enabledAddOns.add(addOn);
            }
        }

        // Fix: issue 219
        // Check if there is any keyboards created if not, lets create a default english keyboard
        if( enabledAddOns.size() == 0 ) {
            final SharedPreferences.Editor editor = sharedPreferences.edit( );
            final KeyboardAddOnAndBuilder addOn = allAddOns.get( 0 );
            editor.putBoolean( addOn.getId( ) , true );
            editor.commit( );
            enabledAddOns.add( addOn );
        }

        if (AnyApplication.DEBUG)
        {
	        for(final KeyboardAddOnAndBuilder addOn : enabledAddOns) {
	            Log.d(TAG, "Factory provided addon: "+addOn.getId());
	        }
        }

        return enabledAddOns;
	}
	
	private KeyboardFactory() {
		super(TAG, "com.menny.android.anysoftkeyboard.KEYBOARD", "com.menny.android.anysoftkeyboard.keyboards", 
				"Keyboards", "Keyboard",
				R.xml.keyboards, true);
	}

	@Override
	protected KeyboardAddOnAndBuilder createConcreateAddOn(Context context,
			String prefId, int nameId, String description, int sortIndex,
			AttributeSet attrs) {
		
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
      String sentenceSeparators = attrs.getAttributeValue(null, 
    		  XML_SENTENCE_SEPARATOR_CHARACTERS_ATTRIBUTE);
      if (sentenceSeparators == null)
    	  sentenceSeparators = DEFAULT_SENTENCE_SEPARATORS;
      final int physicalTranslationResId = attrs.getAttributeResourceValue(null,
              XML_PHYSICAL_TRANSLATION_RES_ID_ATTRIBUTE, -1);
      
      // A keyboard is enabled by default if it is the first one (index==1)
      final boolean keyboardDefault = attrs.getAttributeBooleanValue(null,
      		XML_DEFAULT_ATTRIBUTE, sortIndex==1);

      // asserting
      if ((prefId == null) || (nameId == -1) || (layoutResId == -1)) {
          Log.e(TAG, "External Keyboard does not include all mandatory details! Will not create keyboard.");
          return null;
      } else {
          if (AnyApplication.DEBUG) {
              Log.d(TAG,
                      "External keyboard details: prefId:" + prefId + " nameId:"
                      + nameId + " resId:" + layoutResId
                      + " landscapeResId:" + landscapeLayoutResId
                      + " iconResId:" + iconResId + " defaultDictionary:"
                      + defaultDictionary);
          }
          final KeyboardAddOnAndBuilder creator = new KeyboardAddOnAndBuilder(context,
                  prefId, nameId, layoutResId, landscapeLayoutResId,
                  defaultDictionary, iconResId, physicalTranslationResId,
                  additionalIsLetterExceptions, sentenceSeparators, description, sortIndex,
                  keyboardDefault );

          return creator;
      }
	}
    
}
