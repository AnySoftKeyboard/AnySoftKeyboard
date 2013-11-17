package com.anysoftkeyboard.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.menny.android.anysoftkeyboard.R;

/**
 * Created by menny on 11/16/13.
 */
public class SetUpKeyboardWizardFragment extends android.support.v4.app.Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.keyboard_setup_wizard_layout, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        //no actionbar and no menu here. This is a full screen thing!
        MainSettingsActivity activity = (MainSettingsActivity) getActivity();
        activity.setFullScreen(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        MainSettingsActivity activity = (MainSettingsActivity) getActivity();
        activity.setFullScreen(false);
    }
}