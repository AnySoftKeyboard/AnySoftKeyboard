package com.anysoftkeyboard.ui.settings;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.anysoftkeyboard.PermissionsRequestCodes;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.permissions.PermissionsFragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class MainSettingsActivityTest {


    @Test(expected = IllegalArgumentException.class)
    public void testUnknownAppShortcut() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("unknown_id"));
        activityController.setup();
    }

    @Test
    public void testNoAppShortcutExtra() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class);
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof MainFragment);
    }

    @Test
    public void testNoAppShortcutExtraButWithIntent() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, new Intent(RuntimeEnvironment.application, MainSettingsActivity.class));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof MainFragment);
    }

    @Test
    public void testKeyboardsAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("keyboards"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof KeyboardAddOnBrowserFragment);

        Assert.assertFalse(activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testThemesAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("themes"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof KeyboardThemeSelectorFragment);

        Assert.assertFalse(activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testGesturesAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("gestures"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof GesturesSettingsFragment);

        Assert.assertFalse(activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testQuickKeysAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("quick_keys"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof QuickTextSettingsFragment);

        Assert.assertFalse(activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    public void testUiAdditionalAppShortcutPassed() {
        ActivityController<MainSettingsActivity> activityController = Robolectric.buildActivity(MainSettingsActivity.class, createAppShortcutIntent("ui_tweaks"));
        activityController.setup();

        MainSettingsActivity activity = activityController.get();
        Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);

        Assert.assertNotNull(fragment);
        Assert.assertTrue(fragment instanceof AdditionalUiSettingsFragment);

        Assert.assertFalse(activity.getIntent().hasExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenNotGranted() {
        ShadowApplication.getInstance().denyPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent = PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                RuntimeEnvironment.application, MyMainSettingsActivity.class,
                PermissionsRequestCodes.CONTACTS.getRequestCode(), Manifest.permission.READ_CONTACTS);

        MyMainSettingsActivity.lastCreatedRequest = null;
        ActivityController<MyMainSettingsActivity> activityController = Robolectric.buildActivity(MyMainSettingsActivity.class, requestIntent);
        activityController.setup();

        Assert.assertNotNull(MyMainSettingsActivity.lastCreatedRequest);
        Assert.assertEquals(PermissionsRequestCodes.CONTACTS.getRequestCode(), MyMainSettingsActivity.lastCreatedRequest.getRequestCode());
        Assert.assertArrayEquals(new String[] {Manifest.permission.READ_CONTACTS}, MyMainSettingsActivity.lastCreatedRequest.getRequestedPermissions());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenNotGrantedAndUserGrants() {
        ShadowApplication.getInstance().denyPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent = PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                RuntimeEnvironment.application, MyMainSettingsActivity.class,
                PermissionsRequestCodes.CONTACTS.getRequestCode(), Manifest.permission.READ_CONTACTS);

        MyMainSettingsActivity.lastCreatedRequest = null;
        ActivityController<MyMainSettingsActivity> activityController = Robolectric.buildActivity(MyMainSettingsActivity.class, requestIntent);
        activityController.setup();

        PermissionsRequest lastCreatedRequest = MyMainSettingsActivity.lastCreatedRequest;
        Assert.assertNotNull(lastCreatedRequest);

        MyMainSettingsActivity.lastCreatedRequest = null;
        lastCreatedRequest.onPermissionsGranted();
        Assert.assertNull(MyMainSettingsActivity.lastCreatedRequest);
        Assert.assertNull(ShadowApplication.getInstance().getLatestDialog());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenNotGrantedAndUserDenies() {
        ShadowApplication.getInstance().denyPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent = PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                RuntimeEnvironment.application, MyMainSettingsActivity.class,
                PermissionsRequestCodes.CONTACTS.getRequestCode(), Manifest.permission.READ_CONTACTS);

        MyMainSettingsActivity.lastCreatedRequest = null;
        ActivityController<MyMainSettingsActivity> activityController = Robolectric.buildActivity(MyMainSettingsActivity.class, requestIntent);
        activityController.setup();

        PermissionsRequest lastCreatedRequest = MyMainSettingsActivity.lastCreatedRequest;
        Assert.assertNotNull(lastCreatedRequest);

        MyMainSettingsActivity.lastCreatedRequest = null;
        lastCreatedRequest.onPermissionsDenied(new String[0], new String[] {Manifest.permission.READ_CONTACTS}, new String[0]);
        Assert.assertNull(MyMainSettingsActivity.lastCreatedRequest);

        Assert.assertNotNull(ShadowApplication.getInstance().getLatestDialog());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.M)
    public void testContactsPermissionRequestedWhenGrantedBefore() {
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.READ_CONTACTS);

        Intent requestIntent = PermissionsFragmentChauffeurActivity.createIntentToPermissionsRequest(
                RuntimeEnvironment.application, MyMainSettingsActivity.class,
                PermissionsRequestCodes.CONTACTS.getRequestCode(), Manifest.permission.READ_CONTACTS);

        MyMainSettingsActivity.lastCreatedRequest = null;
        ActivityController<MyMainSettingsActivity> activityController = Robolectric.buildActivity(MyMainSettingsActivity.class, requestIntent);
        activityController.setup();

        Assert.assertNull(MyMainSettingsActivity.lastCreatedRequest);
    }

    private static Intent createAppShortcutIntent(String shortcutId) {
        Intent intent = new Intent(Intent.ACTION_VIEW, null, RuntimeEnvironment.application, MainSettingsActivity.class);
        intent.putExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID, shortcutId);

        return intent;
    }

    public static class MyMainSettingsActivity extends MainSettingsActivity {
        public static PermissionsRequest lastCreatedRequest;
        @NonNull
        @Override
        protected PermissionsRequest createPermissionRequestFromIntentRequest(int requestId, @NonNull String[] permissions, @NonNull Intent intent) {
            return lastCreatedRequest = super.createPermissionRequestFromIntentRequest(requestId, permissions, intent);
        }
    }
}