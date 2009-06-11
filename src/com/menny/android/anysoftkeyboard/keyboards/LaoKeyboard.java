package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;

import com.menny.android.anysoftkeyboard.R;

public class LaoKeyboard extends AnyKeyboard /*implements HardKeyboardTranslator*/
{
	public LaoKeyboard(Context context) 
	{
		super(context, R.xml.lao_qwerty, true, "Lao", "lao_keyboard", true);
	}

	@Override
	public int getKeyboardIcon() 
	{
		return R.drawable.lao;
	}
}
