package com.anysoftkeyboard.keyboards.views;

import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class MiniKeyboardActionListenerTest {

  private MiniKeyboardActionListener mUnderTest;
  private OnKeyboardActionListener mMockParentListener;
  private Runnable mMockKeyboardDismissAction;

  @Before
  public void setUp() throws Exception {
    mMockParentListener = Mockito.mock(OnKeyboardActionListener.class);
    mMockKeyboardDismissAction = Mockito.mock(Runnable.class);
    mUnderTest =
        new MiniKeyboardActionListener(() -> mMockParentListener, mMockKeyboardDismissAction);
    Mockito.verifyZeroInteractions(mMockParentListener, mMockKeyboardDismissAction);
  }

  @Test
  public void testOnKey() {
    final AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
    final int[] nearByKeyCodes = {3};
    mUnderTest.onKey(1, key, 2, nearByKeyCodes, true);
    final InOrder inOrder = Mockito.inOrder(mMockParentListener, mMockKeyboardDismissAction);
    inOrder
        .verify(mMockParentListener)
        .onKey(
            Mockito.eq(1),
            Mockito.same(key),
            Mockito.eq(2),
            Mockito.same(nearByKeyCodes),
            Mockito.eq(true));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testOnKeyOnEnter() {
    final AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
    final int[] nearByKeyCodes = {3};
    mUnderTest.onKey(KeyCodes.ENTER, key, 2, nearByKeyCodes, true);
    final InOrder inOrder = Mockito.inOrder(mMockParentListener, mMockKeyboardDismissAction);
    inOrder
        .verify(mMockParentListener)
        .onKey(
            Mockito.eq(KeyCodes.ENTER),
            Mockito.same(key),
            Mockito.eq(2),
            Mockito.same(nearByKeyCodes),
            Mockito.eq(true));
    inOrder.verify(mMockKeyboardDismissAction).run();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testOnKeyOnShot() {
    final AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
    final int[] nearByKeyCodes = {3};
    mUnderTest.setInOneShot(true);
    mUnderTest.onKey(1, key, 2, nearByKeyCodes, true);
    final InOrder inOrder = Mockito.inOrder(mMockParentListener, mMockKeyboardDismissAction);
    inOrder
        .verify(mMockParentListener)
        .onKey(
            Mockito.eq(1),
            Mockito.same(key),
            Mockito.eq(2),
            Mockito.same(nearByKeyCodes),
            Mockito.eq(true));
    inOrder.verify(mMockKeyboardDismissAction).run();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testOnKeyOnShotButDelete() {
    final AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
    final int[] nearByKeyCodes = {3};
    mUnderTest.setInOneShot(true);
    mUnderTest.onKey(KeyCodes.DELETE, key, 2, nearByKeyCodes, true);
    final InOrder inOrder = Mockito.inOrder(mMockParentListener, mMockKeyboardDismissAction);
    inOrder
        .verify(mMockParentListener)
        .onKey(
            Mockito.eq(KeyCodes.DELETE),
            Mockito.same(key),
            Mockito.eq(2),
            Mockito.same(nearByKeyCodes),
            Mockito.eq(true));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testOnMultiTapStarted() {
    mUnderTest.onMultiTapStarted();
    final InOrder inOrder = Mockito.inOrder(mMockParentListener, mMockKeyboardDismissAction);
    inOrder.verify(mMockParentListener).onMultiTapStarted();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testOnMultiTapEnded() {
    mUnderTest.onMultiTapEnded();
    final InOrder inOrder = Mockito.inOrder(mMockParentListener, mMockKeyboardDismissAction);
    inOrder.verify(mMockParentListener).onMultiTapEnded();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testOnText() {
    final AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
    final String text = "text";
    mUnderTest.onText(key, text);
    final InOrder inOrder = Mockito.inOrder(mMockParentListener, mMockKeyboardDismissAction);
    inOrder.verify(mMockParentListener).onText(Mockito.same(key), Mockito.same(text));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testOnTextOneShot() {
    final AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
    final String text = "text";
    mUnderTest.setInOneShot(true);
    mUnderTest.onText(key, text);
    final InOrder inOrder = Mockito.inOrder(mMockParentListener, mMockKeyboardDismissAction);
    inOrder.verify(mMockParentListener).onText(Mockito.same(key), Mockito.same(text));
    inOrder.verify(mMockKeyboardDismissAction).run();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testOnTyping() {
    final AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
    final String text = "text";
    mUnderTest.onTyping(key, text);
    final InOrder inOrder = Mockito.inOrder(mMockParentListener, mMockKeyboardDismissAction);
    inOrder.verify(mMockParentListener).onTyping(Mockito.same(key), Mockito.same(text));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testOnTypingOneShot() {
    final AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
    final String text = "text";
    mUnderTest.setInOneShot(true);
    mUnderTest.onTyping(key, text);
    final InOrder inOrder = Mockito.inOrder(mMockParentListener, mMockKeyboardDismissAction);
    inOrder.verify(mMockParentListener).onTyping(Mockito.same(key), Mockito.same(text));
    inOrder.verify(mMockKeyboardDismissAction).run();
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testOnCancel() {
    mUnderTest.onCancel();
    Mockito.verify(mMockKeyboardDismissAction).run();
    Mockito.verifyNoMoreInteractions(mMockKeyboardDismissAction);
    Mockito.verifyZeroInteractions(mMockParentListener);
  }

  @Test
  public void testOnSwipeLeft() {
    mUnderTest.onSwipeLeft(true);
    Mockito.verifyZeroInteractions(mMockParentListener, mMockKeyboardDismissAction);
  }

  @Test
  public void testOnSwipeRight() {
    mUnderTest.onSwipeRight(true);
    Mockito.verifyZeroInteractions(mMockParentListener, mMockKeyboardDismissAction);
  }

  @Test
  public void testOnSwipeUp() {
    mUnderTest.onSwipeUp();
    Mockito.verifyZeroInteractions(mMockParentListener, mMockKeyboardDismissAction);
  }

  @Test
  public void testOnSwipeDown() {
    mUnderTest.onSwipeDown();
    Mockito.verifyZeroInteractions(mMockParentListener, mMockKeyboardDismissAction);
  }

  @Test
  public void testOnPinch() {
    mUnderTest.onPinch();
    Mockito.verifyZeroInteractions(mMockParentListener, mMockKeyboardDismissAction);
  }

  @Test
  public void testOnSeparate() {
    mUnderTest.onSeparate();
    Mockito.verifyZeroInteractions(mMockParentListener, mMockKeyboardDismissAction);
  }

  @Test
  public void testOnPress() {
    mUnderTest.onPress(66);
    Mockito.verify(mMockParentListener).onPress(66);
    Mockito.verifyNoMoreInteractions(mMockParentListener);
    Mockito.verifyZeroInteractions(mMockKeyboardDismissAction);
  }

  @Test
  public void testOnRelease() {
    mUnderTest.onRelease(66);
    Mockito.verify(mMockParentListener).onRelease(66);
    Mockito.verifyNoMoreInteractions(mMockParentListener);
    Mockito.verifyZeroInteractions(mMockKeyboardDismissAction);
  }

  @Test
  public void testOnFirstKeyDown() {
    mUnderTest.onFirstDownKey(66);
    Mockito.verify(mMockParentListener).onFirstDownKey(66);
    Mockito.verifyNoMoreInteractions(mMockParentListener);
    Mockito.verifyZeroInteractions(mMockKeyboardDismissAction);
  }

  @Test
  public void testOnGestureTypingInputStart() {
    Assert.assertFalse(
        mUnderTest.onGestureTypingInputStart(66, 80, Mockito.mock(AnyKeyboard.AnyKey.class), 8888));
    Mockito.verifyZeroInteractions(mMockParentListener, mMockKeyboardDismissAction);
  }

  @Test
  public void testOnGestureTypingInput() {
    mUnderTest.onGestureTypingInput(66, 99, 1231);
    Mockito.verifyZeroInteractions(mMockParentListener, mMockKeyboardDismissAction);
  }

  @Test
  public void testOnGestureTypingInputDone() {
    mUnderTest.onGestureTypingInputDone();
    Mockito.verifyZeroInteractions(mMockParentListener, mMockKeyboardDismissAction);
  }

  @Test
  public void testOnLongPressDone() {
    final AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
    mUnderTest.onLongPressDone(key);
    Mockito.verify(mMockParentListener).onLongPressDone(Mockito.same(key));
    Mockito.verifyNoMoreInteractions(mMockParentListener);
    Mockito.verifyZeroInteractions(mMockKeyboardDismissAction);
  }
}
