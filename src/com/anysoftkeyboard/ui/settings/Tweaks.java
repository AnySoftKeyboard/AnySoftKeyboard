
package com.anysoftkeyboard.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.menny.android.anysoftkeyboard.R;

public class Tweaks extends PreferenceActivity {

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs_tweaks);
    }
}
