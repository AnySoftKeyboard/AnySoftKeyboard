package com.anysoftkeyboard.keyboards.views.preview;

import static com.menny.android.anysoftkeyboard.R.drawable.blacktheme_preview_background;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.PopupWindow;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.Keyboard;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;

import androidx.test.core.app.ApplicationProvider;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class KeyPreviewPopupWindowTest {

    @Test
    public void testPreviewLayoutCorrectlyForNoneLabel() {
        PreviewPopupTheme theme = new PreviewPopupTheme();
        theme.setPreviewKeyBackground(ContextCompat.getDrawable(getApplicationContext(), blacktheme_preview_background));
        theme.setPreviewKeyTextSize(1);
        final KeyPreviewPopupWindow underTest = new KeyPreviewPopupWindow(getApplicationContext(),
                new View(getApplicationContext()), theme);

        PopupWindow createdPopupWindow = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        Keyboard.Key key = Mockito.mock(Keyboard.Key.class);
        Mockito.doReturn((int) 'y').when(key).getPrimaryCode();
        Mockito.doReturn(1).when(key).getCodesCount();
        key.width = 10;
        key.height = 20;
        underTest.showPreviewForKey(key, "y", new Point(1, 1));

        createdPopupWindow = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow();
        Assert.assertNotNull(createdPopupWindow);
    }

    @Test
    public void testPreviewLayoutCorrectlyForLabel() {
        PreviewPopupTheme theme = new PreviewPopupTheme();
        theme.setPreviewKeyBackground(ContextCompat.getDrawable(getApplicationContext(), blacktheme_preview_background));
        theme.setPreviewKeyTextSize(1);
        final KeyPreviewPopupWindow underTest = new KeyPreviewPopupWindow(getApplicationContext(),
                new View(getApplicationContext()), theme);

        PopupWindow createdPopupWindow = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        Keyboard.Key key = Mockito.mock(Keyboard.Key.class);
        Mockito.doReturn((int) 'y').when(key).getPrimaryCode();
        Mockito.doReturn(1).when(key).getCodesCount();
        key.width = 10;
        key.height = 20;
        underTest.showPreviewForKey(key, "yy", new Point(1, 1));

        createdPopupWindow = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getLatestPopupWindow();
        Assert.assertNotNull(createdPopupWindow);
    }
}