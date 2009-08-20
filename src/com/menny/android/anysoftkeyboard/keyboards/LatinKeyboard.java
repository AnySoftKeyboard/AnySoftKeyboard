package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class LatinKeyboard extends AnyKeyboard implements HardKeyboardTranslator//this class implements the HardKeyboardTranslator interface in an empty way, the physical keyboard is Latin...
{
	protected LatinKeyboard(AnyKeyboardContextProvider context, int keyboardLayoutId, int keyboardNameId, Dictionary.Language defaultDictionaryLanguage) 
	{
		super(context, keyboardLayoutId, true, keyboardNameId, true, defaultDictionaryLanguage);
	}
		
	public char translatePhysicalCharacter(int keyCode, int metaKeys) 
	{
		//I'll return 0, so the caller will use defaults.
		return 0;
	}

	/*
	 * there are some keys which we'll like to expand, e.g.,
	 * lowercase:
		a: àâáäãæå
		e: éèêë
		u: ùûüú
		i: îïíì
		o: ôöòóõœø
		c: ç
		n: ñ
		y: ÿý
		s: ß§
		
		upper case:
		E: ÈÉÊË
		Y: ÝŸ
		U: ÛÙÛÜ
		I: ÎÌÏÍ
		O: ÒÓÔÖÕŒØ
		A: ÀÁÂÄÃÅÆ
		S: §
		C: Ç
		N: Ñ
	 */
//	@Override
//	protected void setKeyPopup(Key aKey, boolean shiftState) 
//	{
//		super.setKeyPopup(aKey, shiftState);
//	
//		if ((aKey.codes != null) && (aKey.codes.length > 0))
//        {
//			switch((char)aKey.codes[0])
//			{
//				case 'a':
//					if (shiftState)
//						aKey.popupCharacters = "ÀÁÂÄÃÅÆ";
//					else
//						aKey.popupCharacters = "àâáäãæå";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//				case 'c':
//					if (shiftState)
//						aKey.popupCharacters = "ÇČĆ";
//					else
//						aKey.popupCharacters = "çčć";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//				case 'd':
//					if (shiftState)
//						aKey.popupCharacters = "Đ";
//					else
//						aKey.popupCharacters = "đ";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//				case 'e':
//					if (shiftState)
//						aKey.popupCharacters = "ÈÉÊË€";
//					else
//						aKey.popupCharacters = "éèêë€";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//				case 'i':
//					if (shiftState)
//						aKey.popupCharacters = "ÎÌÏÍ";
//					else
//						aKey.popupCharacters = "îïíì";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//				case 'o':
//					if (shiftState)
//						aKey.popupCharacters = "ÒÓÔÖÕŒØ";
//					else
//						aKey.popupCharacters = "ôöòóõœø";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//				case 's':
//					if (shiftState)
//						aKey.popupCharacters = "ß§Š";
//					else
//						aKey.popupCharacters = "§š";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//				case 'u':
//					if (shiftState)
//						aKey.popupCharacters = "ÛÙÛÜ";
//					else
//						aKey.popupCharacters = "ùûüú";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//				case 'n':
//					if (shiftState)
//						aKey.popupCharacters = "Ñ";
//					else
//						aKey.popupCharacters = "ñ";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//				case 'y':
//					if (shiftState)
//						aKey.popupCharacters = "ÝŸ";
//					else
//						aKey.popupCharacters = "ÿý";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//				case 'z':
//					if (shiftState)
//						aKey.popupCharacters = "Ž";
//					else
//						aKey.popupCharacters = "ž";
//					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
//					break;
//			}
//        }
//	}

	protected void setPopupKeyChars(Key aKey) 
	{
		if ((aKey.codes != null) && (aKey.codes.length > 0))
        {
			switch((char)aKey.codes[0])
			{
				case 'a':
					aKey.popupCharacters = "àâáäãæå";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'c':
					aKey.popupCharacters = "çčć";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'd':
					aKey.popupCharacters = "đ";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'e':
					aKey.popupCharacters = "éèêë€";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'i':
					aKey.popupCharacters = "îïíì";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'o':
					aKey.popupCharacters = "ôöòóõœø";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 's':
					aKey.popupCharacters = "§š";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'u':
					aKey.popupCharacters = "ùûüú";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'n':
					aKey.popupCharacters = "ñ";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'y':
					aKey.popupCharacters = "ÿý";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'z':
					aKey.popupCharacters = "ž";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				default:
					super.setPopupKeyChars(aKey);
			}
        }
	}
}
