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
import android.support.v4.preference.PreferenceFragment;
import android.view.View;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.ui.settings.widget.AddOnListPreference;

import java.util.List;

public abstract class AbstractAddOnSelectorFragment<E extends AddOn> extends PreferenceFragment {

    private AddOnListPreference mAddOnsList;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(getPrefsLayoutResId());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAddOnsList = (AddOnListPreference) findPreference(getString(getAddOnsListPrefKeyResId()));
    }

    protected abstract AddOn getCurrentSelectedAddOn();

    @Override
    public void onStart() {
        super.onStart();
        final List<E> keys = getAllAvailableAddOns();
        AddOnListPreference.populateAddOnListPreference(mAddOnsList, keys, getCurrentSelectedAddOn());
    }

    protected abstract List<E> getAllAvailableAddOns();

    protected abstract int getPrefsLayoutResId();

    protected abstract int getAddOnsListPrefKeyResId();
}