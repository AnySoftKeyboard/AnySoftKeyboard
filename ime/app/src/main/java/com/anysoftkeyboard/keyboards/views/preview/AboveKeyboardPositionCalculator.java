package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Point;
import android.view.View;
import com.anysoftkeyboard.keyboards.Keyboard;

public class AboveKeyboardPositionCalculator implements PositionCalculator {
    @Override
    public Point calculatePositionForPreview(
            Keyboard.Key key, View keyboardView, PreviewPopupTheme theme, int[] windowOffset) {
        // center of the top of the keyboard
        return new Point(
                keyboardView.getLeft() + keyboardView.getWidth() / 2, keyboardView.getTop());
    }
}
