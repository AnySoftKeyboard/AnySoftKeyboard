package com.anysoftkeyboard.saywhat;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.Keyboard;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class OnKeyWordHelperTest {

  @Test
  public void testHandlesNullKey() {
    final OnKeyWordHelper helper = new OnKeyWordHelper("test".toCharArray());
    Assert.assertFalse(helper.shouldShow(null));
  }

  @Test
  public void testHappyPath() {
    final OnKeyWordHelper helper = new OnKeyWordHelper("test".toCharArray());

    Keyboard.Key key = Mockito.mock(Keyboard.Key.class);

    Mockito.doReturn((int) 't').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 'e').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 's').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 't').when(key).getPrimaryCode();
    Assert.assertTrue(helper.shouldShow(key));

    Mockito.doReturn((int) 't').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 'e').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 's').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 't').when(key).getPrimaryCode();
    Assert.assertTrue(helper.shouldShow(key));
  }

  @Test
  public void testPathReset() {
    final OnKeyWordHelper helper = new OnKeyWordHelper("test");

    Keyboard.Key key = Mockito.mock(Keyboard.Key.class);

    Mockito.doReturn((int) 'b').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 't').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 'e').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 's').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 's').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 't').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 'e').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 's').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 't').when(key).getPrimaryCode();
    Assert.assertTrue(helper.shouldShow(key));
  }

  @Test
  public void testPathResetWithSameStart() {
    final OnKeyWordHelper helper = new OnKeyWordHelper("test".toCharArray());

    Keyboard.Key key = Mockito.mock(Keyboard.Key.class);
    Mockito.doReturn((int) 't').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 't').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 'e').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 's').when(key).getPrimaryCode();
    Assert.assertFalse(helper.shouldShow(key));
    Mockito.doReturn((int) 't').when(key).getPrimaryCode();
    Assert.assertTrue(helper.shouldShow(key));
  }
}
