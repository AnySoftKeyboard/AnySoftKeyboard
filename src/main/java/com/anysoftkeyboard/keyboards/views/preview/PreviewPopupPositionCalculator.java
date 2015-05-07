package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Point;
import android.view.View;

import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.utils.Log;

public class PreviewPopupPositionCalculator {
	public static Point calculatePositionForPreview(Keyboard.Key key, PreviewPopupTheme theme, View keyboardView) {
		int[] offsetInWindow = new int[]{0, 0};
		keyboardView.getLocationInWindow(offsetInWindow);
		Log.d("PreviewPopupPositionCalculator", "getLocationInWindow (%d, %d)", offsetInWindow[0], offsetInWindow[1]);
		int[] windowLocation = new int[2];
		keyboardView.getLocationOnScreen(windowLocation);
		Log.d("PreviewPopupPositionCalculator", "getLocationOnScreen (%d, %d)", windowLocation[0], windowLocation[1]);

		Point point = new Point(key.x + (key.width/2), key.y + offsetInWindow[1] + key.height);
		Log.d("PreviewPopupPositionCalculator", "point (%d, %d)", point.x, point.y);
		return point;
	}
}
