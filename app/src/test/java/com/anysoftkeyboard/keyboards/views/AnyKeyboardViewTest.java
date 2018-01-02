package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.widget.PopupWindow;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.extradraw.ExtraDraw;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowSystemClock;

import static com.anysoftkeyboard.keyboards.Keyboard.EDGE_LEFT;
import static com.anysoftkeyboard.keyboards.Keyboard.EDGE_RIGHT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;

@RunWith(AnySoftKeyboardTestRunner.class)
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
        motionEvent.recycle();
        Mockito.verify(mMockKeyboardListener).onPress(primaryCode);
        Mockito.verify(mMockKeyboardListener).onFirstDownKey(primaryCode);
        Mockito.verifyNoMoreInteractions(mMockKeyboardListener);

        Mockito.reset(mMockKeyboardListener);

        motionEvent = MotionEvent.obtain(100, 110, MotionEvent.ACTION_UP, key.x + 1, key.y + 1, 0);
        mViewUnderTest.onTouchEvent(motionEvent);
        motionEvent.recycle();
        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onKey(Mockito.eq(primaryCode), same(key), Mockito.eq(0), any(int[].class), Mockito.eq(true));
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

        Assert.assertFalse(mViewUnderTest.areTouchesDisabled(null));
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
        Assert.assertTrue(mViewUnderTest.areTouchesDisabled(null));

        ViewTestUtils.navigateFromTo(mViewUnderTest, key2, key2, 20, false, true);

        Assert.assertFalse(mViewUnderTest.areTouchesDisabled(null));
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
        inOrder.verify(mMockKeyboardListener).onKey(Mockito.eq(primaryKey2), same(key2), Mockito.eq(0), any(int[].class), Mockito.eq(true));
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
        ViewTestUtils.navigateFromTo(mViewUnderTest, new Point(10, -20), new Point(10, 1), 100, false, false);
        Assert.assertTrue(currentlyShownPopup.isShowing());

        ViewTestUtils.navigateFromTo(mViewUnderTest, new Point(10, 1), new Point(10, mViewUnderTest.getThemedKeyboardDimens().getNormalKeyHeight() + 10), 100, false, false);
        Assert.assertFalse(currentlyShownPopup.isShowing());
    }

    @Test
    public void testSlideToExtensionKeyboardWhenDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_extension_keyboard_enabled, false);
        ShadowSystemClock.sleep(1225);
        Assert.assertNull(ShadowApplication.getInstance().getLatestPopupWindow());
        ViewTestUtils.navigateFromTo(mViewUnderTest, new Point(10, 10), new Point(10, -20), 200, true, false);

        PopupWindow currentlyShownPopup = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(currentlyShownPopup);
    }

    @Test
    public void testSwipeUpToUtilitiesKeyboard() {
        ShadowSystemClock.sleep(1225);
        Assert.assertNull(ShadowApplication.getInstance().getLatestPopupWindow());
        //flinging up
        final Keyboard.Key spaceKey = findKey(' ');
        final Point upPoint = ViewTestUtils.getKeyCenterPoint(spaceKey);
        upPoint.offset(0, -(mViewUnderTest.mSwipeYDistanceThreshold + 1));
        Assert.assertFalse(mViewUnderTest.areTouchesDisabled(null));
        ViewTestUtils.navigateFromTo(mViewUnderTest, ViewTestUtils.getKeyCenterPoint(spaceKey), upPoint, 30, true, true);

        Mockito.verify(mMockKeyboardListener).onFirstDownKey(' ');
        Mockito.verify(mMockKeyboardListener).onSwipeUp();

        mViewUnderTest.openUtilityKeyboard();

        PopupWindow currentlyShownPopup = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNotNull(currentlyShownPopup);
        Assert.assertTrue(currentlyShownPopup.isShowing());
        AnyKeyboardViewBase miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(19, miniKeyboard.getKeyboard().getKeys().size());

        //hiding
        mViewUnderTest.closing();
        Assert.assertFalse(currentlyShownPopup.isShowing());

        Mockito.reset(mMockKeyboardListener);

        //doing it again
        ViewTestUtils.navigateFromTo(mViewUnderTest, ViewTestUtils.getKeyCenterPoint(spaceKey), upPoint, 30, true, true);

        Mockito.verify(mMockKeyboardListener).onFirstDownKey(' ');
        Mockito.verify(mMockKeyboardListener).onSwipeUp();
    }

    @Test
    public void testQuickTextPopupHappyPath() {
        AnyKeyboard.AnyKey quickTextPopupKey = findKey(KeyCodes.QUICK_TEXT);
        Assert.assertNotNull(quickTextPopupKey);
        KeyDrawableStateProvider provider = new KeyDrawableStateProvider(R.attr.key_type_function, R.attr.key_type_action, R.attr.action_done, R.attr.action_search, R.attr.action_go);
        Assert.assertArrayEquals(provider.KEY_STATE_FUNCTIONAL_NORMAL, quickTextPopupKey.getCurrentDrawableState(provider));

        ViewTestUtils.navigateFromTo(mViewUnderTest, quickTextPopupKey, quickTextPopupKey, 400, true, false);
        Mockito.verify(mMockKeyboardListener).onKey(Mockito.eq(KeyCodes.QUICK_TEXT_POPUP), same(quickTextPopupKey), Mockito.eq(0), Mockito.nullable(int[].class), Mockito.eq(true));
    }

    @Test
    public void testLongPressEnter() throws Exception {
        AnyKeyboard.AnyKey enterKey = findKey(KeyCodes.ENTER);
        Assert.assertNotNull(enterKey);
        Assert.assertEquals(KeyCodes.ENTER, enterKey.getPrimaryCode());
        Assert.assertEquals(KeyCodes.SETTINGS, enterKey.longPressCode);

        ViewTestUtils.navigateFromTo(mViewUnderTest, enterKey, enterKey, 400, true, true);
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(Mockito.eq(KeyCodes.ENTER), any(Keyboard.Key.class), Mockito.anyInt(), any(int[].class), Mockito.anyBoolean());
        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onPress(KeyCodes.ENTER);
        inOrder.verify(mMockKeyboardListener).onKey(Mockito.eq(KeyCodes.SETTINGS), any(Keyboard.Key.class), Mockito.anyInt(), Mockito.nullable(int[].class), Mockito.anyBoolean());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testEdgeTouchLeftKeyA() {
        AnyKeyboard.AnyKey edgeKey = findKey('a');
        Assert.assertNotNull(edgeKey);
        Assert.assertEquals(EDGE_LEFT, edgeKey.edgeFlags);

        final Point edgeTouchPoint = new Point(0, edgeKey.y + 5);
        Assert.assertTrue(edgeKey.isInside(edgeTouchPoint.x, edgeTouchPoint.y));
        Assert.assertTrue(edgeTouchPoint.x < edgeKey.x);

        ViewTestUtils.navigateFromTo(mViewUnderTest, edgeTouchPoint, edgeTouchPoint, 40, true, true);
        Mockito.verify(mMockKeyboardListener).onKey(Mockito.eq((int) 'a'), same(edgeKey), Mockito.eq(0), any(int[].class), Mockito.eq(true));
    }

    @Test
    public void testEdgeTouchRightKeyL() {
        AnyKeyboard.AnyKey edgeKey = findKey('l');
        Assert.assertNotNull(edgeKey);
        Assert.assertEquals(EDGE_RIGHT, edgeKey.edgeFlags);

        final Point edgeTouchPoint = new Point(mViewUnderTest.getThemedKeyboardDimens().getKeyboardMaxWidth() - 1, edgeKey.y + 5);
        Assert.assertTrue(edgeKey.isInside(edgeTouchPoint.x, edgeTouchPoint.y));
        Assert.assertTrue(edgeTouchPoint.x > edgeKey.x + edgeKey.width + edgeKey.gap);

        ViewTestUtils.navigateFromTo(mViewUnderTest, edgeTouchPoint, edgeTouchPoint, 40, true, true);
    }

    @Test
    public void testExtraDrawMultiple() {
        ExtraDraw mockDraw1 = Mockito.mock(ExtraDraw.class);
        ExtraDraw mockDraw2 = Mockito.mock(ExtraDraw.class);
        Mockito.doReturn(true).when(mockDraw1).onDraw(any(), any(), same(mViewUnderTest));
        Mockito.doReturn(true).when(mockDraw2).onDraw(any(), any(), same(mViewUnderTest));

        Robolectric.getForegroundThreadScheduler().pause();
        Assert.assertFalse(Robolectric.getForegroundThreadScheduler().areAnyRunnable());
        mViewUnderTest.addExtraDraw(mockDraw1);
        mViewUnderTest.addExtraDraw(mockDraw2);

        Mockito.verify(mockDraw1, Mockito.never()).onDraw(any(), any(), same(mViewUnderTest));
        Mockito.verify(mockDraw2, Mockito.never()).onDraw(any(), any(), same(mViewUnderTest));

        Assert.assertTrue(Robolectric.getForegroundThreadScheduler().size() > 0);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        mViewUnderTest.onDraw(Mockito.mock(Canvas.class));

        Mockito.verify(mockDraw1, Mockito.times(1)).onDraw(any(), any(), same(mViewUnderTest));
        Mockito.verify(mockDraw2, Mockito.times(1)).onDraw(any(), any(), same(mViewUnderTest));

        Assert.assertTrue(Robolectric.getForegroundThreadScheduler().size() > 0);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        mViewUnderTest.onDraw(Mockito.mock(Canvas.class));

        Mockito.verify(mockDraw1, Mockito.times(2)).onDraw(any(), any(), same(mViewUnderTest));
        Mockito.verify(mockDraw2, Mockito.times(2)).onDraw(any(), any(), same(mViewUnderTest));

        Assert.assertTrue(Robolectric.getForegroundThreadScheduler().size() > 0);

        Mockito.doReturn(false).when(mockDraw1).onDraw(any(), any(), same(mViewUnderTest));

        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        mViewUnderTest.onDraw(Mockito.mock(Canvas.class));

        Mockito.verify(mockDraw1, Mockito.times(3)).onDraw(any(), any(), same(mViewUnderTest));
        Mockito.verify(mockDraw2, Mockito.times(3)).onDraw(any(), any(), same(mViewUnderTest));

        Assert.assertTrue(Robolectric.getForegroundThreadScheduler().size() > 0);

        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        mViewUnderTest.onDraw(Mockito.mock(Canvas.class));

        Mockito.verify(mockDraw1, Mockito.times(3)).onDraw(any(), any(), same(mViewUnderTest));
        Mockito.verify(mockDraw2, Mockito.times(4)).onDraw(any(), any(), same(mViewUnderTest));

        Assert.assertTrue(Robolectric.getForegroundThreadScheduler().size() > 0);

        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        mViewUnderTest.onDraw(Mockito.mock(Canvas.class));

        Mockito.verify(mockDraw1, Mockito.times(3)).onDraw(any(), any(), same(mViewUnderTest));
        Mockito.verify(mockDraw2, Mockito.times(5)).onDraw(any(), any(), same(mViewUnderTest));

        Assert.assertTrue(Robolectric.getForegroundThreadScheduler().size() > 0);

        Mockito.doReturn(false).when(mockDraw2).onDraw(any(), any(), same(mViewUnderTest));

        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        mViewUnderTest.onDraw(Mockito.mock(Canvas.class));

        Mockito.verify(mockDraw1, Mockito.times(3)).onDraw(any(), any(), same(mViewUnderTest));
        Mockito.verify(mockDraw2, Mockito.times(6)).onDraw(any(), any(), same(mViewUnderTest));

        Assert.assertFalse(Robolectric.getForegroundThreadScheduler().size() > 0);

        //adding another one
        ExtraDraw mockDraw3 = Mockito.mock(ExtraDraw.class);
        mViewUnderTest.addExtraDraw(mockDraw3);
        Assert.assertTrue(Robolectric.getForegroundThreadScheduler().size() > 0);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        mViewUnderTest.onDraw(Mockito.mock(Canvas.class));

        Mockito.verify(mockDraw1, Mockito.times(3)).onDraw(any(), any(), same(mViewUnderTest));
        Mockito.verify(mockDraw2, Mockito.times(6)).onDraw(any(), any(), same(mViewUnderTest));
        Mockito.verify(mockDraw3, Mockito.times(1)).onDraw(any(), any(), same(mViewUnderTest));

        Assert.assertFalse(Robolectric.getForegroundThreadScheduler().size() > 0);
    }

    @Test
    public void testWithLongPressDeleteKeyOutput() {
        final AnyKeyboard.AnyKey key = findKey(KeyCodes.DELETE);
        key.longPressCode = KeyCodes.DELETE_WORD;

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 10, true, true);

        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(mMockKeyboardListener)
                .onKey(captor.capture(), same(key), Mockito.anyInt(), any(int[].class), Mockito.anyBoolean());

        Assert.assertEquals(KeyCodes.DELETE, captor.getValue().intValue());

        Mockito.reset(mMockKeyboardListener);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 1000, true, true);

        captor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(mMockKeyboardListener, Mockito.times(16))
                .onKey(captor.capture(), same(key), Mockito.anyInt(), Mockito.nullable(int[].class), Mockito.anyBoolean());

        for (int valueIndex = 0; valueIndex < captor.getAllValues().size(); valueIndex++) {
            final int keyCode = captor.getAllValues().get(valueIndex);
            //the first onKey will be the regular keycode
            //then, the long-press timer will kick off and will
            //repeat the long-press keycode.
            if (valueIndex == 0) {
                Assert.assertEquals(KeyCodes.DELETE, keyCode);
            } else {
                Assert.assertEquals(KeyCodes.DELETE_WORD, keyCode);
            }
        }
    }
}