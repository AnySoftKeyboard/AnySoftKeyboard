package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;

import android.content.Context;

public class KeyboardFactory 
{
	public static synchronized AnyKeyboard[] createAlphaBetKeyboards(Context applicationContext)
	{
		ArrayList<AnyKeyboard> keyboards = new ArrayList<AnyKeyboard>();
		keyboards.add(new EnglishKeyboard(applicationContext));
		keyboards.add(new HebrewKeyboard(applicationContext));
		//Arabic keyboard - issue 16 - no ready yet.
		//keyboards.add(new ArabicKeyboard(applicationContext));
		
		//Lao keyboard - issue 10
		keyboards.add(new LaoKeyboard(applicationContext));
		
        return keyboards.toArray(null);
	}
}
