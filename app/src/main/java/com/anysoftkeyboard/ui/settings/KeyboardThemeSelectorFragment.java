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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public class KeyboardThemeSelectorFragment extends AbstractAddOnsBrowserFragment<KeyboardTheme> {

    public KeyboardThemeSelectorFragment() {
        super("KeyboardThemeSelectorFragment", R.string.keyboard_theme_list_title, true, false, true);
    }

    @NonNull
    @Override
    protected AddOnsFactory<KeyboardTheme> getAddOnFactory() {
        return AnyApplication.getKeyboardThemeFactory(getContext());
    }

    @Override
    protected void onTweaksOptionSelected() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof FragmentChauffeurActivity) {
            FragmentChauffeurActivity chauffeurActivity = (FragmentChauffeurActivity) activity;
            chauffeurActivity.addFragmentToUi(new KeyboardThemeTweaksFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
        }
    }

    @Override
    protected int getMarketSearchTitle() {
        return R.string.search_market_for_keyboard_addons;
    }

    @Nullable
    @Override
    protected String getMarketSearchKeyword() {
        return "theme";
    }

    @Override
    protected void applyAddOnToDemoKeyboardView(@NonNull KeyboardTheme addOn, @NonNull DemoAnyKeyboardView demoKeyboardView) {
        demoKeyboardView.resetKeyboardTheme(addOn);
        AnyKeyboard defaultKeyboard = AnyApplication.getKeyboardFactory(getContext()).getEnabledAddOn().createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        defaultKeyboard.loadKeyboard(demoKeyboardView.getThemedKeyboardDimens());
        demoKeyboardView.setKeyboard(defaultKeyboard, null, null);
    }
}