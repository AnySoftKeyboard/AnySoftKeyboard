package com.anysoftkeyboard.ui.settings.setup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.anysoftkeyboard.addons.AddOn
import com.anysoftkeyboard.addons.AddOnsFactory
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension
import com.anysoftkeyboard.keyboards.Keyboard
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView
import com.anysoftkeyboard.prefs.DirectBootAwareSharedPreferences
import com.anysoftkeyboard.ui.settings.MainSettingsActivity
import com.menny.android.anysoftkeyboard.AnyApplication
import com.menny.android.anysoftkeyboard.R
import java.util.Random

open class WizardPageWelcomeFragment : WizardPageBaseFragment(), View.OnClickListener {

  private lateinit var demoAnyKeyboardView: DemoAnyKeyboardView
  private var performDemoKeyboardChange: Runnable? = null

  override fun getPageLayoutId(): Int = R.layout.keyboard_setup_wizard_page_welcome_layout

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.findViewById<View>(R.id.go_to_start_setup).setOnClickListener(this)
    view.findViewById<View>(R.id.setup_wizard_welcome_privacy_action).setOnClickListener(this)
    view.findViewById<View>(R.id.skip_setup_wizard).setOnClickListener(this)

    demoAnyKeyboardView = view.findViewById(R.id.demo_keyboard_view)
  }

  override fun isStepCompleted(context: Context): Boolean {
    // note: we can not use mSharedPrefs, since this method might be
    // called before onAttached is called.
    val prefs = mSharedPrefs ?: DirectBootAwareSharedPreferences.create(context)
    return prefs.getBoolean(STARTED_PREF_KEY, false)
  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.go_to_start_setup -> {
        mSharedPrefs.edit().putBoolean(STARTED_PREF_KEY, true).apply()
        refreshWizardPager()
      }

      R.id.setup_wizard_welcome_privacy_action -> {
        val privacyUrl = getString(R.string.privacy_policy)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)))
      }

      R.id.skip_setup_wizard -> {
        startActivity(Intent(context, MainSettingsActivity::class.java))
        requireActivity().finish()
      }

      else ->
          throw IllegalArgumentException(
              "Failed to handle ${v.id} in WizardPageDoneAndMoreSettingsFragment",
          )
    }
  }

  override fun onStart() {
    super.onStart()
    val changeRunnable = ChangeDemoKeyboardRunnable(context, demoAnyKeyboardView)
    performDemoKeyboardChange = changeRunnable
    changeRunnable.run()
    SetupSupport.popupViewAnimationWithIds(view, R.id.go_to_start_setup)
  }

  override fun onStop() {
    super.onStop()
    performDemoKeyboardChange?.let { demoAnyKeyboardView.removeCallbacks(it) }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    demoAnyKeyboardView.onViewNotRequired()
  }

  private class ChangeDemoKeyboardRunnable(
      private val context: Context?,
      private val demoAnyKeyboardView: DemoAnyKeyboardView,
  ) : Runnable {
    private val random = Random()
    private val keyboardBuilder = AnyApplication.getKeyboardFactory(context).enabledAddOn

    override fun run() {
      demoAnyKeyboardView.setKeyboardTheme(
          getRandomAddOn(AnyApplication.getKeyboardThemeFactory(context)),
      )

      val bottomRow: KeyboardExtension = getRandomAddOn(AnyApplication.getBottomRowFactory(context))
      val topRow: KeyboardExtension = getRandomAddOn(AnyApplication.getTopRowFactory(context))

      val keyboard = keyboardBuilder?.createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL)
      keyboard?.let {
        it.loadKeyboard(demoAnyKeyboardView.themedKeyboardDimens, topRow, bottomRow)
        demoAnyKeyboardView.setKeyboard(it, null, null)
      }

      demoAnyKeyboardView.postDelayed(this, DELAY_MILLIS_BEFORE_RESETTING_KEYBOARD.toLong())
    }

    private fun <T : AddOn> getRandomAddOn(factory: AddOnsFactory<T>): T {
      val list = factory.allAddOns
      return list[random.nextInt(list.size)]
    }
  }

  companion object {
    private const val STARTED_PREF_KEY = "setup_wizard_STARTED_SETUP_PREF_KEY"
    const val DELAY_MILLIS_BEFORE_RESETTING_KEYBOARD = 1000
  }
}
