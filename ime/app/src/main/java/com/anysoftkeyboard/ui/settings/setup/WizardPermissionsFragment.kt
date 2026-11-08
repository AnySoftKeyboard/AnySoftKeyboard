package com.anysoftkeyboard.ui.settings.setup

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.anysoftkeyboard.base.utils.Logger
import com.anysoftkeyboard.permissions.PermissionRequestHelper
import com.menny.android.anysoftkeyboard.AnyApplication
import com.menny.android.anysoftkeyboard.R
import pub.devrel.easypermissions.AfterPermissionGranted

class WizardPermissionsFragment : WizardPageBaseFragment(), View.OnClickListener {

  private var notificationSkipped = false

  override fun getPageLayoutId(): Int = R.layout.keyboard_setup_wizard_page_permissions_layout

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    notificationSkipped = false
    view.findViewById<View>(R.id.ask_for_contact_permissions_action).setOnClickListener(this)
    view.findViewById<View>(R.id.disable_contacts_dictionary).setOnClickListener(this)
    view.findViewById<View>(R.id.open_permissions_wiki_action).setOnClickListener(this)
    view.findViewById<View>(R.id.ask_for_notification_permissions_action).setOnClickListener(this)
    view.findViewById<View>(R.id.skip_notification_permissions_action).setOnClickListener(this)
  }

  override fun isStepCompleted(context: Context): Boolean =
      isContactsPermComplete(context) && isNotificationPermComplete(context)

  private fun isContactsPermComplete(context: Context): Boolean =
      isContactsDictionaryDisabled(context) ||
          ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
              PackageManager.PERMISSION_GRANTED

  private fun isNotificationPermComplete(context: Context): Boolean {
    if (notificationSkipped) return true

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
          PackageManager.PERMISSION_GRANTED
    }

    return true
  }

  private fun isContactsDictionaryDisabled(context: Context): Boolean =
      !AnyApplication.prefs(context)
          .getBoolean(
              R.string.settings_key_use_contacts_dictionary,
              R.bool.settings_default_contacts_dictionary,
          )
          .get()

  override fun refreshFragmentUi() {
    super.refreshFragmentUi()
    setContactsPermissionCardVisibility()
    setNotificationPermissionCardVisibility()
  }

  @AfterPermissionGranted(PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE)
  fun setNotificationPermissionCardVisibility() {
    val notificationGroup = view?.findViewById<View>(R.id.notification_permission_group) ?: return

    if (isNotificationPermComplete(requireContext())) {
      notificationGroup.visibility = View.GONE
    } else {
      notificationGroup.visibility = View.VISIBLE
    }
  }

  @AfterPermissionGranted(PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE)
  fun setContactsPermissionCardVisibility() {
    val group = view?.findViewById<View>(R.id.contacts_permission_group) ?: return

    if (isContactsPermComplete(requireContext())) {
      group.visibility = View.GONE
    } else {
      group.visibility = View.VISIBLE
    }
  }

  override fun onClick(v: View) {
    val activity = activity as? AppCompatActivity ?: return

    when (v.id) {
      R.id.ask_for_contact_permissions_action -> enableContactsDictionary()

      R.id.disable_contacts_dictionary -> {
        mSharedPrefs
            .edit()
            .putBoolean(getString(R.string.settings_key_use_contacts_dictionary), false)
            .apply()
        refreshWizardPager()
      }

      R.id.open_permissions_wiki_action -> {
        val browserIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(resources.getString(R.string.permissions_wiki_site_url)),
            )
        try {
          startActivity(browserIntent)
        } catch (weirdException: ActivityNotFoundException) {
          Logger.w(
              "WizardPermissionsFragment",
              "Can not open '%' since there is nothing on the device that can handle it.",
              browserIntent.data,
          )
        }
      }

      R.id.ask_for_notification_permissions_action ->
          AnyApplication.notifier(activity).askForNotificationPostPermission(this)

      R.id.skip_notification_permissions_action -> {
        notificationSkipped = true
        refreshWizardPager()
      }

      else ->
          throw IllegalArgumentException(
              "Failed to handle ${v.id} in WizardPermissionsFragment",
          )
    }
  }

  @AfterPermissionGranted(PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE)
  fun enableContactsDictionary() {
    mSharedPrefs
        .edit()
        .putBoolean(getString(R.string.settings_key_use_contacts_dictionary), true)
        .apply()

    val granted =
        PermissionRequestHelper.check(
            this,
            PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE,
        )
    if (granted) {
      refreshWizardPager()
    }
  }
}
