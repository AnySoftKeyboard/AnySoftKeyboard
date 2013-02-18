
package com.anysoftkeyboard.ui.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.anysoftkeyboard.utils.Workarounds;
import com.menny.android.anysoftkeyboard.R;

public class Dictionaries extends PreferenceActivity {

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs_dictionaries);
        if (Workarounds.getApiLevel() < 5) {
        	//disabling the Contacts dictionary
        	Preference contactsDictionary = findPreference(getResources().getString(R.string.settings_key_use_contacts_dictionary));
        	contactsDictionary.setEnabled(false);
        	contactsDictionary.setSummary(R.string.use_contacts_dictionary_not_supported_summary);
        }
    }
}
