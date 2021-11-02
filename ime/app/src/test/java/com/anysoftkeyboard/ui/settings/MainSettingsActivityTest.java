package com.anysoftkeyboard.ui.settings;

import static android.Manifest.permission.LOCATION_HARDWARE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.content.Intent.ACTION_VIEW;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.android.PermissionRequestHelper;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextKeysBrowseFragment;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class MainSettingsActivityTest {

    private static Intent createAppShortcutIntent(String shortcutId) {
        Intent intent =
                new Intent(ACTION_VIEW, null, getApplicationContext(), MainSettingsActivity.class);
        intent.putExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID, shortcutId);

        return intent;
    }

    @NonNull
    private static Intent getContactsIntent() {
        Intent requestIntent =
                new Intent(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class);
        requestIntent.putExtra(
                MainSettingsActivity.EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY, READ_CONTACTS);
        requestIntent.setAction(MainSettingsActivity.ACTION_REQUEST_PERMISSION_ACTIVITY);
        return requestIntent;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownAppShortcut() {
        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(createAppShortcutIntent("unknown_id"));
        activityController.moveToState(Lifecycle.State.RESUMED);
    }

    @Test
    public void testNoAppShortcutExtra() {
        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(MainSettingsActivity.class);
        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(
                activity -> {
                    Fragment fragment =
                            activity.getSupportFragmentManager()
                                    .findFragmentById(R.id.main_ui_content);

                    Assert.assertNotNull(fragment);
                    Assert.assertTrue(fragment instanceof MainFragment);
                });
    }

    @Test
    public void testNoAppShortcutExtraButWithIntent() {
        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(
                        new Intent(getApplicationContext(), MainSettingsActivity.class));
        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(
                activity -> {
                    Fragment fragment =
                            activity.getSupportFragmentManager()
                                    .findFragmentById(R.id.main_ui_content);

                    Assert.assertNotNull(fragment);
                    Assert.assertTrue(fragment instanceof MainFragment);
                });
    }

    @Test
    public void testBottomNavClicks() {
        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(MainSettingsActivity.class);
        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(
                activity -> {
                    BottomNavigationView bottomNav = activity.findViewById(R.id.bottom_navigation);
                    Assert.assertEquals(R.id.bottom_nav_home_button, bottomNav.getSelectedItemId());
                    Assert.assertTrue(
                            activity.getSupportFragmentManager()
                                            .findFragmentById(R.id.main_ui_content)
                                    instanceof MainFragment);

                    bottomNav.setSelectedItemId(R.id.bottom_nav_language_button);
                    TestRxSchedulers.drainAllTasks();
                    Assert.assertTrue(
                            activity.getSupportFragmentManager()
                                            .findFragmentById(R.id.main_ui_content)
                                    instanceof LanguageSettingsFragment);

                    bottomNav.setSelectedItemId(R.id.bottom_nav_ui_button);
                    TestRxSchedulers.drainAllTasks();
                    Assert.assertTrue(
                            activity.getSupportFragmentManager()
                                            .findFragmentById(R.id.main_ui_content)
                                    instanceof UserInterfaceSettingsFragment);

                    bottomNav.setSelectedItemId(R.id.bottom_nav_quick_text_button);
                    TestRxSchedulers.drainAllTasks();
                    Assert.assertTrue(
                            activity.getSupportFragmentManager()
                                            .findFragmentById(R.id.main_ui_content)
                                    instanceof QuickTextKeysBrowseFragment);

                    bottomNav.setSelectedItemId(R.id.bottom_nav_gestures_button);
                    TestRxSchedulers.drainAllTasks();
                    Assert.assertTrue(
                            activity.getSupportFragmentManager()
                                            .findFragmentById(R.id.main_ui_content)
                                    instanceof GesturesSettingsFragment);

                    bottomNav.setSelectedItemId(R.id.bottom_nav_home_button);
                    TestRxSchedulers.drainAllTasks();
                    Assert.assertTrue(
                            activity.getSupportFragmentManager()
                                            .findFragmentById(R.id.main_ui_content)
                                    instanceof MainFragment);
                });
    }

    @Test
    public void testKeyboardsAppShortcutPassed() {
        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(createAppShortcutIntent("keyboards"));
        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(
                activity -> {
                    Fragment fragment =
                            activity.getSupportFragmentManager()
                                    .findFragmentById(R.id.main_ui_content);

                    Assert.assertNotNull(fragment);
                    Assert.assertTrue(fragment instanceof KeyboardAddOnBrowserFragment);
                    BottomNavigationView bottomNav = activity.findViewById(R.id.bottom_navigation);
                    Assert.assertEquals(
                            R.id.bottom_nav_language_button, bottomNav.getSelectedItemId());

                    Assert.assertFalse(
                            activity.getIntent()
                                    .hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
                });
    }

    @Test
    public void testThemesAppShortcutPassed() {
        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(createAppShortcutIntent("themes"));
        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(
                activity -> {
                    Fragment fragment =
                            activity.getSupportFragmentManager()
                                    .findFragmentById(R.id.main_ui_content);

                    Assert.assertNotNull(fragment);
                    Assert.assertTrue(fragment instanceof KeyboardThemeSelectorFragment);
                    BottomNavigationView bottomNav = activity.findViewById(R.id.bottom_navigation);
                    Assert.assertEquals(R.id.bottom_nav_ui_button, bottomNav.getSelectedItemId());

                    Assert.assertFalse(
                            activity.getIntent()
                                    .hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
                });
    }

    @Test
    public void testGesturesAppShortcutPassed() {
        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(createAppShortcutIntent("gestures"));
        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(
                activity -> {
                    Fragment fragment =
                            activity.getSupportFragmentManager()
                                    .findFragmentById(R.id.main_ui_content);

                    Assert.assertNotNull(fragment);
                    Assert.assertTrue(fragment instanceof GesturesSettingsFragment);
                    BottomNavigationView bottomNav = activity.findViewById(R.id.bottom_navigation);
                    Assert.assertEquals(
                            R.id.bottom_nav_gestures_button, bottomNav.getSelectedItemId());

                    Assert.assertFalse(
                            activity.getIntent()
                                    .hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
                });
    }

    @Test
    public void testQuickKeysAppShortcutPassed() {
        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(createAppShortcutIntent("quick_keys"));
        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(
                activity -> {
                    Fragment fragment =
                            activity.getSupportFragmentManager()
                                    .findFragmentById(R.id.main_ui_content);

                    Assert.assertNotNull(fragment);
                    Assert.assertTrue(fragment instanceof QuickTextKeysBrowseFragment);
                    BottomNavigationView bottomNav = activity.findViewById(R.id.bottom_navigation);
                    Assert.assertEquals(
                            R.id.bottom_nav_quick_text_button, bottomNav.getSelectedItemId());

                    Assert.assertFalse(
                            activity.getIntent()
                                    .hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
                });
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenNotGranted() {
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                .denyPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent = getContactsIntent();
        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(requestIntent);
        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(
                activity -> {
                    final ShadowActivity.PermissionsRequest lastRequestedPermission =
                            Shadows.shadowOf(activity).getLastRequestedPermission();
                    Assert.assertNotNull(lastRequestedPermission);
                    Assert.assertEquals(
                            PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE,
                            lastRequestedPermission.requestCode);
                });
    }

    @Test(expected = IllegalArgumentException.class)
    @Config(sdk = Build.VERSION_CODES.M)
    public void testFailsIfUnknownPermission() {
        Intent requestIntent = getContactsIntent();
        requestIntent.putExtra(
                MainSettingsActivity.EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY,
                LOCATION_HARDWARE);
        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(requestIntent);
        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(Assert::assertNotNull);
    }
}
