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
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public class QuickTextKeys extends AddOnSelector<QuickTextKey> {

    @Override
    protected String getAdditionalMarketQueryString() {
        return " quick key";
    }

    @Override
    protected int getAddonsListPrefKeyResId() {
        return R.string.settings_key_active_quick_text_key;
    }

    @Override
    protected int getPrefsLayoutResId() {
        return R.xml.prefs_addon_quick_keys_selector;
    }

    @Override
    protected List<QuickTextKey> getAllAvailableAddOns() {
        return QuickTextKeyFactory.getAllAvailableQuickKeys(getApplicationContext());
    }

    @Override
    protected boolean allowExternalPacks() {
        return true;
    }

    @Override
    protected AddOn getCurrentSelectedAddOn() {
        return QuickTextKeyFactory.getCurrentQuickTextKey(getApplicationContext());
    }
}