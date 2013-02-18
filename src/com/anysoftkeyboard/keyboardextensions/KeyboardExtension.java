package com.anysoftkeyboard.keyboardextensions;

import com.anysoftkeyboard.addons.AddOnImpl;

import android.content.Context;

public class KeyboardExtension extends AddOnImpl {
	
	public static final int TYPE_BOTTOM = 1;
	public static final int TYPE_TOP = 2;
	public static final int TYPE_EXTENSION = 3;
	public static final int TYPE_HIDDEN_BOTTOM = 4;
	
	private final int mKeyboardResId;
	private final int mExtensionType;
	
	public KeyboardExtension(Context askContext, Context packageContext, String id, int nameResId, int keyboardResId, int type,
			String description, int sortIndex) {
		super(askContext, packageContext, id, nameResId, description, sortIndex);
		mKeyboardResId = keyboardResId;
		mExtensionType = type;
	}

	public int getKeyboardResId() {
		return mKeyboardResId;
	}

	public int getExtensionType() {
		return mExtensionType;
	}
}