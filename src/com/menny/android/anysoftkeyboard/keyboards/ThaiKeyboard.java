package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;

public class ThaiKeyboard extends InternalAnyKeyboard /*implements HardKeyboardTranslator*/
{
	public ThaiKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, R.xml.thai_qwerty);
	}

	@Override
	public boolean isLetter(char keyValue)
	{
		return 
			((keyValue > 0x0e01 && keyValue < 0x0e3a) || 
			(keyValue > 0x0e40 && keyValue < 0x0e4e) || 
			(keyValue > 0x0e50 && keyValue < 0x0e5b) || 
			Character.isLetter(keyValue) || 
			(keyValue == '\''));
	}
}