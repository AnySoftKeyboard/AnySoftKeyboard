/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ui.settings

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.anysoftkeyboard.addons.AddOnsFactory
import com.anysoftkeyboard.keyboards.Keyboard
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView
import com.anysoftkeyboard.overlay.OverlayData
import com.anysoftkeyboard.overlay.OverlayDataImpl
import com.anysoftkeyboard.theme.KeyboardTheme
import com.f2prateek.rx.preferences2.Preference
import com.menny.android.anysoftkeyboard.AnyApplication
import com.menny.android.anysoftkeyboard.R

class KeyboardThemeSelectorFragment :
    AbstractAddOnsBrowserFragment<KeyboardTheme>(
        "KeyboardThemeSelectorFragment",
        R.string.keyboard_theme_list_title,
        true,
        false,
        true,
    ) {

  private lateinit var applySummaryText: TextView
  private lateinit var applyPrefs: Preference<Boolean>
  private lateinit var selectedKeyboardView: DemoAnyKeyboardView
  private var overlayData: OverlayData = OverlayDataImpl()

  override fun getAddOnFactory(): AddOnsFactory<KeyboardTheme> =
      AnyApplication.getKeyboardThemeFactory(requireContext())

  override fun onTweaksOptionSelected() {
    Navigation.findNavController(requireView())
        .navigate(
            KeyboardThemeSelectorFragmentDirections
                .actionKeyboardThemeSelectorFragmentToKeyboardThemeTweaksFragment(),
        )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    selectedKeyboardView = view.findViewById(R.id.demo_keyboard_view)

    applyPrefs =
        AnyApplication.prefs(requireContext())
            .getBoolean(
                R.string.settings_key_apply_remote_app_colors,
                R.bool.settings_default_apply_remote_app_colors,
            )
    val demoView = view.findViewById<ViewGroup>(R.id.demo_keyboard_view_background)
    val applyOverlayView =
        layoutInflater.inflate(R.layout.prefs_adapt_theme_to_remote_app, demoView, false)
    demoView.addView(applyOverlayView)
    applySummaryText = applyOverlayView.findViewById(R.id.apply_overlay_summary)
    val checkBox = applyOverlayView.findViewById<CheckBox>(R.id.apply_overlay)
    val demoAppsRoot = applyOverlayView.findViewById<View>(R.id.overlay_demo_apps_root)
    checkBox.setOnCheckedChangeListener { _, isChecked ->
      applyPrefs.set(isChecked)
      applySummaryText.setText(
          if (isChecked) R.string.apply_overlay_summary_on else R.string.apply_overlay_summary_off,
      )
      demoAppsRoot.visibility = if (isChecked) View.VISIBLE else View.GONE
      if (!isChecked) {
        overlayData = OverlayDataImpl() /*empty one, to clear overlay*/
        selectedKeyboardView.setThemeOverlay(overlayData)
      }
    }

    checkBox.isChecked = applyPrefs.get()

    demoAppsRoot
        .findViewById<View>(R.id.theme_app_demo_phone)
        .setOnClickListener(this::onDemoAppClicked)
    demoAppsRoot
        .findViewById<View>(R.id.theme_app_demo_twitter)
        .setOnClickListener(this::onDemoAppClicked)
    demoAppsRoot
        .findViewById<View>(R.id.theme_app_demo_whatsapp)
        .setOnClickListener(this::onDemoAppClicked)
    demoAppsRoot
        .findViewById<View>(R.id.theme_app_demo_gmail)
        .setOnClickListener(this::onDemoAppClicked)
  }

  private fun onDemoAppClicked(view: View) {
    val primaryBackground: Int
    val secondaryBackground: Int
    val primaryText: Int
    val secondaryText: Int
    when (view.id) {
      R.id.theme_app_demo_phone -> {
        primaryBackground = R.color.overlay_demo_app_phone_primary_background
        secondaryBackground = R.color.overlay_demo_app_phone_secondary_background
        primaryText = R.color.overlay_demo_app_phone_primary_text
        secondaryText = R.color.overlay_demo_app_phone_primary_text
      }

      R.id.theme_app_demo_twitter -> {
        primaryBackground = R.color.overlay_demo_app_twitter_primary_background
        secondaryBackground = R.color.overlay_demo_app_twitter_secondary_background
        primaryText = R.color.overlay_demo_app_twitter_primary_text
        secondaryText = R.color.overlay_demo_app_twitter_primary_text
      }

      R.id.theme_app_demo_whatsapp -> {
        primaryBackground = R.color.overlay_demo_app_whatsapp_primary_background
        secondaryBackground = R.color.overlay_demo_app_whatsapp_secondary_background
        primaryText = R.color.overlay_demo_app_whatsapp_primary_text
        secondaryText = R.color.overlay_demo_app_whatsapp_primary_text
      }

      R.id.theme_app_demo_gmail -> {
        primaryBackground = R.color.overlay_demo_app_gmail_primary_background
        secondaryBackground = R.color.overlay_demo_app_gmail_secondary_background
        primaryText = R.color.overlay_demo_app_gmail_primary_text
        secondaryText = R.color.overlay_demo_app_gmail_primary_text
      }

      else -> throw IllegalArgumentException("Unknown demo app view ID ${view.id}")
    }

    val activity = requireActivity()
    overlayData =
        OverlayDataImpl(
            ContextCompat.getColor(activity, primaryBackground),
            ContextCompat.getColor(activity, secondaryBackground),
            ContextCompat.getColor(activity, primaryText),
            ContextCompat.getColor(activity, primaryText),
            ContextCompat.getColor(activity, secondaryText),
        )

    selectedKeyboardView.setThemeOverlay(overlayData)
  }

  override fun getMarketSearchTitle(): Int = R.string.search_market_for_keyboard_addons

  override fun getMarketSearchKeyword(): String? = "theme"

  override fun applyAddOnToDemoKeyboardView(
      addOn: KeyboardTheme,
      demoKeyboardView: DemoAnyKeyboardView,
  ) {
    demoKeyboardView.setKeyboardTheme(addOn)
    selectedKeyboardView.setThemeOverlay(overlayData)
    val defaultKeyboard =
        AnyApplication.getKeyboardFactory(requireContext())
            .enabledAddOn
            ?.createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL)
    defaultKeyboard?.let {
      it.loadKeyboard(demoKeyboardView.themedKeyboardDimens)
      demoKeyboardView.setKeyboard(it, null, null)
    }
  }
}
