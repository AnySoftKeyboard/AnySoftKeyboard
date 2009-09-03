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
		super(context, R.xml.heb_qwerty, true, R.string.heb_keyboard, false, Dictionary.Language.Hebrew);
	}

	@Override
	public int getKeyboardIcon() 
	{
		return R.drawable.he;
	}
	
	@Override
	protected String getDomainsKeyText() {
		return ".co.il";
	}
	
	@Override
	protected int getDomainsPopupId() {
		return R.xml.popup_domains_il;
	}
	
	@Override
	protected int getDomainsKeyDrawable() {
		// TODO Auto-generated method stub
		//should use my own.
		return super.getDomainsKeyDrawable();
	}
	
	public char translatePhysicalCharacter(int primaryCode, int metaState) 
	{
//		if (((metaState&KeyEvent.META_ALT_ON) == 0) &&
//    	    ((metaState&KeyEvent.META_SHIFT_ON) == 0))
//		{
//			if (msPhysicalKeysMap.containsKey(primaryCode))
//				return (char)msPhysicalKeysMap.get(primaryCode).intValue();
//			else
//				return 0;
//		}
//		else if (((metaState&KeyEvent.META_ALT_ON) != 0) &&
//				 (primaryCode == KeyEvent.KEYCODE_COMMA))
//		{
//			//this is a special case - we support comma by giving 
//			//ALT+comma, since comma itself is TET Hebrew letter.
//			return (char)',';
//		}
//		else if (((metaState&KeyEvent.META_SHIFT_ON) != 0) &&
//				 (primaryCode == KeyEvent.KEYCODE_COMMA))
//		{
//			//this is a special case - we support comma by giving 
//			//shift+comma, since question itself is TET Hebrew letter.
//			return (char)'?';
//		}
//		else
//		{
//			return 0;
//		}
		if (((metaState&KeyEvent.META_SHIFT_ON) != 0) &&
				 (primaryCode == KeyEvent.KEYCODE_COMMA))
		{
			//this is a special case - we support comma by giving 
			//shift+comma, since question itself is TET Hebrew letter.
			return (char)',';
		}
		else
			return msKeySequenceHandler.getSequenceCharacter(primaryCode, getKeyboardContext());
	}
}
