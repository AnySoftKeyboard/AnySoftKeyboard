package com.menny.android.anysoftkeyboard;

import android.content.Context;

public interface AnyKeyboardContextProvider 
{
	Context getApplicationContext();
	void deleteLastCharactersFromInput(int lenght);
	void appendCharactersToInput(CharSequence text);
}
