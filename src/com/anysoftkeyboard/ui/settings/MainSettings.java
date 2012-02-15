package com.anysoftkeyboard.ui.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;

import com.anysoftkeyboard.ui.tutorials.WelcomeHowToNoticeActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
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

		final Preference label = super.findPreference("prefs_title_key");
		label.setSummary(label.getSummary()+version);
	}

	public static PackageInfo getPackageInfo(Context context) throws NameNotFoundException {
		return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//I wont to help the user configure the keyboard
		if (!linearSearch( Secure.getString(getContentResolver(), Secure.ENABLED_INPUT_METHODS), getPackageName() ) )
		{
			//ASK is not enabled, but installed. Has the user forgot how to turn it on?
			if (!WelcomeHowToNoticeActivity.hasWelcomeActivityShown(getApplicationContext()))
			{
				//this is the first time the application is loaded.
				Log.i(TAG, "Welcome should be shown");
				showDialog(DIALOG_WELCOME);
			}
		}
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
	
	/**
	 * Search array for an entry BEGINNING with key.
	 * 
	 * @param array the array to search over
	 * @param key the string to search for
	 * @return true if the key was found in the array
	 */
	private static boolean linearSearch( String listOfIme, final String key )
	{
		if (TextUtils.isEmpty(listOfIme) || TextUtils.isEmpty(key))
			return false;
		if (AnyApplication.DEBUG)
			Log.d(TAG, "Currently these are the IME enabled in the OS: "+listOfIme);
		String[] arrayOfIme = listOfIme.split(":");
		if (arrayOfIme == null)
			return false;
		
		for(final String ime : arrayOfIme)
		{
			if (TextUtils.isEmpty(ime)) continue;
			if (AnyApplication.DEBUG)
				Log.d(TAG, "Is '"+ime+"' starts with '"+key+"'?");
			//checking "startsWith" since the OS list is something like this:
			//com.android.inputmethod.latin/.LatinIME:com.menny.android.anysoftkeyboard/.SoftKeyboard
			if (ime.startsWith(key)) return true;
		}
		
		if (AnyApplication.DEBUG)
			Log.d(TAG, "'"+key+"' was not found in the list of IMEs!");
		return false;
	}
}
