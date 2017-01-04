package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.widget.PopupWindow;

import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowSystemClock;

@RunWith(RobolectricTestRunner.class)
public class AnyKeyboardViewTest extends AnyKeyboardViewWithMiniKeyboardTest {

    private AnyKeyboardView mViewUnderTest;

    @Override
    protected AnyKeyboardViewBase createViewToTest(Context context) {
        return new AnyKeyboardView(context, null);
    }

    @Override
    protected void setCreatedKeyboardView(@NonNull AnyKeyboardViewBase view) {
        super.setCreatedKeyboardView(view);
        mViewUnderTest = (AnyKeyboardView) view;
    }

    @Test
    public void testKeyClickHappyPath() {
        AnyKeyboard.AnyKey key = findKey('a');
        int primaryCode = key.getCodeAtIndex(0, false);
        Mockito.verifyZeroInteractions(mMockKeyboardListener);

        MotionEvent motionEvent = MotionEvent.obtain(100, 100, MotionEvent.ACTION_DOWN, key.x + 1, key.y + 1, 0);
        mViewUnderTest.onTouchEvent(motionEvent);
		if (motionEvent != null) {
			motionEvent.recycle();
		}
        Mockito.verify(mMockKeyboardListener).onPress(primaryCode);
        Mockito.verify(mMockKeyboardListener).onFirstDownKey(primaryCode);
        Mockito.verifyNoMoreInteractions(mMockKeyboardListener);

        Mockito.reset(mMockKeyboardListener);

        motionEvent = MotionEvent.obtain(100, 110, MotionEvent.ACTION_UP, key.x + 1, key.y + 1, 0);
        mViewUnderTest.onTouchEvent(motionEvent);
		if (motionEvent != null) {
			motionEvent.recycle();
		}
        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onKey(Mockito.eq(primaryCode), Mockito.same(key), Mockito.eq(0), Mockito.any(int[].class), Mockito.eq(true));
        inOrder.verify(mMockKeyboardListener).onRelease(primaryCode);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testTouchIsDisabledOnGestureUntilAllPointersAreUp() {
        final int primaryKey1 = 'a';
        final int keyAIndex = findKeyIndex(primaryKey1);
        final int keyFIndex = findKeyIndex('f');
        final int keyJIndex = findKeyIndex('j');
        AnyKeyboard.AnyKey key1 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(keyAIndex);
        AnyKeyboard.AnyKey key2 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(keyJIndex);

        Assert.assertFalse(mViewUnderTest.areTouchesDisabled());
        //this is a swipe gesture
        ViewTestUtils.navigateFromTo(mViewUnderTest, key1, key2, 100, true, false/*don't send UP event*/);

        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onPress(primaryKey1);
        Mockito.verify(mMockKeyboardListener).onFirstDownKey(primaryKey1);
        //swipe gesture will be detected at key "f".
        for (int keyIndex = keyAIndex; keyIndex < keyFIndex; keyIndex++) {
            inOrder.verify(mMockKeyboardListener).onRelease(mEnglishKeyboard.getKeys().get(keyIndex).getCodeAtIndex(0, false));
            inOrder.verify(mMockKeyboardListener).onPress(mEnglishKeyboard.getKeys().get(keyIndex + 1).getCodeAtIndex(0, false));
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

    @Test
    public void testSlideToExtensionKeyboard() {
        ShadowSystemClock.sleep(1225);
        Assert.assertNull(ShadowApplication.getInstance().getLatestPopupWindow());
        ViewTestUtils.navigateFromTo(mViewUnderTest, new Point(10, 10), new Point(10, -20), 200, true, false);

        PopupWindow currentlyShownPopup = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNotNull(currentlyShownPopup);
        Assert.assertTrue(currentlyShownPopup.isShowing());
        AnyKeyboardViewBase miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(20, miniKeyboard.getKeyboard().getKeys().size());
        //now moving back to the main keyboard - not quite yet
        ViewTestUtils.navigateFromTo(mViewUnderTest, new Point(10, -20), new Point(10, mViewUnderTest.getThemedKeyboardDimens().getNormalKeyHeight() - 10), 100, false, false);
        Assert.assertTrue(currentlyShownPopup.isShowing());

        ViewTestUtils.navigateFromTo(mViewUnderTest, new Point(10, mViewUnderTest.getThemedKeyboardDimens().getNormalKeyHeight() - 10), new Point(10, mViewUnderTest.getThemedKeyboardDimens().getNormalKeyHeight() + 10), 100, false, false);
        Assert.assertFalse(currentlyShownPopup.isShowing());
    }

    @Test
    public void testQuickTextPopupHappyPath() {
        AnyKeyboard.AnyKey quickTextPopupKey = findKey(KeyCodes.QUICK_TEXT);
        Assert.assertNotNull(quickTextPopupKey);
        KeyDrawableStateProvider provider = new KeyDrawableStateProvider(R.attr.key_type_function, R.attr.key_type_action, R.attr.action_done, R.attr.action_search, R.attr.action_go);
        Assert.assertArrayEquals(provider.KEY_STATE_FUNCTIONAL_NORMAL, quickTextPopupKey.getCurrentDrawableState(provider));

        ViewTestUtils.navigateFromTo(mViewUnderTest, quickTextPopupKey, quickTextPopupKey, 400, true, false);
        Mockito.verify(mMockKeyboardListener).onKey(Mockito.eq(KeyCodes.QUICK_TEXT_POPUP), Mockito.same(quickTextPopupKey), Mockito.eq(0), Mockito.any(int[].class), Mockito.eq(true));
    }

    @Test
    public void testLongPressEnter() throws Exception {
        AnyKeyboard.AnyKey enterKey = findKey(KeyCodes.ENTER);
        Assert.assertNotNull(enterKey);
        Assert.assertEquals(KeyCodes.ENTER, enterKey.getPrimaryCode());
        Assert.assertEquals(KeyCodes.SETTINGS, enterKey.longPressCode);

        ViewTestUtils.navigateFromTo(mViewUnderTest, enterKey, enterKey, 400, true, true);
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(Mockito.eq(KeyCodes.ENTER), Mockito.any(Keyboard.Key.class), Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyBoolean());
        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onPress(KeyCodes.ENTER);
        inOrder.verify(mMockKeyboardListener).onKey(Mockito.eq(KeyCodes.SETTINGS), Mockito.any(Keyboard.Key.class), Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyBoolean());
        inOrder.verifyNoMoreInteractions();
    }
}