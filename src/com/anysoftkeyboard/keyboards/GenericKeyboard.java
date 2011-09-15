package com.anysoftkeyboard.keyboards;

import com.anysoftkeyboard.AnyKeyboardContextProvider;

public class GenericKeyboard extends ExternalAnyKeyboard 
{
	private final boolean mDisableKeyPreviews;
	
	public GenericKeyboard(AnyKeyboardContextProvider context, int xmlLayoutResId, int nameResId, String prefKeyId, int mode, boolean disableKeyPreviews) 
	{
		super(context, context.getApplicationContext(), xmlLayoutResId, xmlLayoutResId, prefKeyId, nameResId, -1, -1, null, null, mode);
		setExtensionLayout(null);
		mDisableKeyPreviews = disableKeyPreviews;
	}
	
	public GenericKeyboard(AnyKeyboardContextProvider context, int xmlLayoutResId, int xmlLandscapeLayoutResId,  int nameResId, String prefKeyId, int mode) 
	{
		super(context, context.getApplicationContext(), xmlLayoutResId, xmlLandscapeLayoutResId, prefKeyId, nameResId, -1, -1, null, null, mode);
		setExtensionLayout(null);
		mDisableKeyPreviews = false;
	}
	
	public boolean disableKeyPreviews()
	{
		return mDisableKeyPreviews;
	}
}
