package com.anysoftkeyboard.ui.settings;

import android.os.Build;
import androidx.preference.Preference;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M)
public class NextWordSettingsFragmentTest
    extends RobolectricFragmentTestCase<NextWordSettingsFragment> {
  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.nextWordSettingsFragment;
  }

  @Test
  public void testShowLanguageStats() {
    final NextWordSettingsFragment nextWordSettingsFragment = startFragment();

    com.anysoftkeyboard.rx.TestRxSchedulers.backgroundFlushAllJobs();
    TestRxSchedulers.foregroundFlushAllJobs();

    final Preference enStats = nextWordSettingsFragment.findPreference("en_stats");
    Assert.assertNotNull(enStats);
    Assert.assertEquals("en - English", enStats.getTitle());
  }
}
