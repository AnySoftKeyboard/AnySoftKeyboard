package com.anysoftkeyboard.ui.settings.setup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.anysoftkeyboard.keyboards.Keyboard
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView
import com.anysoftkeyboard.ui.settings.MainSettingsActivity
import com.menny.android.anysoftkeyboard.AnyApplication
import com.menny.android.anysoftkeyboard.R

class WizardPageDoneAndMoreSettingsFragment : WizardPageBaseFragment(), View.OnClickListener {

  private lateinit var demoAnyKeyboardView: DemoAnyKeyboardView

  override fun getPageLayoutId(): Int =
      R.layout.keyboard_setup_wizard_page_additional_settings_layout

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.findViewById<View>(R.id.go_to_languages_action).setOnClickListener(this)
    view.findViewById<View>(R.id.go_to_theme_action).setOnClickListener(this)
    view.findViewById<View>(R.id.go_to_all_settings_action).setOnClickListener(this)

    demoAnyKeyboardView = view.findViewById(R.id.demo_keyboard_view)
  }

  override fun isStepCompleted(context: Context): Boolean = false

  override fun onClick(v: View) {
    val activity = requireActivity() as AppCompatActivity
    when (v.id) {
      R.id.go_to_languages_action ->
          startActivity(
              Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse(requireContext().getString(R.string.deeplink_url_keyboards)),
                  requireContext(),
                  MainSettingsActivity::class.java,
              ),
          )

      R.id.go_to_theme_action ->
          startActivity(
              Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse(requireContext().getString(R.string.deeplink_url_themes)),
                  requireContext(),
                  MainSettingsActivity::class.java,
              ),
          )

      R.id.go_to_all_settings_action -> {
        startActivity(Intent(context, MainSettingsActivity::class.java))
        activity.finish()
      }

      else ->
          throw IllegalArgumentException(
              "Failed to handle ${v.id} in WizardPageDoneAndMoreSettingsFragment",
          )
    }
  }

  override fun onStart() {
    super.onStart()
    val defaultKeyboard =
        AnyApplication.getKeyboardFactory(requireContext())
            .enabledAddOn
            ?.createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL)
    defaultKeyboard?.let {
      it.loadKeyboard(demoAnyKeyboardView.themedKeyboardDimens)
      demoAnyKeyboardView.setKeyboard(it, null, null)
    }

    SetupSupport.popupViewAnimationWithIds(
        view,
        R.id.go_to_languages_action,
        0,
        R.id.go_to_theme_action,
        0,
        R.id.go_to_all_settings_action,
    )
  }

  override fun onDestroyView() {
    super.onDestroyView()
    demoAnyKeyboardView.onViewNotRequired()
  }
}
