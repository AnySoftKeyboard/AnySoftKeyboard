package com.anysoftkeyboard.utils;

import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.core.graphics.PaintCompat;
import emoji.utils.JavaEmojiUtils;

public class EmojiUtils {

  public static boolean isLabelOfEmoji(@NonNull CharSequence label) {
    return JavaEmojiUtils.isLabelOfEmoji(label);
  }

  public static boolean containsSkinTone(
      @NonNull CharSequence text, @NonNull JavaEmojiUtils.SkinTone skinTone) {
    return JavaEmojiUtils.containsSkinTone(text, skinTone);
  }

  public static CharSequence removeSkinTone(
      @NonNull CharSequence text, @NonNull JavaEmojiUtils.SkinTone skinTone) {
    return JavaEmojiUtils.removeSkinTone(text, skinTone);
  }

  public static boolean containsGender(
      @NonNull CharSequence text, @NonNull JavaEmojiUtils.Gender gender) {
    return JavaEmojiUtils.containsGender(text, gender);
  }

  public static CharSequence removeGender(
      @NonNull CharSequence text, @NonNull JavaEmojiUtils.Gender gender) {
    return JavaEmojiUtils.removeGender(text, gender);
  }

  public static boolean isRenderable(@NonNull Paint paint, @NonNull CharSequence text) {
    return !isLabelOfEmoji(text) || PaintCompat.hasGlyph(paint, text.toString());
  }
}
