package com.anysoftkeyboard.ui.settings;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public class UserInterfaceSettingsFragment extends Fragment implements View.OnClickListener {

    private DemoAnyKeyboardView mDemoAnyKeyboardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_interface_root_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.settings_tile_themes).setOnClickListener(this);
        view.findViewById(R.id.settings_tile_effects).setOnClickListener(this);
        view.findViewById(R.id.settings_tile_even_more).setOnClickListener(this);
        mDemoAnyKeyboardView = view.findViewById(R.id.demo_keyboard_view);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            view.findViewById(R.id.demo_keyboard_view_background).setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.ui_root_tile);

        AnyKeyboard defaultKeyboard = AnyApplication.getKeyboardFactory(getContext()).getEnabledAddOn().createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        defaultKeyboard.loadKeyboard(mDemoAnyKeyboardView.getThemedKeyboardDimens());
        mDemoAnyKeyboardView.setKeyboard(defaultKeyboard, null, null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settings_tile_themes:
                addFragmentToUi(new KeyboardThemeSelectorFragment());
                break;
            case R.id.settings_tile_effects:
                addFragmentToUi(new EffectsSettingsFragment());
                break;
            case R.id.settings_tile_even_more:
                addFragmentToUi(new AdditionalUiSettingsFragment());
                break;
        }
    }

    private void addFragmentToUi(Fragment fragment) {
        ((FragmentChauffeurActivity) getActivity()).addFragmentToUi(fragment, TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
    }
}
