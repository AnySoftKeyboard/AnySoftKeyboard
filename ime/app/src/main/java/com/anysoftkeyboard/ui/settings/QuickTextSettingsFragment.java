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

import android.os.Build;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.menny.android.anysoftkeyboard.R;

public class QuickTextSettingsFragment extends PreferenceFragmentCompat {

    private static void hidePref(Preference preference) {
        preference.setEnabled(false);
        preference.setVisible(false);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_quick_text_addons_prefs);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            hidePref(findPreference(getString(R.string.settings_key_default_emoji_gender)));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                hidePref(findPreference(getString(R.string.settings_key_default_emoji_skin_tone)));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.quick_text_keys_group));
    }
}
