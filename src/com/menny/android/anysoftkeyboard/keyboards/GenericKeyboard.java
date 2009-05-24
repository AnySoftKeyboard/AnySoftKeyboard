package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;


public class GenericKeyboard extends AnyKeyboard 
{

	public GenericKeyboard(Context context, int xmlLayoutResId,
			boolean supportsShift, String keyboardName,
    		String keyboardEnabledPref) 
	{
		super(context, xmlLayoutResId, supportsShift, false, keyboardName, keyboardEnabledPref);
	}
}
