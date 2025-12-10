package com.anysoftkeyboard.keyboards.views;

import android.graphics.Color;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlayDataImpl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class CandidateViewTest {

    @Test
    public void test_whenOverlayHasTransparentBlack_thenNormalizationIsNotApplied() {
        OverlayData overlay = new OverlayDataImpl();
        OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

        Assert.assertEquals("The overlay should not have been normalized, so the original " +
                        "PrimaryTextColor should have been retained", Color.TRANSPARENT,
                normalizedOverlay.getPrimaryTextColor());
        Assert.assertEquals("The overlay should not have been normalized, so the original " +
                "SecondaryTextColor should have been retained", Color.TRANSPARENT,
                normalizedOverlay.getSecondaryTextColor());
    }

    @Test
    public void test_whenOverlayHasAllDarkColors_thenNormalizationToLightTextIsApplied() {
        OverlayData overlay = new OverlayDataImpl(Color.BLACK, Color.BLACK, Color.BLACK,
                Color.BLACK, Color.BLACK);
        OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

        Assert.assertEquals("The overlay should have been normalized to dark background with " +
                        "light text", Color.WHITE, normalizedOverlay.getPrimaryTextColor());
        Assert.assertEquals("The overlay should have been normalized to dark background with " +
                        "light text", Color.LTGRAY, normalizedOverlay.getSecondaryTextColor());
    }

    @Test
    public void test_whenOverlayHasAllLightColors_thenNormalizationToDarkTextIsApplied() {
        OverlayData overlay = new OverlayDataImpl(Color.WHITE, Color.WHITE, Color.WHITE,
                Color.WHITE, Color.WHITE);
        OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

        Assert.assertEquals("The overlay should have been normalized to light background with " +
                        "dark text", Color.BLACK, normalizedOverlay.getPrimaryTextColor());
        Assert.assertEquals("The overlay should have been normalized to light background with " +
                        "dark text", Color.DKGRAY, normalizedOverlay.getSecondaryTextColor());
    }
}