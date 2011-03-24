
package com.menny.android.anysoftkeyboard.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.menny.android.anysoftkeyboard.R;

public class Tweaks extends PreferenceActivity {

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.prefs_tweaks);
    }
}
