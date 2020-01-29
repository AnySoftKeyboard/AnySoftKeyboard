package com.anysoftkeyboard.keyboards.views;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;

import android.content.Context;
import android.graphics.Point;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.MotionEvent;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.shadows.ShadowToast;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnyKeyboardViewBaseTest {
    OnKeyboardActionListener mMockKeyboardListener;
    AnyKeyboard mEnglishKeyboard;
    private AnyKeyboardViewBase mUnderTest;
    private PointerTracker mMockPointerTrack;

    @Before
    public void setUp() throws Exception {
        mMockPointerTrack = Mockito.mock(PointerTracker.class);
        mMockKeyboardListener = Mockito.mock(OnKeyboardActionListener.class);
        AnyKeyboardViewBase view = createViewToTest(getApplicationContext());
        Assert.assertTrue(view.willNotDraw());
        view.setKeyboardTheme(
                AnyApplication.getKeyboardThemeFactory(getApplicationContext()).getEnabledAddOn());
        setCreatedKeyboardView(view);
        mUnderTest.setOnKeyboardActionListener(mMockKeyboardListener);
        Assert.assertTrue(view.willNotDraw());

        mEnglishKeyboard =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOn()
                        .createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        mEnglishKeyboard.loadKeyboard(mUnderTest.getThemedKeyboardDimens());

        mUnderTest.setKeyboard(mEnglishKeyboard, 0);
        Assert.assertFalse(view.willNotDraw());
    }

    @CallSuper
    protected void setCreatedKeyboardView(@NonNull AnyKeyboardViewBase view) {
        mUnderTest = view;
    }

    protected AnyKeyboardViewBase createViewToTest(Context context) {
        return new AnyKeyboardViewBase(context, null);
    }

    @Test
    public void testDoesNotCrashWhenSettingTheme() {
        final KeyboardThemeFactory keyboardThemeFactory =
                AnyApplication.getKeyboardThemeFactory(getApplicationContext());
        mUnderTest.setKeyboardTheme(keyboardThemeFactory.getAllAddOns().get(2));
        mUnderTest.setKeyboardTheme(keyboardThemeFactory.getAllAddOns().get(5));
        mUnderTest.setKeyboardTheme(keyboardThemeFactory.getAllAddOns().get(1));
    }

    @Test
    public void testKeyboardViewCreated() {
        Assert.assertNotNull(mUnderTest);
    }

    @Test
    public void testLongPressOutput() {
        AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(5);
        key.longPressCode = 'z';
        mUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, false, mMockPointerTrack);

        Mockito.verify(mMockPointerTrack).onCancelEvent();
        Mockito.verify(mMockKeyboardListener)
                .onKey(
                        eq((int) 'z'),
                        Mockito.same(key),
                        eq(0),
                        Mockito.nullable(int[].class),
                        eq(true));
        Mockito.verify(mMockKeyboardListener, Mockito.never())
                .onKey(
                        eq(key.getPrimaryCode()),
                        Mockito.any(Keyboard.Key.class),
                        Mockito.anyInt(),
                        Mockito.nullable(int[].class),
                        Mockito.anyBoolean());
    }

    @Test
    public void testLongPressCallback() {
        AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(15);
        key.longPressCode = 'z';

        ViewTestUtils.navigateFromTo(mUnderTest, key, key, 1000, true, false);

        Mockito.verify(mMockKeyboardListener).onLongPressDone(same(key));
    }

    @Test
    public void testNotLongPressCallback() {
        AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(15);
        key.longPressCode = 'z';

        ViewTestUtils.navigateFromTo(mUnderTest, key, key, 100, true, true);

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onLongPressDone(any());
    }

    @Test
    public void testNotLongPressKeyCallback() {
        AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(15);
        key.longPressCode = 0;
        key.popupResId = 0;
        key.popupCharacters = "";

        ViewTestUtils.navigateFromTo(mUnderTest, key, key, 1000, true, true);

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onLongPressDone(any());
    }

    @Test
    public void testLongPressOutputTagsToast() {
        AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
        Mockito.doReturn(Arrays.asList("tag", "tag2")).when(key).getKeyTags();

        mUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, false, mMockPointerTrack);
        Mockito.verify(mMockPointerTrack, Mockito.never()).onCancelEvent();
        Mockito.verify(mMockKeyboardListener, Mockito.never())
                .onKey(
                        Mockito.anyInt(),
                        Mockito.any(Keyboard.Key.class),
                        Mockito.anyInt(),
                        Mockito.any(int[].class),
                        Mockito.anyBoolean());
        Assert.assertEquals(":tag, :tag2", ShadowToast.getTextOfLatestToast());
        Assert.assertEquals(Gravity.CENTER, ShadowToast.getLatestToast().getGravity());
    }

    @Test
    public void testLongPressKeyPressState() {
        final Keyboard.Key key = findKey('f');
        KeyDrawableStateProvider provider =
                new KeyDrawableStateProvider(
                        R.attr.key_type_function,
                        R.attr.key_type_action,
                        R.attr.action_done,
                        R.attr.action_search,
                        R.attr.action_go);
        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);

        ViewTestUtils.navigateFromTo(mUnderTest, keyPoint, keyPoint, 400, true, false);
        Assert.assertArrayEquals(provider.KEY_STATE_PRESSED, key.getCurrentDrawableState(provider));

        mUnderTest.onTouchEvent(
                MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP,
                        keyPoint.x,
                        keyPoint.y,
                        0));

        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));
    }

    @Test
    public void testRegularPressKeyPressState() {
        final Keyboard.Key key = findKey('f');
        KeyDrawableStateProvider provider =
                new KeyDrawableStateProvider(
                        R.attr.key_type_function,
                        R.attr.key_type_action,
                        R.attr.action_done,
                        R.attr.action_search,
                        R.attr.action_go);
        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);

        ViewTestUtils.navigateFromTo(mUnderTest, keyPoint, keyPoint, 60, true, false);
        Assert.assertArrayEquals(provider.KEY_STATE_PRESSED, key.getCurrentDrawableState(provider));

        mUnderTest.onTouchEvent(
                MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP,
                        keyPoint.x,
                        keyPoint.y,
                        0));

        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));
    }

    @Test
    public void testWithLongPressOutputLongPressKeyPressState() {
        final AnyKeyboard.AnyKey key = findKey('f');
        key.longPressCode = 'z';
        KeyDrawableStateProvider provider =
                new KeyDrawableStateProvider(
                        R.attr.key_type_function,
                        R.attr.key_type_action,
                        R.attr.action_done,
                        R.attr.action_search,
                        R.attr.action_go);
        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);

        ViewTestUtils.navigateFromTo(mUnderTest, keyPoint, keyPoint, 80, true, false);
        Assert.assertArrayEquals(provider.KEY_STATE_PRESSED, key.getCurrentDrawableState(provider));
        ViewTestUtils.navigateFromTo(mUnderTest, keyPoint, keyPoint, 300, false, false);
        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));

        mUnderTest.onTouchEvent(
                MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP,
                        keyPoint.x,
                        keyPoint.y,
                        0));

        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));
    }

    @Test
    public void testWithLongPressOutputRegularPressKeyPressState() {
        final AnyKeyboard.AnyKey key = findKey('f');
        key.longPressCode = 'z';
        KeyDrawableStateProvider provider =
                new KeyDrawableStateProvider(
                        R.attr.key_type_function,
                        R.attr.key_type_action,
                        R.attr.action_done,
                        R.attr.action_search,
                        R.attr.action_go);
        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);

        ViewTestUtils.navigateFromTo(mUnderTest, keyPoint, keyPoint, 60, true, false);
        Assert.assertArrayEquals(provider.KEY_STATE_PRESSED, key.getCurrentDrawableState(provider));

        mUnderTest.onTouchEvent(
                MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP,
                        keyPoint.x,
                        keyPoint.y,
                        0));

        Assert.assertArrayEquals(provider.KEY_STATE_NORMAL, key.getCurrentDrawableState(provider));
    }

    @Test
    public void testDefaultAutoCase() {
        final AnyKeyboard.AnyKey fKey = findKey('f');
        mUnderTest.getKeyboard().setShifted(false);

        Assert.assertEquals("f", mUnderTest.adjustLabelToShiftState(fKey));

        mUnderTest.getKeyboard().setShifted(true);
        Assert.assertEquals("F", mUnderTest.adjustLabelToShiftState(fKey));
    }

    @Test
    public void testThemeUpperCase() {
        final AnyKeyboard.AnyKey fKey = findKey('f');
        mUnderTest.getKeyboard().setShifted(false);

        mUnderTest.setKeyboardTheme(
                AnyApplication.getKeyboardThemeFactory(getApplicationContext())
                        .getAddOnById("8a56f044-22d3-480a-9221-f3b7a9c85905"));

        Assert.assertEquals("F", mUnderTest.adjustLabelToShiftState(fKey));

        mUnderTest.getKeyboard().setShifted(true);
        Assert.assertEquals("F", mUnderTest.adjustLabelToShiftState(fKey));
    }

    @Test
    public void testCaseOverrideToAlwaysUpper() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_theme_case_type_override, "upper");

        final AnyKeyboard.AnyKey fKey = findKey('f');
        mUnderTest.getKeyboard().setShifted(false);

        Assert.assertEquals("F", mUnderTest.adjustLabelToShiftState(fKey));

        mUnderTest.getKeyboard().setShifted(true);
        Assert.assertEquals("F", mUnderTest.adjustLabelToShiftState(fKey));
    }

    @Test
    public void testCaseOverrideToAlwaysLower() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_theme_case_type_override, "lower");

        final AnyKeyboard.AnyKey fKey = findKey('f');
        mUnderTest.getKeyboard().setShifted(false);

        Assert.assertEquals("f", mUnderTest.adjustLabelToShiftState(fKey));

        mUnderTest.getKeyboard().setShifted(true);
        Assert.assertEquals("f", mUnderTest.adjustLabelToShiftState(fKey));
    }

    @Test
    public void testCaseOverrideToAuto() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_theme_case_type_override, "auto");

        mUnderTest.setKeyboardTheme(
                AnyApplication.getKeyboardThemeFactory(getApplicationContext())
                        .getAddOnById("8a56f044-22d3-480a-9221-f3b7a9c85905"));

        final AnyKeyboard.AnyKey fKey = findKey('f');
        mUnderTest.getKeyboard().setShifted(false);

        Assert.assertEquals("f", mUnderTest.adjustLabelToShiftState(fKey));

        mUnderTest.getKeyboard().setShifted(true);
        Assert.assertEquals("F", mUnderTest.adjustLabelToShiftState(fKey));
    }

    @Nullable
    protected AnyKeyboard.AnyKey findKey(int codeToFind) {
        final int index = findKeyIndex(codeToFind);
        if (index == -1) {
            return null;
        } else {
            return (AnyKeyboard.AnyKey) mUnderTest.getKeyboard().getKeys().get(index);
        }
    }

    protected int findKeyIndex(int codeToFind) {
        Keyboard keyboard = mUnderTest.getKeyboard();
        if (keyboard == null) return -1;
        List<Keyboard.Key> keys = keyboard.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            Keyboard.Key key = keys.get(i);
            if (key.getPrimaryCode() == codeToFind) return i;
        }

        return -1;
    }
}
