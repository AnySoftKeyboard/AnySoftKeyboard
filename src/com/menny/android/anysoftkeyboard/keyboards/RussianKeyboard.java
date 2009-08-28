package com.menny.android.anysoftkeyboard.keyboards;

import java.util.HashMap;

import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class RussianKeyboard extends AnyKeyboard implements HardKeyboardTranslator
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
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_E, 1077);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_F, 1092);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_G, 1075);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_H, 1095);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_I, 1080);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_J, 1081);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_K, 1082);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_L, 1083);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_M, 1084);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_N, 1085);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_O, 1086);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_P, 1087);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_Q, 1103);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_R, 1088);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_S, 1089);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_T, 1090);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_U, 1091);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_V, 1074);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_W, 1096);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_X, 1093);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_Y, 1099);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_Z, 1079);
    	msPhysicalKeysMap.put(KeyEvent.KEYCODE_COMMA, 1100);
    	
    	msPhysicalShiftKeysMap = new HashMap<Integer, Integer>();
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_A, 1040);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_B, 1041);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_C, 1062);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_D, 1044);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_E, 1045);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_F, 1060);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_G, 1043);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_H, 1063);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_I, 1048);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_J, 1049);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_K, 1050);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_L, 1051);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_M, 1052);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_N, 1053);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_O, 1054);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_P, 1055);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_Q, 1071);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_R, 1056);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_S, 1057);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_T, 1058);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_U, 1059);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_V, 1042);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_W, 1064);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_X, 1061);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_Y, 1067);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_Z, 1047);
    	msPhysicalShiftKeysMap.put(KeyEvent.KEYCODE_COMMA, 1068);
    }

	public RussianKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, R.xml.russian_qwerty, false, R.string.ru_keyboard, false, Dictionary.Language.None);
	}

	@Override
	public int getKeyboardIcon() 
	{
		return R.drawable.ru;
	}
	
	private enum SequenceStage
	{
		None,
		u,
		U,
		w,
		W,
		e,
		E,
		z,
		Z,
		comma,
		COMMA,
	}
	
	private SequenceStage mCurrentSequenceStage = SequenceStage.None;
	
	public char translatePhysicalCharacter(int primaryCode, int metaState) 
	{
		if ((metaState&KeyEvent.META_ALT_ON) == 0)
		{
			if ((mCurrentSequenceStage == SequenceStage.u) &&
					(primaryCode == KeyEvent.KEYCODE_U))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				return (char)1102;
			} else if ((mCurrentSequenceStage == SequenceStage.w) &&
					(primaryCode == KeyEvent.KEYCODE_W))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				return (char)1097;
			} else if ((mCurrentSequenceStage == SequenceStage.e) &&
					(primaryCode == KeyEvent.KEYCODE_E))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				return (char)1101;
			} else if ((mCurrentSequenceStage == SequenceStage.z) &&
					(primaryCode == KeyEvent.KEYCODE_H))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				return (char)1078;
			}  else if ((mCurrentSequenceStage == SequenceStage.comma) &&
					(primaryCode == KeyEvent.KEYCODE_COMMA))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				return (char)1098;
			}  else  
			//upper case
			if ((mCurrentSequenceStage == SequenceStage.U) &&
					(primaryCode == KeyEvent.KEYCODE_U))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				return (char)1070;
			} else if ((mCurrentSequenceStage == SequenceStage.W) &&
					(primaryCode == KeyEvent.KEYCODE_W))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				return (char)1065;
			} else if ((mCurrentSequenceStage == SequenceStage.E) &&
					(primaryCode == KeyEvent.KEYCODE_E))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				return (char)1069;
			} else if ((mCurrentSequenceStage == SequenceStage.Z) &&
					(primaryCode == KeyEvent.KEYCODE_H))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				return (char)1046;
			} else if ((mCurrentSequenceStage == SequenceStage.COMMA) &&
					(primaryCode == KeyEvent.KEYCODE_COMMA))
			{
				mCurrentSequenceStage = SequenceStage.None;
				super.getKeyboardContext().deleteLastCharactersFromInput(1);
				return (char)1066;
			}
			
			//ok, it is none of the above states (cause they have RETURN)
			
			if ((metaState&KeyEvent.META_SHIFT_ON) == 0)
			{
				if (primaryCode == KeyEvent.KEYCODE_E)
					mCurrentSequenceStage = SequenceStage.e;
				else if (primaryCode == KeyEvent.KEYCODE_U)
					mCurrentSequenceStage = SequenceStage.u;
				else if (primaryCode == KeyEvent.KEYCODE_W)
					mCurrentSequenceStage = SequenceStage.w;
				else if (primaryCode == KeyEvent.KEYCODE_Z)
					mCurrentSequenceStage = SequenceStage.z;
				else if (primaryCode == KeyEvent.KEYCODE_COMMA)
					mCurrentSequenceStage = SequenceStage.comma;
				else
					mCurrentSequenceStage = SequenceStage.None;
				
				if (msPhysicalKeysMap.containsKey(primaryCode))
					return (char)msPhysicalKeysMap.get(primaryCode).intValue();
				else
					return 0;
			}
			else
			{
				if (primaryCode == KeyEvent.KEYCODE_E)
					mCurrentSequenceStage = SequenceStage.E;
				else if (primaryCode == KeyEvent.KEYCODE_U)
					mCurrentSequenceStage = SequenceStage.U;
				else if (primaryCode == KeyEvent.KEYCODE_W)
					mCurrentSequenceStage = SequenceStage.W;
				else if (primaryCode == KeyEvent.KEYCODE_Z)
					mCurrentSequenceStage = SequenceStage.Z;
				else if (primaryCode == KeyEvent.KEYCODE_COMMA)
					mCurrentSequenceStage = SequenceStage.COMMA;
				else
					mCurrentSequenceStage = SequenceStage.None;
				
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
