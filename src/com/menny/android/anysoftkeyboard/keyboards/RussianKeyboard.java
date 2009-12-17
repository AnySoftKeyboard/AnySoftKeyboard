package com.menny.android.anysoftkeyboard.keyboards;

import android.content.res.Configuration;
import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

public class RussianKeyboard extends AnyKeyboard implements HardKeyboardTranslator
{
	private static final HardKeyboardSequenceHandler msKeySequenceHandler;
	static
	{
		msKeySequenceHandler = new HardKeyboardSequenceHandler();
		/*
		йцукенгшщзхъ
		фывапролджэ
		ячсмитьбю
		*/
		msKeySequenceHandler.addQwertyTranslation("\u0439\u0446\u0443\u043a\u0435\u043d\u0433\u0448\u0449\u0437\u0444\u044b\u0432\u0430\u043f\u0440\u043e\u043b\u0434\u044f\u0447\u0441\u043c\u0438\u0442\u044c");
		/*
		"oo" = "х"
		"pp" = "ь"
		"kk" = "ж"
		"ll" = "э"
		"mm" = "б"
		",," = "ю"
		0x445 0x44C 0x436 0x44D 0x431 0x44E
		 */
		msKeySequenceHandler.addSequence(new int[]{KeyEvent.KEYCODE_O, KeyEvent.KEYCODE_O}, (char)0x445);
		msKeySequenceHandler.addSequence(new int[]{KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_P}, (char)0x44C);
		msKeySequenceHandler.addSequence(new int[]{KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_K}, (char)0x436);
		msKeySequenceHandler.addSequence(new int[]{KeyEvent.KEYCODE_L, KeyEvent.KEYCODE_L}, (char)0x44D);
		msKeySequenceHandler.addSequence(new int[]{KeyEvent.KEYCODE_M, KeyEvent.KEYCODE_M}, (char)0x431);
		msKeySequenceHandler.addSequence(new int[]{KeyEvent.KEYCODE_COMMA, KeyEvent.KEYCODE_COMMA}, (char)0x44E);
	}
	public RussianKeyboard(AnyKeyboardContextProvider context) 
	{
		super(context, getKeyboardId(context));
	}
	
	private static int getKeyboardId(AnyKeyboardContextProvider context) 
	{
		//4 rows only in portrait mode only
		final boolean inPortraitMode = 
			(context.getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
		
		if (inPortraitMode && context.getSharedPreferences().getBoolean(KeyboardFactory.RU_KEYBOARD_4_ROWS, true))
			return R.xml.russian_qwerty_4_rows;
		else
			return R.xml.russian_qwerty;
	}

	public void translatePhysicalCharacter(HardKeyboardAction action) 
	{
		if (action.isAltActive())
			return;
		else
		{
			char translated = msKeySequenceHandler.getSequenceCharacter((char)action.getKeyCode(), getKeyboardContext());
			if (translated != 0)
			{
				if (action.isShiftActive())
					translated = Character.toUpperCase(translated);
				action.setNewKeyCode(translated);
			}
		}
	}
}
