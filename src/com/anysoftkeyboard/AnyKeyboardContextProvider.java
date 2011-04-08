package com.anysoftkeyboard;


import android.content.Context;
import android.content.SharedPreferences;

public interface AnyKeyboardContextProvider 
{
	Context getApplicationContext();
	void deleteLastCharactersFromInput(int lenght);
	void appendCharactersToInput(CharSequence text);
	SharedPreferences getSharedPreferences();
	//void showToastMessage(int resId, boolean forShortTime);
	//void performLengthyOperation(int textResId, final Runnable thingToDo);
}
