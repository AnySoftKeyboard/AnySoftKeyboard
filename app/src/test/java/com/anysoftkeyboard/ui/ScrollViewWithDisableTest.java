package com.anysoftkeyboard.ui;

import android.view.MotionEvent;
import android.view.View;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowMotionEvent;

@RunWith(AnySoftKeyboardTestRunner.class)
public class ScrollViewWithDisableTest {

    private ScrollViewWithDisable mUnderTest;

    @Before
    public void setup() {
        mUnderTest = new ScrollViewWithDisable(RuntimeEnvironment.application);
        mUnderTest.addView(new View(RuntimeEnvironment.application));
    }

    @Test
    public void testOnTouchEventEnabled() throws Exception {
        mUnderTest.setEnabled(true);

        Assert.assertTrue(mUnderTest.onTouchEvent(ShadowMotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, 1f, 1f, 0)));
    }

    @Test
    public void testOnTouchEventDisabled() throws Exception {
        mUnderTest.setEnabled(false);

        Assert.assertFalse(mUnderTest.onTouchEvent(ShadowMotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, 1f, 1f, 0)));
    }

    @Test
    public void onInterceptTouchEventDisabled() throws Exception {
        mUnderTest.setEnabled(false);

        Assert.assertFalse(mUnderTest.onInterceptTouchEvent(ShadowMotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, 1f, 1f, 0)));
    }

}