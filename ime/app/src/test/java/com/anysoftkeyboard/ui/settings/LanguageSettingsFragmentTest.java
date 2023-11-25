package com.anysoftkeyboard.ui.settings;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class LanguageSettingsFragmentTest
    extends BaseSettingsFragmentTest<LanguageSettingsFragment> {

  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.languageSettingsFragment;
  }

  @Test
  public void testNavigationKeyboards() {
    final LanguageSettingsFragment languageSettingsFragment = startFragment();

    Assert.assertTrue(
        ViewTestUtils.navigateByClicking(languageSettingsFragment, R.id.settings_tile_keyboards)
            instanceof KeyboardAddOnBrowserFragment);
  }

  @Test
  public void testNavigationGrammar() {
    final LanguageSettingsFragment languageSettingsFragment = startFragment();

    Assert.assertTrue(
        ViewTestUtils.navigateByClicking(languageSettingsFragment, R.id.settings_tile_grammar)
            instanceof DictionariesFragment);
  }

  @Test
  public void testNavigationTweaks() {
    final LanguageSettingsFragment languageSettingsFragment = startFragment();

    Assert.assertTrue(
        ViewTestUtils.navigateByClicking(languageSettingsFragment, R.id.settings_tile_even_more)
            instanceof AdditionalLanguageSettingsFragment);
  }
}
