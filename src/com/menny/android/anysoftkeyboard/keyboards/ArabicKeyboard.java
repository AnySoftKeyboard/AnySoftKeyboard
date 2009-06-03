package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;

import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class ArabicKeyboard extends AnyKeyboard implements HardKeyboardTranslator
{
	public ArabicKeyboard(Context context) 
	{
		super(context, R.xml.arabic_qwerty, false, "Arabic", "arabic_keyboard");
	}

	@Override
	public int getKeyboardIcon() 
	{
		return R.drawable.ar;
	}
	
	public char translatePhysicalCharacter(int primaryCode, int metaState) 
	{
		return 0;
	}
}
