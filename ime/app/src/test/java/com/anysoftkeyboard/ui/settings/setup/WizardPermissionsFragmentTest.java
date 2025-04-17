package com.anysoftkeyboard.ui.settings.setup;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@Config(sdk = Build.VERSION_CODES.M)
public class WizardPermissionsFragmentTest
    extends RobolectricWizardFragmentTestCase<WizardPermissionsFragment> {

  @NonNull
  @Override
  protected WizardPermissionsFragment createFragment() {
    return new WizardPermissionsFragment();
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
  @Ignore("checkPermission returns DENIED for read-contacts")
  public void testWhenNoPermissionsRequestNeeded() {
    WizardPermissionsFragment fragment = startFragment();

    Assert.assertTrue(fragment.isStepCompleted(fragment.requireContext()));

    var contacts = fragment.getView().findViewById(R.id.contacts_permission_group);
    Assert.assertEquals(View.GONE, contacts.getVisibility());

    var notifications = fragment.getView().findViewById(R.id.notification_permission_group);
    Assert.assertEquals(View.GONE, notifications.getVisibility());
  }

  @Test
  public void testKeyboardEnabledAndDefaultButNoPermission() {
    final String flatASKComponent =
        new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName())
            .flattenToString();
    Settings.Secure.putString(
        getApplicationContext().getContentResolver(),
        Settings.Secure.ENABLED_INPUT_METHODS,
        flatASKComponent);
    Settings.Secure.putString(
        getApplicationContext().getContentResolver(),
        Settings.Secure.DEFAULT_INPUT_METHOD,
        flatASKComponent);

    WizardPermissionsFragment fragment = startFragment();
    Assert.assertFalse(fragment.isStepCompleted(getApplicationContext()));

    var contacts = fragment.getView().findViewById(R.id.contacts_permission_group);
    Assert.assertEquals(View.VISIBLE, contacts.getVisibility());

    // no need for this in M
    var notifications = fragment.getView().findViewById(R.id.notification_permission_group);
    Assert.assertEquals(View.GONE, notifications.getVisibility());

    // can handle wiki?
    fragment.getView().findViewById(R.id.open_permissions_wiki_action).performClick();
    Intent wikiIntent =
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
            .getNextStartedActivity();
    Assert.assertEquals(Intent.ACTION_VIEW, wikiIntent.getAction());
    Assert.assertEquals(
        "https://github.com/AnySoftKeyboard/AnySoftKeyboard/wiki/Why-Does-AnySoftKeyboard-Requires-Extra-Permissions",
        wikiIntent.getData().toString());
    // can disable Contacts
    fragment.getView().findViewById(R.id.disable_contacts_dictionary).performClick();
    Assert.assertFalse(
        SharedPrefsHelper.getPrefValue(R.string.settings_key_use_contacts_dictionary, true));

    // disabling contacts
    Assert.assertTrue(fragment.isStepCompleted(getApplicationContext()));
  }

  @Test
  public void testKeyboardEnabledAndDefaultButDictionaryDisabled() {
    final String flatASKComponent =
        new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName())
            .flattenToString();
    Settings.Secure.putString(
        getApplicationContext().getContentResolver(),
        Settings.Secure.ENABLED_INPUT_METHODS,
        flatASKComponent);
    Settings.Secure.putString(
        getApplicationContext().getContentResolver(),
        Settings.Secure.DEFAULT_INPUT_METHOD,
        flatASKComponent);
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_use_contacts_dictionary, false);

    WizardPermissionsFragment fragment = startFragment();
    Assert.assertTrue(fragment.isStepCompleted(getApplicationContext()));

    var contacts = fragment.getView().findViewById(R.id.contacts_permission_group);
    Assert.assertEquals(View.GONE, contacts.getVisibility());
  }

  @Test
  public void testKeyboardEnabledAndDefaultAndHasPermission() {
    final String flatASKComponent =
        new ComponentName(BuildConfig.APPLICATION_ID, SoftKeyboard.class.getName())
            .flattenToString();
    Settings.Secure.putString(
        getApplicationContext().getContentResolver(),
        Settings.Secure.ENABLED_INPUT_METHODS,
        flatASKComponent);
    Settings.Secure.putString(
        getApplicationContext().getContentResolver(),
        Settings.Secure.DEFAULT_INPUT_METHOD,
        flatASKComponent);
    Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
        .grantPermissions(Manifest.permission.READ_CONTACTS);

    WizardPermissionsFragment fragment = startFragment();
    Assert.assertTrue(fragment.isStepCompleted(getApplicationContext()));
    var contacts = fragment.getView().findViewById(R.id.contacts_permission_group);
    Assert.assertEquals(View.GONE, contacts.getVisibility());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.S_V2)
  public void testDoesNotShowNotificationGroupBeforeT() {
    var fragment = startFragment();
    var group = fragment.getView().findViewById(R.id.notification_permission_group);
    Assert.assertEquals(View.GONE, group.getVisibility());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void testDoesNotShowNotificationGroupIfGranted() {
    var appShadow = Shadows.shadowOf(RuntimeEnvironment.getApplication());
    appShadow.grantPermissions(Manifest.permission.POST_NOTIFICATIONS);

    var fragment = startFragment();
    var group = fragment.getView().findViewById(R.id.notification_permission_group);
    Assert.assertEquals(View.GONE, group.getVisibility());
    var contacts = fragment.getView().findViewById(R.id.contacts_permission_group);
    Assert.assertEquals(View.VISIBLE, contacts.getVisibility());
    Assert.assertFalse(fragment.isStepCompleted(getApplicationContext()));

    appShadow.grantPermissions(Manifest.permission.READ_CONTACTS);
    fragment.refreshFragmentUi();

    Assert.assertEquals(View.GONE, contacts.getVisibility());
    Assert.assertTrue(fragment.isStepCompleted(getApplicationContext()));
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void testShowNotificationGroupIfNotGranted() {
    var fragment = startFragment();
    var group = fragment.getView().findViewById(R.id.notification_permission_group);
    Assert.assertEquals(View.VISIBLE, group.getVisibility());

    View linkView = fragment.getView().findViewById(R.id.ask_for_notification_permissions_action);
    var clickHandler = Shadows.shadowOf(linkView).getOnClickListener();
    Assert.assertNotNull(clickHandler);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void testHidesNotificationGroupIfNotGrantedButSkipped() {
    var fragment = startFragment();
    var group = fragment.getView().findViewById(R.id.notification_permission_group);
    Assert.assertEquals(View.VISIBLE, group.getVisibility());

    View skipLink = fragment.getView().findViewById(R.id.skip_notification_permissions_action);
    var clickSkipHandler = Shadows.shadowOf(skipLink).getOnClickListener();
    Assert.assertNotNull(clickSkipHandler);

    clickSkipHandler.onClick(skipLink);

    Assert.assertEquals(View.GONE, group.getVisibility());
  }
}
