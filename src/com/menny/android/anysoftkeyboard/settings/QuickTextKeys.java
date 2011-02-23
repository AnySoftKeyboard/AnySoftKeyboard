package com.menny.android.anysoftkeyboard.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;
import com.menny.android.anysoftkeyboard.MainForm;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.quicktextkeys.QuickTextKeyBuildersFactory;
import com.menny.android.anysoftkeyboard.quicktextkeys.QuickTextKeyBuildersFactory.QuickTextKeyBuilder;
import java.util.ArrayList;

/**
 *
 * @author Malcolm
 */
public class QuickTextKeys extends PreferenceActivity {
	private PreferenceCategory mQuickTextKeysGroup;

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.prefs_quick_text_keys);
        mQuickTextKeysGroup = (PreferenceCategory) findPreference("quick_text_key_addons_group");
        //mDefaultPreferencesCount = mKeyboardsGroup.getPreferenceCount();

		final Preference searcher = (Preference) findPreference("search_for_quick_text_keys_packs");
		searcher.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				if (preference.getKey().equals("search_for_quick_text_keys_packs")) {
					try {
						MainForm.searchMarketForAddons(QuickTextKeys.this.getApplicationContext());
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
		final ArrayList<QuickTextKeyBuilder> creators = QuickTextKeyBuildersFactory
				.getAllBuilders(getApplicationContext());

		String[] ids = new String[creators.size()];
		String[] names = new String[creators.size()];
		int entryPos = 0;
		for (QuickTextKeyBuilder creator : creators) {
			Context creatorContext = creator.getPackageContext() == null
					? getApplicationContext() : creator.getPackageContext();
			ids[entryPos] = creator.getId();
			names[entryPos] = creatorContext.getString(creator.getQuickTextKeyNameResId());
			entryPos++;
		}
		ListPreference keysList = (ListPreference)
					findPreference(getString(R.string.settings_key_active_quick_text_key));
		keysList.setEntries(names);
		keysList.setEntryValues(ids);
	}
}