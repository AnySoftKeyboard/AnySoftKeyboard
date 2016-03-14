
/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ui.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import com.anysoftkeyboard.ui.settings.wordseditor.AbbreviationDictionaryEditorFragment;
import com.anysoftkeyboard.ui.settings.wordseditor.UserDictionaryEditorFragment;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public class DictionariesFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.prefs_dictionaries);
        findPreference(getString(R.string.user_dict_editor_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.abbreviation_dict_editor_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.next_word_dict_settings_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.settings_key_use_contacts_dictionary)).setOnPreferenceClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.special_dictionaries_group));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        MainSettingsActivity activity = (MainSettingsActivity) getActivity();
        if (activity == null) return false;
        if (preference.getKey().equals(getString(R.string.user_dict_editor_key))) {
            activity.addFragmentToUi(new UserDictionaryEditorFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
            return true;
        } else if (preference.getKey().equals(getString(R.string.abbreviation_dict_editor_key))) {
            activity.addFragmentToUi(new AbbreviationDictionaryEditorFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
            return true;
        } else if (preference.getKey().equals(getString(R.string.next_word_dict_settings_key))) {
            activity.addFragmentToUi(new NextWordSettingsFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
            return true;
        } else if (preference.getKey().equals(getString(R.string.settings_key_use_contacts_dictionary))) {
            if (((CheckBoxPreference) preference).isChecked()) {
                //user enabled Contacts!
                //ensuring we have permission to use it
                activity.startContactsPermissionRequest();
            }
        }
        return false;
    }
}
