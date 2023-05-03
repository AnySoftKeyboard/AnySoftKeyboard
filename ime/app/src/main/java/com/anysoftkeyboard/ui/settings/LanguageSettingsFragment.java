package com.anysoftkeyboard.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.menny.android.anysoftkeyboard.R;

public class LanguageSettingsFragment extends Fragment implements View.OnClickListener {

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.language_root_settings, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    view.findViewById(R.id.settings_tile_keyboards).setOnClickListener(this);
    view.findViewById(R.id.settings_tile_grammar).setOnClickListener(this);
    view.findViewById(R.id.settings_tile_even_more).setOnClickListener(this);
  }

  @Override
  public void onStart() {
    super.onStart();
    getActivity().setTitle(R.string.language_root_tile);
  }

  @Override
  public void onClick(View view) {
    final NavController navController = Navigation.findNavController(requireView());
    switch (view.getId()) {
      case R.id.settings_tile_keyboards:
        navController.navigate(
            LanguageSettingsFragmentDirections
                .actionLanguageSettingsFragmentToKeyboardAddOnBrowserFragment());
        break;
      case R.id.settings_tile_grammar:
        navController.navigate(
            LanguageSettingsFragmentDirections
                .actionLanguageSettingsFragmentToDictionariesFragment());
        break;
      case R.id.settings_tile_even_more:
        navController.navigate(
            LanguageSettingsFragmentDirections
                .actionLanguageSettingsFragmentToAdditionalLanguageSettingsFragment());
        break;
      default:
        throw new IllegalArgumentException(
            "Failed to handle " + view.getId() + " in LanguageSettingsFragment");
    }
  }
}
