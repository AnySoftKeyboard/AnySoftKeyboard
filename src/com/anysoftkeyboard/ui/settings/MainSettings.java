package com.anysoftkeyboard.ui.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.anysoftkeyboard.ui.tutorials.WelcomeHowToNoticeActivity;
import com.menny.android.anysoftkeyboard.R;

public class MainSettings extends PreferenceActivity {

	private static final String TAG = "ASK_PREFS";

	private static final int DIALOG_WELCOME = 1;
	
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

		Preference label = findPreference("prefs_title_key");
		label.setSummary(label.getSummary()+version);
	}

	private void setAddOnGroupSummary(String preferenceGroupKey, AddOn addOn) {
		String summary = getResources().getString(R.string.selected_add_on_summary, addOn.getName());
		findPreference(preferenceGroupKey).setSummary(summary);
	}

	public static PackageInfo getPackageInfo(Context context) throws NameNotFoundException {
		return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//I wont to help the user configure the keyboard
		if (WelcomeHowToNoticeActivity.shouldShowWelcomeActivity(getApplicationContext()))
		{
			//this is the first time the application is loaded.
			Log.i(TAG, "Welcome should be shown");
			showDialog(DIALOG_WELCOME);
		}
		
		setAddOnGroupSummary("keyboard_theme_group", 
				KeyboardThemeFactory.getCurrentKeyboardTheme(getApplicationContext()));
		setAddOnGroupSummary("quick_text_keys_group", 
				QuickTextKeyFactory.getCurrentQuickTextKey(getApplicationContext()));
		setAddOnGroupSummary("top_generic_row_group", 
				KeyboardExtensionFactory.getCurrentKeyboardExtension(getApplicationContext(),KeyboardExtension.TYPE_TOP));
		setAddOnGroupSummary("bottom_generic_row_group", 
				KeyboardExtensionFactory.getCurrentKeyboardExtension(getApplicationContext(),KeyboardExtension.TYPE_BOTTOM));
		setAddOnGroupSummary("extension_keyboard_group", 
				KeyboardExtensionFactory.getCurrentKeyboardExtension(getApplicationContext(),KeyboardExtension.TYPE_EXTENSION));
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_WELCOME)
		{
			AlertDialog dialog = new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle(R.string.how_to_enable_dialog_title)
				.setMessage(R.string.how_to_enable_dialog_text)
				.setPositiveButton(R.string.how_to_enable_dialog_show_me, 
						new OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Intent i = new Intent(getApplicationContext(), WelcomeHowToNoticeActivity.class);
								startActivity(i);
							}
						})
				.setNegativeButton(R.string.how_to_enable_dialog_dont_show_me, new OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).create();
			
			return dialog;
		}
		else
		{
			return super.onCreateDialog(id);
		}
	}
	
	
}
