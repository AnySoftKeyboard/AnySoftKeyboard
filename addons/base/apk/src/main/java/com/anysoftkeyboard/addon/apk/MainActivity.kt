package com.anysoftkeyboard.addon.apk

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.anysoftkeyboard.addon.apk.ui.AddOnAppScreen
import com.anysoftkeyboard.addon.apk.ui.AddOnTheme
import com.anysoftkeyboard.addon.base.apk.R

const val ASK_PACKAGE_NAME = "com.menny.android.anysoftkeyboard"

abstract class MainActivityBase(
    @StringRes private val addOnName: Int,
    @StringRes private val addOnDescription: Int,
    @StringRes private val addOnWebsite: Int,
    @StringRes private val addOnReleaseNotes: Int,
    @DrawableRes private val screenshot: Int,
) : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val version =
        packageManager.getPackageInfo(packageName, 0).run { "$versionName ($versionCode)" }

    setContent {
      AddOnTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
          Surface(
              modifier = Modifier.fillMaxSize().padding(innerPadding),
              color = MaterialTheme.colorScheme.background,
          ) {
            AddOnAppScreen(
                addOnName = addOnName,
                addOnDescription = addOnDescription,
                addOnWebsite = addOnWebsite,
                addOnReleaseNotes = addOnReleaseNotes,
                screenshot = screenshot,
                version = version,
                isAskInstalled = isAnySoftKeyboardInstalled(),
                onActionClick = { handleActionClick() },
                onHideLauncherClick = { hideLauncherIcon() },
                onWebsiteClick = { url -> openWebsite(url) },
            )
          }
        }
      }
    }
  }

  private fun handleActionClick() {
    if (isAnySoftKeyboardInstalled()) {
      try {
        packageManager.getLaunchIntentForPackage(ASK_PACKAGE_NAME)?.let { intent ->
          startActivity(intent)
        }
      } catch (ex: Exception) {
        Log.e("ASK_ADD_ON", "Could not launch AnySoftKeyboard!", ex)
      }
    } else {
      try {
        val search = Intent(Intent.ACTION_VIEW)
        val uri =
            Uri.Builder()
                .scheme("market")
                .authority("search")
                .appendQueryParameter("q", ASK_PACKAGE_NAME)
                .build()
        search.setData(uri)
        startActivity(search)
      } catch (ex: Exception) {
        Log.e("ASK_ADD_ON", "Could not launch Store search!", ex)
      }
    }
  }

  private fun hideLauncherIcon() {
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

  private fun openWebsite(url: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (ex: Exception) {
      Log.e("ASK_ADD_ON", "Could not open URL!", ex)
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
