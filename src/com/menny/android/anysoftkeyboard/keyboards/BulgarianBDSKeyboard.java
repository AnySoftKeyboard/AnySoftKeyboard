package com.menny.android.anysoftkeyboard.keyboards;


import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;

public class BulgarianBDSKeyboard extends AnyKeyboard/* implements HardKeyboardTranslator*/
{
	public BulgarianBDSKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, R.xml.bg_bds_qwerty, false, R.string.bg_bds_keyboard, "bg_bds_keyboard", false);
	}

	@Override
	public int getKeyboardIcon() 
	{
		return R.drawable.bg_bds;
	}
}
