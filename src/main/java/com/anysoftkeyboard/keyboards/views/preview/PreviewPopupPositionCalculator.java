package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import com.anysoftkeyboard.keyboards.Keyboard;

public class PreviewPopupPositionCalculator {
	public static Point calculatePositionForPreview(Keyboard.Key key, View keyboardView, PreviewPopupTheme theme) {
		int[] offsetInWindow = new int[]{0, 0};
		keyboardView.getLocationInWindow(offsetInWindow);

		Rect padding = new Rect();
		theme.getPreviewKeyBackground().getPadding(padding);

		return new Point(key.x + (key.width / 2), key.y + offsetInWindow[1] + key.height + padding.bottom);
	}
}
