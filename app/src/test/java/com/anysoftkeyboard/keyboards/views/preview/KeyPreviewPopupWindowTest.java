package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.PopupWindow;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AnySoftKeyboardTestRunner.class)
public class KeyPreviewPopupWindowTest {

    @Test
    public void testPreviewLayoutCorrectlyForNoneLabel() {
        PreviewPopupTheme theme = new PreviewPopupTheme();
        theme.setPreviewKeyBackground(ContextCompat.getDrawable(RuntimeEnvironment.application, R.drawable.blacktheme_preview_background));
        theme.setPreviewKeyTextSize(1);
        KeyPreviewPopupWindow underTest = new KeyPreviewPopupWindow(RuntimeEnvironment.application, new View(RuntimeEnvironment.application), theme);

        PopupWindow createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        Keyboard.Key key = Mockito.mock(Keyboard.Key.class);
        Mockito.doReturn((int) 'y').when(key).getPrimaryCode();
        Mockito.doReturn(1).when(key).getCodesCount();
        key.width = 10;
        key.height = 20;
        underTest.showPreviewForKey(key, "y", new Point(1, 1));

        createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNotNull(createdPopupWindow);
    }

    @Test
    public void testPreviewLayoutCorrectlyForLabel() {
        PreviewPopupTheme theme = new PreviewPopupTheme();
        theme.setPreviewKeyBackground(ContextCompat.getDrawable(RuntimeEnvironment.application, R.drawable.blacktheme_preview_background));
        theme.setPreviewKeyTextSize(1);
        KeyPreviewPopupWindow underTest = new KeyPreviewPopupWindow(RuntimeEnvironment.application, new View(RuntimeEnvironment.application), theme);

        PopupWindow createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        Keyboard.Key key = Mockito.mock(Keyboard.Key.class);
        Mockito.doReturn((int) 'y').when(key).getPrimaryCode();
        Mockito.doReturn(1).when(key).getCodesCount();
        key.width = 10;
        key.height = 20;
        underTest.showPreviewForKey(key, "yy", new Point(1, 1));

        createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNotNull(createdPopupWindow);
    }
}