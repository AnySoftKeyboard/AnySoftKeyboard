package com.anysoftkeyboard.ui.settings;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.ui.dev.DeveloperToolsFragment;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;

public class MainTweaksFragmentTest extends RobolectricFragmentTestCase<MainTweaksFragment> {
  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.mainTweaksFragment;
  }

  @Test
  public void testNavigateToDevTools() {
    MainTweaksFragment fragment = startFragment();

    final Preference preferenceDevTools = fragment.findPreference(MainTweaksFragment.DEV_TOOLS_KEY);
    preferenceDevTools.getOnPreferenceClickListener().onPreferenceClick(preferenceDevTools);

    TestRxSchedulers.foregroundFlushAllJobs();
    Fragment navigatedToFragment = getCurrentFragment();
    Assert.assertTrue(navigatedToFragment instanceof DeveloperToolsFragment);
  }
}
