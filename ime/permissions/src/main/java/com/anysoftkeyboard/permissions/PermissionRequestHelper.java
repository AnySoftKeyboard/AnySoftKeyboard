package com.anysoftkeyboard.permissions;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public abstract class PermissionRequestHelper {
  public static final int STORAGE_PERMISSION_REQUEST_READ_CODE = 892342;
  public static final int STORAGE_PERMISSION_REQUEST_WRITE_CODE = 892343;
  public static final int CONTACTS_PERMISSION_REQUEST_CODE = 892344;
  public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 892345;

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
  @VisibleForTesting
  static int getRationale(int requestCode) {
    return switch (requestCode) {
      case CONTACTS_PERMISSION_REQUEST_CODE -> R.string.contacts_permissions_rationale;
      case NOTIFICATION_PERMISSION_REQUEST_CODE -> R.string.notifications_permissions_rationale;
      case STORAGE_PERMISSION_REQUEST_READ_CODE, STORAGE_PERMISSION_REQUEST_WRITE_CODE -> R.string
          .storage_permission_rationale;
      default -> throw new IllegalArgumentException("Unknown request code " + requestCode);
    };
  }

  @NonNull @VisibleForTesting
  static String[] getPermissionsStrings(int requestCode) {
    switch (requestCode) {
      case CONTACTS_PERMISSION_REQUEST_CODE -> {
        return new String[] {Manifest.permission.READ_CONTACTS};
      }
      case NOTIFICATION_PERMISSION_REQUEST_CODE -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          return new String[] {Manifest.permission.POST_NOTIFICATIONS};
        } else {
          return new String[0];
        }
      }
      case STORAGE_PERMISSION_REQUEST_READ_CODE, STORAGE_PERMISSION_REQUEST_WRITE_CODE -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
          return new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
          };
        } else {
          return new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
      }
      default -> throw new IllegalArgumentException("Unknown request code " + requestCode);
    }
  }

  public static void onRequestPermissionsResult(
      int requestCode, String[] permissions, int[] grantResults, Object receiver) {
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, receiver);
  }
}
