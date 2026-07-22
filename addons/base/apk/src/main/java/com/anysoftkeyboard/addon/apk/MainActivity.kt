package com.anysoftkeyboard.addon.apk

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.anysoftkeyboard.addon.base.apk.R
import com.anysoftkeyboard.addon.base.apk.databinding.ActivityMainBinding

const val ASK_PACKAGE_NAME = "com.menny.android.anysoftkeyboard"

abstract class MainActivityBase(
    @StringRes private val addOnName: Int,
    @StringRes private val addOnDescription: Int,
    @StringRes private val addOnWebsite: Int,
    @StringRes private val addOnReleaseNotes: Int,
    @DrawableRes private val screenshot: Int,
) : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    binding.appScreenshot.setImageResource(screenshot)
    binding.welcomeDescription.text =
        getString(R.string.welcome_subtitle_template, getText(addOnName))
    binding.packDescription.setText(addOnDescription)
    binding.addOnWebSite.text = getString(R.string.add_on_website_template, getText(addOnWebsite))
    val version =
        packageManager.getPackageInfo(packageName, 0).run { "$versionName ($versionCode)" }
    binding.releaseNotes.text =
        getString(R.string.release_notes_template, version, getText(addOnReleaseNotes))

    if (isAnySoftKeyboardInstalled()) {
      binding.actionDescription.setText(R.string.ask_installed)
      binding.actionButton.setText(R.string.open_ask_main_settings)
      binding.actionButton.setOnClickListener {
        try {
          packageManager.getLaunchIntentForPackage(ASK_PACKAGE_NAME)?.let { intent ->
            it.context.startActivity(intent)
          }
        } catch (ex: Exception) {
          Log.e("ASK_ADD_ON", "Could not launch AnySoftKeyboard!", ex)
        }
      }
    } else {
      binding.actionDescription.setText(R.string.ask_is_missing_need_install)
      binding.actionButton.setText(R.string.open_ask_in_vending)
      binding.actionButton.setOnClickListener {
        try {
          val search = Intent(Intent.ACTION_VIEW)
          val uri =
              Uri.Builder()
                  .scheme("market")
                  .authority("search")
                  .appendQueryParameter("q", ASK_PACKAGE_NAME)
                  .build()
          search.setData(uri)
          it.context.startActivity(search)
        } catch (ex: Exception) {
          Log.e("ASK_ADD_ON", "Could not launch Store search!", ex)
        }
      }
    }

    binding.hideLauncherIconButton.setOnClickListener {
      try {
        val launcherComponent = ComponentName(packageName, "$packageName.LauncherAlias")
        packageManager.setComponentEnabledSetting(
            launcherComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
        Toast.makeText(
                this,
                R.string.launcher_icon_hidden_toast,
                Toast.LENGTH_SHORT,
            )
            .show()
        finish()
      } catch (ex: Exception) {
        Log.e("ASK_ADD_ON", "Could not hide launcher icon!", ex)
      }
    }
  }

  internal fun isAnySoftKeyboardInstalled(): Boolean =
      try {
        packageManager.getPackageInfo(ASK_PACKAGE_NAME, 0)
        true
      } catch (e: Exception) {
        false
      }
}
