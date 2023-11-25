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
import androidx.annotation.VisibleForTesting;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.menny.android.anysoftkeyboard.R;

public class MainTweaksFragment extends PreferenceFragmentCompat {

  @VisibleForTesting static final String DEV_TOOLS_KEY = "dev_tools";

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.prefs_main_tweaks);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Preference preference = findPreference(DEV_TOOLS_KEY);
    if (preference == null) {
      throw new NullPointerException(
          "Preference with key '"
              + DEV_TOOLS_KEY
              + "' was not found in resource "
              + R.xml.prefs_main_tweaks);
    } else {
      preference.setOnPreferenceClickListener(this::onDevToolsPreferenceClicked);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    MainSettingsActivity.setActivityTitle(this, getString(R.string.tweaks_group));
  }

  private boolean onDevToolsPreferenceClicked(Preference p) {
    Navigation.findNavController(requireView())
        .navigate(MainTweaksFragmentDirections.actionMainTweaksFragmentToDeveloperToolsFragment());
    return true;
  }
}
