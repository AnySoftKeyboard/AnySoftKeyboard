package com.anysoftkeyboard.keyboards.views;

import android.view.LayoutInflater;
import android.view.View;

import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class KeyboardViewContainerViewTest {

    private KeyboardViewContainerView mUnderTest;

    @Before
    public void setup() {
        mUnderTest = (KeyboardViewContainerView) LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.main_keyboard_layout, null, false);
    }

    @Test
    public void testDefaultInflation() {
        Assert.assertEquals(1, mUnderTest.getChildCount());
        View child = mUnderTest.getChildAt(0);
        Assert.assertNotNull(child);
        Assert.assertTrue(child instanceof AnyKeyboardView);
    }

    @Test
    public void testAddView() {
        AnyKeyboardView mock = Mockito.mock(AnyKeyboardView.class);
        mUnderTest.addView(mock);

        Assert.assertEquals(2, mUnderTest.getChildCount());
        Assert.assertSame(mock, mUnderTest.getChildAt(1));
        Assert.assertNotSame(mock, mUnderTest.getChildAt(0));

        mUnderTest.removeView(mUnderTest.getChildAt(0));
        Assert.assertEquals(1, mUnderTest.getChildCount());
        Assert.assertSame(mock, mUnderTest.getChildAt(0));
    }

    @Test
    public void testSetOnKeyboardActionListener() {
        AnyKeyboardView mock1 = Mockito.mock(AnyKeyboardView.class);
        AnyKeyboardView mock2 = Mockito.mock(AnyKeyboardView.class);

        OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);
        mUnderTest.removeAllViews();

        mUnderTest.addView(mock1);

        Mockito.verify(mock1, Mockito.never()).setOnKeyboardActionListener(Mockito.any(OnKeyboardActionListener.class));

        mUnderTest.setOnKeyboardActionListener(listener);

        Mockito.verify(mock1).setOnKeyboardActionListener(listener);

        mUnderTest.addView(mock2);

        Mockito.verify(mock2).setOnKeyboardActionListener(listener);
    }

    @Test
    public void testSetShifted() {
        AnyKeyboardView mock1 = Mockito.mock(AnyKeyboardView.class);
        AnyKeyboardView mock2 = Mockito.mock(AnyKeyboardView.class);

        mUnderTest.removeAllViews();
        mUnderTest.addView(mock1);
        mUnderTest.addView(mock2);

        mUnderTest.setShifted(true);
        Mockito.verify(mock2).setShifted(true);
        Mockito.verify(mock1, Mockito.never()).setShifted(Mockito.anyBoolean());

        mUnderTest.removeView(mock2);

        Mockito.reset(mock1, mock2);

        mUnderTest.setShifted(true);

        Mockito.verify(mock1).setShifted(true);
        Mockito.verify(mock2, Mockito.never()).setShifted(Mockito.anyBoolean());
    }
}