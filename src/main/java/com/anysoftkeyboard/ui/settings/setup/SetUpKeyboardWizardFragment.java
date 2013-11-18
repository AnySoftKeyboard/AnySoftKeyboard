package com.anysoftkeyboard.ui.settings.setup;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.PassengerFragment;

/**
 * This fragment will guide the user through the process of enabling, switch to and configuring AnySoftKeyboard.
 * This will be done with three pages, each for a different task:
 * 1) enable
 * 2) switch to
 * 3) additional settings (and saying 'Thank You' for switching to).
 */
public class SetUpKeyboardWizardFragment extends PassengerFragment {

    ViewPager mWizardPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.keyboard_setup_wizard_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWizardPager = (ViewPager) view.findViewById(R.id.wizard_pages_pager);
        mWizardPager.setAdapter(new WizardPagesAdapter(getActivity(), getChildFragmentManager()));
    }

    @Override
    public void onStart() {
        super.onStart();
        //no actionbar and no menu here. This is a full screen thing!
        MainSettingsActivity activity = (MainSettingsActivity) getActivity();
        activity.setFullScreen(true);
        //checking to see which page should be shown on start
        int positionToStartAt = 0;
        if (SetupSupport.isThisKeyboardEnabled(getActivity())) {
            positionToStartAt = 1;
            if (SetupSupport.isThisKeyboardSetAsDefaultIME(getActivity())) {
                positionToStartAt = 2;
            }
        }

        final int switchToPosition = positionToStartAt;
        mWizardPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWizardPager.setCurrentItem(switchToPosition, true);
            }
        }, getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    @Override
    public void onStop() {
        super.onStop();
        MainSettingsActivity activity = (MainSettingsActivity) getActivity();
        activity.setFullScreen(false);
    }

}