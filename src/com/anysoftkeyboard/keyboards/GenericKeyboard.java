package com.anysoftkeyboard.keyboards;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.addons.AddOn;

public class GenericKeyboard extends ExternalAnyKeyboard 
{
	private final boolean mDisableKeyPreviews;
	
	public GenericKeyboard(AnyKeyboardContextProvider context, int xmlLayoutResId, int nameResId, String prefKeyId, int mode, boolean disableKeyPreviews) 
	{
		super(context, context.getApplicationContext(), xmlLayoutResId, xmlLayoutResId, prefKeyId, nameResId, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, null, null, "", mode);
		setExtensionLayout(null);
		mDisableKeyPreviews = disableKeyPreviews;
	}
	
	public GenericKeyboard(AnyKeyboardContextProvider context, int xmlLayoutResId, int xmlLandscapeLayoutResId,  int nameResId, String prefKeyId, int mode) 
	{
		super(context, context.getApplicationContext(), xmlLayoutResId, xmlLandscapeLayoutResId, prefKeyId, nameResId, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, null, null, "", mode);
		setExtensionLayout(null);
		mDisableKeyPreviews = false;
	}
	
	public boolean disableKeyPreviews()
	{
		return mDisableKeyPreviews;
	}
}
