package com.anysoftkeyboard.ui.settings;

import java.util.List;

import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.menny.android.anysoftkeyboard.R;

public class ExtensionKeyboardSelector extends AddOnSelector<KeyboardExtension> {

	@Override
	protected String getAdditionalMarketQueryString() {
		return " extension keyboard";
	}
	
	@Override
	protected List<KeyboardExtension> getAllAvailableAddOns() {
		return KeyboardExtensionFactory.getAllAvailableExtensions(getApplicationContext(), KeyboardExtension.TYPE_EXTENSION);
	}
	
	@Override
	protected int getAddonsListPrefKeyResId() {
		return R.string.settings_key_ext_kbd_ext_ketboard_key;
	}
	
	@Override
	protected int getPrefsLayoutResId() {
		return R.layout.prefs_addon_extension_keyboard_selector;
	}
	
	@Override
	protected boolean allowExternalPacks() {
		return false;
	}
}