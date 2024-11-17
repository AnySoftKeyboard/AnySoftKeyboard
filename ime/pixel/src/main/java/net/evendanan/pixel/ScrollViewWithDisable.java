package net.evendanan.pixel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ScrollViewWithDisable extends ScrollView {
  public ScrollViewWithDisable(Context context) {
    super(context);
  }

  public ScrollViewWithDisable(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ScrollViewWithDisable(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ScrollViewWithDisable(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
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
