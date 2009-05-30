package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;
import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class LaoKeyboard extends AnyKeyboard implements HardKeyboardTranslator
{
	public LaoKeyboard(Context context) 
	{
		super(context, R.xml.lao_qwerty, true, "Lao", "lao_keyboard");
	}

	@Override
	public int getKeyboardIcon() 
	{
		return R.drawable.lao;
	}
	
	public char translatePhysicalCharacter(int primaryCode, int metaState) 
	{
		return 0;
	}
}
