package com.anysoftkeyboard.ui.settings;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.Intent.ACTION_VIEW;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.PermissionsRequestCodes.CONTACTS;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextKeysBrowseFragment;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.chauffeur.lib.permissions.PermissionsFragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

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
        ActivityController<MainSettingsActivity> activityController =
                Robolectric.buildActivity(
                        MainSettingsActivity.class, createAppShortcutIntent("unknown_id"));
        activityController.setup();
    }

    @Test
    public void testNoAppShortcutExtra() {
        ActivityController<MainSettingsActivity> activityController =
                Robolectric.buildActivity(MainSettingsActivity.class);
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment =
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof MainFragment);
    }

    @Test
    public void testNoAppShortcutExtraButWithIntent() {
        ActivityController<MainSettingsActivity> activityController =
                Robolectric.buildActivity(
                        MainSettingsActivity.class,
                        new Intent(getApplicationContext(), MainSettingsActivity.class));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment =
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof MainFragment);
    }

    @Test
    public void testBottomNavClicks() {
        ActivityController<MainSettingsActivity> activityController =
                Robolectric.buildActivity(MainSettingsActivity.class);
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        BottomNavigationView bottomNav =
                (BottomNavigationView) activity.findViewById(R.id.bottom_navigation);
        Assert.assertEquals(R.id.bottom_nav_home_button, bottomNav.getSelectedItemId());
        Assert.assertTrue(
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content)
                        instanceof MainFragment);

        bottomNav.setSelectedItemId(R.id.bottom_nav_language_button);
        Assert.assertTrue(
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content)
                        instanceof LanguageSettingsFragment);

        bottomNav.setSelectedItemId(R.id.bottom_nav_ui_button);
        Assert.assertTrue(
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content)
                        instanceof UserInterfaceSettingsFragment);

        bottomNav.setSelectedItemId(R.id.bottom_nav_quick_text_button);
        Assert.assertTrue(
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content)
                        instanceof QuickTextKeysBrowseFragment);

        /*bottomNav.setSelectedItemId(R.id.bottom_nav_gestures_button);
        Assert.assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content) instanceof GesturesSettingsFragment);*/

        bottomNav.setSelectedItemId(R.id.bottom_nav_home_button);
        Assert.assertTrue(
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content)
                        instanceof MainFragment);
    }

    @Test
    public void testKeyboardsAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController =
                Robolectric.buildActivity(
                        MainSettingsActivity.class, createAppShortcutIntent("keyboards"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment =
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof KeyboardAddOnBrowserFragment);
        BottomNavigationView bottomNav =
                (BottomNavigationView) activity.findViewById(R.id.bottom_navigation);
        Assert.assertEquals(R.id.bottom_nav_language_button, bottomNav.getSelectedItemId());

        Assert.assertFalse(
                activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testThemesAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController =
                Robolectric.buildActivity(
                        MainSettingsActivity.class, createAppShortcutIntent("themes"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment =
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof KeyboardThemeSelectorFragment);
        BottomNavigationView bottomNav =
                (BottomNavigationView) activity.findViewById(R.id.bottom_navigation);
        Assert.assertEquals(R.id.bottom_nav_ui_button, bottomNav.getSelectedItemId());

        Assert.assertFalse(
                activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testGesturesAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController =
                Robolectric.buildActivity(
                        MainSettingsActivity.class, createAppShortcutIntent("gestures"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment =
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof GesturesSettingsFragment);
        BottomNavigationView bottomNav =
                (BottomNavigationView) activity.findViewById(R.id.bottom_navigation);
        Assert.assertEquals(R.id.bottom_nav_gestures_button, bottomNav.getSelectedItemId());

        Assert.assertFalse(
                activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testQuickKeysAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController =
                Robolectric.buildActivity(
                        MainSettingsActivity.class, createAppShortcutIntent("quick_keys"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment =
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof QuickTextKeysBrowseFragment);
        BottomNavigationView bottomNav =
                (BottomNavigationView) activity.findViewById(R.id.bottom_navigation);
        Assert.assertEquals(R.id.bottom_nav_quick_text_button, bottomNav.getSelectedItemId());

        Assert.assertFalse(
                activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenNotGranted() {
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                .denyPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent =
                PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                        getApplicationContext(),
                        MyMainSettingsActivity.class,
                        CONTACTS.getRequestCode(),
                        READ_CONTACTS);

        MyMainSettingsActivity.lastCreatedRequest = null;
        ActivityController<MyMainSettingsActivity> activityController =
                Robolectric.buildActivity(MyMainSettingsActivity.class, requestIntent);
        activityController.setup();

        Assert.assertNotNull(MyMainSettingsActivity.lastCreatedRequest);
        Assert.assertEquals(
                PermissionsRequestCodes.CONTACTS.getRequestCode(),
                MyMainSettingsActivity.lastCreatedRequest.getRequestCode());
        Assert.assertArrayEquals(
                new String[] {Manifest.permission.READ_CONTACTS},
                MyMainSettingsActivity.lastCreatedRequest.getRequestedPermissions());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenNotGrantedAndUserGrants() {
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                .denyPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent =
                PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                        getApplicationContext(),
                        MyMainSettingsActivity.class,
                        CONTACTS.getRequestCode(),
                        READ_CONTACTS);

        MyMainSettingsActivity.lastCreatedRequest = null;
        ActivityController<MyMainSettingsActivity> activityController =
                Robolectric.buildActivity(MyMainSettingsActivity.class, requestIntent);
        activityController.setup();

        PermissionsRequest lastCreatedRequest = MyMainSettingsActivity.lastCreatedRequest;
        Assert.assertNotNull(lastCreatedRequest);

        MyMainSettingsActivity.lastCreatedRequest = null;
        lastCreatedRequest.onPermissionsGranted();
        Assert.assertNull(MyMainSettingsActivity.lastCreatedRequest);
        Assert.assertNull(
                Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                        .getLatestDialog());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenNotGrantedAndUserDenies() {
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                .denyPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent =
                PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                        getApplicationContext(),
                        MyMainSettingsActivity.class,
                        CONTACTS.getRequestCode(),
                        READ_CONTACTS);

        MyMainSettingsActivity.lastCreatedRequest = null;
        ActivityController<MyMainSettingsActivity> activityController =
                Robolectric.buildActivity(MyMainSettingsActivity.class, requestIntent);
        activityController.setup();

        PermissionsRequest lastCreatedRequest = MyMainSettingsActivity.lastCreatedRequest;
        Assert.assertNotNull(lastCreatedRequest);

        MyMainSettingsActivity.lastCreatedRequest = null;
        lastCreatedRequest.onPermissionsDenied(
                new String[0], new String[] {Manifest.permission.READ_CONTACTS}, new String[0]);
        Assert.assertNull(MyMainSettingsActivity.lastCreatedRequest);

        Assert.assertNotNull(
                Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                        .getLatestDialog());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenGrantedBefore() {
        Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                .grantPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent =
                PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                        getApplicationContext(),
                        MyMainSettingsActivity.class,
                        CONTACTS.getRequestCode(),
                        READ_CONTACTS);

        MyMainSettingsActivity.lastCreatedRequest = null;
        ActivityController<MyMainSettingsActivity> activityController =
                Robolectric.buildActivity(MyMainSettingsActivity.class, requestIntent);
        activityController.setup();

        Assert.assertNull(MyMainSettingsActivity.lastCreatedRequest);
    }

    public static class MyMainSettingsActivity extends MainSettingsActivity {
        public static PermissionsRequest lastCreatedRequest;

        @NonNull
        @Override
        protected PermissionsRequest createPermissionRequestFromIntentRequest(
                int requestId, @NonNull String[] permissions, @NonNull Intent intent) {
            return lastCreatedRequest =
                    super.createPermissionRequestFromIntentRequest(requestId, permissions, intent);
        }
    }
}
