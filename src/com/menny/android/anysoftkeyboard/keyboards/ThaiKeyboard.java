package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;

public class ThaiKeyboard extends AnyKeyboard /*implements HardKeyboardTranslator*/
{
	public ThaiKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, KeyboardFactory.THAI_KEYBOARD, R.xml.thai_qwerty, true, R.string.thai_keyboard, true, Dictionary.Language.None, R.drawable.thai);
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