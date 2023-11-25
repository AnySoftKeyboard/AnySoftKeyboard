package com.anysoftkeyboard.android;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import com.menny.android.anysoftkeyboard.R;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public abstract class PermissionRequestHelper {
  public static final int STORAGE_PERMISSION_REQUEST_READ_CODE = 892342;
  public static final int STORAGE_PERMISSION_REQUEST_WRITE_CODE = 892343;
  public static final int CONTACTS_PERMISSION_REQUEST_CODE = 892344;

  public static boolean check(@NonNull Fragment fragment, int requestCode) {
    final String[] permissions = getPermissionsStrings(requestCode);
    if (EasyPermissions.hasPermissions(fragment.requireContext(), permissions)) {
      return true;
    } else {
      // Do not have permissions, request them now
      EasyPermissions.requestPermissions(
          new PermissionRequest.Builder(fragment, requestCode, permissions)
              .setRationale(getRationale(requestCode))
              .setPositiveButtonText(R.string.allow_permission)
              .setTheme(R.style.Theme_AppCompat_Dialog_Alert)
              .build());
      return false;
    }
  }

  public static boolean check(@NonNull Activity activity, int requestCode) {
    final String[] permissions = getPermissionsStrings(requestCode);
    if (EasyPermissions.hasPermissions(activity, permissions)) {
      return true;
    } else {
      // Do not have permissions, request them now
      EasyPermissions.requestPermissions(
          new PermissionRequest.Builder(activity, requestCode, permissions)
              .setRationale(getRationale(requestCode))
              .setPositiveButtonText(R.string.allow_permission)
              .setTheme(R.style.Theme_AppCompat_Dialog_Alert)
              .build());
      return false;
    }
  }

  @StringRes
  private static int getRationale(int requestCode) {
    switch (requestCode) {
      case CONTACTS_PERMISSION_REQUEST_CODE:
        return R.string.contacts_permissions_dialog_message;
      case STORAGE_PERMISSION_REQUEST_READ_CODE:
      case STORAGE_PERMISSION_REQUEST_WRITE_CODE:
        return R.string.storage_permission_rationale;
      default:
        throw new IllegalArgumentException("Unknown request code " + requestCode);
    }
  }

  @NonNull private static String[] getPermissionsStrings(int requestCode) {
    if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
      return new String[] {Manifest.permission.READ_CONTACTS};
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        return new String[] {
          Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        };
      } else {
        return new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
      }
    }
  }

  public static void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults, Object receiver) {
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, receiver);
  }
}
