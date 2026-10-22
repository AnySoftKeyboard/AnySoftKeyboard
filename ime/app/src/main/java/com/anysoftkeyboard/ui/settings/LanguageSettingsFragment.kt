package com.anysoftkeyboard.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.menny.android.anysoftkeyboard.R

class LanguageSettingsFragment : Fragment(), View.OnClickListener {

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? = inflater.inflate(R.layout.language_root_settings, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.findViewById<View>(R.id.settings_tile_keyboards).setOnClickListener(this)
    view.findViewById<View>(R.id.settings_tile_grammar).setOnClickListener(this)
    view.findViewById<View>(R.id.settings_tile_even_more).setOnClickListener(this)
  }

  override fun onStart() {
    super.onStart()
    requireActivity().setTitle(R.string.language_root_tile)
  }

  override fun onClick(view: View) {
    val navController = Navigation.findNavController(requireView())
    when (view.id) {
      R.id.settings_tile_keyboards ->
          navController.navigate(
              LanguageSettingsFragmentDirections
                  .actionLanguageSettingsFragmentToKeyboardAddOnBrowserFragment(),
          )

      R.id.settings_tile_grammar ->
          navController.navigate(
              LanguageSettingsFragmentDirections
                  .actionLanguageSettingsFragmentToDictionariesFragment(),
          )

      R.id.settings_tile_even_more ->
          navController.navigate(
              LanguageSettingsFragmentDirections
                  .actionLanguageSettingsFragmentToAdditionalLanguageSettingsFragment(),
          )

      else ->
          throw IllegalArgumentException(
              "Failed to handle ${view.id} in LanguageSettingsFragment",
          )
    }
  }
}
