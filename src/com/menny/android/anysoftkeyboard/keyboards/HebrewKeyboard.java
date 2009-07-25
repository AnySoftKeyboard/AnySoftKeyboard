package com.menny.android.anysoftkeyboard.keyboards;

import java.util.HashMap;

import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class HebrewKeyboard extends AnyKeyboard implements HardKeyboardTranslator
{	
	private final static HashMap<Integer, Integer> msPhysicalKeysMap;
    
    static
    {    	
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
    
	public HebrewKeyboard(AnyKeyboardContextProvider context, String keyboardPrefId) 
	{
		super(context, R.xml.heb_qwerty, true, R.string.heb_keyboard, keyboardPrefId, false);
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
			if (msPhysicalKeysMap.containsKey(primaryCode))
				return (char)msPhysicalKeysMap.get(primaryCode).intValue();
			else
				return 0;
		}
		else if (((metaState&KeyEvent.META_ALT_ON) != 0) &&
				 (primaryCode == KeyEvent.KEYCODE_COMMA))
		{
			//this is a special case - we support comma by giving 
			//ALT+comma, since comma itself is TET Hebrew letter.
			return (char)',';
		}
		else if (((metaState&KeyEvent.META_SHIFT_ON) != 0) &&
				 (primaryCode == KeyEvent.KEYCODE_COMMA))
		{
			//this is a special case - we support comma by giving 
			//shift+comma, since question itself is TET Hebrew letter.
			return (char)'?';
		}
		else
		{
			return 0;
		}
	}
}
