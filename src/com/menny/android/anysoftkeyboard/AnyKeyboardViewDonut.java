package com.menny.android.anysoftkeyboard;

import android.content.Context;
import android.util.AttributeSet;

public class AnyKeyboardViewDonut extends AnyKeyboardView
{
	public AnyKeyboardViewDonut(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void requestRedraw() {
		super.invalidateAllKeys();
	}    	
}