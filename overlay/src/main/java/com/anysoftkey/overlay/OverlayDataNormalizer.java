package com.anysoftkey.overlay;

import android.content.ComponentName;
import android.graphics.Color;
import android.support.annotation.FloatRange;

public class OverlayDataNormalizer implements OverlyDataCreator {
    private final OverlyDataCreator mOriginal;
    private final float mDarkPrimaryFixFactor;

    public OverlayDataNormalizer(OverlyDataCreator original, @FloatRange(from = 0f, to = 1f) float darkPrimaryFixFactor) {
        mOriginal = original;
        mDarkPrimaryFixFactor = darkPrimaryFixFactor;
    }

    @Override
    public OverlayData createOverlayData(ComponentName remoteApp) {
        final OverlayData original = mOriginal.createOverlayData(remoteApp);
        if (original.getPrimaryColor() == original.getPrimaryDarkColor() && original.getPrimaryDarkColor() != 0) {
            original.setPrimaryDarkColor(Color.argb(
                    Color.alpha(original.getPrimaryDarkColor()),
                    (int) (Color.red(original.getPrimaryColor()) * mDarkPrimaryFixFactor),
                    (int) (Color.red(original.getPrimaryColor()) * mDarkPrimaryFixFactor),
                    (int) (Color.red(original.getPrimaryColor()) * mDarkPrimaryFixFactor)));
        }
        return original;
    }
}
