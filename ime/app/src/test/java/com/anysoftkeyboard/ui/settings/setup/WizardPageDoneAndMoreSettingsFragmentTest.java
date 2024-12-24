package com.anysoftkeyboard.ui.settings.setup;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

public class WizardPageDoneAndMoreSettingsFragmentTest
    extends RobolectricWizardFragmentTestCase<WizardPageDoneAndMoreSettingsFragment> {

  @NonNull
  @Override
  protected WizardPageDoneAndMoreSettingsFragment createFragment() {
    return new WizardPageDoneAndMoreSettingsFragment();
  }

  @Test
  public void testIsStepCompletedAlwaysFalse() {
    Assert.assertFalse(
        startFragment().isStepCompleted(ApplicationProvider.getApplicationContext()));
  }

  @Test
  public void testGoToLanguagesOnClick() {
    final WizardPageDoneAndMoreSettingsFragment fragment = startFragment();
    final ShadowApplication shadowApplication =
        Shadows.shadowOf((Application) getApplicationContext());
    shadowApplication.clearNextStartedActivities();

    final View clickView = fragment.getView().findViewById(R.id.go_to_languages_action);
    View.OnClickListener clickHandler = Shadows.shadowOf(clickView).getOnClickListener();
    clickHandler.onClick(clickView);

    final Intent startIntent = shadowApplication.getNextStartedActivity();
    Assert.assertNotNull(startIntent);
    final Intent expected =
        new Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getApplicationContext().getString(R.string.deeplink_url_keyboards)),
            getApplicationContext(),
            MainSettingsActivity.class);
    assertChauffeurIntent(expected, startIntent);
  }

  @Test
  public void testGoToThemesOnClick() {
    final WizardPageDoneAndMoreSettingsFragment fragment = startFragment();
    final ShadowApplication shadowApplication =
        Shadows.shadowOf((Application) getApplicationContext());
    shadowApplication.clearNextStartedActivities();

    final View clickView = fragment.getView().findViewById(R.id.go_to_theme_action);
    View.OnClickListener clickHandler = Shadows.shadowOf(clickView).getOnClickListener();
    clickHandler.onClick(clickView);

    final Intent startIntent = shadowApplication.getNextStartedActivity();
    Assert.assertNotNull(startIntent);
    final Intent expected =
        new Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getApplicationContext().getString(R.string.deeplink_url_themes)),
            getApplicationContext(),
            MainSettingsActivity.class);
    Assert.assertNotNull(startIntent);
    assertChauffeurIntent(expected, startIntent);
  }

  @Test
  public void testGoToAllSettingsOnClick() {
    final WizardPageDoneAndMoreSettingsFragment fragment = startFragment();
    final ShadowApplication shadowApplication =
        Shadows.shadowOf((Application) getApplicationContext());
    shadowApplication.clearNextStartedActivities();

    final View clickView = fragment.getView().findViewById(R.id.go_to_all_settings_action);
    View.OnClickListener clickHandler = Shadows.shadowOf(clickView).getOnClickListener();
    clickHandler.onClick(clickView);

    final Intent startIntent = shadowApplication.getNextStartedActivity();
    Assert.assertNotNull(startIntent);
    assertChauffeurIntent(
        new Intent(fragment.requireContext(), MainSettingsActivity.class), startIntent);
  }

  private static void assertChauffeurIntent(@NonNull Intent expected, @NonNull Intent actual) {
    Assert.assertEquals(expected.getComponent(), actual.getComponent());
    Assert.assertEquals(expected.getAction(), actual.getAction());
    Assert.assertTrue(
        (expected.getExtras() == null && actual.getExtras() == null)
            || (expected.getExtras() != null && actual.getExtras() != null));
    if (expected.getExtras() != null) {
      Assert.assertEquals(expected.getExtras().toString(), actual.getExtras().toString());
    }
  }
}
