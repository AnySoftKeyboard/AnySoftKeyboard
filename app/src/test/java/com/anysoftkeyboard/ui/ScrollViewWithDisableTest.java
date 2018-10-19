package com.anysoftkeyboard.ui;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.view.MotionEvent;
import android.view.View;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ScrollViewWithDisableTest {

    private ScrollViewWithDisable mUnderTest;

    @Before
    public void setup() {
        mUnderTest = new ScrollViewWithDisable(getApplicationContext());
        mUnderTest.addView(new View(getApplicationContext()));
    }

    @Test
    public void testOnTouchEventEnabled() throws Exception {
        mUnderTest.setEnabled(true);

        Assert.assertTrue(mUnderTest.onTouchEvent(MotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, 1f, 1f, 0)));
    }

    @Test
    public void testOnTouchEventDisabled() throws Exception {
        mUnderTest.setEnabled(false);

        Assert.assertFalse(mUnderTest.onTouchEvent(MotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, 1f, 1f, 0)));
    }

    @Test
    public void onInterceptTouchEventDisabled() throws Exception {
        mUnderTest.setEnabled(false);

        Assert.assertFalse(mUnderTest.onInterceptTouchEvent(MotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, 1f, 1f, 0)));
    }

}