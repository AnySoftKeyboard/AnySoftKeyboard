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

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.FragmentChauffeurActivity;

import java.util.List;

public class KeyboardThemeSelectorFragment extends AbstractAddOnSelectorFragment<KeyboardTheme> implements Preference.OnPreferenceClickListener {

    @Override
    protected int getAddOnsListPrefKeyResId() {
        return R.string.settings_key_keyboard_theme_key;
    }

    @Override
    protected int getPrefsLayoutResId() {
        return R.xml.prefs_addon_keyboard_theme_selector;
    }

    @Override
    protected List<KeyboardTheme> getAllAvailableAddOns() {
        return KeyboardThemeFactory.getAllAvailableThemes(getActivity().getApplicationContext());
    }

    @Override
    protected AddOn getCurrentSelectedAddOn() {
        return KeyboardThemeFactory.getCurrentKeyboardTheme(getActivity().getApplicationContext());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findPreference(getString(R.string.tweaks_group_key)).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.tweaks_group_key))) {
            Activity activity = getActivity();
            if (activity != null && activity instanceof FragmentChauffeurActivity) {
                ((FragmentChauffeurActivity)activity).addFragmentToUi(new KeyboardThemeTweaksFragment(), FragmentChauffeurActivity.FragmentUiContext.DeeperExperience);
                return true;
            }
        }
        return false;
    }
}