package com.anysoftkeyboard.ui.settings;

import android.os.Build;
import androidx.preference.Preference;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class QuickTextSettingsFragmentTest
    extends RobolectricFragmentTestCase<QuickTextSettingsFragment> {

  @Test
  @Config(sdk = Build.VERSION_CODES.Q)
  public void testVisibleAtQ() {
    Preference preference = startFragment().findPreference("settings_key_default_emoji_skin_tone");
    Assert.assertNotNull(preference);
    Assert.assertTrue(preference.isVisible());
    Assert.assertTrue(preference.isEnabled());

    preference = startFragment().findPreference("settings_key_default_emoji_gender");
    Assert.assertNotNull(preference);
    // for now, we are hiding this always. Although, once we figure this out
    // it should be visible in Q
    Assert.assertFalse(preference.isVisible());
    Assert.assertFalse(preference.isEnabled());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.N)
  public void testVisibleAtN() {
    Preference preference = startFragment().findPreference("settings_key_default_emoji_skin_tone");
    Assert.assertNotNull(preference);
    Assert.assertTrue(preference.isVisible());
    Assert.assertTrue(preference.isEnabled());

    // gender is still hidden in N
    preference = startFragment().findPreference("settings_key_default_emoji_gender");
    Assert.assertNotNull(preference);
    Assert.assertFalse(preference.isVisible());
    Assert.assertFalse(preference.isEnabled());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.M)
  public void testInvisibleBeforeN() {
    Preference preference = startFragment().findPreference("settings_key_default_emoji_skin_tone");
    Assert.assertNotNull(preference);
    Assert.assertFalse(preference.isVisible());
    Assert.assertFalse(preference.isEnabled());

    preference = startFragment().findPreference("settings_key_default_emoji_gender");
    Assert.assertNotNull(preference);
    Assert.assertFalse(preference.isVisible());
    Assert.assertFalse(preference.isEnabled());
  }

  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.quickTextSettingsFragment;
  }
}
