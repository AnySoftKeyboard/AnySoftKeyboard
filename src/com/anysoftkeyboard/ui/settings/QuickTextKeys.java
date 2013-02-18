package com.anysoftkeyboard.ui.settings;

import java.util.List;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.menny.android.anysoftkeyboard.R;

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