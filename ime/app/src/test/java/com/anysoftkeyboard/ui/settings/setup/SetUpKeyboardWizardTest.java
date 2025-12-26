package com.anysoftkeyboard.ui.settings.setup;

import android.annotation.TargetApi;
import android.database.ContentObserver;
import android.os.Build;
import android.provider.Settings;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.InputMethodManagerShadow;
import com.menny.android.anysoftkeyboard.R;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SetUpKeyboardWizardTest {

  @Rule
  public ActivityScenarioRule<SetupWizardActivity> mActivityScenarioRule =
      new ActivityScenarioRule<>(SetupWizardActivity.class);

  @Before
  public void setup() {
    InputMethodManagerShadow.setKeyboardEnabled(RuntimeEnvironment.getApplication(), false);
  }

  @TargetApi(Build.VERSION_CODES.M)
  @Test
  @Config(sdk = Build.VERSION_CODES.M)
  public void testHasPermissionsPage() {
    mActivityScenarioRule.getScenario().moveToState(Lifecycle.State.STARTED);
    mActivityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              // ensuring we registered for Secure settings observing
              final Collection<ContentObserver> contentObservers =
                  Shadows.shadowOf(activity.getContentResolver())
                      .getContentObservers(Settings.Secure.CONTENT_URI);
              Assert.assertEquals(1, contentObservers.size());

              final ViewPager2 pager = activity.findViewById(R.id.wizard_pages_pager);
              Assert.assertNotNull(pager);
              Assert.assertEquals(5, pager.getAdapter().getItemCount());
              Assert.assertTrue(
                  ((FragmentStateAdapter) pager.getAdapter()).createFragment(3)
                      instanceof WizardPermissionsFragment);
            });
  }
}
