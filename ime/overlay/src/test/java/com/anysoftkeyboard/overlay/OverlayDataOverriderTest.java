package com.anysoftkeyboard.overlay;

import android.content.ComponentName;
import android.graphics.Color;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class OverlayDataOverriderTest {

  private OverlayData overlay(int primaryColor, int darkPrimaryColor, int textColor) {
    return new OverlayDataImpl(primaryColor, darkPrimaryColor, 0, textColor, 0);
  }

  private OverlayDataOverrider mUnderTest;
  private OverlyDataCreator mOriginal;

  @Before
  public void setup() {
    mOriginal = Mockito.mock(OverlyDataCreator.class);
    OverlayData originalData = overlay(Color.GRAY, Color.DKGRAY, Color.WHITE);
    Mockito.doReturn(originalData).when(mOriginal).createOverlayData(Mockito.any());

    HashMap<String, OverlayData> overrides = new HashMap<>();
    overrides.put("com.example", overlay(Color.BLUE, 0, 0));

    mUnderTest = new OverlayDataOverrider(mOriginal, overrides);
  }

  @Test
  public void testReturnsOriginalIfNotInMap() {
    Assert.assertEquals(
        Color.GRAY,
        mUnderTest
            .createOverlayData(new ComponentName("com.example4", "Activity"))
            .getPrimaryColor());
    Mockito.verify(mOriginal).createOverlayData(new ComponentName("com.example4", "Activity"));
  }

  @Test
  public void testReturnsOverrideIfInMap() {
    Assert.assertEquals(
        Color.BLUE,
        mUnderTest
            .createOverlayData(new ComponentName("com.example", "Activity"))
            .getPrimaryColor());
    Mockito.verifyZeroInteractions(mOriginal);
  }
}
