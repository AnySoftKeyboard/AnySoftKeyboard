
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
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.anysoftkeyboard.ui.settings.widget.AddOnListPreference;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public class AdditionalUiSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.prefs_addtional_ui_addons_prefs);
        findPreference(getString(R.string.tweaks_group_key)).setOnPreferenceClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.more_ui_settings_group));
        Context appContext = getActivity().getApplicationContext();
        //updating the data in the add-on selectors
        AddOnListPreference bottomRow = (AddOnListPreference) findPreference(getString(R.string.settings_key_ext_kbd_bottom_row_key));
        AddOnListPreference.populateAddOnListPreference(bottomRow,
                KeyboardExtensionFactory.getAllAvailableExtensions(appContext, KeyboardExtension.TYPE_BOTTOM),
                KeyboardExtensionFactory.getCurrentKeyboardExtension(appContext, KeyboardExtension.TYPE_BOTTOM));

        AddOnListPreference topRow = (AddOnListPreference) findPreference(getString(R.string.settings_key_ext_kbd_top_row_key));
        AddOnListPreference.populateAddOnListPreference(topRow,
                KeyboardExtensionFactory.getAllAvailableExtensions(appContext, KeyboardExtension.TYPE_TOP),
                KeyboardExtensionFactory.getCurrentKeyboardExtension(appContext, KeyboardExtension.TYPE_TOP));

        AddOnListPreference extKeyboard = (AddOnListPreference) findPreference(getString(R.string.settings_key_ext_kbd_ext_ketboard_key));
        AddOnListPreference.populateAddOnListPreference(extKeyboard,
                KeyboardExtensionFactory.getAllAvailableExtensions(appContext, KeyboardExtension.TYPE_EXTENSION),
                KeyboardExtensionFactory.getCurrentKeyboardExtension(appContext, KeyboardExtension.TYPE_EXTENSION));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.tweaks_group_key))) {
            Activity activity = getActivity();
            if (activity != null && activity instanceof FragmentChauffeurActivity) {
                ((FragmentChauffeurActivity)activity).addFragmentToUi(new UiTweaksFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                return true;
            }
        }
        return false;
    }
}
