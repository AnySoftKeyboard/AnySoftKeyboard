package com.anysoftkeyboard.keyboards.views;

import android.graphics.Point;
import android.view.View;
import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.preview.PreviewPopupTheme;

public class PopupKeyboardPositionCalculator {

    public static Point calculatePositionForPopupKeyboard(
            Keyboard.Key key,
            View keyboardView,
            AnyKeyboardViewBase popupKeyboardView,
            PreviewPopupTheme theme,
            int[] windowOffset) {
        Point point = new Point(key.x + windowOffset[0], key.y + windowOffset[1]);
        point.offset(0, theme.getVerticalOffset());
        // moving the keyboard to the left, so the first key will be above the initial X
        point.offset(-popupKeyboardView.getPaddingLeft(), 0);
        // moving the keyboard down, so the bottom padding will not push the keys too high
        point.offset(0, popupKeyboardView.getPaddingBottom());
        // moving the keyboard its height up
        point.offset(0, -popupKeyboardView.getMeasuredHeight());

        boolean shouldMirrorKeys = false;
        // now we need to see the the popup is positioned correctly:
        // 1) if the right edge is off the screen, then we'll try to put the right edge over the
        // popup key
        if (point.x + popupKeyboardView.getMeasuredWidth() > keyboardView.getMeasuredWidth()) {
            int mirroredX = key.x + windowOffset[0] - popupKeyboardView.getMeasuredWidth();
            // adding the width of the key - now the right most popup key is above the finger
            mirroredX += key.width;
            mirroredX += popupKeyboardView.getPaddingRight();
            shouldMirrorKeys = true;
            point = new Point(mirroredX, point.y);
        }
        // 2) if it took too much to adjust the X, then forget about it.
        if (point.x < 0) {
            point.offset(-point.x, 0);
            shouldMirrorKeys = false;
        }

        if (shouldMirrorKeys) ((AnyPopupKeyboard) popupKeyboardView.getKeyboard()).mirrorKeys();

        return point;
    }
}
