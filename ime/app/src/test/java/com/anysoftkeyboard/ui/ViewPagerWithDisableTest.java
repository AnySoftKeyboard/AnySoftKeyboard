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
public class ViewPagerWithDisableTest {
    private ViewPagerWithDisable mUnderTest;

    @Before
    public void setup() {
        mUnderTest = new ViewPagerWithDisable(getApplicationContext());
        mUnderTest.addView(new View(getApplicationContext()));
    }

    @Test
    public void testOnTouchEventDisabled() throws Exception {
        mUnderTest.setEnabled(false);

        Assert.assertFalse(
                mUnderTest.onTouchEvent(
                        MotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, 1f, 1f, 0)));
    }

    @Test
    public void onInterceptTouchEventDisabled() throws Exception {
        mUnderTest.setEnabled(false);

        Assert.assertFalse(
                mUnderTest.onInterceptTouchEvent(
                        MotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, 1f, 1f, 0)));
    }
}
