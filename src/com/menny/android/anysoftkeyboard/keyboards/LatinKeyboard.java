package com.menny.android.anysoftkeyboard.keyboards;

import android.content.res.Configuration;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary.Language;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class LatinKeyboard extends AnyKeyboard implements HardKeyboardTranslator//this class implements the HardKeyboardTranslator interface in an empty way, the physical keyboard is Latin...
{
	protected LatinKeyboard(AnyKeyboardContextProvider context, int keyboardLayoutId, int keyboardNameId, Dictionary.Language defaultDictionaryLanguage) 
	{
		super(context, keyboardLayoutId, true, keyboardNameId, true, defaultDictionaryLanguage);
	}
	
	//this class implements the HardKeyboardTranslator interface in an empty way, the physical keyboard is Latin...
	public void translatePhysicalCharacter(HardKeyboardAction action) 
	{
		//I'll do nothing, so the caller will use defaults.
	}

	protected void setPopupKeyChars(Key aKey) 
	{
		if ((aKey.codes != null) && (aKey.codes.length > 0))
        {
			switch((char)aKey.codes[0])
			{
				case 'a':
					aKey.popupCharacters = "\u00e0\u00e2\u00e1\u00e4\u00e3\u00e6\u00e5\u0105";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'c':
					aKey.popupCharacters = "\u00e7\u010d\u0107";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'd':
					aKey.popupCharacters = "\u0111";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'e':
					aKey.popupCharacters = "\u00e9\u00e8\u00ea\u00eb\u0119\u20ac";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'i':
					aKey.popupCharacters = "\u00ee\u00ef\u00ed\u00ec\u0142";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'o':
					aKey.popupCharacters = "\u00f4\u00f6\u00f2\u00f3\u00f5\u0153\u00f8";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 's':
					aKey.popupCharacters = "\u00a7\u0161\u015b\u00df";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'u':
					aKey.popupCharacters = "\u00f9\u00fb\u00fc\u00fa";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'n':
					aKey.popupCharacters = "\u00f1";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'y':
					aKey.popupCharacters = "\u00ff\u00fd";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'z':
					aKey.popupCharacters = "\u017e\u017c";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				default:
					super.setPopupKeyChars(aKey);
			}
        }
	}
}

class FinnishSwedishKeyboard extends LatinKeyboard
{
	private final Configuration mConfig;
	public FinnishSwedishKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, R.xml.fin_swedish_qwerty, R.string.finnish_swedish_keyboard, Language.Swedish);
		mConfig = context.getApplicationContext().getResources().getConfiguration();
	}
	
	@Override
	public Language getDefaultDictionaryLanguage() {
		if (mConfig.locale.toString().startsWith("fi"))
			return Language.Finnish;
		
		return super.getDefaultDictionaryLanguage();
	}
}
