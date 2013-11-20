package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.provider.Settings;
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

    private Context mAppContext;

    private final ContentObserver mSecureSettingsChanged = new ContentObserver(null) {
        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            mWizardPager.getAdapter().notifyDataSetChanged();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAppContext = getActivity().getApplicationContext();
        mAppContext.getContentResolver().registerContentObserver(Settings.Secure.CONTENT_URI, true, mSecureSettingsChanged);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.keyboard_setup_wizard_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWizardPager = (ViewPager) view.findViewById(R.id.wizard_pages_pager);
        mWizardPager.setAdapter(new WizardPagesAdapter(getChildFragmentManager()));
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
        }, getResources().getInteger(android.R.integer.config_longAnimTime));
    }

    @Override
    public void onStop() {
        super.onStop();
        MainSettingsActivity activity = (MainSettingsActivity) getActivity();
        activity.setFullScreen(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAppContext.getContentResolver().unregisterContentObserver(mSecureSettingsChanged);
    }
}