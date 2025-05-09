package com.anysoftkeyboard.utils;

import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.core.graphics.PaintCompat;

public class EmojiUtils {
  public static boolean isLabelOfEmoji(@NonNull CharSequence label) {
    if (label.length() == 0) return false;
    final char hs = label.charAt(0);

    return 0xd800 <= hs && hs <= 0xdbff;
  }

  public static boolean isRenderable(@NonNull Paint paint, @NonNull CharSequence text) {
    return !isLabelOfEmoji(text) || PaintCompat.hasGlyph(paint, text.toString());
  }
}
