package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Point;
import com.anysoftkeyboard.keyboards.Keyboard;

public interface PositionCalculator {
  Point calculatePositionForPreview(Keyboard.Key key, PreviewPopupTheme theme, int[] windowOffset);
}
