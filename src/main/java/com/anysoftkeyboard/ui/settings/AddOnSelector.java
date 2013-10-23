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

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.ui.MainForm;
import com.anysoftkeyboard.ui.settings.widget.AddOnListPreference;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public abstract class AddOnSelector<E extends AddOn> extends PreferenceActivity {
    private static final String SEARCH_MARKET_PACKS_PREF_KEY = "search_for_addon_packs_at_market";
    private AddOnListPreference mAddonsList;

    private final static int DIALOG_NO_EXTERNAL_PACKS_FOR_NOW = 234234;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(getPrefsLayoutResId());

        final Preference searcher = findPreference(SEARCH_MARKET_PACKS_PREF_KEY);
        searcher.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (preference.getKey().equals(SEARCH_MARKET_PACKS_PREF_KEY)) {
                    if (allowExternalPacks()) {
                        try {
                            MainForm.searchMarketForAddons(AddOnSelector.this.getApplicationContext(), getAdditionalMarketQueryString());
                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), getText(R.string.no_market_store_available), Toast.LENGTH_LONG).show();
                            Log.e("ASK-SETTINGS", "Failed to launch market!", ex);
                        }
                    } else {
                        showDialog(DIALOG_NO_EXTERNAL_PACKS_FOR_NOW);
                    }
                    return true;
                }
                return false;
            }
        });

        mAddonsList = (AddOnListPreference) findPreference(getString(getAddonsListPrefKeyResId()));
    }

    protected abstract AddOn getCurrentSelectedAddOn();

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_NO_EXTERNAL_PACKS_FOR_NOW) {
            AlertDialog alert = new AlertDialog.Builder(this)
                    .setTitle(R.string.no_extrenal_packs_support_title)
                    .setMessage(R.string.no_extrenal_packs_support_message)
                    .setCancelable(true)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            return alert;
        } else {
            return super.onCreateDialog(id);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final List<E> keys = getAllAvailableAddOns();
        AddOn[] addOns = new AddOn[keys.size()];
        keys.toArray(addOns);
        mAddonsList.setAddOnsList(addOns);
        mAddonsList.setSelectedAddOn(getCurrentSelectedAddOn());
    }

    protected abstract boolean allowExternalPacks();

    protected abstract String getAdditionalMarketQueryString();

    protected abstract List<E> getAllAvailableAddOns();

    protected abstract int getPrefsLayoutResId();

    protected abstract int getAddonsListPrefKeyResId();

//	protected abstract int getScreenTitleResId();
//	protected abstract int getAddonsCategoryTitleResId();
//	protected abstract int getAddonsListTitleResId();
//	protected abstract int getAddonsListSummaryResId();
//	protected abstract int getAddonsListDialogTitleResId();	
}