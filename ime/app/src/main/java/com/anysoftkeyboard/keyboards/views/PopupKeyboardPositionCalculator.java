package com.anysoftkeyboard.keyboards.views;

import android.graphics.Point;
import android.view.View;
import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.preview.PreviewPopupTheme;

public class PopupKeyboardPositionCalculator {

    private static final String TAG = "ASKPositionCalculator";

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

        final boolean shouldMirrorKeys;
        switch (popupKeyboardView.getKeyboard().reverse) {
            case Keyboard.REVERSE_ALWAYS:
                shouldMirrorKeys = true;
                break;
            case Keyboard.REVERSE_NEVER:
                shouldMirrorKeys = false;
                break;
            default: // should be Keyboard.REVERSE_AUTO:
                // the popup will be reversed if and only if it is in the right hand of the key:
                if (key.x + key.width / 2 > keyboardView.getMeasuredWidth() / 2) {
                    int mirroredX = key.x + windowOffset[0] - popupKeyboardView.getMeasuredWidth();
                    // adding the width of the key - now the right most popup key is above the
                    // finger
                    mirroredX += key.width;
                    mirroredX += popupKeyboardView.getPaddingRight();
                    shouldMirrorKeys = true;
                    point = new Point(mirroredX, point.y);
                } else {
                    shouldMirrorKeys = false;
                }
        }

        if (shouldMirrorKeys) ((AnyPopupKeyboard) popupKeyboardView.getKeyboard()).mirrorKeys();

        return point;
    }
}
