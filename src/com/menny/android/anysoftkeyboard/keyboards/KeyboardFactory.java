package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;

public class KeyboardFactory 
{
	public static synchronized AnyKeyboard[] createAlphaBetKeyboards(Context applicationContext)
	{
		AnyKeyboard[] mKeyboards = new AnyKeyboard[3];
        mKeyboards[0] = new EnglishKeyboard(applicationContext);
        mKeyboards[1] = new HebrewKeyboard(applicationContext);
        mKeyboards[2] = new LaoKeyboard(applicationContext);
		
        return mKeyboards;
	}
	
	

}
