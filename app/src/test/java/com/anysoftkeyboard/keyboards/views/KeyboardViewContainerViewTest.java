package com.anysoftkeyboard.keyboards.views;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.view.LayoutInflater;
import android.view.View;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import androidx.test.core.app.ApplicationProvider;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class KeyboardViewContainerViewTest {

    private KeyboardViewContainerView mUnderTest;

    @Before
    public void setup() {
        mUnderTest = (KeyboardViewContainerView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.main_keyboard_layout, null, false);
    }

    @Test
    public void testDefaultInflation() {
        Assert.assertEquals(2, mUnderTest.getChildCount());
        Assert.assertTrue(mUnderTest.getChildAt(0) instanceof CandidateView);
        Assert.assertTrue(mUnderTest.getChildAt(1) instanceof AnyKeyboardView);
    }

    @Test
    public void testAddView() {
        AnyKeyboardView mock = Mockito.mock(AnyKeyboardView.class);
        mUnderTest.addView(mock);

        Assert.assertEquals(3, mUnderTest.getChildCount());
        Assert.assertSame(mock, mUnderTest.getChildAt(2));

        Mockito.verify(mock, Mockito.never()).setKeyboardTheme(Mockito.any());
        Mockito.verify(mock, Mockito.never()).setThemeOverlay(Mockito.any());
    }

    @Test
    public void testAddViewWhenHasThemeWasSet() {
        mUnderTest.setKeyboardTheme(AnyApplication.getKeyboardThemeFactory(ApplicationProvider.getApplicationContext()).getEnabledAddOn());
        AnyKeyboardView mock = Mockito.mock(AnyKeyboardView.class);
        mUnderTest.addView(mock);

        Assert.assertEquals(3, mUnderTest.getChildCount());
        Assert.assertSame(mock, mUnderTest.getChildAt(2));

        Mockito.verify(mock).setKeyboardTheme(Mockito.any());
        Mockito.verify(mock).setThemeOverlay(Mockito.any());
    }

    @Test
    public void testSetOnKeyboardActionListener() {
        AnyKeyboardView mock1 = Mockito.mock(AnyKeyboardView.class);
        AnyKeyboardView mock2 = Mockito.mock(AnyKeyboardView.class);

        mUnderTest.removeAllViews();

        mUnderTest.addView(mock1);

        Mockito.verify(mock1, Mockito.never()).setOnKeyboardActionListener(Mockito.any(OnKeyboardActionListener.class));

        final OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);

        mUnderTest.setOnKeyboardActionListener(listener);

        Mockito.verify(mock1).setOnKeyboardActionListener(listener);

        mUnderTest.addView(mock2);

        Mockito.verify(mock2).setOnKeyboardActionListener(listener);
    }

    @Test
    public void testGetStandardKeyboardView() {
        final InputViewBinder originalView = mUnderTest.getStandardKeyboardView();
        Assert.assertNotNull(originalView);
        Assert.assertTrue(originalView instanceof AnyKeyboardView);

        AnyKeyboardView mock1 = Mockito.mock(AnyKeyboardView.class);
        AnyKeyboardView mock2 = Mockito.mock(AnyKeyboardView.class);

        mUnderTest.addView(mock1);
        mUnderTest.addView(mock2);

        Assert.assertSame(originalView, mUnderTest.getStandardKeyboardView());
    }

    @Test
    public void testGetCandidateView() {
        final CandidateView originalView = mUnderTest.getCandidateView();
        Assert.assertNotNull(originalView);

        AnyKeyboardView mock2 = Mockito.mock(AnyKeyboardView.class);

        mUnderTest.addView(mock2);

        Assert.assertSame(originalView, mUnderTest.getCandidateView());

        mUnderTest.removeView(mock2);

        Assert.assertSame(originalView, mUnderTest.getCandidateView());
    }

    @Test
    public void testAddRemoveAction() {
        View view = new View(mUnderTest.getContext());
        KeyboardViewContainerView.StripActionProvider provider = Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
        Mockito.doReturn(view).when(provider).inflateActionView(Mockito.any());

        mUnderTest.addStripAction(provider);

        Mockito.verify(provider).inflateActionView(mUnderTest);
        Mockito.verify(provider, Mockito.never()).onRemoved();
        Assert.assertEquals(3, mUnderTest.getChildCount());
        Assert.assertSame(view, mUnderTest.getChildAt(2));

        mUnderTest.removeStripAction(provider);
        Mockito.verify(provider).onRemoved();
        Assert.assertEquals(2, mUnderTest.getChildCount());
    }

    @Test
    public void testDoubleAddDoesNotAddAgain() {
        View view = new View(mUnderTest.getContext());
        KeyboardViewContainerView.StripActionProvider provider = Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
        Mockito.doReturn(view).when(provider).inflateActionView(Mockito.any());

        mUnderTest.addStripAction(provider);
        mUnderTest.addStripAction(provider);

        Mockito.verify(provider).inflateActionView(mUnderTest);
        Mockito.verify(provider, Mockito.never()).onRemoved();
        Assert.assertEquals(3, mUnderTest.getChildCount());
        Assert.assertSame(view, mUnderTest.getChildAt(2));
    }
}