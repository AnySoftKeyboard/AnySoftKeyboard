package com.anysoftkeyboard.ui.settings;

import android.support.annotation.NonNull;

import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;

public class LanguageSettingsFragmentTest extends BaseSettingsFragmentTest<LanguageSettingsFragment> {

    @NonNull
    @Override
    protected LanguageSettingsFragment createFragment() {
        return new LanguageSettingsFragment();
    }

    @Test
    public void testNavigationKeyboards() {
        final LanguageSettingsFragment languageSettingsFragment = startFragment();

        Assert.assertTrue(navigateByClicking(languageSettingsFragment, R.id.settings_tile_keyboards) instanceof KeyboardAddOnBrowserFragment);
    }

    @Test
    public void testNavigationGrammar() {
        final LanguageSettingsFragment languageSettingsFragment = startFragment();

        Assert.assertTrue(navigateByClicking(languageSettingsFragment, R.id.settings_tile_grammar) instanceof DictionariesFragment);
    }

    @Test
    public void testNavigationTweaks() {
        final LanguageSettingsFragment languageSettingsFragment = startFragment();

        Assert.assertTrue(navigateByClicking(languageSettingsFragment, R.id.settings_tile_even_more) instanceof AdditionalLanguageSettingsFragment);
    }
}