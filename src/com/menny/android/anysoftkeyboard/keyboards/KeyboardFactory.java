package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;
import com.menny.android.anysoftkeyboard.R;

import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class KeyboardFactory 
{
	public static synchronized ArrayList<AnyKeyboard> createAlphaBetKeyboards(AnyKeyboardContextProvider contextProvider)
	{
		ArrayList<AnyKeyboard> keyboards = new ArrayList<AnyKeyboard>();
		keyboards.add(new EnglishKeyboard(contextProvider));
		//issue 36: AZERTY keyboard
		keyboards.add(new LatinKeyboard(contextProvider, R.xml.azerty, R.string.azerty_keyboard, "azerty_keyboard"));
		//issue 31
		keyboards.add(new LatinKeyboard(contextProvider, R.xml.dvorak, R.string.dvorak_keyboard, "dvorak_keyboard"));
		keyboards.add(new HebrewKeyboard(contextProvider));
		//Arabic keyboard - issue 16 - no ready yet.
		//keyboards.add(new ArabicKeyboard(contextProvider));
		//BG - issue 25
		keyboards.add(new BulgarianBDSKeyboard(contextProvider));
		keyboards.add(new BulgarianPhoneticKeyboard(contextProvider));
		
		//Lao keyboard - issue 10
		keyboards.add(new LaoKeyboard(contextProvider));
		
		//Issue 39:  	 Finnish/Swedish keyboard
		keyboards.add(new LatinKeyboard(contextProvider, R.xml.fin_swedish_qwerty, R.string.finnish_swedish_keyboard, "finnish_swedish_keyboard"));
		
		//Issue 42: Catalan keyboard
		keyboards.add(new LatinKeyboard(contextProvider, R.xml.catalan, R.string.catalan_keyboard, "catalan_keyboard"));
		
		//Issue 37: Swiss keyboards
		keyboards.add(new LatinKeyboard(contextProvider, R.xml.ch_fr_qwerty, R.string.ch_fr_keyboard, "ch_fr_keyboard"));
		keyboards.add(new LatinKeyboard(contextProvider, R.xml.ch_de_qwerty, R.string.ch_fr_keyboard, "ch_fr_keyboard"));
		
		Log.i("AnySoftKeyboard", "KeyboardFactory created "+keyboards.size()+" keyboards.");
        return keyboards;
	}
}
