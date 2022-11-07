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
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.menny.android.anysoftkeyboard.R;

public class DictionariesFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_dictionaries);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(getString(R.string.user_dict_editor_key)).setOnPreferenceClickListener(this);
        findPreference(getString(R.string.abbreviation_dict_editor_key))
                .setOnPreferenceClickListener(this);
        findPreference(getString(R.string.next_word_dict_settings_key))
                .setOnPreferenceClickListener(this);
        findPreference(getString(R.string.settings_key_use_contacts_dictionary))
                .setOnPreferenceClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.special_dictionaries_group));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final NavController navController = Navigation.findNavController(requireView());
        if (preference.getKey().equals(getString(R.string.user_dict_editor_key))) {
            navController.navigate(
                    DictionariesFragmentDirections
                            .actionDictionariesFragmentToUserDictionaryEditorFragment());
            return true;
        } else if (preference.getKey().equals(getString(R.string.abbreviation_dict_editor_key))) {
            navController.navigate(
                    DictionariesFragmentDirections
                            .actionDictionariesFragmentToAbbreviationDictionaryEditorFragment());
            return true;
        } else if (preference.getKey().equals(getString(R.string.next_word_dict_settings_key))) {
            navController.navigate(
                    DictionariesFragmentDirections
                            .actionDictionariesFragmentToNextWordSettingsFragment());
            return true;
        } else if (preference
                        .getKey()
                        .equals(getString(R.string.settings_key_use_contacts_dictionary))
                && ((CheckBoxPreference) preference).isChecked()) {
            // user enabled Contacts!
            // ensuring we have permission to use it
            ((MainSettingsActivity) requireActivity()).startContactsPermissionRequest();
        }
        return false;
    }
}
