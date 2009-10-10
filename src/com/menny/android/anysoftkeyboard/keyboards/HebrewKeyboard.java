package com.menny.android.anysoftkeyboard.keyboards;

import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class HebrewKeyboard extends AnyKeyboard implements HardKeyboardTranslator
{	
	private static final HardKeyboardSequenceHandler msKeySequenceHandler;
	static
	{
		msKeySequenceHandler = new HardKeyboardSequenceHandler();
		msKeySequenceHandler.addQwertyTranslation("\u05e5\u05e3\u05e7\u05e8\u05d0\u05d8\u05d5\u05df\u05dd\u05e4\u05e9\u05d3\u05d2\u05db\u05e2\u05d9\u05d7\u05dc\u05da\u05d6\u05e1\u05d1\u05d4\u05e0\u05de\u05e6");
		msKeySequenceHandler.addSequence(new int[]{KeyEvent.KEYCODE_COMMA}, '\u05ea');
	}
	public HebrewKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, KeyboardFactory.HEBREW_KEYBOARD, R.xml.heb_qwerty, true, R.string.heb_keyboard, false, Dictionary.Language.Hebrew, R.drawable.he);
	}
	
	@Override
	public boolean isLetter(char keyValue) {
		//Hebrew also support "
		return super.isLetter(keyValue) || (keyValue == '\"');
	}
	
	@Override
	protected void setPopupKeyChars(Key aKey) {
		if (aKey.codes[0] == 1513)
		{
			aKey.popupResId = R.xml.popup;
			aKey.popupCharacters = "\u20aa";
		}
		else
			super.setPopupKeyChars(aKey);
	}
	
	public void translatePhysicalCharacter(HardKeyboardAction action) 
	{
		if (action.isShiftActive() && (action.getKeyCode() == KeyEvent.KEYCODE_COMMA))
		{
			//see issue 129
			//this is a special case - we support comma by giving 
			//shift+comma, since question itself is TET Hebrew letter.
			action.setNewKeyCode((char)',');
		}
		else if (action.isAltActive() && (action.getKeyCode() == KeyEvent.KEYCODE_COMMA))
		{
			//see issue 129
			//this is a special case - we support comma by giving 
			//alt+comma, since question itself is TET Hebrew letter.
			action.setNewKeyCode((char)'?');
		}
		else if (action.isAltActive() && (action.getKeyCode() == KeyEvent.KEYCODE_A))
		{//New Israeli Shekel (ALT+HEBREW SHIN) - issue 90
			action.setNewKeyCode((char)'\u20aa');
		}
		else if ((!action.isAltActive()) && (!action.isShiftActive()))
		{
			final char translated = msKeySequenceHandler.getSequenceCharacter(action.getKeyCode(), getKeyboardContext());
			if (translated != 0)
				action.setNewKeyCode(translated);
		}
	}
}
