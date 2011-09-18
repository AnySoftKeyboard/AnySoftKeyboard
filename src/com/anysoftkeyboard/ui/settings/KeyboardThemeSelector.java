package com.anysoftkeyboard.ui.settings;

import java.util.List;

import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.R;

public class KeyboardThemeSelector extends AddOnSelector<KeyboardTheme> {

	@Override
	protected String getAdditionalMarketQueryString() {
		return " theme";
	}
	
	@Override
	protected int getAddonsListPrefKeyResId() {
		return R.string.settings_key_keyboard_theme_key;
	}
	
	@Override
	protected int getPrefsLayoutResId() {
		return R.layout.prefs_addon_keyboard_theme_selector;
	}
	
	@Override
	protected List<KeyboardTheme> getAllAvailableAddOns() {
		return KeyboardThemeFactory.getAllAvailableQuickKeys(getApplicationContext());
	}
	
	@Override
	protected boolean allowExternalPacks() {
		return false;
	}
}