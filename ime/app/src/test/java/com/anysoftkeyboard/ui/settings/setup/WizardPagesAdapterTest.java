package com.anysoftkeyboard.ui.settings.setup;

import android.os.Build;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class WizardPagesAdapterTest {

  @Rule
  public ActivityScenarioRule<MainSettingsActivity> mRule =
      new ActivityScenarioRule<>(MainSettingsActivity.class);

  @Test
  @Config(sdk = Build.VERSION_CODES.M)
  public void testHasPermissionsPageForAndroidM() {
    mRule
        .getScenario()
        .onActivity(
            activity -> {
              WizardPagesAdapter adapter = new WizardPagesAdapter(activity, false);

              Assert.assertEquals(5, adapter.getItemCount());
              Assert.assertTrue(adapter.createFragment(3) instanceof WizardPermissionsFragment);

              adapter = new WizardPagesAdapter(activity, true);
              Assert.assertEquals(6, adapter.getItemCount());
              Assert.assertTrue(adapter.createFragment(3) instanceof WizardPermissionsFragment);
              Assert.assertTrue(adapter.createFragment(4) instanceof WizardLanguagePackFragment);
            });
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.M)
  public void testNoPermissionsPageBeforeAndroidM() {
    mRule
        .getScenario()
        .onActivity(
            activity -> {
              WizardPagesAdapter adapter = new WizardPagesAdapter(activity, false);

              Assert.assertEquals(4, adapter.getItemCount());
              for (int fragmentIndex = 0; fragmentIndex < adapter.getItemCount(); fragmentIndex++) {
                Assert.assertFalse(
                    adapter.createFragment(fragmentIndex) instanceof WizardPermissionsFragment);
              }
            });
  }
}
