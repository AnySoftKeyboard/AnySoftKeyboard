package com.anysoftkeyboard.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.viewpager.widget.ViewPager;

public class ViewPagerWithDisable extends ViewPager {
  public ViewPagerWithDisable(Context context) {
    super(context);
  }

  public ViewPagerWithDisable(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (isEnabled()) return super.onTouchEvent(ev);
    else return false;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (isEnabled()) return super.onInterceptTouchEvent(ev);
    else return false;
  }
}
