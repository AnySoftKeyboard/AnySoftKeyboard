package com.anysoftkeyboard.keyboards.views.preview;

import android.support.v4.content.ContextCompat;
import android.widget.PopupWindow;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewBase;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AnySoftKeyboardTestRunner.class)
public class KeyPreviewsManagerTest {

    private Keyboard.Key mTestKey;
    private PreviewPopupTheme mTheme;
    private AnyKeyboardViewBase mKeyboardView;

    @Before
    public void setup() {
        mKeyboardView = Mockito.mock(AnyKeyboardViewBase.class);
        Mockito.doReturn(new int[]{1, 2}).when(mKeyboardView).getLocationInWindow();
        mTestKey = Mockito.mock(Keyboard.Key.class);
        mTestKey.x = 12;
        mTestKey.y = 11;
        mTestKey.width = 10;
        mTestKey.showPreview = true;
        mTestKey.height = 20;
        Mockito.doReturn((int) 'y').when(mTestKey).getPrimaryCode();
        Mockito.doReturn(1).when(mTestKey).getCodesCount();
        mTheme = new PreviewPopupTheme();
        mTheme.setPreviewKeyBackground(ContextCompat.getDrawable(RuntimeEnvironment.application, R.drawable.blacktheme_preview_background));
        mTheme.setPreviewKeyTextSize(1);
    }

    @Test
    public void testNoPopupForEnter() {
        KeyPreviewsManager underTest = new KeyPreviewsManager(RuntimeEnvironment.application, mKeyboardView, mTheme);

        PopupWindow createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        Mockito.doReturn(KeyCodes.ENTER).when(mTestKey).getPrimaryCode();
        underTest.showPreviewForKey(mTestKey, "");

        createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }

    @Test
    public void testNoPopupForNoPreview() {
        KeyPreviewsManager underTest = new KeyPreviewsManager(RuntimeEnvironment.application, mKeyboardView, mTheme);

        PopupWindow createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        mTestKey.showPreview = false;
        underTest.showPreviewForKey(mTestKey, "y");

        createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }

    @Test
    public void testNoPopupForModifier() {
        KeyPreviewsManager underTest = new KeyPreviewsManager(RuntimeEnvironment.application, mKeyboardView, mTheme);

        PopupWindow createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        mTestKey.modifier = true;
        underTest.showPreviewForKey(mTestKey, "y");

        createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }

    @Test
    public void testNoPopupWhenDisabled() {
        KeyPreviewsManager underTest = new KeyPreviewsManager(RuntimeEnvironment.application, mKeyboardView, mTheme);
        underTest.setEnabled(false);

        PopupWindow createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        underTest.showPreviewForKey(mTestKey, "y");

        createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }

    @Test
    public void testPopupForRegularKey() {
        KeyPreviewsManager underTest = new KeyPreviewsManager(RuntimeEnvironment.application, mKeyboardView, mTheme);

        PopupWindow createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        underTest.showPreviewForKey(mTestKey, "y");

        createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNotNull(createdPopupWindow);
    }

    @Test
    public void testNoPopupWhenTextSizeIsZero() {
        mTheme.setPreviewKeyTextSize(0);
        KeyPreviewsManager underTest = new KeyPreviewsManager(RuntimeEnvironment.application, mKeyboardView, mTheme);

        PopupWindow createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        underTest.showPreviewForKey(mTestKey, "y");

        createdPopupWindow = ShadowApplication.getInstance().getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }
}