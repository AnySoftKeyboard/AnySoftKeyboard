package com.anysoftkeyboard.keyboards.views;

import static android.os.SystemClock.sleep;

import static com.anysoftkeyboard.keyboards.Keyboard.EDGE_LEFT;
import static com.anysoftkeyboard.keyboards.Keyboard.EDGE_RIGHT;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.widget.PopupWindow;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.extradraw.ExtraDraw;
import com.anysoftkeyboard.keyboards.views.preview.KeyPreviewsController;
import com.anysoftkeyboard.keyboards.views.preview.PreviewPopupTheme;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;

import java.util.Arrays;
import java.util.List;

import androidx.test.core.app.ApplicationProvider;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnyKeyboardViewTest extends AnyKeyboardViewWithMiniKeyboardTest {

    private AnyKeyboardView mViewUnderTest;
    private boolean mThemeWasSet;
    private KeyPreviewsController mSpiedPreviewManager;

    @Override
    protected AnyKeyboardViewBase createViewToTest(Context context) {
        return new AnyKeyboardView(context, null) {

            @Override
            protected boolean setValueFromTheme(TypedArray remoteTypedArray, int[] padding, int localAttrId, int remoteTypedArrayIndex) {
                mThemeWasSet = true;
                return super.setValueFromTheme(remoteTypedArray, padding, localAttrId, remoteTypedArrayIndex);
            }

            @Override
            protected KeyPreviewsController createKeyPreviewManager(Context context, PreviewPopupTheme previewPopupTheme) {
                return mSpiedPreviewManager = Mockito.spy(super.createKeyPreviewManager(context, previewPopupTheme));
            }
        };
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

        MotionEvent motionEvent = MotionEvent.obtain(100, 100, MotionEvent.ACTION_DOWN, key.centerX, key.centerY, 0);
        mViewUnderTest.onTouchEvent(motionEvent);
        motionEvent.recycle();
        Mockito.verify(mMockKeyboardListener).onPress(primaryCode);
        Mockito.verify(mMockKeyboardListener).onFirstDownKey(primaryCode);
        Mockito.verify(mMockKeyboardListener).onGestureTypingInputStart(eq(key.centerX), eq(key.centerY), same(key), anyLong());
        Mockito.verifyNoMoreInteractions(mMockKeyboardListener);

        Mockito.reset(mMockKeyboardListener);

        motionEvent = MotionEvent.obtain(100, 110, MotionEvent.ACTION_UP, key.centerX, key.centerY, 0);
        mViewUnderTest.onTouchEvent(motionEvent);
        motionEvent.recycle();
        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onKey(eq(primaryCode), same(key), eq(0), any(int[].class), eq(true));
        inOrder.verify(mMockKeyboardListener).onRelease(primaryCode);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testDisregardIfSameTheme() {
        final KeyboardThemeFactory keyboardThemeFactory = AnyApplication.getKeyboardThemeFactory(getApplicationContext());
        Assert.assertTrue(mThemeWasSet);
        mThemeWasSet = false;
        mViewUnderTest.setKeyboardTheme(keyboardThemeFactory.getAllAddOns().get(2));
        Assert.assertTrue(mThemeWasSet);
        mThemeWasSet = false;
        mViewUnderTest.setKeyboardTheme(keyboardThemeFactory.getAllAddOns().get(2));
        Assert.assertFalse(mThemeWasSet);

        mViewUnderTest.setKeyboardTheme(keyboardThemeFactory.getAllAddOns().get(3));
        Assert.assertTrue(mThemeWasSet);
    }

    @Test
    public void testKeyClickDomain() {
        mEnglishKeyboard = AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn()
                .createKeyboard(Keyboard.KEYBOARD_ROW_MODE_URL);
        mEnglishKeyboard.loadKeyboard(mViewUnderTest.getThemedKeyboardDimens());

        mViewUnderTest.setKeyboard(mEnglishKeyboard, 0);

        AnyKeyboard.AnyKey key = findKey(KeyCodes.DOMAIN);
        Assert.assertNotNull(key);
        Mockito.reset(mMockKeyboardListener);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 30, true, true);

        Mockito.verify(mMockKeyboardListener).onText(same(key), eq(".com"));
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(anyInt(), any(), anyInt(), any(), anyBoolean());
        Mockito.reset(mMockKeyboardListener);

        Assert.assertNull(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow());

        ViewTestUtils.navigateFromTo(mViewUnderTest, key, key, 1000, true, false);

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onText(any(), any());
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(anyInt(), any(), anyInt(), any(), anyBoolean());
        Mockito.reset(mMockKeyboardListener);

        PopupWindow currentlyShownPopup = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow();
        Assert.assertNotNull(currentlyShownPopup);
        Assert.assertTrue(currentlyShownPopup.isShowing());
        AnyKeyboardViewBase miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
    }

    @Test
    public void testTouchIsDisabledOnGestureUntilAllPointersAreUp() {
        final int primaryKey1 = 'a';
        final int keyAIndex = findKeyIndex(primaryKey1);
        final int keyDIndex = findKeyIndex('d');
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
        for (int keyIndex = keyAIndex; keyIndex < keyDIndex; keyIndex++) {
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
        inOrder.verify(mMockKeyboardListener).onKey(eq(primaryKey2), same(key2), eq(0), any(int[].class), eq(true));
        inOrder.verify(mMockKeyboardListener).onRelease(primaryKey2);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testSlideToExtensionKeyboard() {
        sleep(1225);
        Assert.assertNull(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow());
        ViewTestUtils.navigateFromTo(mViewUnderTest, new Point(10, 10), new Point(10, -20), 200, true, false);

        PopupWindow currentlyShownPopup = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow();
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
        sleep(1225);
        Assert.assertNull(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow());
        ViewTestUtils.navigateFromTo(mViewUnderTest, new Point(10, 10), new Point(10, -20), 200, true, false);

        PopupWindow currentlyShownPopup = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow();
        Assert.assertNull(currentlyShownPopup);
    }

    @Test
    public void testSwipeUpToUtilitiesKeyboard() {
        sleep(1225);
        Assert.assertNull(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow());
        //flinging up
        final Keyboard.Key spaceKey = findKey(' ');
        final Point upPoint = ViewTestUtils.getKeyCenterPoint(spaceKey);
        upPoint.offset(0, -(mViewUnderTest.mSwipeYDistanceThreshold + 1));
        Assert.assertFalse(mViewUnderTest.areTouchesDisabled(null));
        ViewTestUtils.navigateFromTo(mViewUnderTest, ViewTestUtils.getKeyCenterPoint(spaceKey), upPoint, 30, true, true);

        Mockito.verify(mMockKeyboardListener).onFirstDownKey(' ');
        Mockito.verify(mMockKeyboardListener).onSwipeUp();

        mViewUnderTest.openUtilityKeyboard();

        PopupWindow currentlyShownPopup = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow();
        Assert.assertNotNull(currentlyShownPopup);
        Assert.assertTrue(currentlyShownPopup.isShowing());
        AnyKeyboardViewBase miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(21, miniKeyboard.getKeyboard().getKeys().size());

        //hiding
        mViewUnderTest.resetInputView();
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
        Mockito.verify(mMockKeyboardListener).onKey(eq(KeyCodes.QUICK_TEXT_POPUP), same(quickTextPopupKey), eq(0), Mockito.nullable(int[].class), eq(true));
    }

    @Test
    public void testLongPressEnter() throws Exception {
        AnyKeyboard.AnyKey enterKey = findKey(KeyCodes.ENTER);
        Assert.assertNotNull(enterKey);
        Assert.assertEquals(KeyCodes.ENTER, enterKey.getPrimaryCode());
        Assert.assertEquals(KeyCodes.SETTINGS, enterKey.longPressCode);

        ViewTestUtils.navigateFromTo(mViewUnderTest, enterKey, enterKey, 400, true, true);
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(eq(KeyCodes.ENTER), any(Keyboard.Key.class), Mockito.anyInt(), any(int[].class), Mockito.anyBoolean());
        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onPress(KeyCodes.ENTER);
        inOrder.verify(mMockKeyboardListener).onKey(eq(KeyCodes.SETTINGS), same(enterKey), Mockito.anyInt(), Mockito.nullable(int[].class), Mockito.anyBoolean());
        inOrder.verify(mMockKeyboardListener).onLongPressDone(same(enterKey));
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
        Mockito.verify(mMockKeyboardListener).onKey(eq((int) 'a'), same(edgeKey), eq(0), any(int[].class), eq(true));
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
    public void testDoesNotAddExtraDrawIfAnimationsAreOff() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "none");
        ExtraDraw mockDraw1 = Mockito.mock(ExtraDraw.class);
        Mockito.doReturn(true).when(mockDraw1).onDraw(any(), any(), same(mViewUnderTest));

        Robolectric.getForegroundThreadScheduler().pause();
        Assert.assertFalse(Robolectric.getForegroundThreadScheduler().areAnyRunnable());
        mViewUnderTest.addExtraDraw(mockDraw1);

        Mockito.verify(mockDraw1, Mockito.never()).onDraw(any(), any(), any());

        Assert.assertEquals(0, Robolectric.getForegroundThreadScheduler().size());
    }

    @Test
    public void testDoesNotAddExtraDrawIfRtlWorkaround() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_workaround_disable_rtl_fix, false);
        ExtraDraw mockDraw1 = Mockito.mock(ExtraDraw.class);
        Mockito.doReturn(true).when(mockDraw1).onDraw(any(), any(), same(mViewUnderTest));

        Robolectric.getForegroundThreadScheduler().pause();
        Assert.assertFalse(Robolectric.getForegroundThreadScheduler().areAnyRunnable());
        mViewUnderTest.addExtraDraw(mockDraw1);

        Mockito.verify(mockDraw1, Mockito.never()).onDraw(any(), any(), any());

        Assert.assertEquals(0, Robolectric.getForegroundThreadScheduler().size());
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

    @Test
    public void testPreviewsShouldBeClearedOnThemeSet() {
        Mockito.reset(mSpiedPreviewManager);

        mViewUnderTest.setKeyboardTheme(AnyApplication.getKeyboardThemeFactory(getApplicationContext()).getAllAddOns().get(1));

        Mockito.verify(mSpiedPreviewManager).resetTheme();
        Mockito.verify(mSpiedPreviewManager, Mockito.never()).destroy();
    }

    @Test
    public void testWatermarkSetsBounds() {
        final int dimen = getApplicationContext().getResources().getDimensionPixelOffset(R.dimen.watermark_size);

        List<Drawable> watermarks = Arrays.asList(Mockito.mock(Drawable.class), Mockito.mock(Drawable.class));
        mViewUnderTest.setWatermark(watermarks);
        for (Drawable watermark : watermarks) {
            Mockito.verify(watermark).setBounds(0, 0, dimen, dimen);
        }
    }

    @Test
    public void testWatermarkDrawn() {
        List<Drawable> watermarks = Arrays.asList(Mockito.mock(Drawable.class), Mockito.mock(Drawable.class));
        mViewUnderTest.setWatermark(watermarks);

        final Canvas canvas = Mockito.mock(Canvas.class);
        mViewUnderTest.onDraw(canvas);

        for (Drawable watermark : watermarks) {
            Mockito.verify(watermark).draw(canvas);
        }

        final int dimen = getApplicationContext().getResources().getDimensionPixelOffset(R.dimen.watermark_size);
        final int margin = getApplicationContext().getResources().getDimensionPixelOffset(R.dimen.watermark_margin);
        final int y = mViewUnderTest.getHeight() - dimen - margin;
        final int x = 479;//location of the edge of the last key
        final InOrder inOrder = Mockito.inOrder(canvas);

        inOrder.verify(canvas).translate(x - dimen - margin, y);
        inOrder.verify(canvas).translate(-x + dimen + margin, -y);
        inOrder.verify(canvas).translate(x - dimen - dimen - margin - margin, y);
        inOrder.verify(canvas).translate(-x + dimen + dimen + margin + margin, -y);
    }
}