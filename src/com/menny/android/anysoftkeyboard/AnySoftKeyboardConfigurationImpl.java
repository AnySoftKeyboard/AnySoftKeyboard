package com.menny.android.anysoftkeyboard;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
	private String mLayoutChangeKeysSize;
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
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mIme);
		
		upgradeSettingsValues(sp);
		
		handleConfigurationChange(sp);
	}
	
	private void upgradeSettingsValues(SharedPreferences sp) {
		Log.d("AnySoftKeyboard", "Checking if configuration upgrade is needed.");
		String currentChangeLayoutKeysSize = sp.getString("keyboard_layout_change_method", "Small");
		if ((currentChangeLayoutKeysSize == null) || (currentChangeLayoutKeysSize.length() == 0) ||
			(currentChangeLayoutKeysSize.equals("1")) || (currentChangeLayoutKeysSize.equals("2")) || (currentChangeLayoutKeysSize.equals("3")))
		{
			String newValue = "Small";
			Log.d("AnySoftKeyboard", "keyboard_layout_change_method holds an old value: "+(currentChangeLayoutKeysSize != null? currentChangeLayoutKeysSize : "NULL"));
			if (currentChangeLayoutKeysSize.equals("1")) newValue = "Small";
			else if (currentChangeLayoutKeysSize.equals("2")) newValue = "None";
			else if (currentChangeLayoutKeysSize.equals("3")) newValue = "Big";
			Editor e = sp.edit();
			Log.d("AnySoftKeyboard", "keyboard_layout_change_method will be changed to: "+newValue);
			e.putString("keyboard_layout_change_method", newValue);
			e.commit();
		}
	}


	
	public boolean handleConfigurationChange(SharedPreferences sp)
	{
		Log.i("AnySoftKeyboard", "**** handleConfigurationChange: ");
		boolean handled = false;
		// this change requires the recreation of the keyboards.
		// so we wont mark the 'handled' result.
		mLayoutChangeKeysSize = sp.getString("keyboard_layout_change_method", "Small");
		Log.i("AnySoftKeyboard", "** mChangeKeysMode: "+mLayoutChangeKeysSize);
		
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

	public String getChangeLayoutKeysSize() {
		return mLayoutChangeKeysSize;
	}
	
	public boolean getShowKeyPreview()
	{
		return mShowKeyPreview;
	}
}

