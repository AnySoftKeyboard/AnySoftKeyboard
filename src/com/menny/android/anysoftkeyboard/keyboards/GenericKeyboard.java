package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;

public class GenericKeyboard extends AnyKeyboard 
{
	public GenericKeyboard(AnyKeyboardContextProvider context, int xmlLayoutResId, int keyboardNameId) 
	{
		super(context, "NONE", xmlLayoutResId, keyboardNameId, true, Dictionary.Language.None, com.menny.android.anysoftkeyboard.R.drawable.sym_keyboard_notification_icon);
	}	
}
