package com.anysoftkeyboard;


import com.anysoftkeyboard.keyboards.KeyboardSwitcher;

import android.content.Context;
import android.content.SharedPreferences;

public interface AnyKeyboardContextProvider 
{
	Context getApplicationContext();
	void deleteLastCharactersFromInput(int lenght);
	SharedPreferences getSharedPreferences();
	KeyboardSwitcher getKeyboardSwitcher();
}
