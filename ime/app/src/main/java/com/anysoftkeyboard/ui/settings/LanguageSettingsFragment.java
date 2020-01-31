package com.anysoftkeyboard.ui.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public class LanguageSettingsFragment extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.language_root_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
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
        switch (view.getId()) {
            case R.id.settings_tile_keyboards:
                addFragmentToUi(new KeyboardAddOnBrowserFragment());
                break;
            case R.id.settings_tile_grammar:
                addFragmentToUi(new DictionariesFragment());
                break;
            case R.id.settings_tile_even_more:
                addFragmentToUi(new AdditionalLanguageSettingsFragment());
                break;
            default:
                throw new IllegalArgumentException(
                        "Failed to handle " + view.getId() + " in LanguageSettingsFragment");
        }
    }

    private void addFragmentToUi(Fragment fragment) {
        ((FragmentChauffeurActivity) getActivity())
                .addFragmentToUi(fragment, TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
    }
}
