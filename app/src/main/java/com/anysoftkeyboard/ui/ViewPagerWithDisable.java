package com.anysoftkeyboard.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerWithDisable extends ViewPager {
    public ViewPagerWithDisable(Context context) {
        super(context);
    }

    public ViewPagerWithDisable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isEnabled())
            return super.onTouchEvent(ev);
        else
            return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isEnabled())
            return super.onInterceptTouchEvent(ev);
        else
            return false;
    }
}
