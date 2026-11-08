package com.anysoftkeyboard.ui.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.anysoftkeyboard.keyboards.Keyboard
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView
import com.menny.android.anysoftkeyboard.AnyApplication
import com.menny.android.anysoftkeyboard.R

class UserInterfaceSettingsFragment : Fragment(), View.OnClickListener {

  private lateinit var demoAnyKeyboardView: DemoAnyKeyboardView

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? = inflater.inflate(R.layout.user_interface_root_settings, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.findViewById<View>(R.id.settings_tile_themes).setOnClickListener(this)
    view.findViewById<View>(R.id.settings_tile_effects).setOnClickListener(this)
    view.findViewById<View>(R.id.settings_tile_even_more).setOnClickListener(this)
    demoAnyKeyboardView = view.findViewById(R.id.demo_keyboard_view)
    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      view.findViewById<View>(R.id.demo_keyboard_view_background).visibility = View.GONE
    }
  }

  override fun onStart() {
    super.onStart()
    requireActivity().setTitle(R.string.ui_root_tile)

    val defaultKeyboard =
        AnyApplication.getKeyboardFactory(requireContext())
            .enabledAddOn
            ?.createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL)
    defaultKeyboard?.let {
      it.loadKeyboard(demoAnyKeyboardView.themedKeyboardDimens)
      demoAnyKeyboardView.setKeyboard(it, null, null)
    }
  }

  override fun onClick(view: View) {
    val navController = Navigation.findNavController(requireView())
    when (view.id) {
      R.id.settings_tile_themes ->
          navController.navigate(
              UserInterfaceSettingsFragmentDirections
                  .actionUserInterfaceSettingsFragmentToKeyboardThemeSelectorFragment(),
          )

      R.id.settings_tile_effects ->
          navController.navigate(
              UserInterfaceSettingsFragmentDirections
                  .actionUserInterfaceSettingsFragmentToEffectsSettingsFragment(),
          )

      R.id.settings_tile_even_more ->
          navController.navigate(
              UserInterfaceSettingsFragmentDirections
                  .actionUserInterfaceSettingsFragmentToAdditionalUiSettingsFragment(),
          )

      else ->
          throw IllegalArgumentException(
              "Failed to handle ${view.id} in UserInterfaceSettingsFragment",
          )
    }
  }
}
