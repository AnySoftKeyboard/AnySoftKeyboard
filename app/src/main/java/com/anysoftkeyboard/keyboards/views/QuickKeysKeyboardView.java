package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.anysoftkeyboard.keyboards.AnyKeyboard;

/**
 * This class will draw a keyboard and will make sure that
 * the keys are split into rows as per the space in the physical view
 */
public class QuickKeysKeyboardView extends AnyKeyboardViewWithMiniKeyboard {

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
    protected void setKeyboard(AnyKeyboard keyboard, float verticalCorrection) {
        super.setKeyboard(keyboard, 0/*no vertical correct here*/);
    }

    public void setKeyboard(AnyKeyboard keyboard) {
        super.setKeyboard(keyboard, 0/*no vertical correct here*/);
    }
}
