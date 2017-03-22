package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Typeface;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PreviewPopupThemeTest {
    private PreviewPopupTheme mUnderTest;

    @Before
    public void setUp() throws Exception {
        mUnderTest = new PreviewPopupTheme();
    }

    @Test
    public void testInitialState() {
        Assert.assertEquals(PreviewPopupTheme.ANIMATION_STYLE_APPEAR, mUnderTest.getPreviewAnimationType());
        Assert.assertEquals(Typeface.DEFAULT, mUnderTest.getKeyStyle());
    }

    @Test
    public void testPreviewAnimationTypes() {
        mUnderTest.setPreviewAnimationType(PreviewPopupTheme.ANIMATION_STYLE_NONE);
        Assert.assertEquals(PreviewPopupTheme.ANIMATION_STYLE_NONE, mUnderTest.getPreviewAnimationType());
        mUnderTest.setPreviewAnimationType(PreviewPopupTheme.ANIMATION_STYLE_EXTEND);
        Assert.assertEquals(PreviewPopupTheme.ANIMATION_STYLE_EXTEND, mUnderTest.getPreviewAnimationType());
    }

}