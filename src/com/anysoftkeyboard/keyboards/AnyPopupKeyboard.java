package com.anysoftkeyboard.keyboards;

import java.util.HashSet;
import java.util.List;

import android.content.Context;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;

public class AnyPopupKeyboard extends AnyKeyboard {

	private int mAdditionalWidth = 0;
	private boolean mOneKeyPressPopup = true;
	
	public AnyPopupKeyboard(AnyKeyboardContextProvider askContext, Context context,//note: the context can be from a different package!
    		int xmlLayoutResId, 
    		final int maxWidth, final int keyHorizontalGap, final int rowVerticalGap)
	{
		super(askContext, context, xmlLayoutResId, -1);
		loadKeyboard(maxWidth, keyHorizontalGap, rowVerticalGap);
	}
	
	public AnyPopupKeyboard(AnyKeyboardContextProvider askContext, CharSequence popupCharacters, 
			final int maxWidth, final int keyHorizontalGap, final int rowVerticalGap)
	{
		super(askContext, askContext.getApplicationContext(), R.xml.popup);
		loadKeyboard(maxWidth, keyHorizontalGap, rowVerticalGap);
		
		List<Key> keys = getKeys();
		//now adding the popups
		Key baseKey = keys.get(0);
		Row row = baseKey.row;
		baseKey.codes = new int[]{(int)popupCharacters.charAt(0)};
		baseKey.codes = new int[]{(int)popupCharacters.charAt(0)};
		baseKey.edgeFlags = EDGE_LEFT;
		baseKey.label = String.valueOf(popupCharacters.charAt(0));
		int x = baseKey.width + row.defaultHorizontalGap;
		for(int popupCharIndex=1;popupCharIndex<popupCharacters.length();popupCharIndex++)
		{
			Key aKey = new Key(row);
			aKey.codes = new int[]{(int)popupCharacters.charAt(popupCharIndex)};
			aKey.label = String.valueOf(popupCharacters.charAt(popupCharIndex));
			aKey.x = x;
			aKey.y = 0;
			final int xOffset = aKey.width + row.defaultHorizontalGap;
			x += xOffset;
			mAdditionalWidth += xOffset;
			keys.add(aKey);
		}
		//adding edge flag to the last key
		keys.get(keys.size() - 1).edgeFlags += EDGE_RIGHT;
	}
	
	@Override
	public HashSet<Character> getSentenceSeparators() {
		return null;
	}
	
	@Override
	public int getMinWidth() {
		return super.getMinWidth() + mAdditionalWidth;
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

	public boolean isOneKeyEventPopup() {
		return mOneKeyPressPopup;
	}
	
	public void setIsOneKeyEventPopup(boolean oneKey)
	{
		mOneKeyPressPopup = oneKey;
	}
	
	@Override
	protected void addGenericRows(AnyKeyboardContextProvider askContext, Context context, int mode) {
		//no generic rows in popups, only in main keyboard
	}

}
