package com.anysoftkeyboard.ui.settings.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anysoftkeyboard.ui.settings.KeyboardAddOnSettingsFragment;
import com.anysoftkeyboard.ui.settings.KeyboardThemeSelectorFragment;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.FragmentChauffeurActivity;

public class WizardPageDoneAndMoreSettingsFragment extends WizardPageBaseFragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.keyboard_setup_wizard_page_additional_settings_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.go_to_languages_action).setOnClickListener(this);
        view.findViewById(R.id.go_to_theme_action).setOnClickListener(this);
        view.findViewById(R.id.go_to_all_settings_action).setOnClickListener(this);
    }

    @Override
    protected boolean isStepCompleted() {
        return false;//this step is never done! You can always configure more :)
    }

    @Override
    protected boolean isStepPreConditionDone() {
        return SetupSupport.isThisKeyboardSetAsDefaultIME(getActivity());
    }

    @Override
    public void onClick(View v) {
        MainSettingsActivity activity = (MainSettingsActivity) getActivity();
        switch (v.getId()) {
            case R.id.go_to_languages_action:
                activity.addFragmentToUi(new KeyboardAddOnSettingsFragment(), FragmentChauffeurActivity.FragmentUiContext.DeeperExperience);
                break;
            case R.id.go_to_theme_action:
                activity.addFragmentToUi(new KeyboardThemeSelectorFragment(), FragmentChauffeurActivity.FragmentUiContext.DeeperExperience);
                break;
            case R.id.go_to_all_settings_action:
                activity.onNavigateToRootClicked(v);
                activity.openDrawer();
                break;
        }
    }
}
