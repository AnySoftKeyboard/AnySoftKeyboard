package com.anysoftkeyboard.gesturetyping;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface GestureTypingPathDraw {
    void draw(Canvas canvas);

    void handleTouchEvent(MotionEvent event);

    interface OnInvalidateCallback {
        void invalidate();
    }
}
