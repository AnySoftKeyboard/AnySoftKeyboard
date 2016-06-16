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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.preference.PreferenceFragment;
import android.view.View;

import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

import java.util.Collections;
import java.util.List;

public class AdditionalUiSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.prefs_addtional_ui_addons_prefs);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(getString(R.string.tweaks_group_key)).setOnPreferenceClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.more_ui_settings_group));

        final Preference topRowSelector = findPreference(getString(R.string.settings_key_ext_kbd_top_row_key));
        topRowSelector.setOnPreferenceClickListener(this);
        topRowSelector.setSummary(getString(R.string.top_generic_row_summary, KeyboardExtensionFactory.getCurrentKeyboardExtension(getContext(), KeyboardExtension.TYPE_TOP).getName()));

        final Preference topBottomSelector = findPreference(getString(R.string.settings_key_ext_kbd_bottom_row_key));
        topBottomSelector.setOnPreferenceClickListener(this);
        topBottomSelector.setSummary(getString(R.string.bottom_generic_row_summary, KeyboardExtensionFactory.getCurrentKeyboardExtension(getContext(), KeyboardExtension.TYPE_BOTTOM).getName()));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof FragmentChauffeurActivity) {
            FragmentChauffeurActivity chauffeurActivity = (FragmentChauffeurActivity) activity;
            final String key = preference.getKey();
            if (key.equals(getString(R.string.tweaks_group_key))) {
                chauffeurActivity.addFragmentToUi(new UiTweaksFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                return true;
            } else if (key.equals(getString(R.string.settings_key_ext_kbd_top_row_key))) {
                chauffeurActivity.addFragmentToUi(new TopRowAddOnBrowserFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                return true;
            } else if (key.equals(getString(R.string.settings_key_ext_kbd_bottom_row_key))) {
                chauffeurActivity.addFragmentToUi(new BottomRowAddOnBrowserFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                return true;
            }
        }

        return false;
    }

    public static abstract class RowAddOnBrowserFragment extends AbstractKeyboardAddOnsBrowserFragment<KeyboardExtension> {

        private final int mRowType;
        private final int mPrefKeyResourceId;

        protected RowAddOnBrowserFragment(@NonNull String tag, int rowType, @StringRes int titleResourceId, @StringRes int prefKeyResourceId) {
            super(tag, titleResourceId, true);
            this.mRowType = rowType;
            this.mPrefKeyResourceId = prefKeyResourceId;
        }

        @NonNull
        @Override
        protected final List<KeyboardExtension> getEnabledAddOns() {
            return Collections.singletonList(KeyboardExtensionFactory.getCurrentKeyboardExtension(getContext(), mRowType));
        }

        @NonNull
        @Override
        protected final List<KeyboardExtension> getAllAvailableAddOns() {
            return KeyboardExtensionFactory.getAllAvailableExtensions(getContext(), mRowType);
        }

        @Override
        protected final void onEnabledAddOnsChanged(@NonNull List<String> newEnabledAddOns) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            editor.putString(getString(mPrefKeyResourceId), newEnabledAddOns.get(0));
            editor.commit();
        }

        @Override
        protected final boolean isSingleSelectedAddOn() {
            return true;
        }

        @Nullable
        @Override
        protected final String getMarketSearchKeyword() {
            return null;
        }

        @Override
        protected final int getMarketSearchTitle() {
            return 0;
        }

        @Override
        protected final void applyAddOnToDemoKeyboardView(@NonNull KeyboardExtension addOn, @NonNull DemoAnyKeyboardView demoKeyboardView) {
            AnyKeyboard defaultKeyboard = KeyboardFactory.getEnabledKeyboards(getContext()).get(0).createKeyboard(getContext(), getResources().getInteger(R.integer.keyboard_mode_normal));
            loadKeyboardWithAddOn(demoKeyboardView, defaultKeyboard, addOn);
            demoKeyboardView.setKeyboard(defaultKeyboard);
        }

        protected abstract void loadKeyboardWithAddOn(@NonNull DemoAnyKeyboardView demoKeyboardView, AnyKeyboard defaultKeyboard, KeyboardExtension addOn);
    }

    public static class TopRowAddOnBrowserFragment extends RowAddOnBrowserFragment {

        public TopRowAddOnBrowserFragment() {
            super("TopRowAddOnBrowserFragment", KeyboardExtension.TYPE_TOP, R.string.top_generic_row_dialog_title, R.string.settings_key_ext_kbd_top_row_key);
        }

        @Override
        protected void loadKeyboardWithAddOn(@NonNull DemoAnyKeyboardView demoKeyboardView, AnyKeyboard defaultKeyboard, KeyboardExtension addOn) {
            defaultKeyboard.loadKeyboard(demoKeyboardView.getThemedKeyboardDimens(),
                    addOn,
                    KeyboardExtensionFactory.getCurrentKeyboardExtension(getContext(), KeyboardExtension.TYPE_BOTTOM));
        }
    }

    public static class BottomRowAddOnBrowserFragment extends RowAddOnBrowserFragment {

        public BottomRowAddOnBrowserFragment() {
            super("BottomRowAddOnBrowserFragment", KeyboardExtension.TYPE_BOTTOM, R.string.bottom_generic_row_dialog_title, R.string.settings_key_ext_kbd_bottom_row_key);
        }

        @Override
        protected void loadKeyboardWithAddOn(@NonNull DemoAnyKeyboardView demoKeyboardView, AnyKeyboard defaultKeyboard, KeyboardExtension addOn) {
            defaultKeyboard.loadKeyboard(demoKeyboardView.getThemedKeyboardDimens(),
                    KeyboardExtensionFactory.getCurrentKeyboardExtension(getContext(), KeyboardExtension.TYPE_TOP),
                    addOn);
        }
    }

}
