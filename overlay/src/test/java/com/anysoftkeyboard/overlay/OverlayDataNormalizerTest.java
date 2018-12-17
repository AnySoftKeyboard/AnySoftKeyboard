package com.anysoftkeyboard.overlay;

import android.content.ComponentName;
import android.graphics.Color;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlayDataNormalizer;
import com.anysoftkeyboard.overlay.OverlyDataCreator;

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
        mUnderTest = new OverlayDataNormalizer(mOriginal, 0.7f);
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
    public void testReturnsFixedIfPrimaryAndDarkAreTheSame() {
        OverlayData original = setupOriginal(Color.GRAY, Color.GRAY, Color.WHITE);
        Assert.assertTrue(original.isValid());
        final OverlayData fixed = mUnderTest.createOverlayData(mTestComponent);
        Assert.assertSame(original, fixed);
        Assert.assertEquals(Color.GRAY, fixed.getPrimaryColor());
        Assert.assertEquals(95, Color.red(fixed.getPrimaryDarkColor()));
        Assert.assertEquals(95, Color.green(fixed.getPrimaryDarkColor()));
        Assert.assertEquals(95, Color.blue(fixed.getPrimaryDarkColor()));
        //sanity
        Assert.assertEquals(136, Color.blue(Color.GRAY));
        Assert.assertEquals(Color.WHITE, fixed.getPrimaryTextColor());
    }

    @Test
    public void testReturnsOriginalIfPrimaryAndDarkAreTheSameAndAreZero() {
        OverlayData original = setupOriginal(Color.BLACK, Color.BLACK, Color.WHITE);
        final OverlayData fixed = mUnderTest.createOverlayData(mTestComponent);
        Assert.assertSame(original, fixed);
        Assert.assertTrue(original.isValid());
        Assert.assertEquals(Color.BLACK, fixed.getPrimaryColor());
        Assert.assertEquals(Color.BLACK, fixed.getPrimaryDarkColor());
        Assert.assertEquals(Color.WHITE, fixed.getPrimaryTextColor());
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