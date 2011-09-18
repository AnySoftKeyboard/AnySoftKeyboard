package com.anysoftkeyboard.ui.settings;

import java.util.List;

import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.menny.android.anysoftkeyboard.R;

public class BottomRowSelector extends AddOnSelector<KeyboardExtension> {

	@Override
	protected String getAdditionalMarketQueryString() {
		return " bottom row";
	}
	
	@Override
	protected int getPrefsLayoutResId() {
		return R.layout.prefs_addon_bottom_row_selector;
	}
	
	@Override
	protected int getAddonsListPrefKeyResId() {
		return R.string.settings_key_ext_kbd_bottom_row_key;
	}
	
	@Override
	protected List<KeyboardExtension> getAllAvailableAddOns() {
		return KeyboardExtensionFactory.getAllAvailableExtensions(getApplicationContext(), KeyboardExtension.TYPE_BOTTOM);
	}
	
	@Override
	protected boolean allowExternalPacks() {
		return false;
	}
}