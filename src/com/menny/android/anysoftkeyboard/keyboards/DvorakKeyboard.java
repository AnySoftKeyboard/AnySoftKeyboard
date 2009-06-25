package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;

public class DvorakKeyboard extends EnglishKeyboard
{
	
	public DvorakKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, R.xml.dvorak, R.string.dvorak_keyboard, "dvorak_keyboard");
	}
}
