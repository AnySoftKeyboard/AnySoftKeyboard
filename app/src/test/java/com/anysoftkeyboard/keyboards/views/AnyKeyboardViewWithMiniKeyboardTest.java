package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Point;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.ExternalAnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;

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
    public void testShortPressWhenNoPrimaryKeyAndNoPopupItemsShouldNotOutput() throws Exception {
        ExternalAnyKeyboard anyKeyboard = new ExternalAnyKeyboard(
                new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application), RuntimeEnvironment.application,
                RuntimeEnvironment.application, R.xml.keyboard_with_keys_with_no_codes, R.xml.keyboard_with_keys_with_no_codes, "test", 0, 0,
                "en", "", "", Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        anyKeyboard.loadKeyboard(mViewUnderTest.mKeyboardDimens);
        mViewUnderTest.setKeyboard(anyKeyboard, 0);

        Assert.assertEquals(7, anyKeyboard.getKeys().size());
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        final AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) anyKeyboard.getKeys().get(3);

        Assert.assertEquals(0, key.getPrimaryCode());
        Assert.assertEquals(0, key.getCodesCount());
        Assert.assertEquals(0, key.popupResId);
        Assert.assertNull(key.label);
        Assert.assertNull(key.popupCharacters);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, true, false);

        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, false, true);

        Mockito.verify(mMockKeyboardListener).onKey(eq(0), same(key), eq(0), any(), anyBoolean());
    }

    @Test
    public void testShortPressWithLabelWhenNoPrimaryKeyAndNoPopupItemsShouldNotOutput() throws Exception {
        ExternalAnyKeyboard anyKeyboard = new ExternalAnyKeyboard(
                new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application), RuntimeEnvironment.application,
                RuntimeEnvironment.application, R.xml.keyboard_with_keys_with_no_codes, R.xml.keyboard_with_keys_with_no_codes, "test", 0, 0,
                "en", "", "", Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        anyKeyboard.loadKeyboard(mViewUnderTest.mKeyboardDimens);
        mViewUnderTest.setKeyboard(anyKeyboard, 0);

        Assert.assertEquals(7, anyKeyboard.getKeys().size());
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        final AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) anyKeyboard.getKeys().get(4);

        Assert.assertEquals(0, key.getPrimaryCode());
        Assert.assertEquals(0, key.getCodesCount());
        Assert.assertEquals(0, key.popupResId);
        Assert.assertEquals("d", key.label);
        Assert.assertNull(key.popupCharacters);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, true, false);

        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, false, true);

        Mockito.verify(mMockKeyboardListener).onKey(eq(0), same(key), eq(0), any(), anyBoolean());
    }

    @Test
    public void testShortPressWhenNoPrimaryKeyAndPopupCharactersShouldShowPopupWindow() throws Exception {
        ExternalAnyKeyboard anyKeyboard = new ExternalAnyKeyboard(
                new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application), RuntimeEnvironment.application,
                RuntimeEnvironment.application, R.xml.keyboard_with_keys_with_no_codes, R.xml.keyboard_with_keys_with_no_codes, "test", 0, 0,
                "en", "", "", Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        anyKeyboard.loadKeyboard(mViewUnderTest.mKeyboardDimens);
        mViewUnderTest.setKeyboard(anyKeyboard, 0);

        final AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) anyKeyboard.getKeys().get(1);

        Assert.assertEquals(0, key.getPrimaryCode());
        Assert.assertEquals(0, key.getCodesCount());
        Assert.assertEquals(R.xml.popup_one_row, key.popupResId);
        Assert.assertEquals("b", key.label);
        Assert.assertEquals("abc", key.popupCharacters);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, true, false);

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        AnyKeyboardViewBase miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(3, miniKeyboard.getKeyboard().getKeys().size());

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(anyInt(), any(), anyInt(), any(), anyBoolean());

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, false, true);
    }

    @Test
    public void testShortPressWhenNoPrimaryKeyAndPopupCharactersShouldNotShowPopupWindowIfApiLevelIsBefore8() throws Exception {
        ExternalAnyKeyboard anyKeyboard = new ExternalAnyKeyboard(
                new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application, 7), RuntimeEnvironment.application,
                RuntimeEnvironment.application, R.xml.keyboard_with_keys_with_no_codes, R.xml.keyboard_with_keys_with_no_codes, "test", 0, 0,
                "en", "", "", Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        anyKeyboard.loadKeyboard(mViewUnderTest.mKeyboardDimens);
        mViewUnderTest.setKeyboard(anyKeyboard, 0);

        final AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) anyKeyboard.getKeys().get(1);

        Assert.assertEquals('b', key.getPrimaryCode());
        Assert.assertEquals(1, key.getCodesCount());
        Assert.assertEquals(R.xml.popup_one_row, key.popupResId);
        Assert.assertEquals("b", key.label);
        Assert.assertEquals("abc", key.popupCharacters);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, true, false);

        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(anyInt(), any(), anyInt(), any(), anyBoolean());

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, false, true);

        Mockito.verify(mMockKeyboardListener).onKey(eq((int) 'b'), same(key), anyInt(), any(), anyBoolean());
    }

    @Test
    public void testShortPressWhenNoPrimaryKeyAndPopupLayoutShouldShowPopupWindow() throws Exception {
        ExternalAnyKeyboard anyKeyboard = new ExternalAnyKeyboard(
                new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application), RuntimeEnvironment.application,
                RuntimeEnvironment.application, R.xml.keyboard_with_keys_with_no_codes, R.xml.keyboard_with_keys_with_no_codes, "test", 0, 0,
                "en", "", "", Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        anyKeyboard.loadKeyboard(mViewUnderTest.mKeyboardDimens);
        mViewUnderTest.setKeyboard(anyKeyboard, 0);

        final AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) anyKeyboard.getKeys().get(0);

        Assert.assertEquals(0, key.getPrimaryCode());
        Assert.assertEquals(0, key.getCodesCount());
        Assert.assertEquals(R.xml.popup_16keys_wxyz, key.popupResId);
        Assert.assertEquals("a", key.label);
        Assert.assertNull(key.popupCharacters);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, true, false);

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        AnyKeyboardViewBase miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(6, miniKeyboard.getKeyboard().getKeys().size());

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(anyInt(), any(), anyInt(), any(), anyBoolean());
    }

    @Test
    public void testShortPressWhenNoPrimaryKeyButTextWithoutPopupShouldOutputText() throws Exception {
        ExternalAnyKeyboard anyKeyboard = new ExternalAnyKeyboard(
                new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application), RuntimeEnvironment.application,
                RuntimeEnvironment.application, R.xml.keyboard_with_keys_with_no_codes, R.xml.keyboard_with_keys_with_no_codes, "test", 0, 0,
                "en", "", "", Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        anyKeyboard.loadKeyboard(mViewUnderTest.mKeyboardDimens);
        mViewUnderTest.setKeyboard(anyKeyboard, 0);

        final AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) anyKeyboard.getKeys().get(5);

        Assert.assertEquals(0, key.getPrimaryCode());
        Assert.assertEquals(0, key.getCodesCount());
        Assert.assertEquals(0, key.popupResId);
        Assert.assertEquals("text", key.label);
        Assert.assertNull(key.popupCharacters);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, true, false);

        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 10, false, true);

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(anyInt(), nullable(Keyboard.Key.class), anyInt(), Mockito.nullable(int[].class), Mockito.anyBoolean());
        Mockito.verify(mMockKeyboardListener).onText(same(key), eq("texting"));
    }

    @Test
    public void testShortPressWhenNoPrimaryKeyButTextWithPopupShouldOutputText() throws Exception {
        ExternalAnyKeyboard anyKeyboard = new ExternalAnyKeyboard(
                new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application), RuntimeEnvironment.application,
                RuntimeEnvironment.application, R.xml.keyboard_with_keys_with_no_codes, R.xml.keyboard_with_keys_with_no_codes, "test", 0, 0,
                "en", "", "", Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        anyKeyboard.loadKeyboard(mViewUnderTest.mKeyboardDimens);
        mViewUnderTest.setKeyboard(anyKeyboard, 0);

        final AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) anyKeyboard.getKeys().get(6);

        Assert.assertEquals(0, key.getPrimaryCode());
        Assert.assertEquals(0, key.getCodesCount());
        Assert.assertEquals(R.xml.popup_16keys_wxyz, key.popupResId);
        Assert.assertEquals("popup", key.label);
        Assert.assertNull(key.popupCharacters);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, true, false);

        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 10, false, true);

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(anyInt(), nullable(Keyboard.Key.class), anyInt(), Mockito.nullable(int[].class), Mockito.anyBoolean());
        Mockito.verify(mMockKeyboardListener).onText(same(key), eq("popping"));
    }

    @Test
    public void testLongPressWhenNoPrimaryKeyButTextShouldOpenMiniKeyboard() throws Exception {
        ExternalAnyKeyboard anyKeyboard = new ExternalAnyKeyboard(
                new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application), RuntimeEnvironment.application,
                RuntimeEnvironment.application, R.xml.keyboard_with_keys_with_no_codes, R.xml.keyboard_with_keys_with_no_codes, "test", 0, 0,
                "en", "", "", Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        anyKeyboard.loadKeyboard(mViewUnderTest.mKeyboardDimens);
        mViewUnderTest.setKeyboard(anyKeyboard, 0);

        final AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) anyKeyboard.getKeys().get(6);

        Assert.assertEquals(0, key.getPrimaryCode());
        Assert.assertEquals(0, key.getCodesCount());
        Assert.assertEquals(R.xml.popup_16keys_wxyz, key.popupResId);
        Assert.assertEquals("popup", key.label);
        Assert.assertNull(key.popupCharacters);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 1000, true, false);

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        AnyKeyboardViewBase miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(6, miniKeyboard.getKeyboard().getKeys().size());

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(anyInt(), any(), anyInt(), any(), anyBoolean());
    }

    @Test
    public void testShortPressWhenNoPrimaryKeyAndNoPopupItemsButLongPressCodeShouldNotOutputLongPress() throws Exception {
        ExternalAnyKeyboard anyKeyboard = new ExternalAnyKeyboard(
                new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application), RuntimeEnvironment.application,
                RuntimeEnvironment.application, R.xml.keyboard_with_keys_with_no_codes, R.xml.keyboard_with_keys_with_no_codes, "test", 0, 0,
                "en", "", "", Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        anyKeyboard.loadKeyboard(mViewUnderTest.mKeyboardDimens);
        mViewUnderTest.setKeyboard(anyKeyboard, 0);

        final AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) anyKeyboard.getKeys().get(2);

        Assert.assertEquals(0, key.getPrimaryCode());
        Assert.assertEquals(0, key.getCodesCount());
        Assert.assertEquals(0, key.popupResId);
        Assert.assertEquals(45, key.longPressCode);
        Assert.assertEquals("c", key.label);
        Assert.assertNull(key.popupCharacters);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, true, false);

        Mockito.verifyZeroInteractions(mMockKeyboardListener);

        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 10, false, true);

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(eq(45), nullable(Keyboard.Key.class), anyInt(), Mockito.any(int[].class), Mockito.anyBoolean());
        Mockito.verify(mMockKeyboardListener).onKey(eq(0), same(key), eq(0), Mockito.any(int[].class), Mockito.anyBoolean());
    }

    @Test
    public void testLongPressWhenNoPrimaryKeyAndNoPopupItemsButLongPressCodeShouldOutputLongPress() throws Exception {
        ExternalAnyKeyboard anyKeyboard = new ExternalAnyKeyboard(
                new DefaultAddOn(RuntimeEnvironment.application, RuntimeEnvironment.application), RuntimeEnvironment.application,
                RuntimeEnvironment.application, R.xml.keyboard_with_keys_with_no_codes, R.xml.keyboard_with_keys_with_no_codes, "test", 0, 0,
                "en", "", "", Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        anyKeyboard.loadKeyboard(mViewUnderTest.mKeyboardDimens);
        mViewUnderTest.setKeyboard(anyKeyboard, 0);

        final AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) anyKeyboard.getKeys().get(2);

        Assert.assertEquals(0, key.getPrimaryCode());
        Assert.assertEquals(0, key.getCodesCount());
        Assert.assertEquals(0, key.popupResId);
        Assert.assertEquals(45, key.longPressCode);
        Assert.assertEquals("c", key.label);
        Assert.assertNull(key.popupCharacters);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 1000, true, false);

        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        Mockito.verify(mMockKeyboardListener).onKey(eq(45), same(key), eq(0), Mockito.nullable(int[].class), Mockito.anyBoolean());
    }

    @Test
    public void testLongPressKeyWithPopupCharacters() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        final Keyboard.Key key = findKey('w');
        Assert.assertTrue(key.popupCharacters.length() > 0);
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, false, mMockPointerTracker);

        Mockito.verify(mMockPointerTracker, Mockito.never()).onCancelEvent();
        Assert.assertEquals(0, mViewUnderTest.mPointerQueue.size());
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
        Mockito.verify(mMockKeyboardListener).onKey(eq(969), Mockito.any(Keyboard.Key.class), eq(0), Mockito.any(int[].class), eq(true));
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(eq((int) 'w'), Mockito.any(Keyboard.Key.class), Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyBoolean());
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
        Assert.assertEquals(11, miniKeyboard.getKeyboard().getKeys().size());
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