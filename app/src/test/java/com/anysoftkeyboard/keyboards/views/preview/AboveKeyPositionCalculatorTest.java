package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.keyboards.Keyboard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AboveKeyPositionCalculatorTest {
    private AboveKeyPositionCalculator mUnderTest;
    private Keyboard.Key mTestKey;
    private PreviewPopupTheme mTheme;

    @Before
    public void setup() {
        mUnderTest = new AboveKeyPositionCalculator();
        mTestKey = Mockito.mock(Keyboard.Key.class);
        mTestKey.x = 12;
        mTestKey.y = 11;
        mTestKey.width = 10;
        mTestKey.height = 20;
        mTheme = new PreviewPopupTheme();
        Drawable background = Mockito.mock(Drawable.class);
        mTheme.setPreviewKeyBackground(background);
    }

    @Test
    public void testCalculatePositionForPreviewWithNoneExtendAnimation() throws Exception {
        mTheme.setPreviewAnimationType(PreviewPopupTheme.ANIMATION_STYLE_APPEAR);

        int[] offsets = new int[]{50, 60};

        Point result = mUnderTest.calculatePositionForPreview(mTestKey, Mockito.mock(View.class), mTheme, offsets);

        Assert.assertEquals(mTestKey.x + mTestKey.width / 2 + offsets[0], result.x);
        Assert.assertEquals(mTestKey.y + offsets[1], result.y);
    }

    @Test
    public void testCalculatePositionForPreviewWithExtendAnimation() throws Exception {
        mTheme.setPreviewAnimationType(PreviewPopupTheme.ANIMATION_STYLE_EXTEND);

        int[] offsets = new int[]{50, 60};

        Point result = mUnderTest.calculatePositionForPreview(mTestKey, Mockito.mock(View.class), mTheme, offsets);

        Assert.assertEquals(mTestKey.x + mTestKey.width / 2 + offsets[0], result.x);
        Assert.assertEquals(mTestKey.y + mTestKey.height + offsets[1], result.y);
    }

    @Test
    public void testCalculatePositionForPreviewWithBackgroundPadding() throws Exception {
        mTheme.setPreviewAnimationType(PreviewPopupTheme.ANIMATION_STYLE_APPEAR);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Rect padding = (Rect) invocation.getArguments()[0];
                padding.bottom = 13;
                return true;
            }
        }).when(mTheme.getPreviewKeyBackground()).getPadding(Mockito.any(Rect.class));

        int[] offsets = new int[]{50, 60};

        Point result = mUnderTest.calculatePositionForPreview(mTestKey, Mockito.mock(View.class), mTheme, offsets);

        Assert.assertEquals(mTestKey.x + mTestKey.width / 2 + offsets[0], result.x);
        Assert.assertEquals(mTestKey.y + offsets[1] + 13/*padding*/, result.y);
    }

}