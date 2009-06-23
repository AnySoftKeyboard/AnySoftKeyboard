package com.menny.android.anysoftkeyboard.keyboards;

import java.util.HashMap;

import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class BulgarianPhoneticKeyboard extends AnyKeyboard implements HardKeyboardTranslator
{
	private final static HashMap<Integer, Integer> msPhysicalKeysMap;
	private final static HashMap<Integer, Integer> msPhysicalShiftKeysMap;
    
    static
    {    	
    	msPhysicalKeysMap = new HashMap<Integer, Integer>();
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_A, 1072);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_B, 1073);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_C, 1094);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_D, 1076);
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
    	
    	msPhysicalShiftKeysMap = new HashMap<Integer, Integer>();
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_A, 1513);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_B, 1504);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_C, 1489);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_D, 1490);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_E, 1511);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_F, 1499);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_G, 1506);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_H, 1497);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_I, 1503);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_J, 1495);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_K, 1500);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_L, 1498);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_M, 1510);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_N, 1502);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_O, 1501);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_P, 1508);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_Q, 1509);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_R, 1512);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_S, 1491);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_T, 1488);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_U, 1493);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_V, 1492);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_W, 1507);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_X, 1505);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_Y, 1496);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_Z, 1494);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_COMMA, 1514);
    }

	public BulgarianPhoneticKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, R.xml.bg_ph_qwerty, false, R.string.bg_ph_keyboard, "bg_ph_keyboard", false);
	}

	@Override
	public int getKeyboardIcon() 
	{
		return R.drawable.bg_ph;
	}
	
	public char translatePhysicalCharacter(int primaryCode, int metaState) 
	{
		if ((metaState&KeyEvent.META_ALT_ON) == 0)
		{
			if ((metaState&KeyEvent.META_SHIFT_ON) == 0)
			{
				if (msPhysicalKeysMap.containsKey(primaryCode))
					return (char)msPhysicalKeysMap.get(primaryCode).intValue();
				else
					return 0;
			}
			else
			{
				if (msPhysicalShiftKeysMap.containsKey(primaryCode))
					return (char)msPhysicalShiftKeysMap.get(primaryCode).intValue();
				else
					return 0;
			}
		}
		else
		{
			return 0;
		}
	}
}
