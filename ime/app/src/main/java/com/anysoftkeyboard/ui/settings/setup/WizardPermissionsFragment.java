package com.anysoftkeyboard.ui.settings.setup;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.permissions.PermissionRequestHelper;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class WizardPermissionsFragment extends WizardPageBaseFragment
    implements View.OnClickListener {

  private boolean mNotificationSkipped = false;

  @Override
  protected int getPageLayoutId() {
    return R.layout.keyboard_setup_wizard_page_permissions_layout;
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mNotificationSkipped = false;
    view.findViewById(R.id.ask_for_contact_permissions_action).setOnClickListener(this);
    view.findViewById(R.id.disable_contacts_dictionary).setOnClickListener(this);
    view.findViewById(R.id.open_permissions_wiki_action).setOnClickListener(this);
    view.findViewById(R.id.ask_for_notification_permissions_action).setOnClickListener(this);
    view.findViewById(R.id.skip_notification_permissions_action).setOnClickListener(this);
  }

  @Override
  protected boolean isStepCompleted(@NonNull Context context) {
    return isContactsPermComplete(context) && isNotificationPermComplete(context);
  }

  private boolean isContactsPermComplete(@NonNull Context context) {
    return isContactsDictionaryDisabled(context)
        || // either the user disabled Contacts
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED; // or the user granted permission
  }

  private boolean isNotificationPermComplete(@NonNull Context context) {
    if (mNotificationSkipped) return true;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
          == PackageManager.PERMISSION_GRANTED;
    }

    return true;
  }

  private boolean isContactsDictionaryDisabled(Context context) {
    return !AnyApplication.prefs(context)
        .getBoolean(
            R.string.settings_key_use_contacts_dictionary,
            R.bool.settings_default_contacts_dictionary)
        .get();
  }

  @Override
  public void refreshFragmentUi() {
    super.refreshFragmentUi();
    setContactsPermissionCardVisibility();
    setNotificationPermissionCardVisibility();
  }

  @AfterPermissionGranted(PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE)
  private void setNotificationPermissionCardVisibility() {
    var notificationGroup = getView().findViewById(R.id.notification_permission_group);

    if (isNotificationPermComplete(requireContext())) {
      notificationGroup.setVisibility(View.GONE);
    } else {
      notificationGroup.setVisibility(View.VISIBLE);
    }
  }

  @AfterPermissionGranted(PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE)
  private void setContactsPermissionCardVisibility() {
    var group = getView().findViewById(R.id.contacts_permission_group);

    if (isContactsPermComplete(requireContext())) {
      group.setVisibility(View.GONE);
    } else {
      group.setVisibility(View.VISIBLE);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
  @Override
  public void onClick(View v) {
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (activity == null) return;

    // Resource IDs cannot be used in a switch statement in Android library modules
    // Fixing by replacing switch with if-else if.
    final int viewId = v.getId();
    if (viewId == R.id.ask_for_contact_permissions_action) {
      enableContactsDictionary();
    } else if (viewId == R.id.disable_contacts_dictionary) {
      mSharedPrefs
              .edit()
              .putBoolean(getString(R.string.settings_key_use_contacts_dictionary), false)
              .apply();
      refreshWizardPager();
    } else if (viewId == R.id.open_permissions_wiki_action) {
      Intent browserIntent =
              new Intent(
                      Intent.ACTION_VIEW,
                      Uri.parse(getResources().getString(R.string.permissions_wiki_site_url)));
      try {
        startActivity(browserIntent);
      } catch (ActivityNotFoundException weirdException) {
        // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/516
        // this means that there is nothing on the device
        // that can handle Intent.ACTION_VIEW with "https" schema..
        // silently swallowing it
        Logger.w(
                "WizardPermissionsFragment",
                "Can not open '%' since there is nothing on the device that can handle" + " it.",
                browserIntent.getData());
      }
    } else if (viewId == R.id.ask_for_notification_permissions_action) {
      AnyApplication.notifier(activity).askForNotificationPostPermission(this);
    } else if (viewId == R.id.skip_notification_permissions_action) {
      mNotificationSkipped = true;
      refreshWizardPager();
    } else {
      throw new IllegalArgumentException(
              "Failed to handle " + v.getId() + " in WizardPermissionsFragment");
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
  @AfterPermissionGranted(PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE)
  public void enableContactsDictionary() {
    mSharedPrefs
        .edit()
        .putBoolean(getString(R.string.settings_key_use_contacts_dictionary), true)
        .apply();

    if (PermissionRequestHelper.check(
        this, PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE)) {
      refreshWizardPager();
    }
  }
}
