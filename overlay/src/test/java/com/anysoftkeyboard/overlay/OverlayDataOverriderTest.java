package com.anysoftkeyboard.overlay;

import android.content.ComponentName;
import android.graphics.Color;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.HashMap;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class OverlayDataOverriderTest {

    private OverlayDataOverrider mUnderTest;
    private OverlyDataCreator mOriginal;

    @Before
    public void setup() {
        mOriginal = Mockito.mock(OverlyDataCreator.class);
        OverlayData originalData = new OverlayData();
        originalData.setPrimaryColor(Color.GRAY);
        originalData.setPrimaryDarkColor(Color.DKGRAY);
        originalData.setPrimaryTextColor(Color.WHITE);
        Mockito.doReturn(originalData).when(mOriginal).createOverlayData(Mockito.any());

        HashMap<String, OverlayData> overrides = new HashMap<>();
        overrides.put("com.example", new OverlayData());
        overrides.get("com.example").setPrimaryColor(Color.BLUE);

        mUnderTest = new OverlayDataOverrider(mOriginal, overrides);
    }

    @Test
    public void testReturnsOriginalIfNotInMap() {
        Assert.assertEquals(Color.GRAY, mUnderTest.createOverlayData(new ComponentName("com.example4", "Activity")).getPrimaryColor());
        Mockito.verify(mOriginal).createOverlayData(new ComponentName("com.example4", "Activity"));
    }

    @Test
    public void testReturnsOverrideIfInMap() {
        Assert.assertEquals(Color.BLUE, mUnderTest.createOverlayData(new ComponentName("com.example", "Activity")).getPrimaryColor());
        Mockito.verifyZeroInteractions(mOriginal);
    }
}