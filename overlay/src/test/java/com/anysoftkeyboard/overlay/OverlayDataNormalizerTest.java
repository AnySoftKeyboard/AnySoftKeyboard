package com.anysoftkeyboard.overlay;

import android.content.ComponentName;
import android.graphics.Color;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class OverlayDataNormalizerTest {

    private OverlyDataCreator mOriginal;
    private OverlayDataNormalizer mUnderTest;
    private ComponentName mTestComponent;

    @Before
    public void setup() {
        mOriginal = Mockito.mock(OverlyDataCreator.class);
        mUnderTest = new OverlayDataNormalizer(mOriginal, 96, false);
        mTestComponent = new ComponentName("com.example", "com.example.Activity");
    }

    @Test
    public void testReturnsOriginalIfAllOkay() {
        OverlayData original = setupOriginal(Color.GRAY, Color.DKGRAY, Color.WHITE);
        final OverlayData fixed = mUnderTest.createOverlayData(mTestComponent);
        Assert.assertSame(original, fixed);
        Assert.assertTrue(fixed.isValid());
        Assert.assertEquals(Color.GRAY, fixed.getPrimaryColor());
        Assert.assertEquals(Color.DKGRAY, fixed.getPrimaryDarkColor());
        Assert.assertEquals(Color.WHITE, fixed.getPrimaryTextColor());
    }

    @Test
    public void testReturnsOriginalIfInvalid() {
        OverlayData original = setupOriginal(Color.GRAY, Color.GRAY, Color.GRAY);
        final OverlayData fixed = mUnderTest.createOverlayData(mTestComponent);
        Assert.assertSame(original, fixed);
        Assert.assertFalse(fixed.isValid());
    }

    @Test
    public void testReturnsFixedIfInvalidButWasAskedToFix() {
        mUnderTest = new OverlayDataNormalizer(mOriginal, 96, true);
        OverlayData original = setupOriginal(Color.GRAY, Color.GRAY, Color.GRAY);
        final OverlayData fixed = mUnderTest.createOverlayData(mTestComponent);
        Assert.assertSame(original, fixed);
        Assert.assertTrue(fixed.isValid());
        Assert.assertEquals(Color.GRAY, fixed.getPrimaryColor());
        Assert.assertEquals(Color.GRAY, fixed.getPrimaryDarkColor());
        Assert.assertEquals(Color.WHITE, fixed.getPrimaryTextColor());
        Assert.assertEquals(Color.LTGRAY, fixed.getSecondaryTextColor());
    }

    @Test
    public void testReturnsFixedIfTextIsTooClose() {
        OverlayData original = setupOriginal(Color.GRAY, Color.DKGRAY, Color.LTGRAY);
        final OverlayData fixed = mUnderTest.createOverlayData(mTestComponent);
        Assert.assertSame(original, fixed);
        Assert.assertTrue(fixed.isValid());
        Assert.assertEquals(Color.GRAY, fixed.getPrimaryColor());
        Assert.assertEquals(Color.DKGRAY, fixed.getPrimaryDarkColor());
        Assert.assertEquals(Color.WHITE, fixed.getPrimaryTextColor());
        Assert.assertEquals(Color.LTGRAY, fixed.getSecondaryTextColor());
    }

    @Test
    public void testReturnsFixedToWhiteIfDarkIfTextIsTooClose() {
        OverlayData original = setupOriginal(Color.DKGRAY, Color.DKGRAY, Color.GRAY);
        final OverlayData fixed = mUnderTest.createOverlayData(mTestComponent);
        Assert.assertSame(original, fixed);
        Assert.assertTrue(fixed.isValid());
        Assert.assertEquals(Color.DKGRAY, fixed.getPrimaryColor());
        Assert.assertEquals(Color.DKGRAY, fixed.getPrimaryDarkColor());
        Assert.assertEquals(Color.WHITE, fixed.getPrimaryTextColor());
        Assert.assertEquals(Color.LTGRAY, fixed.getSecondaryTextColor());
    }

    @Test
    public void testReturnsFixedToBlackIfLightIfTextIsTooClose() {
        OverlayData original = setupOriginal(Color.LTGRAY, Color.DKGRAY, Color.GRAY);
        final OverlayData fixed = mUnderTest.createOverlayData(mTestComponent);
        Assert.assertSame(original, fixed);
        Assert.assertTrue(fixed.isValid());
        Assert.assertEquals(Color.LTGRAY, fixed.getPrimaryColor());
        Assert.assertEquals(Color.DKGRAY, fixed.getPrimaryDarkColor());
        Assert.assertEquals(Color.BLACK, fixed.getPrimaryTextColor());
        Assert.assertEquals(Color.DKGRAY, fixed.getSecondaryTextColor());
    }

    @Test
    public void testLuminance() {
        Assert.assertEquals(255, OverlayDataNormalizer.luminance(Color.WHITE));
        Assert.assertEquals(0, OverlayDataNormalizer.luminance(Color.BLACK));
        Assert.assertEquals(136, OverlayDataNormalizer.luminance(Color.GRAY));
        Assert.assertEquals(19, OverlayDataNormalizer.luminance(Color.BLUE));
        Assert.assertEquals(183, OverlayDataNormalizer.luminance(Color.GREEN));
        Assert.assertEquals(55, OverlayDataNormalizer.luminance(Color.RED));
        Assert.assertEquals(73, OverlayDataNormalizer.luminance(Color.MAGENTA));
    }

    private OverlayData setupOriginal(int primary, int darkPrimary, int textColor) {
        OverlayData data = new OverlayData();
        data.setPrimaryColor(primary);
        data.setPrimaryDarkColor(darkPrimary);
        data.setPrimaryTextColor(textColor);

        Mockito.doReturn(data).when(mOriginal).createOverlayData(Mockito.any());

        return data;
    }
}
