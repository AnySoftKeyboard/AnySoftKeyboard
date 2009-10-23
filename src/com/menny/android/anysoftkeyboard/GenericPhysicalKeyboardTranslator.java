package com.menny.android.anysoftkeyboard;


import com.menny.android.anysoftkeyboard.keyboards.HardKeyboardSequenceHandler;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardAction;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

class GenericPhysicalKeyboardTranslator implements HardKeyboardTranslator
{
	private final HardKeyboardSequenceHandler mKeyboardSequence;
	private final AnyKeyboardContextProvider mInputHandler;
	
	public GenericPhysicalKeyboardTranslator(AnyKeyboardContextProvider inputHandler)
	{
		mInputHandler = inputHandler;
		mKeyboardSequence = new HardKeyboardSequenceHandler();
		mKeyboardSequence.addQwertyTranslation("qwertyuiopasdfghjklzxcvbnm");
	}
	
	public void translatePhysicalCharacter(HardKeyboardAction action) {
		//On regular character, and no ALT, I'll "translate" to the same key.
		if (!action.isAltActive())
		{
			char translated = mKeyboardSequence.getSequenceCharacter(action.getKeyCode(), mInputHandler);
			if (action.isShiftActive())
				translated = Character.toUpperCase(translated);
			
			if (translated > 0)
				action.setNewKeyCode(translated);
		}		
	}
}
