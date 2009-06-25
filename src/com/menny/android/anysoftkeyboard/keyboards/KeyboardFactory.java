package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;

import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class KeyboardFactory 
{
	public static synchronized AnyKeyboard[] createAlphaBetKeyboards(AnyKeyboardContextProvider contextProvider)
	{
		ArrayList<AnyKeyboard> keyboards = new ArrayList<AnyKeyboard>();
		keyboards.add(new EnglishKeyboard(contextProvider));
		//issue 31
		keyboards.add(new DvorakKeyboard(contextProvider));
		keyboards.add(new HebrewKeyboard(contextProvider));
		//Arabic keyboard - issue 16 - no ready yet.
		//keyboards.add(new ArabicKeyboard(applicationContext));
		//BG - issue 25
		keyboards.add(new BulgarianBDSKeyboard(contextProvider));
		keyboards.add(new BulgarianPhoneticKeyboard(contextProvider));
		
		//Lao keyboard - issue 10
		keyboards.add(new LaoKeyboard(contextProvider));
		//this function will create a new array if needed.
		AnyKeyboard[] keyboardsArray = keyboards.toArray(new AnyKeyboard[1]);
        
        Log.i("AnySoftKeyboard", "KeyboardFactory created "+keyboardsArray.length+" keyboards.");
        return keyboardsArray;
	}
}
