package com.anysoftkeyboard.ui.settings;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.menny.android.anysoftkeyboard.R;

public class MainSettings extends PreferenceActivity {

	private static final String TAG = "ASK_PREFS";

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.prefs);
        
        String version = "";
        try {
			final PackageInfo info = getPackageInfo(getApplicationContext());
			version = info.versionName + " (release "+info.versionCode+")";
		} catch (final NameNotFoundException e) {
			Log.e(TAG, "Failed to locate package information! This is very weird... I'm installed.");
		}

		final Preference label = super.findPreference("prefs_title_key");
		label.setSummary(label.getSummary()+version);
	}

	public static PackageInfo getPackageInfo(Context context) throws NameNotFoundException {
		return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	}
}
