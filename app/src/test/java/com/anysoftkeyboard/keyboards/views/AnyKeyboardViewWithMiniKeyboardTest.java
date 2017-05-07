package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Point;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnyKeyboardViewWithMiniKeyboardTest extends AnyKeyboardViewBaseTest {

    private AnyKeyboardViewWithMiniKeyboard mViewUnderTest;
    private PointerTracker mMockPointerTracker;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mMockPointerTracker = Mockito.mock(PointerTracker.class);
    }

    @Override
    protected void setCreatedKeyboardView(@NonNull AnyKeyboardViewBase view) {
        super.setCreatedKeyboardView(view);
        mViewUnderTest = (AnyKeyboardViewWithMiniKeyboard) view;
    }

    @Override
    protected AnyKeyboardViewBase createViewToTest(Context context) {
        return new AnyKeyboardViewWithMiniKeyboard(context, null);
    }

    @Test
    public void testPopupShownListener() throws Exception {
        AnyKeyboardViewWithMiniKeyboard.OnPopupShownListener listener = Mockito.mock(AnyKeyboardViewWithMiniKeyboard.OnPopupShownListener.class);

        mViewUnderTest.setOnPopupShownListener(listener);
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        Mockito.verifyZeroInteractions(listener);

        final Keyboard.Key key = findKey('w');

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);
        ViewTestUtils.navigateFromTo(mViewUnderTest, keyPoint, keyPoint, 400, true, false);
        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        Mockito.verify(listener).onPopupKeyboardShowingChanged(true);

        mViewUnderTest.onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        Mockito.verify(listener).onPopupKeyboardShowingChanged(false);
    }

    @Test
    public void testLongPressKeyWithPopupCharacters() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        final Keyboard.Key key = findKey('w');
        Assert.assertTrue(key.popupCharacters.length() > 0);
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, false, mMockPointerTracker);

        Mockito.verify(mMockPointerTracker, Mockito.never()).onCancelEvent();
        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        AnyKeyboardViewBase miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(3, miniKeyboard.getKeyboard().getKeys().size());
    }

    @Test
    public void testLongPressKeyWithPopupCharactersWhileShifted() throws Exception {
        final Keyboard.Key key = findKey('w');
        Assert.assertTrue(key.popupCharacters.length() > 0);
        mViewUnderTest.setShifted(true);
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, false, mMockPointerTracker);

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        final AnyKeyboardViewBase miniKeyboardView = mViewUnderTest.getMiniKeyboard();
        final AnyKeyboard miniKeyboard = miniKeyboardView.getKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertTrue(miniKeyboard.isShifted());

        Assert.assertTrue(miniKeyboardView.getKeyDetector().isKeyShifted(miniKeyboard.getKeys().get(0)));
    }

    @Test
    public void testLongPressWithPopupDoesNotOutputPrimaryCode() throws Exception {
        final Keyboard.Key key = findKey('w');

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);
        ViewTestUtils.navigateFromTo(mViewUnderTest, keyPoint, keyPoint, 400, true, false);
        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(Mockito.anyInt(), Mockito.any(Keyboard.Key.class), Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyBoolean());

        mViewUnderTest.onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        //not sure about this. Maybe the output should be the first key in the popup
        //FIXME: suppose to be '2' and not code 969 (omega)
        Mockito.verify(mMockKeyboardListener).onKey(Mockito.eq(969), Mockito.any(Keyboard.Key.class), Mockito.eq(0), Mockito.any(int[].class), Mockito.eq(true));
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(Mockito.eq((int) 'w'), Mockito.any(Keyboard.Key.class), Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyBoolean());
    }

    @Test
    public void testLongPressKeyWithoutAny() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        final Keyboard.Key keyWithoutPopups = findKey('v');
        //sanity checks
        Assert.assertTrue(TextUtils.isEmpty(keyWithoutPopups.popupCharacters));
        Assert.assertEquals(0, keyWithoutPopups.popupResId);
        //action
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), keyWithoutPopups, false, mMockPointerTracker);

        Mockito.verify(mMockPointerTracker, Mockito.never()).onCancelEvent();
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
    }

    @Test
    public void testLongPressKeyWithPopupLayout() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), findKey('e'), false, mMockPointerTracker);

        Mockito.verify(mMockPointerTracker, Mockito.never()).onCancelEvent();
        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        AnyKeyboardViewBase miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(10, miniKeyboard.getKeyboard().getKeys().size());
    }

    @Test
    public void testNonStickyPopupDismissedAfterUpEvent() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        final Keyboard.Key key = findKey('e');
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, false, mMockPointerTracker);

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);
        mViewUnderTest.onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
    }

    @Test
    public void testStickyPopupStaysAroundAfterUpEvent() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        final Keyboard.Key key = findKey('e');
        Assert.assertEquals(R.xml.popup_qwerty_e, key.popupResId);

        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, true, mMockPointerTracker);

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);
        mViewUnderTest.onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        //but gets dismissed when cancel is called
        mViewUnderTest.closing();
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
    }

    @Test
    public void testLongPressKeyPressStateWithLayout() {
        final Keyboard.Key key = findKey('e');
        Assert.assertEquals(R.xml.popup_qwerty_e, key.popupResId/*sanity check*/);

        KeyDrawableStateProvider provider = new KeyDrawableStateProvider(R.attr.key_type_function, R.attr.key_type_action, R.attr.action_done, R.attr.action_search, R.attr.action_go);
        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);

        ViewTestUtils.navigateFromTo(mViewUnderTest, keyPoint, keyPoint, 400, true, false);
        Assert.assertArrayEquals(provider.KEY_STATE_PRESSED, key.getCurrentDrawableState(provider));

        mViewUnderTest.onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));
    }

    @Test
    public void testLongPressKeyPressStateWithPopupCharacters() {
        final AnyKeyboard.AnyKey key = findKey('w');
        Assert.assertTrue(key.popupCharacters.length() > 0);

        KeyDrawableStateProvider provider = new KeyDrawableStateProvider(R.attr.key_type_function, R.attr.key_type_action, R.attr.action_done, R.attr.action_search, R.attr.action_go);
        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);

        ViewTestUtils.navigateFromTo(mViewUnderTest, keyPoint, keyPoint, 400, true, false);
        Assert.assertArrayEquals(provider.KEY_STATE_PRESSED, key.getCurrentDrawableState(provider));

        mViewUnderTest.onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));
    }

}