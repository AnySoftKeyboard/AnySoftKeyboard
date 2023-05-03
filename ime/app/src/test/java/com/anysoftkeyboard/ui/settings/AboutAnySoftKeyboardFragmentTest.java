package com.anysoftkeyboard.ui.settings;

import android.app.Application;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Shadows;

public class AboutAnySoftKeyboardFragmentTest
    extends RobolectricFragmentTestCase<AboutAnySoftKeyboardFragment> {

  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.aboutAnySoftKeyboardFragment;
  }

  @Test
  public void testWebSiteClick() {
    AboutAnySoftKeyboardFragment fragment = startFragment();
    TextView link = fragment.getView().findViewById(R.id.about_web_site_link);
    Assert.assertNotNull(link);

    Shadows.shadowOf(link).checkedPerformClick();

    Intent intent =
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
            .getNextStartedActivity();

    Assert.assertNotNull(intent);
    Assert.assertEquals(Intent.ACTION_VIEW, intent.getAction());
    Assert.assertEquals("https://anysoftkeyboard.github.io/", intent.getData().toString());
  }

  @Test
  public void testShareApp() {
    AboutAnySoftKeyboardFragment fragment = startFragment();
    View icon = fragment.getView().findViewById(R.id.share_app_details);
    Assert.assertNotNull(icon);

    Shadows.shadowOf(icon).checkedPerformClick();

    Intent intent =
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
            .getNextStartedActivity();

    Assert.assertNotNull(intent);
    Assert.assertEquals(Intent.ACTION_CHOOSER, intent.getAction());

    Intent sharingIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
    Assert.assertNotNull(sharingIntent);
  }

  @Test
  public void testRateApp() {
    AboutAnySoftKeyboardFragment fragment = startFragment();
    View icon = fragment.getView().findViewById(R.id.rate_app_in_store);
    Assert.assertNotNull(icon);

    Shadows.shadowOf(icon).checkedPerformClick();

    Intent intent =
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
            .getNextStartedActivity();

    Assert.assertNotNull(intent);
    Assert.assertEquals(Intent.ACTION_VIEW, intent.getAction());
    Assert.assertEquals(
        "http://play.google.com/store/apps/details?id=com.menny.android.anysoftkeyboard",
        intent.getData().toString());
  }

  @Test
  public void testPrivacyPolicyClick() {
    AboutAnySoftKeyboardFragment fragment = startFragment();
    TextView link = fragment.getView().findViewById(R.id.about_privacy_link);
    Assert.assertNotNull(link);

    link.performClick();

    Intent intent =
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
            .getNextStartedActivity();

    Assert.assertNotNull(intent);
    Assert.assertEquals(Intent.ACTION_VIEW, intent.getAction());
    Assert.assertEquals(
        "http://anysoftkeyboard.github.io/privacy-policy/", intent.getData().toString());
  }

  @Test
  public void testAdditionalLicenses() {
    AboutAnySoftKeyboardFragment fragment = startFragment();
    TextView link = fragment.getView().findViewById(R.id.about_legal_stuff_link);
    Assert.assertNotNull(link);

    Shadows.shadowOf(link).getOnClickListener().onClick(link);

    ensureAllScheduledJobsAreDone();

    Fragment nextFragment = getCurrentFragment();

    Assert.assertNotNull(nextFragment);
    Assert.assertTrue(
        nextFragment instanceof AboutAnySoftKeyboardFragment.AdditionalSoftwareLicensesFragment);
  }

  @Test
  public void testVersionInfo() {
    AboutAnySoftKeyboardFragment fragment = startFragment();
    TextView copyright = fragment.getView().findViewById(R.id.about_copyright);
    Assert.assertTrue(copyright.getText().toString().contains("Menny"));
    Assert.assertTrue(copyright.getText().toString().contains("©"));
    Assert.assertTrue(
        copyright
            .getText()
            .toString()
            .contains(Integer.toString(new GregorianCalendar().get(Calendar.YEAR))));

    TextView version = fragment.getView().findViewById(R.id.about_app_version);
    Assert.assertTrue(version.getText().toString().contains(BuildConfig.VERSION_NAME));
    Assert.assertTrue(
        version.getText().toString().contains(Integer.toString(BuildConfig.VERSION_CODE)));
  }
}
