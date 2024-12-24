package com.anysoftkeyboard.ui.settings.setup;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;
import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowContentResolver;

@SuppressWarnings("cast")
public class WizardPageEnableKeyboardFragmentTest
    extends RobolectricWizardFragmentTestCase<WizardPageEnableKeyboardFragment> {

  @NonNull
  @Override
  protected WizardPageEnableKeyboardFragment createFragment() {
    return new WizardPageEnableKeyboardFragment();
  }

  @Test
  public void testKeyboardNotEnabled() {
    WizardPageEnableKeyboardFragment fragment = startFragment();
    Assert.assertFalse(fragment.isStepCompleted(getApplicationContext()));

    ImageView stateIcon = fragment.getView().findViewById(R.id.step_state_icon);
    Assert.assertNotNull(stateIcon);

    Assert.assertEquals(
        R.drawable.ic_wizard_enabled_off,
        Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
    Assert.assertTrue(stateIcon.isClickable());

    View.OnClickListener stateIconClickHandler = Shadows.shadowOf(stateIcon).getOnClickListener();
    View.OnClickListener linkClickHandler =
        Shadows.shadowOf(
                (View) fragment.getView().findViewById(R.id.go_to_language_settings_action))
            .getOnClickListener();

    Assert.assertNotNull(stateIconClickHandler);
    Assert.assertSame(stateIconClickHandler, linkClickHandler);
  }

  @Test
  public void testClickToEnableReachesSettings() {
    WizardPageEnableKeyboardFragment fragment = startFragment();
    Assert.assertFalse(fragment.isStepCompleted(getApplicationContext()));

    final View linkToClick = fragment.getView().findViewById(R.id.go_to_language_settings_action);
    View.OnClickListener linkClickHandler = Shadows.shadowOf(linkToClick).getOnClickListener();

    Assert.assertNotNull(linkClickHandler);

    linkClickHandler.onClick(linkToClick);

    final Intent nextStartedActivity =
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
            .getNextStartedActivity();

    Assert.assertEquals(Settings.ACTION_INPUT_METHOD_SETTINGS, nextStartedActivity.getAction());
  }

  @Test
  public void testClickedSkipped() {
    var fragment = startFragment();

    final View link = fragment.getView().findViewById(R.id.skip_setup_wizard);
    var linkClickHandler = Shadows.shadowOf(link).getOnClickListener();

    Assert.assertNotNull(linkClickHandler);

    linkClickHandler.onClick(link);

    final Intent nextStartedActivity =
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
            .getNextStartedActivity();

    Assert.assertEquals(
        MainSettingsActivity.class.getName(), nextStartedActivity.getComponent().getClassName());
  }

  @Test
  public void testKeyboardEnabled() {
    final String flatASKComponent =
        new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName())
            .flattenToString();
    Settings.Secure.putString(
        getApplicationContext().getContentResolver(),
        Settings.Secure.ENABLED_INPUT_METHODS,
        flatASKComponent);

    WizardPageEnableKeyboardFragment fragment = startFragment();
    Assert.assertTrue(fragment.isStepCompleted(getApplicationContext()));

    ImageView stateIcon = fragment.getView().findViewById(R.id.step_state_icon);
    Assert.assertNotNull(stateIcon);

    Assert.assertEquals(
        R.drawable.ic_wizard_enabled_on,
        Shadows.shadowOf(stateIcon.getDrawable()).getCreatedFromResId());
    Assert.assertFalse(stateIcon.isClickable());
  }

  @Test
  public void testSettingsObserverRemovedOnDestroy() {
    final ShadowContentResolver shadowContentResolver =
        Shadows.shadowOf(getApplicationContext().getContentResolver());
    WizardPageEnableKeyboardFragment fragment = startFragment();
    Assert.assertEquals(
        1, shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI).size());

    // now moving to the settings app
    final View linkToClick = fragment.getView().findViewById(R.id.go_to_language_settings_action);
    View.OnClickListener linkClickHandler = Shadows.shadowOf(linkToClick).getOnClickListener();

    linkClickHandler.onClick(linkToClick);
    TestRxSchedulers.drainAllTasks();
    getActivityController().pause();
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(
        2, shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI).size());
    getActivityController().destroy();
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(
        0, shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI).size());
  }

  @Test
  public void testSettingsObserverRemovedOnReallyLongWait() {
    final ShadowContentResolver shadowContentResolver =
        Shadows.shadowOf(getApplicationContext().getContentResolver());
    WizardPageEnableKeyboardFragment fragment = startFragment();
    Assert.assertEquals(
        1, shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI).size());

    // now moving to the settings app
    final View linkToClick = fragment.getView().findViewById(R.id.go_to_language_settings_action);
    View.OnClickListener linkClickHandler = Shadows.shadowOf(linkToClick).getOnClickListener();

    linkClickHandler.onClick(linkToClick);
    TestRxSchedulers.drainAllTasks();
    getActivityController().pause();
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(
        2, shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI).size());
    TestRxSchedulers.drainAllTasks();
    TestRxSchedulers.foregroundAdvanceBy(100 * 1000);
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(
        1, shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI).size());
  }

  @Test
  public void testSettingsObserverRemovedOnRestart() {
    final ShadowContentResolver shadowContentResolver =
        Shadows.shadowOf(getApplicationContext().getContentResolver());
    WizardPageEnableKeyboardFragment fragment = startFragment();
    Assert.assertEquals(
        1, shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI).size());

    // now moving to the settings app
    final View linkToClick = fragment.getView().findViewById(R.id.go_to_language_settings_action);
    View.OnClickListener linkClickHandler = Shadows.shadowOf(linkToClick).getOnClickListener();

    linkClickHandler.onClick(linkToClick);
    TestRxSchedulers.drainAllTasks();
    getActivityController().pause().stop();
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(
        2, shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI).size());
    getActivityController().restart().start().resume();
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(
        1, shadowContentResolver.getContentObservers(Settings.Secure.CONTENT_URI).size());
  }

  @Test
  public void testSettingsObserverReturnsToActivityOnTrigger() {
    final ShadowContentResolver shadowContentResolver =
        Shadows.shadowOf(getApplicationContext().getContentResolver());
    WizardPageEnableKeyboardFragment fragment = startFragment();

    // now moving to the settings app
    final View linkToClick = fragment.getView().findViewById(R.id.go_to_language_settings_action);
    View.OnClickListener linkClickHandler = Shadows.shadowOf(linkToClick).getOnClickListener();

    linkClickHandler.onClick(linkToClick);
    TestRxSchedulers.drainAllTasks();
    getActivityController().pause().stop();
    TestRxSchedulers.drainAllTasks();

    final ShadowApplication shadowApplication =
        Shadows.shadowOf((Application) getApplicationContext());
    shadowApplication.clearNextStartedActivities();

    // enabling this IME
    final String flatASKComponent =
        new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName())
            .flattenToString();
    Settings.Secure.putString(
        getApplicationContext().getContentResolver(),
        Settings.Secure.ENABLED_INPUT_METHODS,
        flatASKComponent);

    shadowContentResolver
        .getContentObservers(Settings.Secure.CONTENT_URI)
        .forEach(v -> v.onChange(false));
    TestRxSchedulers.drainAllTasks();
    TestRxSchedulers.foregroundAdvanceBy(100);
    TestRxSchedulers.drainAllTasks();

    final Intent restartActivityIntent = shadowApplication.getNextStartedActivity();
    Assert.assertNotNull(restartActivityIntent);
    Assert.assertEquals(
        SetupWizardActivity.class.getName(), restartActivityIntent.getComponent().getClassName());
    Assert.assertEquals(
        getApplicationContext().getPackageName(),
        restartActivityIntent.getComponent().getPackageName());
  }

  @Test
  public void testSettingsObserverDoesNotReturnToActivityOnTriggerIfNotEnabled() {
    final ShadowContentResolver shadowContentResolver =
        Shadows.shadowOf(getApplicationContext().getContentResolver());
    WizardPageEnableKeyboardFragment fragment = startFragment();

    // now moving to the settings app
    final View linkToClick = fragment.getView().findViewById(R.id.go_to_language_settings_action);
    View.OnClickListener linkClickHandler = Shadows.shadowOf(linkToClick).getOnClickListener();

    linkClickHandler.onClick(linkToClick);
    TestRxSchedulers.drainAllTasks();
    getActivityController().pause().stop();
    TestRxSchedulers.drainAllTasks();

    final ShadowApplication shadowApplication =
        Shadows.shadowOf((Application) getApplicationContext());
    shadowApplication.clearNextStartedActivities();

    shadowContentResolver
        .getContentObservers(Settings.Secure.CONTENT_URI)
        .forEach(v -> v.onChange(false));
    TestRxSchedulers.drainAllTasks();
    TestRxSchedulers.foregroundAdvanceBy(100);
    TestRxSchedulers.drainAllTasks();

    Assert.assertNull(shadowApplication.getNextStartedActivity());
  }
}
