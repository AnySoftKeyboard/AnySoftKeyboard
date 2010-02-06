package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;

public class LaoKeyboard extends InternalAnyKeyboard /*implements HardKeyboardTranslator*/
{
	public LaoKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, R.xml.lao_qwerty);
	}
}
