package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;

public class LaoKeyboard extends AnyKeyboard /*implements HardKeyboardTranslator*/
{
	public LaoKeyboard(AnyKeyboardContextProvider context, String keyboardPrefId) 
	{
		super(context, R.xml.lao_qwerty, true, R.string.lao_keyboard, keyboardPrefId, true);
	}

	@Override
	public int getKeyboardIcon() 
	{
		return R.drawable.lao;
	}
}
