package com.anysoftkeyboard.keyboards;

import java.util.HashSet;
import java.util.List;

import android.content.Context;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;

public class AnyPopupKeyboard extends AnyKeyboard {

	private int mAdditionalWidth = 0;
	private boolean mOneKeyPressPopup = true;
	private static final HashSet<Character> msEmptySet = new HashSet<Character>(0);
	
	public AnyPopupKeyboard(AnyKeyboardContextProvider askContext, Context context,//note: the context can be from a different package!
    		int xmlLayoutResId, 
    		final KeyboardDimens keyboardDimens)
	{
		super(askContext, context, xmlLayoutResId, -1);
		loadKeyboard(keyboardDimens);
	}
	
	public AnyPopupKeyboard(AnyKeyboardContextProvider askContext, CharSequence popupCharacters, 
    		final KeyboardDimens keyboardDimens)
	{
		super(askContext, askContext.getApplicationContext(), R.xml.popup);
		
		loadKeyboard(keyboardDimens);
		
		final float rowVerticalGap = keyboardDimens.getRowVerticalGap();
    	final float keyHorizontalGap = keyboardDimens.getKeyHorizontalGap();
        
		List<Key> keys = getKeys();
		//now adding the popups
		final float y = rowVerticalGap;
		Key baseKey = keys.get(0);
		Row row = baseKey.row;
		baseKey.codes = new int[]{(int)popupCharacters.charAt(0)};
		baseKey.label = ""+popupCharacters.charAt(0);
		baseKey.edgeFlags |= EDGE_LEFT;
		float x = baseKey.width + row.defaultHorizontalGap;
		for(int popupCharIndex=1;popupCharIndex<popupCharacters.length();popupCharIndex++)
		{
			x += (keyHorizontalGap/2);
			
			Key aKey = new AnyKey(row, keyboardDimens);
			aKey.codes = new int[]{(int)popupCharacters.charAt(popupCharIndex)};
			aKey.label = ""+popupCharacters.charAt(popupCharIndex);
			aKey.x = (int)x;
			aKey.width -= keyHorizontalGap;//the gap is on both sides
			aKey.y = (int)y;
			final int xOffset = (int)(aKey.width + row.defaultHorizontalGap + (keyHorizontalGap/2));
			x += xOffset;
			mAdditionalWidth += xOffset;
			keys.add(aKey);
		}
		//adding edge flag to the last key
		keys.get(0).edgeFlags |= EDGE_LEFT;
		keys.get(keys.size() - 1).edgeFlags |= EDGE_RIGHT;
	}
	
	@Override
	public HashSet<Character> getSentenceSeparators() {
		return msEmptySet;
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
	protected void addGenericRows(AnyKeyboardContextProvider askContext, Context context, int mode, KeyboardDimens keyboardDimens) {
		//no generic rows in popups, only in main keyboard
	}

	@Override
	protected boolean keyboardSupportShift() {
		return true;
	}
}
