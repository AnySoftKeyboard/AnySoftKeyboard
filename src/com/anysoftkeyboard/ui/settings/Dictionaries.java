
package com.anysoftkeyboard.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.menny.android.anysoftkeyboard.R;

public class Dictionaries extends PreferenceActivity {

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.prefs_dictionaries);
    }
}
