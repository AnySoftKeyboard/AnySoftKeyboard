package com.anysoftkeyboard.ui.settings;

import java.util.List;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.ui.MainForm;

public abstract class AddOnSelector<E extends AddOn> extends PreferenceActivity {
	private static final String SEARCH_MARKET_PACKS_PREF_KEY = "search_for_addon_packs_at_market";
	private ListPreference mAddonsList;
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(getPrefsLayoutResId());

		final Preference searcher = findPreference(SEARCH_MARKET_PACKS_PREF_KEY);
		searcher.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				if (preference.getKey().equals(SEARCH_MARKET_PACKS_PREF_KEY)) {
					try {
						MainForm.searchMarketForAddons(AddOnSelector.this.getApplicationContext(), getAdditionalMarketQueryString());
					} catch (Exception ex) {
						Log.e("ASK-SETTINGS", "Failed to launch market!", ex);
					}
					return true;
				}
				return false;
			}
		});
		
		mAddonsList = (ListPreference) findPreference(getString(getAddonsListPrefKeyResId()));
    }

	
	@Override
	protected void onResume() {
		super.onResume();
		final List<E> keys = getAllAvailableAddOns();

		String[] ids = new String[keys.size()];
		String[] names = new String[keys.size()];
		int entryPos = 0;
		for (E aKey : keys) {
			ids[entryPos] = aKey.getId();
			names[entryPos] = aKey.getName();
			entryPos++;
		}
		mAddonsList.setEntries(names);
		mAddonsList.setEntryValues(ids);
	}
	
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