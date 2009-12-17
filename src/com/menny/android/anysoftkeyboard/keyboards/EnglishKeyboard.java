package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class EnglishKeyboard extends LatinKeyboard implements HardKeyboardTranslator//this class implements the HardKeyboardTranslator interface in an empty way, the physical keyboard is Latin...
{
	public EnglishKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, R.xml.qwerty);
	}
	
	//this class implements the HardKeyboardTranslator interface in an empty way, the physical keyboard is Latin...
	public void translatePhysicalCharacter(HardKeyboardAction action) 
	{
		//I'll do nothing, so the caller will use defaults.
	}
}
