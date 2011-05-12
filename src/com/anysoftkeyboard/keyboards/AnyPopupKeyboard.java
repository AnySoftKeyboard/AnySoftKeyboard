package com.anysoftkeyboard.keyboards;

import android.content.Context;

import com.anysoftkeyboard.AnyKeyboardContextProvider;

public class AnyPopupKeyboard extends AnyKeyboard {

	public AnyPopupKeyboard(AnyKeyboardContextProvider askContext, Context context,//note: the context can be from a different package!
    		int xmlLayoutResId)
	{
		super(askContext, context, xmlLayoutResId, -1);
	}
	
	public AnyPopupKeyboard(AnyKeyboardContextProvider askContext, Context context,//note: the context can be from a different package!
    		int layoutTemplateResId, CharSequence popupCharacters, int columns, int horizontalPadding)
	{
		super(askContext, context, layoutTemplateResId, popupCharacters, columns, horizontalPadding);
	}
	
	@Override
	public String getDefaultDictionaryLocale() {
		return null;
	}

	@Override
	protected int getKeyboardNameResId() {
		return -1;
	}

	@Override
	public int getKeyboardIconResId() {
		return -1;
	}

	@Override
	public String getKeyboardPrefId() {
		return "keyboard_popup";
	}

}
