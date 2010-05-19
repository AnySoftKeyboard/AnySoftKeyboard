package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class GenericKeyboard extends ExternalAnyKeyboard 
{
	public GenericKeyboard(AnyKeyboardContextProvider context, int xmlLayoutResId, int nameResId, String prefKeyId, boolean withGenericRows) 
	{
		super(context, context.getApplicationContext(), xmlLayoutResId, xmlLayoutResId, prefKeyId, nameResId, -1, -1, null, null, withGenericRows);
	}
}
