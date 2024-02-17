package com.anysoftkeyboard.addon.apk

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
    @DrawableRes private val screenshot: Int,
) : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.appScreenshot.setImageResource(screenshot)
        binding.welcomeDescription.text = getString(R.string.welcome_subtitle_template, getText(addOnName))
        binding.packDescription.text = getText(addOnDescription)

        if (isAnySoftKeyboardInstalled()) {
            binding.actionDescription.setText(R.string.ask_installed)
            binding.actionButton.setText(R.string.open_ask_main_settings)
            binding.actionButton.setOnClickListener {
                try {
                    packageManager.getLaunchIntentForPackage(ASK_PACKAGE_NAME)?.let { intent ->
                        it.context.startActivity(intent)
                    }
                } catch (ex: Exception) {
                    Log.e("ASK_ADD_ON", "Could not launch Store search!", ex)
                }
            }
        } else {
            binding.actionDescription.setText(R.string.ask_is_missing_need_install)
            binding.actionButton.setText(R.string.open_ask_in_vending)
            binding.actionButton.setOnClickListener {
                try {
                    val search = Intent(Intent.ACTION_VIEW)
                    val uri = Uri.Builder()
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
    }

    private fun isAnySoftKeyboardInstalled(): Boolean {
        // TODO: we need to query for a broadcast-receiver, or something
        return try {
            val services = packageManager.getPackageInfo(
                ASK_PACKAGE_NAME,
                PackageManager.GET_SERVICES,
            )
            services.services.any { it.name == "com.menny.android.anysoftkeyboard.SoftKeyboard" }
        } catch (e: Exception) {
            false
        }
    }
}
