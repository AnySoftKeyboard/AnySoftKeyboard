package com.anysoftkeyboard.ime;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardPredictiveBackTest extends AnySoftKeyboardBaseTest {

  @Test
  public void testNullHelperOnPreApi34() {
    Assert.assertNull(mAnySoftKeyboardUnderTest.getDismissAnimationHelper());
  }

  @Test
  public void testResetOnFinishInputViewAndWindowHidden() {
    final KeyboardViewContainerView container = mAnySoftKeyboardUnderTest.getInputViewContainer();
    Assert.assertNotNull(container);
    container.layout(0, 0, 1000, 500);
    container.setPredictiveBackProgress(0.5f);

    mAnySoftKeyboardUnderTest.onWindowHidden();
    Assert.assertEquals(0f, container.getTranslationY(), 0.01f);
    Assert.assertEquals(1.0f, container.getAlpha(), 0.01f);
    Assert.assertEquals(0f, container.getPredictiveBackProgress(), 0.01f);
  }

  @Test
  public void testResetOnHideWindow() {
    final KeyboardViewContainerView container = mAnySoftKeyboardUnderTest.getInputViewContainer();
    Assert.assertNotNull(container);
    container.layout(0, 0, 1000, 500);
    container.setPredictiveBackProgress(0.7f);

    mAnySoftKeyboardUnderTest.hideWindow();
    Assert.assertEquals(0f, container.getTranslationY(), 0.01f);
    Assert.assertEquals(1.0f, container.getAlpha(), 0.01f);
    Assert.assertEquals(0f, container.getPredictiveBackProgress(), 0.01f);
  }
}
