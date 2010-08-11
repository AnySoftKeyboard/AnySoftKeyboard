
package com.menny.android.anysoftkeyboard.settings;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import com.menny.android.anysoftkeyboard.MainForm;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardBuildersFactory;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardBuildersFactory.KeyboardBuilder;

public class Keyboards extends PreferenceActivity {

	// Number of preferences without loading external keyboards
	private int mDefaultPreferencesCount = 0;
	private PreferenceGroup mKeyboardsGroup;

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.prefs_keyboards);
        mKeyboardsGroup = (PreferenceGroup)super.findPreference("prefs_keyboards_screen");
        mDefaultPreferencesCount = mKeyboardsGroup.getPreferenceCount();

		final Preference searcher = (Preference)super.findPreference("search_for_keyboards_packs");
		searcher.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				if (preference.getKey().equals("search_for_keyboards_packs"))
				{
					try
					{
						MainForm.searchMarketForAddons(Keyboards.this.getApplicationContext());
					}
					catch(Exception ex)
					{
						Log.e("ASK-SETTINGS", "Failed to launch market!", ex);
					}
					return true;
				}
				return false;
			}
		});
    }

	@Override
	protected void onResume() {
		super.onResume();

		//first resetting maybe the user has installed a new keyboard
		KeyboardBuildersFactory.resetBuildersCache();
		//getting all keyboards
		final ArrayList<KeyboardBuilder> creators = KeyboardBuildersFactory.getAllBuilders(getApplicationContext());
		
		removeNonDefaultPreferences();

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

			mKeyboardsGroup.addPreference(checkBox);
		}
	}

	private void removeNonDefaultPreferences() {
		// We keep the preferences defined in the xml, everything else goes
		while(mKeyboardsGroup.getPreferenceCount() > mDefaultPreferencesCount)
		{
			mKeyboardsGroup.removePreference(mKeyboardsGroup.getPreference(mDefaultPreferencesCount));
		}
	}
}
