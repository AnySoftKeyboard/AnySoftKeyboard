package com.anysoftkeyboard.ui.settings;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.Intent.ACTION_VIEW;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.PermissionsRequestCodes.CONTACTS;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextKeysBrowseFragment;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.chauffeur.lib.permissions.PermissionsFragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class MainSettingsActivityTest {

    private static Intent createAppShortcutIntent(String shortcutId) {
        Intent intent =
                new Intent(ACTION_VIEW, null, getApplicationContext(), MainSettingsActivity.class);
        intent.putExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID, shortcutId);

        return intent;
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

        Intent requestIntent =
                PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                        getApplicationContext(),
                        MainSettingsActivity.class,
                        CONTACTS.getRequestCode(),
                        READ_CONTACTS);

        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(requestIntent);
        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(
                activity -> {
                    final PermissionsRequest lastCreatedRequest = activity.getLastCreatedRequest();
                    Assert.assertNotNull(lastCreatedRequest);
                    Assert.assertEquals(
                            PermissionsRequestCodes.CONTACTS.getRequestCode(),
                            lastCreatedRequest.getRequestCode());
                    Assert.assertArrayEquals(
                            new String[] {Manifest.permission.READ_CONTACTS},
                            lastCreatedRequest.getRequestedPermissions());
                });
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenNotGrantedAndUserGrants() {
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                .denyPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent =
                PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                        getApplicationContext(),
                        MainSettingsActivity.class,
                        CONTACTS.getRequestCode(),
                        READ_CONTACTS);

        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(requestIntent);
        activityController.moveToState(Lifecycle.State.RESUMED);
        activityController.onActivity(
                activity -> {
                    PermissionsRequest lastCreatedRequest = activity.getLastCreatedRequest();
                    Assert.assertNotNull(lastCreatedRequest);

                    lastCreatedRequest.onPermissionsGranted();
                    Assert.assertNull(activity.getLastCreatedRequest());
                    Assert.assertNull(ShadowDialog.getLatestDialog());
                });
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenNotGrantedAndUserDenies() {
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                .denyPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent =
                PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                        getApplicationContext(),
                        MainSettingsActivity.class,
                        CONTACTS.getRequestCode(),
                        READ_CONTACTS);

        ActivityScenario<MainSettingsActivity> activityController =
                ActivityScenario.launch(requestIntent);

        activityController.moveToState(Lifecycle.State.RESUMED);

        activityController.onActivity(
                activity -> {
                    PermissionsRequest lastCreatedRequest = activity.getLastCreatedRequest();
                    Assert.assertNotNull(lastCreatedRequest);

                    lastCreatedRequest.onPermissionsDenied(
                            new String[0],
                            new String[] {Manifest.permission.READ_CONTACTS},
                            new String[0]);
                    Assert.assertNull(activity.getLastCreatedRequest());

                    Assert.assertNotNull(ShadowDialog.getLatestDialog());
                });
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenGrantedBefore() {
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                .grantPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent =
                PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                        getApplicationContext(),
                        MainSettingsActivity.class,
                        CONTACTS.getRequestCode(),
                        READ_CONTACTS);

        Assert.assertNull(requestIntent);
    }
}
