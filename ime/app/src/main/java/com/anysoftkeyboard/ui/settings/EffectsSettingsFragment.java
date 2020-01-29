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
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public class EffectsSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_effects_prefs);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(getText(R.string.settings_key_power_save_mode))
                .setOnPreferenceClickListener(
                        preference -> {
                            ((MainSettingsActivity) getActivity())
                                    .addFragmentToUi(
                                            new PowerSavingSettingsFragment(),
                                            TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                            return true;
                        });

        findPreference(getText(R.string.settings_key_night_mode))
                .setOnPreferenceClickListener(
                        preference -> {
                            ((MainSettingsActivity) getActivity())
                                    .addFragmentToUi(
                                            new NightModeSettingsFragment(),
                                            TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                            return true;
                        });
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.effects_group));
    }
}
