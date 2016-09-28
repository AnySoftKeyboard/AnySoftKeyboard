package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.widget.PopupWindow;

import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class AnyKeyboardViewWithMiniKeyboardTest {

    private OnKeyboardActionListener mMockKeyboardListener;
    private TestAnyKeyboardViewWithMiniKeyboard mViewUnderTest;
    private AnyKeyboard mEnglishKeyboard;

    @Before
    public void setUp() throws Exception {
        mMockKeyboardListener = Mockito.mock(OnKeyboardActionListener.class);
        mViewUnderTest = new TestAnyKeyboardViewWithMiniKeyboard(RuntimeEnvironment.application);
        mViewUnderTest.setOnKeyboardActionListener(mMockKeyboardListener);

        mEnglishKeyboard = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application)
                .get(0)
                .createKeyboard(RuntimeEnvironment.application, Keyboard.KEYBOARD_MODE_NORMAL);
        mEnglishKeyboard.loadKeyboard(mViewUnderTest.getThemedKeyboardDimens());

        mViewUnderTest.setKeyboard(mEnglishKeyboard, 0);
    }

    @Test
    public void testLongPressKeyWithPopupCharacters() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.getPopupWindow().isShowing());
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), mEnglishKeyboard.getKeys().get(5), false);

        Assert.assertTrue(mViewUnderTest.getPopupWindow().isShowing());
        AnyKeyboardBaseView miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(2, miniKeyboard.getKeyboard().getKeys().size());
    }

    @Test
    public void testLongPressKeyWithPopupLayout() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.getPopupWindow().isShowing());
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), mEnglishKeyboard.getKeys().get(6), false);

        Assert.assertTrue(mViewUnderTest.getPopupWindow().isShowing());
        AnyKeyboardBaseView miniKeyboard = mViewUnderTest.getMiniKeyboard();
        Assert.assertNotNull(miniKeyboard);
        Assert.assertNotNull(miniKeyboard.getKeyboard());
        Assert.assertEquals(8, miniKeyboard.getKeyboard().getKeys().size());
    }

    @Test
    public void testLongPressKeyWithoutAny() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.getPopupWindow().isShowing());
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), mEnglishKeyboard.getKeys().get(17), false);

        Assert.assertFalse(mViewUnderTest.getPopupWindow().isShowing());
    }

    @Test
    public void testNonStickyPopupDismissedAfterUpEvent() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.getPopupWindow().isShowing());
        final Keyboard.Key key = mEnglishKeyboard.getKeys().get(6);
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, false);

        Assert.assertTrue(mViewUnderTest.getPopupWindow().isShowing());

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);
        mViewUnderTest.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(),
                MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertFalse(mViewUnderTest.getPopupWindow().isShowing());
    }

    @Test
    public void testStickyPopupStaysAroundAfterUpEvent() throws Exception {
        Assert.assertNull(mViewUnderTest.getMiniKeyboard());
        Assert.assertFalse(mViewUnderTest.getPopupWindow().isShowing());
        final Keyboard.Key key = mEnglishKeyboard.getKeys().get(6);
        mViewUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, true);

        Assert.assertTrue(mViewUnderTest.getPopupWindow().isShowing());

        Point keyPoint = ViewTestUtils.getKeyCenterPoint(key);
        mViewUnderTest.onTouchEvent(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(),
                MotionEvent.ACTION_UP, keyPoint.x, keyPoint.y, 0));

        Assert.assertTrue(mViewUnderTest.getPopupWindow().isShowing());

        //but gets dismissed when cancel is called
        mViewUnderTest.closing();
        Assert.assertFalse(mViewUnderTest.getPopupWindow().isShowing());
    }

    private static class TestAnyKeyboardViewWithMiniKeyboard extends AnyKeyboardViewWithMiniKeyboard {

        public TestAnyKeyboardViewWithMiniKeyboard(Context context) {
            super(context, null);
        }

        public PopupWindow getPopupWindow() {
            return mMiniKeyboardPopup;
        }
    }

}