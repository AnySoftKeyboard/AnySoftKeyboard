package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.util.AttributeSet;

public class SizeSensitiveAnyKeyboardView extends AnyKeyboardViewBase {
    public SizeSensitiveAnyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SizeSensitiveAnyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getKeyboard() != null) {
            mKeyboardDimens.setKeyboardMaxWidth(w - getPaddingLeft() - getPaddingRight());
            getKeyboard().onKeyboardViewWidthChanged(w, oldw);
            setKeyboard(getKeyboard());
        }
    }
}
