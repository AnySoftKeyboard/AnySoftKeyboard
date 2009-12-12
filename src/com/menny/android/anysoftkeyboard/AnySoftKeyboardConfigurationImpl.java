package com.menny.android.anysoftkeyboard;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.util.Log;

public class AnySoftKeyboardConfigurationImpl implements AnySoftKeyboardConfiguration
{
	private static AnySoftKeyboardConfigurationImpl msInstance;
		
	public static AnySoftKeyboardConfiguration getInstance() {return msInstance;}
	
	private final InputMethodService mIme;
	//this is determined from the version. It includes "tester", the it will be true
	private final boolean mDEBUG;

	private String mSmileyText;
	private String mDomainText;
	private String mChangeKeysMode;
	private boolean mShowKeyPreview;
	
	AnySoftKeyboardConfigurationImpl(InputMethodService ime)
	{
		msInstance = this;
		
		mIme = ime;
		
		Log.i("AnySoftKeyboard", "** Locale:"+ mIme.getResources().getConfiguration().locale.toString());
		String version = "";
        try {
			PackageInfo info = mIme.getApplication().getPackageManager().getPackageInfo(mIme.getApplication().getPackageName(), 0);
			version = info.versionName;
			Log.i("AnySoftKeyboard", "** Version: "+version);
		} catch (NameNotFoundException e) {
			Log.e("AnySoftKeyboard", "Failed to locate package information! This is very weird... I'm installed.");
		}
		
		mDEBUG = version.contains("tester");
		Log.i("AnySoftKeyboard", "** Debug: "+mDEBUG);
		handleConfigurationChange(PreferenceManager.getDefaultSharedPreferences(mIme));
	}
	
	public boolean handleConfigurationChange(SharedPreferences sp)
	{
		Log.i("AnySoftKeyboard", "**** handleConfigurationChange: ");
		boolean handled = false;
		// this change requires the recreation of the keyboards.
		// so we wont mark the 'handled' result.
		mChangeKeysMode = sp.getString("keyboard_layout_change_method", "1");
		Log.i("AnySoftKeyboard", "** mChangeKeysMode: "+mChangeKeysMode);
		
		String newSmileyText = sp.getString("default_smiley_text", ":-) ");
		handled = handled || (!newSmileyText.equals(mSmileyText));
		mSmileyText = newSmileyText;
		Log.i("AnySoftKeyboard", "** mSmileyText: "+mSmileyText);
		
		String newDomainText = sp.getString("default_domain_text", ".com");
		handled = handled || (!newDomainText.equals(mDomainText));
		mDomainText = newDomainText;
		Log.i("AnySoftKeyboard", "** mDomainText: "+mDomainText);
		
		boolean newShowPreview = sp.getBoolean("key_press_preview_popup", true);
		handled = handled || (newShowPreview != mShowKeyPreview);
		mShowKeyPreview = newShowPreview;
		Log.i("AnySoftKeyboard", "** mShowKeyPreview: "+mShowKeyPreview);
		
		return handled;
	}

	public boolean getDEBUG() {return mDEBUG;}

	public String getDomainText() {
		return mDomainText;
	}

	public String getSmileyText() {
		return mSmileyText;
	}

	public String getChangeLayoutMode() {
		return mChangeKeysMode;
	}
	
	public boolean getShowKeyPreview()
	{
		return mShowKeyPreview;
	}
}

