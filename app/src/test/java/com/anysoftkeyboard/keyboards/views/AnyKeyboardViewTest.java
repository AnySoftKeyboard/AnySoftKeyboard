package com.anysoftkeyboard.keyboards.views;

import android.view.MotionEvent;

import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class AnyKeyboardViewTest {

    private OnKeyboardActionListener mMockKeyboardListener;
    private AnyKeyboardView mViewUnderTest;
    private AnyKeyboard mEnglishKeyboard;
    private KeyboardSwitcher mMockKeyboardSwitcher;

    @Before
    public void setUp() throws Exception {
        mMockKeyboardListener = Mockito.mock(OnKeyboardActionListener.class);
        mMockKeyboardSwitcher = Mockito.mock(KeyboardSwitcher.class);
        mViewUnderTest = new AnyKeyboardView(RuntimeEnvironment.application, null);
        mViewUnderTest.setOnKeyboardActionListener(mMockKeyboardListener);
        mViewUnderTest.setKeyboardSwitcher(mMockKeyboardSwitcher);

        mEnglishKeyboard = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application)
                .get(0)
                .createKeyboard(RuntimeEnvironment.application, 0);
        mEnglishKeyboard.loadKeyboard(mViewUnderTest.getThemedKeyboardDimens());

        mViewUnderTest.setKeyboard(mEnglishKeyboard, 0);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testKeyClickHappyPath() {
        AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(14);
        int primaryCode = key.getCodeAtIndex(0, false);
        Mockito.verifyZeroInteractions(mMockKeyboardListener);

        MotionEvent motionEvent = MotionEvent.obtain(100, 100, MotionEvent.ACTION_DOWN, key.x + 1, key.y + 1, 0);
        mViewUnderTest.onTouchEvent(motionEvent);
        Mockito.verify(mMockKeyboardListener).onPress(primaryCode);
        Mockito.verify(mMockKeyboardListener).onFirstDownKey(primaryCode);
        Mockito.verifyNoMoreInteractions(mMockKeyboardListener);

        Mockito.reset(mMockKeyboardListener);

        motionEvent = MotionEvent.obtain(100, 110, MotionEvent.ACTION_UP, key.x + 1, key.y + 1, 0);
        mViewUnderTest.onTouchEvent(motionEvent);
        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onKey(Mockito.eq(primaryCode), Mockito.same(key), Mockito.eq(0), Mockito.any(int[].class), Mockito.eq(true));
        inOrder.verify(mMockKeyboardListener).onRelease(primaryCode);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testTouchIsDisabledOnGestureUntilAllPointersAreUp() {
        AnyKeyboard.AnyKey key1 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(14);
        AnyKeyboard.AnyKey key2 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(20);
        int primaryKey1 = key1.getCodeAtIndex(0, false);
        int primaryKey2 = key2.getCodeAtIndex(0, false);

        Assert.assertFalse(mViewUnderTest.areTouchesDisabled());
        //this is a swipe gesture
        ViewTestUtils.navigateFromTo(mViewUnderTest, key1, key2, 100, true, false/*don't send UP event*/);

        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onPress(primaryKey1);
        Mockito.verify(mMockKeyboardListener).onFirstDownKey(primaryKey1);
        //swipe gesture will be detected at key "f". Which is 17
        for (int keyIndex=14; keyIndex<17; keyIndex++) {
            inOrder.verify(mMockKeyboardListener).onRelease(mEnglishKeyboard.getKeys().get(keyIndex).getCodeAtIndex(0, false));
            inOrder.verify(mMockKeyboardListener).onPress(mEnglishKeyboard.getKeys().get(keyIndex+1).getCodeAtIndex(0, false));
        }
        inOrder.verify(mMockKeyboardListener).onSwipeRight(false);
        inOrder.verifyNoMoreInteractions();
        Assert.assertTrue(mViewUnderTest.areTouchesDisabled());

        ViewTestUtils.navigateFromTo(mViewUnderTest, key2, key2, 20, false, true);

        Assert.assertFalse(mViewUnderTest.areTouchesDisabled());
    }

    @Test
    public void testSlideToNextKeyHappyPath() {
        AnyKeyboard.AnyKey key1 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(14);
        AnyKeyboard.AnyKey key2 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(15);
        int primaryKey1 = key1.getCodeAtIndex(0, false);
        int primaryKey2 = key2.getCodeAtIndex(0, false);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key1, key2, 100, true, true);

        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onPress(primaryKey1);
        Mockito.verify(mMockKeyboardListener).onFirstDownKey(primaryKey1);
        inOrder.verify(mMockKeyboardListener).onRelease(primaryKey1);
        inOrder.verify(mMockKeyboardListener).onPress(primaryKey2);
        inOrder.verify(mMockKeyboardListener).onKey(Mockito.eq(primaryKey2), Mockito.same(key2), Mockito.eq(0), Mockito.any(int[].class), Mockito.eq(true));
        inOrder.verify(mMockKeyboardListener).onRelease(primaryKey2);
        inOrder.verifyNoMoreInteractions();
    }
}