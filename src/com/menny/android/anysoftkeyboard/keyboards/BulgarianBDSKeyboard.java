package com.menny.android.anysoftkeyboard.keyboards;


import java.util.HashMap;

import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class BulgarianBDSKeyboard extends AnyKeyboard implements HardKeyboardTranslator
{
	private final static HashMap<Integer, Integer> msPhysicalKeysMap;
	private final static HashMap<Integer, Integer> msPhysicalShiftKeysMap;
    
    static
    {    	
    	msPhysicalKeysMap = new HashMap<Integer, Integer>();
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_A, 1100);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_B, 1092);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_C, 1098);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_D, 1072);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_E, 1077);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_F, 1086);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_G, 1078);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_H, 1075);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_I, 1089);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_J, 1090);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_K, 1085);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_L, 1074);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_M, 1087);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_N, 1093);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_O, 1076);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_P, 1079);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_Q, 1073);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_R, 1080);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_S, 1103);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_T, 1096);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_U, 1082);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_V, 1101);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_W, 1091);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_X, 1081);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_Y, 1097);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_Z, 1102);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_COMMA, 1088);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_PERIOD, 1083);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_AT, 1084);
    	
    	msPhysicalShiftKeysMap = new HashMap<Integer, Integer>();
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_A, 1117);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_B, 1060);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_C, 1066);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_D, 1040);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_E, 1045);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_F, 1054);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_G, 1046);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_H, 1043);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_I, 1057);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_J, 1058);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_K, 1053);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_L, 1042);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_M, 1055);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_N, 1061);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_O, 1044);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_P, 1047);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_Q, 1041);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_R, 1048);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_S, 1071);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_T, 1064);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_U, 1050);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_V, 1069);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_W, 1059);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_X, 1049);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_Y, 1065);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_Z, 1070);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_COMMA, 1056);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_PERIOD, 1051);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_AT, 1052);
    }
    
	public BulgarianBDSKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, KeyboardFactory.BG_BDS_KEYBOARD, R.xml.bg_bds_qwerty, R.string.bg_bds_keyboard, false, Dictionary.Language.Bulgarian, R.drawable.bg_bds);
	}
	
	private enum SequenceStage
	{
		None,
		t,
		T,
		c,
		C
	}
	
	private SequenceStage mCurrentSequenceStage = SequenceStage.None;
	
	public void translatePhysicalCharacter(HardKeyboardAction action) 
	{
		final int primaryCode = action.getKeyCode(); 
		if (!action.isAltActive())
		{
			if ((mCurrentSequenceStage == SequenceStage.t) &&
				((primaryCode == KeyEvent.KEYCODE_S) || (primaryCode == KeyEvent.KEYCODE_C)))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				action.setNewKeyCode(1094);
				return;
			}
			
			if ((mCurrentSequenceStage == SequenceStage.T) &&
					((primaryCode == KeyEvent.KEYCODE_S) || (primaryCode == KeyEvent.KEYCODE_C)))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				action.setNewKeyCode(1062);
				return;
			}
			
			if ((mCurrentSequenceStage == SequenceStage.c) &&
					(primaryCode == KeyEvent.KEYCODE_H))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				action.setNewKeyCode(1095);
				return;
			}
			
			if ((mCurrentSequenceStage == SequenceStage.C) &&
					(primaryCode == KeyEvent.KEYCODE_H))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				action.setNewKeyCode(1063);
				return;
			}
			
			if (!action.isShiftActive())
			{
				if (primaryCode == KeyEvent.KEYCODE_T)
					mCurrentSequenceStage = SequenceStage.t;
				else if (primaryCode == KeyEvent.KEYCODE_C)
					mCurrentSequenceStage = SequenceStage.c;
				else
					mCurrentSequenceStage = SequenceStage.None;
				
				if (msPhysicalKeysMap.containsKey(primaryCode))
				{
					action.setNewKeyCode(msPhysicalKeysMap.get(primaryCode).intValue());
					return;
				}
				else
					return;
			}
			else
			{
				if (primaryCode == KeyEvent.KEYCODE_T)
					mCurrentSequenceStage = SequenceStage.T;
				else if (primaryCode == KeyEvent.KEYCODE_C)
					mCurrentSequenceStage = SequenceStage.C;
				else
					mCurrentSequenceStage = SequenceStage.None;
				
				if (msPhysicalShiftKeysMap.containsKey(primaryCode))
				{
					action.setNewKeyCode(msPhysicalShiftKeysMap.get(primaryCode).intValue());
					return;
				}
				else
					return;
			}
		}
	}
}
