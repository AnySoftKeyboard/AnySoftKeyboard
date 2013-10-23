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

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public class KeyboardThemeSelectorFragment extends AbstractAddOnSelectorFragment<KeyboardTheme> {

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
        return KeyboardThemeFactory.getAllAvailableQuickKeys(getActivity().getApplicationContext());
    }

    @Override
    protected AddOn getCurrentSelectedAddOn() {
        return KeyboardThemeFactory.getCurrentKeyboardTheme(getActivity().getApplicationContext());
    }
}