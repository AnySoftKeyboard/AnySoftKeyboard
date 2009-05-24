package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;
import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.R;

public class LaoKeyboard extends AnyKeyboard 
{
	private char mPhysicalKeysMapping[] = null;
    
	public LaoKeyboard(Context context) 
	{
		super(context, R.xml.lao_qwerty, false, false, "Lao", "lao_keyboard");
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
		return R.drawable.lao;
	}
	
	@Override
	public char translatePhysicalCharacter(char primaryCode) 
	{
		//sendKey(mCurKeyboard.getPhysicalKeysMapping()[keyCode - KeyEvent.KEYCODE_A]);
		int charIndex = primaryCode - KeyEvent.KEYCODE_A;
		if ((charIndex < 0) || (charIndex >= mPhysicalKeysMapping.length))
			return primaryCode;//out of my array.
		else
			return mPhysicalKeysMapping[charIndex];
	}
}
