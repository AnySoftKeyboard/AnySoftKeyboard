package com.anysoftkeyboard.keyboards.views;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.gesturetyping.Point;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

public class MiniKeyboardActionListenerTest {

    private MiniKeyboardActionListener mUnderTest;
    private AnyKeyboardViewWithMiniKeyboard mMockParent;
    private OnKeyboardActionListener mMockParentListener;

    @Before
    public void setup() {
        mMockParent = Mockito.mock(AnyKeyboardViewWithMiniKeyboard.class);
        mMockParentListener = Mockito.mock(OnKeyboardActionListener.class);
        mMockParent.mKeyboardActionListener = mMockParentListener;
        mUnderTest = new MiniKeyboardActionListener(mMockParent);
    }

    @Test
    public void onGestureTypingInput() {
        mUnderTest.onGestureTypingInput(Collections.<Point>emptyList(), new int[0], 0);
        Mockito.verifyZeroInteractions(mMockParentListener);
    }

    @Test
    public void onRelease() {
        mUnderTest.onRelease(99);
        Mockito.verify(mMockParentListener).onRelease(Mockito.eq(99));
    }

    @Test
    public void onCancel() {
        mUnderTest.onCancel();
        Mockito.verify(mMockParent).dismissPopupKeyboard();
    }

    @Test
    public void onFirstDownKey() {
        mUnderTest.onFirstDownKey(99);
        Mockito.verify(mMockParentListener).onFirstDownKey(Mockito.eq(99));
    }

    @Test
    public void onKeyNotOneShot() {
        mUnderTest.setInOneShot(false);
        mUnderTest.onKey(99, null, 0, new int[0], false);
        Mockito.verify(mMockParentListener).onKey(99, null, 0, new int[0], false);
        Mockito.verifyNoMoreInteractions(mMockParentListener);
    }

    @Test
    public void onKeyNotOneShotButEnter() {
        mUnderTest.setInOneShot(false);
        mUnderTest.onKey(KeyCodes.ENTER, null, 0, new int[0], false);
        Mockito.verify(mMockParentListener).onKey(KeyCodes.ENTER, null, 0, new int[0], false);
        Mockito.verify(mMockParent).dismissPopupKeyboard();
    }

    @Test
    public void onKeyOneShot() {
        mUnderTest.setInOneShot(true);
        mUnderTest.onKey(99, null, 0, new int[0], false);
        Mockito.verify(mMockParentListener).onKey(99, null, 0, new int[0], false);
        Mockito.verify(mMockParent).dismissPopupKeyboard();
    }

    @Test
    public void onKeyOneShotButDelete() {
        mUnderTest.setInOneShot(true);
        mUnderTest.onKey(KeyCodes.DELETE, null, 0, new int[0], false);
        Mockito.verify(mMockParentListener).onKey(KeyCodes.DELETE, null, 0, new int[0], false);
        Mockito.verifyNoMoreInteractions(mMockParentListener);
    }

    @Test
    public void onKeyNotOneShotAndDelete() {
        mUnderTest.setInOneShot(false);
        mUnderTest.onKey(KeyCodes.DELETE, null, 0, new int[0], false);
        Mockito.verify(mMockParentListener).onKey(KeyCodes.DELETE, null, 0, new int[0], false);
        Mockito.verifyNoMoreInteractions(mMockParentListener);
    }

    @Test
    public void onMultiTapEnded() {
        mUnderTest.onMultiTapEnded();
        Mockito.verify(mMockParentListener).onMultiTapEnded();
    }

    @Test
    public void onMultiTapStarted() {
        mUnderTest.onMultiTapStarted();
        Mockito.verify(mMockParentListener).onMultiTapStarted();
    }

    @Test
    public void onPinch() {
        mUnderTest.onPinch();
        Mockito.verifyZeroInteractions(mMockParentListener);
    }

    @Test
    public void onSeparate() {
        mUnderTest.onSeparate();
        Mockito.verifyZeroInteractions(mMockParentListener);
    }

    @Test
    public void onSwipeDown() {
        mUnderTest.onSwipeDown();
        Mockito.verifyZeroInteractions(mMockParentListener);
    }

    @Test
    public void onSwipeRight() {
        mUnderTest.onSwipeRight(true);
        Mockito.verifyZeroInteractions(mMockParentListener);
    }

    @Test
    public void onSwipeLeft() {
        mUnderTest.onSwipeLeft(true);
        Mockito.verifyZeroInteractions(mMockParentListener);
    }

    @Test
    public void onSwipeUp() {
        mUnderTest.onSwipeUp();
        Mockito.verifyZeroInteractions(mMockParentListener);
    }

    @Test
    public void onTextInOneShot() {
        mUnderTest.setInOneShot(true);
        mUnderTest.onText(null, "gg");
        Mockito.verify(mMockParentListener).onText(null, "gg");
        Mockito.verify(mMockParent).dismissPopupKeyboard();
    }

    @Test
    public void onTextNotInOneShot() {
        mUnderTest.setInOneShot(false);
        mUnderTest.onText(null, "gg");
        Mockito.verify(mMockParentListener).onText(null, "gg");
        Mockito.verify(mMockParent, Mockito.never()).dismissPopupKeyboard();
    }
}