package com.menny.android.anysoftkeyboard.keyboards;

import java.util.HashMap;

import android.content.Context;
import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class HebrewKeyboard extends AnyKeyboard implements HardKeyboardTranslator
{	
	//private final static char msPhysicalKeysMapping[];
    private final static HashMap<Integer, Integer> msPhysicalKeysMap;
    
    static
    {
//    	msPhysicalKeysMapping = new char[]{	1513, 1504, 1489, 1490,
//				1511, 1499, 1506, 1497,
//				1503, 1495, 1500, 1498,
//				1510, 1502, 1501, 1508, 1509 /*Q*/,
//				1512, 1491, 1488, 1493, 1492, 
//				1507 /*W*/, 1505, 1496, 1494, 1514};
    	
    	msPhysicalKeysMap = new HashMap<Integer, Integer>();
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_A, 1513);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_B, 1504);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_C, 1489);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_D, 1490);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_E, 1511);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_F, 1499);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_G, 1506);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_H, 1497);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_I, 1503);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_J, 1495);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_K, 1500);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_L, 1498);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_M, 1510);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_N, 1502);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_O, 1501);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_P, 1508);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_Q, 1509);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_R, 1512);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_S, 1491);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_T, 1488);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_U, 1493);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_V, 1492);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_W, 1507);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_X, 1505);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_Y, 1496);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_Z, 1494);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_COMMA, 1514);
    }
    
	public HebrewKeyboard(Context context) 
	{
		super(context, R.xml.heb_qwerty, true, "עיברית", "heb_keyboard");
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
//			int charIndex = primaryCode - KeyEvent.KEYCODE_A;
//			if ((charIndex < 0) || (charIndex >= msPhysicalKeysMapping.length))
//				return (char)primaryCode;//out of my array.
//			else
//				return msPhysicalKeysMapping[charIndex];
			if (msPhysicalKeysMap.containsKey(primaryCode))
				return (char)msPhysicalKeysMap.get(primaryCode).intValue();
			else
				return 0;
		}
		else if (((metaState&KeyEvent.META_ALT_ON) == 0) &&
				 (primaryCode == KeyEvent.KEYCODE_COMMA))
		{
			//this is a special case - we support comma by giving 
			//ALT+comma, since comma itself is TET Hebrew letter.
			return (char)',';
		}
		else
		{
			return 0;
		}
	}
}
