package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.anysoftkeyboard.keyboards.AnyKeyboard;

/**
 * This class will draw a keyboard and will make sure that
 * the keys are split into rows as per the space in the physical view
 */
public class QuickKeysKeyboardView extends SizeSensitiveAnyKeyboardView {

    private boolean mDoubleKeyFontSize = false;

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

    public void setKeyboard(AnyKeyboard keyboard, boolean doubleKeyFontSize) {
        mDoubleKeyFontSize = doubleKeyFontSize;
        setKeyboard(keyboard, 0/*no vertical correct here*/);
    }

    @Override
    public void setKeyboard(AnyKeyboard keyboard, float verticalCorrection) {
        super.setKeyboard(keyboard, 0/*no vertical correct here*/);
    }

    @Override
    protected void setPaintForLabelText(Paint paint) {
        super.setPaintForLabelText(paint);
        if (mDoubleKeyFontSize) {
            //here, in the quick-text keyboard, the since characters are much bigger
            paint.setTextSize(paint.getTextSize() * 2.0f);
        }
    }

    @Override
    protected void setPaintToKeyText(Paint paint) {
        super.setPaintToKeyText(paint);
        if (mDoubleKeyFontSize) {
            //here, in the quick-text keyboard, the since characters are much bigger
            paint.setTextSize(paint.getTextSize() * 2.0f);
        }
    }
}
