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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v4.preference.PreferenceFragment;
import android.view.View;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

import java.util.List;

public class KeyboardAddOnSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.prefs_keyboards);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference("keyboard_addons_group").setOnPreferenceClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.keyboards_group));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ("keyboard_addons_group".equals(preference.getKey())) {
            ((FragmentChauffeurActivity) getActivity()).addFragmentToUi(new KeyboardAddOnBrowserFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
            return true;
        }
        return false;
    }

    public static class KeyboardAddOnBrowserFragment extends AbstractKeyboardAddOnsBrowserFragment<KeyboardAddOnAndBuilder> {
        @NonNull
        @Override
        protected String getFragmentTag() {
            return "LanguageAddOnBrowserFragment";
        }

        @StringRes
        @Override
        protected int getFragmentTitleResourceId() {
            return R.string.keyboards_group;
        }

        @NonNull
        @Override
        protected List<KeyboardAddOnAndBuilder> getEnabledAddOns() {
            return KeyboardFactory.getEnabledKeyboards(getContext());
        }

        @NonNull
        @Override
        protected List<KeyboardAddOnAndBuilder> getAllAvailableAddOns() {
            return KeyboardFactory.getAllAvailableKeyboards(getContext());
        }

        @Override
        protected void onEnabledAddOnsChanged(@NonNull List<String> newEnabledAddOns) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            //disabling everything that is not enabled, and enabling what is
            for (KeyboardAddOnAndBuilder builder : getAllAvailableAddOns()) {
                editor.putBoolean(builder.getId(), newEnabledAddOns.contains(builder.getId()));
            }
            SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
        }

        @Override
        protected boolean isSingleSelectedAddOn() {
            return false;
        }

        @Override
        protected void applyAddOnToDemoKeyboardView(@NonNull KeyboardAddOnAndBuilder addOn, @NonNull DemoAnyKeyboardView demoKeyboardView) {
            AnyKeyboard defaultKeyboard = addOn.createKeyboard(getContext(), getResources().getInteger(R.integer.keyboard_mode_normal));
            defaultKeyboard.loadKeyboard(demoKeyboardView.getThemedKeyboardDimens());
            demoKeyboardView.setKeyboard(defaultKeyboard);
        }
    }
}
