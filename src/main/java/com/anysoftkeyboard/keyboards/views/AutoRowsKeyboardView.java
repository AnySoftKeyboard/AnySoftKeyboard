package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * This class will draw a keyboard and will make sure that
 * the keys are split into rows as per the space in the physical view
 */
public class AutoRowsKeyboardView extends AnyKeyboardBaseView {
	public AutoRowsKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AutoRowsKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
}
