//package com.menny.android.anysoftkeyboard;
//
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.KeyCharacterMap.KeyData;
//
//public class TranslatedKeyEvent extends KeyEvent {
//	private final int mTranslatedUnitcodeKey;
//	
//	public TranslatedKeyEvent(int translatedUnicode, KeyEvent originalEvent)
//	{
//		super(originalEvent);
//		mTranslatedUnitcodeKey = translatedUnicode;
//	}
//	
//	@Override
//	public int getUnicodeChar() {
//		if (AnySoftKeyboard.getDEBUG()) Log.d("AnySoftKeyboard:TranslatedKeyEvent", "getUnicodeChar: "+mTranslatedUnitcodeKey);
//		return mTranslatedUnitcodeKey;
//	}
//	
//	@Override
//	public char getDisplayLabel() {
//		if (AnySoftKeyboard.getDEBUG()) Log.d("AnySoftKeyboard:TranslatedKeyEvent", "getDisplayLabel: "+mTranslatedUnitcodeKey);
//		return (char)mTranslatedUnitcodeKey;
//	}
//	
//	@Override
//	public char getNumber() {
//		if (AnySoftKeyboard.getDEBUG()) Log.d("AnySoftKeyboard:TranslatedKeyEvent", "getNumber: "+mTranslatedUnitcodeKey);
//		return (char)mTranslatedUnitcodeKey;
//	}
//	
//	@Override
//	public int getUnicodeChar(int meta) {
//		if (AnySoftKeyboard.getDEBUG()) Log.d("AnySoftKeyboard:TranslatedKeyEvent", "getUnicodeChar(int meta): "+mTranslatedUnitcodeKey);
//		return (char)mTranslatedUnitcodeKey;
//	}
//	
//	@Override
//	public int describeContents() {
//		if (AnySoftKeyboard.getDEBUG()) Log.d("AnySoftKeyboard:TranslatedKeyEvent", "describeContents");
//		// TODO Auto-generated method stub
//		return super.describeContents();
//	}
//	
//	@Override
//	public boolean getKeyData(KeyData results) {
//		if (AnySoftKeyboard.getDEBUG()) Log.d("AnySoftKeyboard:TranslatedKeyEvent", "getKeyData");
//		return super.getKeyData(results);
//	}
//	
//	@Override
//	public char getMatch(char[] chars) {
//		if (AnySoftKeyboard.getDEBUG()) Log.d("AnySoftKeyboard:TranslatedKeyEvent", "getMatch(char[] chars)");
//		return super.getMatch(chars);
//	}
//	
//	@Override
//	public char getMatch(char[] chars, int modifiers) {
//		if (AnySoftKeyboard.getDEBUG()) Log.d("AnySoftKeyboard:TranslatedKeyEvent", "getMatch(char[] chars, int modifiers)");
//		// TODO Auto-generated method stub
//		return super.getMatch(chars, modifiers);
//	}
//	
//	@Override
//	public boolean isPrintingKey() {
//		if (AnySoftKeyboard.getDEBUG()) Log.d("AnySoftKeyboard:TranslatedKeyEvent", "isPrintingKey");
//		return super.isPrintingKey();
//	}
//	
//	@Override
//	protected Object clone() throws CloneNotSupportedException {
//		if (AnySoftKeyboard.getDEBUG()) Log.d("AnySoftKeyboard:TranslatedKeyEvent", "clone");
//		// TODO Auto-generated method stub
//		return super.clone();
//	}
//}
