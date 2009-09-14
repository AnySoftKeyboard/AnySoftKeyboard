package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;

public class EnglishKeyboard extends LatinKeyboard
{
	public EnglishKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, KeyboardFactory.ENGLISH_KEYBOARD, R.xml.qwerty, R.string.eng_keyboard, Dictionary.Language.English);
	}
	
	@Override
	public int getKeyboardIcon() 
	{
		return R.drawable.en;
	}
}
