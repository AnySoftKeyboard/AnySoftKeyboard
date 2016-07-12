package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import com.anysoftkeyboard.keyboards.Keyboard;

public class AboveKeyPositionCalculator implements PositionCalculator {
	@Override
	public Point calculatePositionForPreview(Keyboard.Key key, View keyboardView, PreviewPopupTheme theme, int[] windowOffset) {
		Point point = new Point(key.x + windowOffset[0], key.y + windowOffset[1]);

		Rect padding = new Rect();
		theme.getPreviewKeyBackground().getPadding(padding);

		point.offset((key.width / 2), key.height + padding.bottom);

		return point;
	}
}
