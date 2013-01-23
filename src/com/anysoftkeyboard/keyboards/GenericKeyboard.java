package com.anysoftkeyboard.keyboards;

import android.content.Context;

import com.anysoftkeyboard.addons.AddOn;

public class GenericKeyboard extends ExternalAnyKeyboard 
{
	private final boolean mDisableKeyPreviews;
	
	public GenericKeyboard(Context askContext, int xmlLayoutResId, String name, String prefKeyId, int mode, boolean disableKeyPreviews) 
	{
		super(askContext, askContext, xmlLayoutResId, xmlLayoutResId, prefKeyId, name, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, null, null, "", mode);
		setExtensionLayout(null);
		mDisableKeyPreviews = disableKeyPreviews;
	}
	
	public GenericKeyboard(Context askContext, int xmlLayoutResId, int xmlLandscapeLayoutResId, String name, String prefKeyId, int mode) 
	{
		super(askContext, askContext, xmlLayoutResId, xmlLandscapeLayoutResId, prefKeyId, name, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, null, null, "", mode);
		setExtensionLayout(null);
		mDisableKeyPreviews = false;
	}
	
	public boolean disableKeyPreviews()
	{
		return mDisableKeyPreviews;
	}
}
