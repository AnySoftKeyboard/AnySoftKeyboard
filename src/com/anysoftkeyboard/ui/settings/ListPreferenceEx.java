package com.anysoftkeyboard.ui.settings;

import android.content.Context;
import android.util.AttributeSet;

public class ListPreferenceEx extends android.preference.ListPreference {

	public ListPreferenceEx(Context context) {
		super(context);
	}
	
	public ListPreferenceEx(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public CharSequence getSummary() {
		//I need to update the summary, in case it includes a String#Format place-holders.
		//now, what does this code do: In some versions of Android (prior to Honeycomb)
		//the getSummary does include the nifty trick of allowing the developer to 
		//use the %s place holder. So, if I include a "%s" in the strings, in Gingerbread it will be printed
		//while in Honeycomb it will be replaced with the current selection.
		//So I hack: If the device is GB, then "getSummary" will include the %s, and the String.format function
		//will replace it. Win!
		//if the device is Honeycomb, then "getSummary" will already replace the %s, and it wont be there, and
		//the String.format function will do nothing! Win again!
		return String.format(super.getSummary().toString(), getEntry());
	}
	
	@Override
	public void setValue(String value) {
		super.setValue(value);
		//so the Summary will be updated
		notifyChanged();
	}

}
