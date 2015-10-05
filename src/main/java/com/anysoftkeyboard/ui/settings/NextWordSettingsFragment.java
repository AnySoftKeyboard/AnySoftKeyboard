package com.anysoftkeyboard.ui.settings;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.PassengerFragmentSupport;

public class NextWordSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.prefs_next_word);
    }

    @Override
    public void onStart() {
        super.onStart();
        PassengerFragmentSupport.setActivityTitle(this, getString(R.string.next_word_dict_settings));
    }
}
