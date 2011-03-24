
package com.menny.android.anysoftkeyboard.settings;

import java.util.ArrayList;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import com.menny.android.anysoftkeyboard.MainForm;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardFactory;

public class Keyboards extends PreferenceActivity {

	// Number of preferences without loading external keyboards
	//private int mDefaultPreferencesCount = 0;
	private PreferenceCategory mKeyboardsGroup;

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.prefs_keyboards);
        mKeyboardsGroup = (PreferenceCategory)super.findPreference("keyboard_addons_group");
        //mDefaultPreferencesCount = mKeyboardsGroup.getPreferenceCount();

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

		//getting all keyboards
		final ArrayList<KeyboardAddOnAndBuilder> creators = KeyboardFactory.getAllAvailableKeyboards(getApplicationContext());
		
		//removeNonDefaultPreferences();
		mKeyboardsGroup.removeAll();
			
		for(final KeyboardAddOnAndBuilder creator : creators)
		{
		    final CheckBoxPreference checkBox = new CheckBoxPreference(getApplicationContext());

			checkBox.setKey(creator.getId());
			checkBox.setTitle(creator.getName());
			checkBox.setPersistent(true);
			checkBox.setDefaultValue(creator.getKeyboardDefaultEnabled());
			checkBox.setSummaryOn(creator.getDescription());
			checkBox.setSummaryOff(creator.getDescription());

			mKeyboardsGroup.addPreference(checkBox);
		}
	}
}
