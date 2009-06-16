package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;

public class GenericKeyboard extends AnyKeyboard 
{
	public GenericKeyboard(Context context, int xmlLayoutResId,
			boolean supportsShift, int keyboardNameId,
    		String keyboardEnabledPref) 
	{
		super(context, xmlLayoutResId, supportsShift, keyboardNameId, keyboardEnabledPref, true);
	}	
}
