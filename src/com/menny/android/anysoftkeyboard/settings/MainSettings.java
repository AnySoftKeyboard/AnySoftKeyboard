package com.menny.android.anysoftkeyboard.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.menny.android.anysoftkeyboard.MainForm;
import com.menny.android.anysoftkeyboard.R;

public class MainSettings extends PreferenceActivity {

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.layout.prefs);
        
        String version = "";
        try {
			final PackageInfo info = getPackageInfo(getApplicationContext());
			version = info.versionName + " (release "+info.versionCode+")";
		} catch (final NameNotFoundException e) {
			Log.e("AnySoftKeyboard", "Failed to locate package information! This is very weird... I'm installed.");
		}

		final Preference label = super.findPreference("prefs_title_key");
		label.setSummary(label.getSummary()+version);
		
		final Preference helper = (Preference)super.findPreference("prefs_help_key");
		helper.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				if (preference.getKey().equals("prefs_help_key"))
				{
						//http://s.evendanan.net/ask_settings
						//Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://s.evendanan.net/ask_settings"));
						//startActivity(browserIntent);
						Intent main = new Intent(getApplicationContext(), MainForm.class);
						startActivity(main);
						finish();
						return true;
				}
				return false;
			}
		});
	}

	public static PackageInfo getPackageInfo(Context context) throws NameNotFoundException {
		return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	}
}
