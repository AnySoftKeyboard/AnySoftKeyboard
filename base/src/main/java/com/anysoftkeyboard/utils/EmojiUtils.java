package com.anysoftkeyboard.utils;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.graphics.PaintCompat;

public class EmojiUtils {

    private static StringBuilder msStringBuilder = new StringBuilder(16);

    private static char SKIN_TONE_PREFIX_CHAR = '\uD83C';

    public enum SkinTone {

        // Fitzpatrick_1('\uDFFA'),//does not exist
        Fitzpatrick_2('\uDFFB'),
        Fitzpatrick_3('\uDFFC'),
        Fitzpatrick_4('\uDFFD'),
        Fitzpatrick_5('\uDFFE'),
        Fitzpatrick_6('\uDFFF');

        private final char mModifier;

        SkinTone(char modifier) {
            mModifier = modifier;
        }
    }

    public static boolean isLabelOfEmoji(CharSequence label) {
        if (label.length() == 0) return false;
        final char hs = label.charAt(0);

        return 0xd800 <= hs && hs <= 0xdbff;
    }

    public static boolean containsSkinTone(CharSequence text, SkinTone skinTone) {
        for (int charIndex = 0; charIndex < text.length() - 1; charIndex++) {
            final char c = text.charAt(charIndex);
            if (c == SKIN_TONE_PREFIX_CHAR && text.charAt(charIndex + 1) == skinTone.mModifier) {
                return true;
            }
        }

        return false;
    }

    public static CharSequence removeSkinTone(
            @NonNull CharSequence text, @NonNull SkinTone skinTone) {
        msStringBuilder.setLength(0);

        for (int charIndex = 0; charIndex < text.length(); charIndex++) {
            final char c = text.charAt(charIndex);
            if (c == SKIN_TONE_PREFIX_CHAR
                    && charIndex < (text.length() - 1)
                    && text.charAt(charIndex + 1) == skinTone.mModifier) {
                charIndex++; // skipping this and next
            } else {
                msStringBuilder.append(c);
            }
        }

        return msStringBuilder.toString();
    }

    public static boolean isRenderable(@NonNull Paint paint, @NonNull CharSequence text) {
        return !isLabelOfEmoji(text) || PaintCompat.hasGlyph(paint, text.toString());
    }
}
