package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.utils.Log;

/**
 * This class will draw a keyboard and will make sure that
 * the keys are split into rows as per the space in the physical view
 */
public class QuickKeysKeyboardView extends AnyKeyboardBaseView {
	public QuickKeysKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPreviewEnabled(false);
	}

	public QuickKeysKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setPreviewEnabled(false);
	}


	@SuppressWarnings("deprecation")
	@Override
	public void setBackgroundDrawable(Drawable background) {
		//no background in this class
		super.setBackgroundDrawable(null);
	}

	@Override
	public void setBackground(Drawable background) {
		//no background in this class
		super.setBackground(null);
	}

	@Override
	public void setKeyboard(AnyKeyboard keyboard, float verticalCorrection) {
		//first, amending keyboard to make sure it is one row
		//this is done since the quick-text popup only supports that
		Log.d("FFFF", "Current keyboard width is %d", keyboard.getMinWidth());
		Log.d("FFFF", "phase 1");
		int xOffset = 0;
		for (Keyboard.Key key : keyboard.getKeys()) {
			Log.d("FFFF", "Key from [%d,%d]", key.x, key.y);
			key.y = 0;
			key.x = xOffset;
			xOffset += key.width;
			Log.d("FFFF", "Key to [%d,%d] with offset %d", key.x, key.y, xOffset);
		}
		//fixing up the keyboard, so it will fit nicely in the width
		final int maxX = getThemedKeyboardDimens().getKeyboardMaxWidth();
		int currentY = 0;
		int xSub = 0;
		Log.d("FFFF", "phase 2. maxX is %d", maxX);
		for (Keyboard.Key key : keyboard.getKeys()) {
			key.y = currentY;
			key.x -= xSub;
			if (key.x + key.width > maxX) {
				currentY += key.height;
				xSub += key.x;
				Log.d("FFFF", "Reached end of row. New currentY is %d, xSub %d", currentY, xSub);
				key.y = currentY;
				key.x = 0;
			}
			Log.d("FFFF", "Key final at [%d,%d]", key.x, key.y);
		}
		keyboard.resetDimensions();
		Log.d("FFFF", "New keyboard width is %d", keyboard.getMinWidth());
		super.setKeyboard(keyboard, 0/*no correction in this popup, the view handles the clicks*/);
	}
}
