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
import android.support.annotation.Nullable;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import com.anysoftkeyboard.ui.settings.wordseditor.AbbreviationDictionaryEditorFragment;
import com.anysoftkeyboard.ui.settings.wordseditor.UserDictionaryEditorFragment;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public class DictionariesFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_dictionaries);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        } else if (preference.getKey().equals(getString(R.string.settings_key_use_contacts_dictionary)) && ((CheckBoxPreference) preference).isChecked()) {
            //user enabled Contacts!
            //ensuring we have permission to use it
            activity.startContactsPermissionRequest();
        }
        return false;
    }
}
