package com.anysoftkeyboard.permissions;

import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.Manifest;
import android.os.Build;
import androidx.test.core.app.ActivityScenario;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.TestFragmentActivity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class PermissionRequestHelperTest {

  @Test
  public void testGetRationale() {
    Assert.assertEquals(
        R.string.contacts_permissions_rationale,
        PermissionRequestHelper.getRationale(
            PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE));
    Assert.assertEquals(
        R.string.storage_permission_rationale,
        PermissionRequestHelper.getRationale(
            PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_READ_CODE));
    Assert.assertEquals(
        R.string.storage_permission_rationale,
        PermissionRequestHelper.getRationale(
            PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_WRITE_CODE));
    Assert.assertEquals(
        R.string.notifications_permissions_rationale,
        PermissionRequestHelper.getRationale(
            PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE));
  }

  @Test
  public void testGetPermissionsStringsContacts() {
    Assert.assertArrayEquals(
        new String[] {Manifest.permission.READ_CONTACTS},
        PermissionRequestHelper.getPermissionsStrings(
            PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE));
  }

  @Test
  @Config(sdk = S_V2)
  public void testGetPermissionsStringsNotificationsOldDevice() {
    Assert.assertArrayEquals(
        new String[0],
        PermissionRequestHelper.getPermissionsStrings(
            PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE));
  }

  @Test
  @Config(sdk = TIRAMISU)
  public void testGetPermissionsStringsNotificationsNewDevice() {
    Assert.assertArrayEquals(
        new String[] {Manifest.permission.POST_NOTIFICATIONS},
        PermissionRequestHelper.getPermissionsStrings(
            PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE));
  }

  @Test
  public void testGetPermissionsStringsStorageNewDevice() {
    Assert.assertArrayEquals(
        new String[] {
          Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        },
        PermissionRequestHelper.getPermissionsStrings(
            PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_READ_CODE));
    Assert.assertArrayEquals(
        new String[] {
          Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        },
        PermissionRequestHelper.getPermissionsStrings(
            PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_WRITE_CODE));
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.KITKAT)
  public void testCheckAlreadyHasPermissionsBeforeM() {
    try (var scenario = ActivityScenario.launch(TestFragmentActivity.class)) {
      scenario.onActivity(
          activity -> {
            Assert.assertTrue(
                PermissionRequestHelper.check(
                    activity, PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE));
            Assert.assertTrue(
                PermissionRequestHelper.check(
                    activity, PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_WRITE_CODE));
            Assert.assertTrue(
                PermissionRequestHelper.check(
                    activity, PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_READ_CODE));
            Assert.assertTrue(
                PermissionRequestHelper.check(
                    activity, PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE));
          });
    }
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.KITKAT)
  public void testCheckAlreadyHasPermissionsWithM() {
    var appShadow = Shadows.shadowOf(RuntimeEnvironment.getApplication());
    appShadow.grantPermissions(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_CONTACTS);
    try (var scenario = ActivityScenario.launch(TestFragmentActivity.class)) {
      scenario.onActivity(
          activity -> {
            Assert.assertTrue(
                PermissionRequestHelper.check(
                    activity, PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE));
            Assert.assertTrue(
                PermissionRequestHelper.check(
                    activity, PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_WRITE_CODE));
            Assert.assertTrue(
                PermissionRequestHelper.check(
                    activity, PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_READ_CODE));
            Assert.assertTrue(
                PermissionRequestHelper.check(
                    activity, PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE));
          });
    }
  }
}
