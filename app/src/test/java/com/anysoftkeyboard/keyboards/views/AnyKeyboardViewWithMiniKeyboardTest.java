package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.keyboards.Keyboard;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AnyKeyboardViewWithMiniKeyboardTest extends AnyKeyboardViewBaseTest {

    private AnyKeyboardViewWithMiniKeyboard mViewUnderTest;

    @Override
    protected void setCreatedKeyboardView(@NonNull AnyKeyboardBaseView view) {
        super.setCreatedKeyboardView(view);
        mViewUnderTest = (AnyKeyboardViewWithMiniKeyboard) view;
    }

    @Override
    protected AnyKeyboardBaseView createViewToTest(Context context) {
        return new AnyKeyboardViewWithMiniKeyboard(context, null);
    }

    @Test
    public void testLongPressKeyWithPopupCharacters() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), mEnglishKeyboard.getKeys().get(5), false);

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        AnyKeyboardBaseView miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(2, miniKeyboard.getKeyboard().getKeys().size());
    }

    @Test
    public void testLongPressWithPopupDoesNotOutputPrimaryCode() throws Exception {
        final Keyboard.Key key = mEnglishKeyboard.getKeys().get(5);

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);
        ViewTestUtils.navigateFromTo(mViewUnderTest, keyPoint, keyPoint, 400, true, false);
        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(Mockito.anyInt(), Mockito.any(Keyboard.Key.class), Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyBoolean());

        mViewUnderTest.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(),
                MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        //not sure about this. Maybe the output should be the first key in the popup
        //FIXME: suppose to be '2' and not 'ŵ'
        Mockito.verify(mMockKeyboardListener).onKey(Mockito.eq((int)'ŵ'), Mockito.any(Keyboard.Key.class), Mockito.eq(0), Mockito.any(int[].class), Mockito.eq(true));
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(Mockito.eq((int)'w'), Mockito.any(Keyboard.Key.class), Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyBoolean());
    }

    @Test
    public void testLongPressKeyWithoutAny() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), mEnglishKeyboard.getKeys().get(17), false);

        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
    }

    @Test
    public void testLongPressKeyWithPopupLayout() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), mEnglishKeyboard.getKeys().get(6), false);

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        AnyKeyboardBaseView miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(8, miniKeyboard.getKeyboard().getKeys().size());
    }

    @Test
    public void testNonStickyPopupDismissedAfterUpEvent() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        final Keyboard.Key key = mEnglishKeyboard.getKeys().get(6);
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, false);

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);
        mViewUnderTest.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(),
                MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
    }

    @Test
    public void testStickyPopupStaysAroundAfterUpEvent() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
        final Keyboard.Key key = mEnglishKeyboard.getKeys().get(6);
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, true);

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);
        mViewUnderTest.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(),
                MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertTrue(mViewUnderTest.mMiniKeyboardPopup.isShowing());

        //but gets dismissed when cancel is called
        mViewUnderTest.closing();
        Assert.assertFalse(mViewUnderTest.mMiniKeyboardPopup.isShowing());
    }

}