package com.menny.android.anysoftkeyboard.keyboards;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class GenericKeyboard extends AnyKeyboard 
{
	private final int mNameResId;
	private final String mPrefId;
	
	public GenericKeyboard(AnyKeyboardContextProvider context, int xmlLayoutResId, int nameResId, String prefKeyId) 
	{
		super(context, context.getApplicationContext(), xmlLayoutResId);
		mNameResId = nameResId;
		mPrefId = prefKeyId;
	}

	@Override
	public String getDefaultDictionaryLocale() {
		return null;
	}

	@Override
	public int getKeyboardIconResId() {
		return -1;
	}

	@Override
	protected int getKeyboardNameResId() {
		return mNameResId;
	}

	@Override
	public String getKeyboardPrefId() {
		return mPrefId;
	}
}
