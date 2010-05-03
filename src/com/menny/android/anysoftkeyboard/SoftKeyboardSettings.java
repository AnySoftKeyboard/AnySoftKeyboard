
package com.menny.android.anysoftkeyboard;

import com.menny.android.anysoftkeyboard.keyboards.KeyboardBuildersFactory;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardBuildersFactory.KeyboardBuilder;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;

import java.util.ArrayList;

public class SoftKeyboardSettings extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.prefs);
        
        String version = "";
        try {
			final PackageInfo info = getPackageInfo(getApplicationContext());
			version = info.versionName + " (release "+info.versionCode+")";
		} catch (final NameNotFoundException e) {
			Log.e("AnySoftKeyboard", "Failed to locate package information! This is very weird... I'm installed.");
		}
		
		final Preference label = super.findPreference("prefs_title_key");
		label.setSummary(label.getSummary()+version);
		//first resetting maybe the user has installed a new keyboard
		KeyboardBuildersFactory.resetBuildersCache();
		//getting all keyboards
		final ArrayList<KeyboardBuilder> creators = KeyboardBuildersFactory.getAllBuilders(getApplicationContext());
		final PreferenceCategory keyboards = (PreferenceCategory)super.findPreference("prefs_keyboards_screen");
		
		for(final KeyboardBuilder creator : creators)
		{
		    final Context creatorContext = creator.getPackageContext() == null?
		            getApplicationContext() : creator.getPackageContext();
		            
			if (creatorContext == getApplicationContext() && creator.getKeyboardNameResId() == R.string.eng_keyboard) {
                continue;//english is an internal keyboard, and is on by default.
            }
			final CheckBoxPreference checkBox = new CheckBoxPreference(getApplicationContext());
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
			checkBox.setKey(creator.getId());
			checkBox.setTitle(creatorContext.getText(creator.getKeyboardNameResId()));
			checkBox.setPersistent(true);
			checkBox.setDefaultValue(false);
			checkBox.setSummaryOn(creator.getDescription());
			checkBox.setSummaryOff(creator.getDescription());
			
			keyboards.addPreference(checkBox);
		}
		
    }

	public static PackageInfo getPackageInfo(Context context) throws NameNotFoundException {
		return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	}
}
