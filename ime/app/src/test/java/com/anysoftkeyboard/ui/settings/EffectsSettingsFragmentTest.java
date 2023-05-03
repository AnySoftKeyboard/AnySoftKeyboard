package com.anysoftkeyboard.ui.settings;

import androidx.fragment.app.Fragment;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class EffectsSettingsFragmentTest
    extends RobolectricFragmentTestCase<EffectsSettingsFragment> {

  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.effectsSettingsFragment;
  }

  @Test
  public void testNavigateToPowerSavingFragment() {
    final EffectsSettingsFragment fragment = startFragment();

    ViewTestUtils.performClick(fragment.findPreference("settings_key_power_save_mode"));

    TestRxSchedulers.foregroundFlushAllJobs();
    final Fragment next = getCurrentFragment();
    Assert.assertTrue(next instanceof PowerSavingSettingsFragment);
  }
}
