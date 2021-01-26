package com.anysoftkeyboard.keyboards.views.preview;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.menny.android.anysoftkeyboard.R.drawable.blacktheme_preview_background;

import android.app.Application;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.widget.PopupWindow;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class KeyPreviewsManagerTest {

    private Keyboard.Key mTestKey;
    private PreviewPopupTheme mTheme;
    private AnyKeyboardViewBase mKeyboardView;
    private PositionCalculator mPositionCalculator;

    private static PopupWindow getLatestPopupWindow() {
        return Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                .getLatestPopupWindow();
    }

    @Before
    public void setup() {
        mPositionCalculator = Mockito.mock(PositionCalculator.class);
        Mockito.doReturn(new Point(2, 3))
                .when(mPositionCalculator)
                .calculatePositionForPreview(Mockito.any(), Mockito.any(), Mockito.any());

        mKeyboardView = Mockito.mock(AnyKeyboardViewBase.class);
        Mockito.doAnswer(
                        a -> {
                            int[] location = a.getArgument(0);
                            location[0] = 1;
                            location[1] = 2;
                            return null;
                        })
                .when(mKeyboardView)
                .getLocationInWindow(Mockito.any());

        mTestKey = Mockito.mock(Keyboard.Key.class);
        mTestKey.x = 12;
        mTestKey.y = 11;
        mTestKey.width = 10;
        mTestKey.showPreview = true;
        mTestKey.height = 20;
        Mockito.doReturn((int) 'y').when(mTestKey).getPrimaryCode();
        Mockito.doReturn(1).when(mTestKey).getCodesCount();
        mTheme = new PreviewPopupTheme();
        mTheme.setPreviewKeyBackground(
                ContextCompat.getDrawable(getApplicationContext(), blacktheme_preview_background));
        mTheme.setPreviewKeyTextSize(1);
    }

    @Test
    public void testNoPopupForEnter() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

        PopupWindow createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        Mockito.doReturn(KeyCodes.ENTER).when(mTestKey).getPrimaryCode();
        underTest.showPreviewForKey(mTestKey, "", mKeyboardView, mTheme);

        createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }

    @Test
    public void testNoPopupForNoPreview() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

        PopupWindow createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        mTestKey.showPreview = false;
        underTest.showPreviewForKey(mTestKey, "y", mKeyboardView, mTheme);

        createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }

    @Test
    public void testNoPopupForModifier() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

        PopupWindow createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        mTestKey.modifier = true;
        underTest.showPreviewForKey(mTestKey, "y", mKeyboardView, mTheme);

        createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }

    @Test
    public void testPopupForRegularKey() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

        Assert.assertNull(getLatestPopupWindow());

        underTest.showPreviewForKey(mTestKey, "y", mKeyboardView, mTheme);

        Assert.assertNotNull(getLatestPopupWindow());
    }

    @Test
    public void testNoPopupWhenTextSizeIsZero() {
        mTheme.setPreviewKeyTextSize(0);
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

        Assert.assertNull(getLatestPopupWindow());

        underTest.showPreviewForKey(mTestKey, "y", mKeyboardView, mTheme);

        Assert.assertNull(getLatestPopupWindow());
    }

    @Test
    public void testCancelAllPreviewsStillReusePreviews() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);
        underTest.showPreviewForKey(mTestKey, "y", mKeyboardView, mTheme);

        final PopupWindow firstPopupWindow = getLatestPopupWindow();
        Assert.assertNotNull(firstPopupWindow);

        Robolectric.flushForegroundThreadScheduler();

        underTest.showPreviewForKey(mTestKey, "y", mKeyboardView, mTheme);
        Assert.assertSame(firstPopupWindow, getLatestPopupWindow());

        Robolectric.flushForegroundThreadScheduler();

        underTest.cancelAllPreviews();

        Robolectric.flushForegroundThreadScheduler();

        underTest.showPreviewForKey(mTestKey, "y", mKeyboardView, mTheme);
        Assert.assertSame(firstPopupWindow, getLatestPopupWindow());
    }
}
