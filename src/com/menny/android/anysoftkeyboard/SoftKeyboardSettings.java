
package com.menny.android.anysoftkeyboard;

import java.util.ArrayList;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardCreatorsFactory;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardCreatorsFactory.KeyboardCreator;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;

public class SoftKeyboardSettings extends PreferenceActivity {
	public final static String PREFERENCES_FILE = "anysoftkeyboard_preferences";
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs);
        
        String version = "";
        try {
			PackageInfo info = super.getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
			version = info.versionName + " (release "+info.versionCode+")";
		} catch (NameNotFoundException e) {
			Log.e("AnySoftKeyboard", "Failed to locate package information! This is very weird... I'm installed.");
		}
		
		Preference label = super.findPreference("prefs_title_key");
		label.setSummary(label.getSummary()+version);
		
		ArrayList<KeyboardCreator> creators = KeyboardCreatorsFactory.getAllCreators(getApplicationContext());
		PreferenceCategory keyboards = (PreferenceCategory)super.findPreference("prefs_keyboards_screen");
		
		for(KeyboardCreator creator : creators)
		{
			if (creator.getKeyboardNameResId() == R.string.eng_keyboard)
				continue;//english is an internal keyboard, and is on by default.
			CheckBoxPreference checkBox = new CheckBoxPreference(getApplicationContext());
			/*
			 * <CheckBoxPreference
				android:key="eng_keyboard"
				android:title="@string/eng_keyboard"
				android:persistent="true" 
				android:defaultValue="true"
				android:summaryOn="QWERTY Latin keyboard"
				android:summaryOff="QWERTY Latin keyboard"
				/>
			 */
			checkBox.setKey(creator.getKeyboardPrefId());
			checkBox.setTitle(creator.getKeyboardNameResId());
			checkBox.setPersistent(true);
			checkBox.setDefaultValue(false);
			checkBox.setSummaryOn(creator.getDescription());
			checkBox.setSummaryOff(creator.getDescription());
			
			keyboards.addPreference(checkBox);
		}
		
    }
}
