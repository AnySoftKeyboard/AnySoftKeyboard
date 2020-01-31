package com.anysoftkeyboard.overlay;

import android.content.ComponentName;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.VisibleForTesting;

public class OverlayDataNormalizer implements OverlyDataCreator {

    private static final int GRAY_LUM = luminance(Color.GRAY);

    private final OverlyDataCreator mOriginal;
    private final int mRequiredTextColorDiff;
    private final boolean mFixInvalid;

    public OverlayDataNormalizer(
            OverlyDataCreator original,
            @IntRange(from = 1, to = 250) int textColorDiff,
            boolean fixInvalid) {
        mOriginal = original;
        mRequiredTextColorDiff = textColorDiff;
        mFixInvalid = fixInvalid;
    }

    @Override
    public OverlayData createOverlayData(ComponentName remoteApp) {
        final OverlayData original = mOriginal.createOverlayData(remoteApp);
        if (original.isValid() || mFixInvalid) {
            final int backgroundLuminance = luminance(original.getPrimaryColor());
            final int diff = backgroundLuminance - luminance(original.getPrimaryTextColor());
            if (mRequiredTextColorDiff > Math.abs(diff)) {
                if (backgroundLuminance > GRAY_LUM) {
                    // closer to white, text will be black
                    original.setPrimaryTextColor(Color.BLACK);
                    original.setSecondaryTextColor(Color.DKGRAY);
                } else {
                    original.setPrimaryTextColor(Color.WHITE);
                    original.setSecondaryTextColor(Color.LTGRAY);
                }
            }
        }
        return original;
    }

    /** Code taken (mostly) from AOSP Color class. */
    @VisibleForTesting
    static int luminance(@ColorInt int color) {
        double r = Color.red(color) * 0.2126;
        double g = Color.green(color) * 0.7152;
        double b = Color.blue(color) * 0.0722;

        return (int) Math.ceil(r + g + b);
    }
}
