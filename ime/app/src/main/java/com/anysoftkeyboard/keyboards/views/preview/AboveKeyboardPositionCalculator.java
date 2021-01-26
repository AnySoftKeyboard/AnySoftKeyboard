package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Point;
import android.graphics.Rect;
import com.anysoftkeyboard.keyboards.Keyboard;

public class AboveKeyboardPositionCalculator implements PositionCalculator {
    @Override
    public Point calculatePositionForPreview(
            Keyboard.Key key, PreviewPopupTheme theme, int[] windowOffset) {
        Point point = new Point(key.x + windowOffset[0], windowOffset[1]);

        Rect padding = new Rect();
        theme.getPreviewKeyBackground().getPadding(padding);

        point.offset((key.width / 2), padding.bottom - theme.getVerticalOffset());

        return point;
    }
}
