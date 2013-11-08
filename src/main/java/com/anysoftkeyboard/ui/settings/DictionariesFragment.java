
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

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import com.anysoftkeyboard.ui.settings.wordseditor.UserDictionaryEditorFragment;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.FragmentChauffeurActivity;

public class DictionariesFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.prefs_dictionaries);
        findPreference(getString(R.string.user_dict_editor_key)).setOnPreferenceClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();
        activity.setTitle(getString(R.string.special_dictionaries_group));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.user_dict_editor_key))) {
            Activity activity = getActivity();
            if (activity != null && activity instanceof FragmentChauffeurActivity) {
                ((FragmentChauffeurActivity)activity).addFragmentToUi(new UserDictionaryEditorFragment(), FragmentChauffeurActivity.FragmentUiContext.DeeperExperience);
                return true;
            }
        }
        return false;
    }
}
