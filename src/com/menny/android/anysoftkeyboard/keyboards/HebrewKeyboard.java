package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;
import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class HebrewKeyboard extends AnyKeyboard implements HardKeyboardTranslator
{	
	private char mPhysicalKeysMapping[] = null;
    
	public HebrewKeyboard(Context context) 
	{
		super(context, R.xml.heb_qwerty, true, "עיברית", "heb_keyboard");
		mPhysicalKeysMapping = new char[]{	1513, 1504, 1489, 1490,
											1511, 1499, 1506, 1497,
											1503, 1495, 1500, 1498,
											1510, 1502, 1501, 1508, 1509 /*Q*/,
											1512, 1491, 1488, 1493, 1492, 
											1507 /*W*/, 1505, 1496, 1494, 1514};
	}

	@Override
	public int getKeyboardIcon() 
	{
		return R.drawable.he;
	}
	
	public char translatePhysicalCharacter(int primaryCode, int metaState) 
	{
		if (((metaState&KeyEvent.META_ALT_ON) == 0) &&
    	    ((metaState&KeyEvent.META_SHIFT_ON) == 0))
		{
			int charIndex = primaryCode - KeyEvent.KEYCODE_A;
			if ((charIndex < 0) || (charIndex >= mPhysicalKeysMapping.length))
				return (char)primaryCode;//out of my array.
			else
				return mPhysicalKeysMapping[charIndex];
		}
		else
		{
			return 0;
		}
	}
}
