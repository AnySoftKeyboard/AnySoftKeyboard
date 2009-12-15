package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;

public class ArabicKeyboard extends AnyKeyboard/* implements HardKeyboardTranslator*/
{
	public ArabicKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, KeyboardFactory.ARABIC_KEYBOARD, R.xml.arabic_qwerty, R.string.arabic_keyboard, false, Dictionary.Language.None, R.drawable.ar);
	}
}
