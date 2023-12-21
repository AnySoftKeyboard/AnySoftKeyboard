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
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.permissions.PermissionRequestHelper;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class WizardPermissionsFragment extends WizardPageBaseFragment
    implements View.OnClickListener {

  @Override
  protected int getPageLayoutId() {
    return R.layout.keyboard_setup_wizard_page_permissions_layout;
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    view.findViewById(R.id.ask_for_permissions_action).setOnClickListener(this);
    mStateIcon.setOnClickListener(this);
    view.findViewById(R.id.disable_contacts_dictionary).setOnClickListener(this);
    view.findViewById(R.id.open_permissions_wiki_action).setOnClickListener(this);
    view.findViewById(R.id.ask_for_notification_permissions_action).setOnClickListener(this);
  }

  @Override
  protected boolean isStepCompleted(@NonNull Context context) {
    return isContactsDictionaryDisabled(context)
        || // either the user disabled Contacts
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED; // or the user granted permission
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
    if (getActivity() != null) {
      @DrawableRes final int stateIcon;
      if (isContactsDictionaryDisabled(getActivity())) {
        mStateIcon.setClickable(true);
        stateIcon = R.drawable.ic_wizard_contacts_disabled;
      } else if (isStepCompleted(getActivity())) {
        mStateIcon.setClickable(false);
        stateIcon = R.drawable.ic_wizard_contacts_on;
      } else {
        stateIcon = R.drawable.ic_wizard_contacts_off;
      }
      mStateIcon.setImageResource(stateIcon);

      setNotificationPermissionCardVisibility();
    }
  }

  @AfterPermissionGranted(PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE)
  private void setNotificationPermissionCardVisibility() {
    var notificationGroup = getView().findViewById(R.id.notification_permission_group);
    notificationGroup.setVisibility(View.GONE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
              requireContext(), Manifest.permission.POST_NOTIFICATIONS)
          != PackageManager.PERMISSION_GRANTED) {
        notificationGroup.setVisibility(View.VISIBLE);
      }
    }
  }

  @Override
  public void onClick(View v) {
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (activity == null) return;

    switch (v.getId()) {
      case R.id.ask_for_permissions_action:
      case R.id.step_state_icon:
        enableContactsDictionary();
        break;
      case R.id.disable_contacts_dictionary:
        mSharedPrefs
            .edit()
            .putBoolean(getString(R.string.settings_key_use_contacts_dictionary), false)
            .apply();
        refreshWizardPager();
        break;
      case R.id.open_permissions_wiki_action:
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
        break;
      case R.id.ask_for_notification_permissions_action:
        AnyApplication.notifier(activity).askForNotificationPostPermission(this);
        break;
      default:
        throw new IllegalArgumentException(
            "Failed to handle " + v.getId() + " in WizardPermissionsFragment");
    }
  }

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
