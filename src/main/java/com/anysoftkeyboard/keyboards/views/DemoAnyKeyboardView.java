package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Will render the keyboard view but will not provide ANY interactivity.
 */
public class DemoAnyKeyboardView extends AnyKeyboardView {

    public DemoAnyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DemoAnyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent me) {
        //not handling ANY touch event.
        return false;
    }
}
