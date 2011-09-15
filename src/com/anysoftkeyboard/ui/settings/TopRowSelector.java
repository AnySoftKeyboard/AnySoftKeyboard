package com.anysoftkeyboard.ui.settings;

import java.util.List;

import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.menny.android.anysoftkeyboard.R;

public class TopRowSelector extends AddOnSelector<KeyboardExtension> {

	@Override
	protected String getAdditionalMarketQueryString() {
		return " top row";
	}
	
	@Override
	protected List<KeyboardExtension> getAllAvailableAddOns() {
		return KeyboardExtensionFactory.getAllAvailableExtensions(getApplicationContext(), KeyboardExtension.TYPE_TOP);
	}
	
	@Override
	protected int getPrefsLayoutResId() {
		return R.layout.prefs_addon_top_row_selector;
	}
	
	@Override
	protected int getAddonsListPrefKeyResId() {
		return R.string.settings_key_ext_kbd_top_row_key;
	}
}