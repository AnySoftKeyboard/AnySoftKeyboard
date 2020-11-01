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
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;

public class LanguageTweaksFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_language_tweaks);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListPreference listPreference =
                (ListPreference)
                        findPreference(getText(R.string.settings_key_layout_for_internet_fields));
        List<KeyboardAddOnAndBuilder> enabledKeyboards =
                AnyApplication.getKeyboardFactory(getContext()).getEnabledAddOns();
        CharSequence[] entries = new CharSequence[enabledKeyboards.size() + 1];
        entries[0] = getString(R.string.no_internet_fields_specific_layout);
        CharSequence[] values = new CharSequence[enabledKeyboards.size() + 1];
        values[0] = "none";
        for (int keyboardIndex = 0; keyboardIndex < enabledKeyboards.size(); keyboardIndex++) {
            final KeyboardAddOnAndBuilder builder = enabledKeyboards.get(keyboardIndex);
            entries[keyboardIndex + 1] = builder.getName() + "\n" + builder.getDescription();
            values[keyboardIndex + 1] = builder.getId();
        }
        listPreference.setEntries(entries);
        listPreference.setEntryValues(values);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.tweaks_group));
    }
}
