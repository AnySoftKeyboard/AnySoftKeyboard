package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;

import com.menny.android.anysoftkeyboard.R;

public class EnglishKeyboard extends AnyKeyboard 
{
	public EnglishKeyboard(Context context) 
	{
		super(context, R.xml.qwerty, true, "English");
	}
}
