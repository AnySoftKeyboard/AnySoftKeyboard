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
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.ui.MainForm;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;

public class Keyboards extends PreferenceActivity {

    // Number of preferences without loading external keyboards
    // private int mDefaultPreferencesCount = 0;
    private PreferenceCategory mKeyboardsGroup;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs_keyboards);
        mKeyboardsGroup = (PreferenceCategory) super
                .findPreference("keyboard_addons_group");
        // mDefaultPreferencesCount = mKeyboardsGroup.getPreferenceCount();

        final Preference searcher = (Preference) super
                .findPreference("search_for_keyboards_packs");
        searcher.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (preference.getKey().equals("search_for_keyboards_packs")) {
                    try {
                        MainForm.searchMarketForAddons(
                                Keyboards.this.getApplicationContext(),
                                " language");
                    } catch (Exception ex) {
                        Log.e("ASK-SETTINGS", "Failed to launch market!", ex);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // getting all keyboards
        final ArrayList<KeyboardAddOnAndBuilder> creators = KeyboardFactory
                .getAllAvailableKeyboards(getApplicationContext());

        // removeNonDefaultPreferences();
        mKeyboardsGroup.removeAll();

        for (final KeyboardAddOnAndBuilder creator : creators) {
            final AddOnCheckBoxPreference checkBox = new AddOnCheckBoxPreference(
                    getApplicationContext(), null);
            checkBox.setAddOn(creator);
            mKeyboardsGroup.addPreference(checkBox);
            /*
			 * final CheckBoxPreference checkBox = new
			 * CheckBoxPreference(getApplicationContext());
			 * 
			 * checkBox.setKey(creator.getId());
			 * checkBox.setTitle(creator.getName());
			 * checkBox.setPersistent(true);
			 * checkBox.setDefaultValue(creator.getKeyboardDefaultEnabled());
			 * checkBox.setSummaryOn(creator.getDescription());
			 * checkBox.setSummaryOff(creator.getDescription());
			 * 
			 * mKeyboardsGroup.addPreference(checkBox);
			 */

        }
    }
}
