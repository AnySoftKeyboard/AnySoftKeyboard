package com.anysoftkeyboard.overlay;

import android.graphics.Color;

import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class OverlayDataTest {

    private OverlayData mUnderTest;

    @Before
    public void setup() {
        mUnderTest = new OverlayData();
    }

    @Test
    public void isValidIfTextColorIsDifferentThanBackground() {
        Assert.assertTrue(overlay(Color.GRAY, Color.GRAY, Color.BLACK).isValid());
        Assert.assertTrue(overlay(Color.GRAY, Color.BLACK, Color.BLUE).isValid());
    }

    @Test
    public void isNotValidIfTextIsSame() {
        Assert.assertFalse(overlay(Color.GRAY, Color.GRAY, Color.GRAY).isValid());
        Assert.assertFalse(overlay(Color.BLACK, Color.BLUE, Color.BLACK).isValid());
        Assert.assertFalse(overlay(Color.MAGENTA, Color.WHITE, Color.WHITE).isValid());
    }

    private OverlayData overlay(int primaryColor, int darkPrimaryColor, int textColor) {
        mUnderTest.setPrimaryColor(primaryColor);
        mUnderTest.setPrimaryDarkColor(darkPrimaryColor);
        mUnderTest.setPrimaryTextColor(textColor);
        return mUnderTest;
    }
}