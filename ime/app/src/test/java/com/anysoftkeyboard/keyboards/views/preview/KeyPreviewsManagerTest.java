package com.anysoftkeyboard.keyboards.views.preview;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.menny.android.anysoftkeyboard.R.drawable.blacktheme_preview_background;

import android.app.Application;
import android.support.v4.content.ContextCompat;
import android.widget.PopupWindow;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.android.PowerSavingTest;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewBase;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
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

    private static PopupWindow getLatestPopupWindow() {
        return Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                .getLatestPopupWindow();
    }

    @Before
    public void setup() {
        mKeyboardView = Mockito.mock(AnyKeyboardViewBase.class);
        Mockito.doReturn(new int[] {1, 2}).when(mKeyboardView).getLocationInWindow();
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
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);

        PopupWindow createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        Mockito.doReturn(KeyCodes.ENTER).when(mTestKey).getPrimaryCode();
        underTest.showPreviewForKey(mTestKey, "");

        createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }

    @Test
    public void testNoPopupForNoPreview() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);

        PopupWindow createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        mTestKey.showPreview = false;
        underTest.showPreviewForKey(mTestKey, "y");

        createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }

    @Test
    public void testNoPopupForModifier() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);

        PopupWindow createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);

        mTestKey.modifier = true;
        underTest.showPreviewForKey(mTestKey, "y");

        createdPopupWindow = getLatestPopupWindow();
        Assert.assertNull(createdPopupWindow);
    }

    @Test
    public void testNoPopupWhenDisabledAndPrefPath() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_key_press_shows_preview_popup, false);
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);

        Assert.assertNull(getLatestPopupWindow());

        underTest.showPreviewForKey(mTestKey, "y");

        Assert.assertNull(getLatestPopupWindow());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_key_press_shows_preview_popup, true);

        Assert.assertNull(getLatestPopupWindow());

        underTest.showPreviewForKey(mTestKey, "y");

        final PopupWindow popupAfterEnabling = getLatestPopupWindow();
        Assert.assertNotNull(popupAfterEnabling);

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_key_press_shows_preview_popup, false);

        Assert.assertEquals(popupAfterEnabling, getLatestPopupWindow());
        Assert.assertFalse(popupAfterEnabling.isShowing());

        underTest.showPreviewForKey(mTestKey, "y");

        Assert.assertEquals(popupAfterEnabling, getLatestPopupWindow());
        Assert.assertFalse(popupAfterEnabling.isShowing());
    }

    @Test
    public void testNoPopUpOnLowPower() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);

        Assert.assertNull(getLatestPopupWindow());

        underTest.showPreviewForKey(mTestKey, "y");
        Assert.assertTrue(getLatestPopupWindow().isShowing());

        PowerSavingTest.sendBatteryState(true);

        underTest.showPreviewForKey(mTestKey, "y");
        Assert.assertFalse(getLatestPopupWindow().isShowing());

        PowerSavingTest.sendBatteryState(false);
        underTest.showPreviewForKey(mTestKey, "y");
        Assert.assertTrue(getLatestPopupWindow().isShowing());
    }

    @Test
    public void testNoPopupWhenAnimationDisabledAndPrefPath() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "none");
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);

        Assert.assertNull(getLatestPopupWindow());

        underTest.showPreviewForKey(mTestKey, "y");

        Assert.assertNull(getLatestPopupWindow());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "some");

        Assert.assertNull(getLatestPopupWindow());

        underTest.showPreviewForKey(mTestKey, "y");

        final PopupWindow popupWindowBeforeDisable = getLatestPopupWindow();
        Assert.assertNotNull(popupWindowBeforeDisable);

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_tweak_animations_level, "none");

        Assert.assertSame(popupWindowBeforeDisable, getLatestPopupWindow());
        Assert.assertFalse(popupWindowBeforeDisable.isShowing());

        underTest.showPreviewForKey(mTestKey, "y");

        Assert.assertSame(popupWindowBeforeDisable, getLatestPopupWindow());
        Assert.assertFalse(popupWindowBeforeDisable.isShowing());
    }

    @Test
    public void testPositionCalculator() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_key_press_preview_popup_position, "above_key");
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);

        Assert.assertTrue(underTest.getPositionCalculator() instanceof AboveKeyPositionCalculator);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_key_press_preview_popup_position, "above_keyboard");

        Assert.assertTrue(
                underTest.getPositionCalculator() instanceof AboveKeyboardPositionCalculator);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_key_press_preview_popup_position, "above_key");

        Assert.assertTrue(underTest.getPositionCalculator() instanceof AboveKeyPositionCalculator);
    }

    @Test
    public void testPopupForRegularKey() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);

        Assert.assertNull(getLatestPopupWindow());

        underTest.showPreviewForKey(mTestKey, "y");

        Assert.assertNotNull(getLatestPopupWindow());
    }

    @Test
    public void testNoPopupWhenTextSizeIsZero() {
        mTheme.setPreviewKeyTextSize(0);
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);

        Assert.assertNull(getLatestPopupWindow());

        underTest.showPreviewForKey(mTestKey, "y");

        Assert.assertNull(getLatestPopupWindow());
    }

    @Test
    public void testResetThemeClearsAllReusablePreviews() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);
        underTest.showPreviewForKey(mTestKey, "y");

        final PopupWindow firstPopupWindow = getLatestPopupWindow();
        Assert.assertNotNull(firstPopupWindow);

        Robolectric.flushForegroundThreadScheduler();

        underTest.showPreviewForKey(mTestKey, "y");
        Assert.assertSame(firstPopupWindow, getLatestPopupWindow());

        Robolectric.flushForegroundThreadScheduler();

        underTest.resetTheme();

        Robolectric.flushForegroundThreadScheduler();

        underTest.showPreviewForKey(mTestKey, "y");
        Assert.assertNotSame(firstPopupWindow, getLatestPopupWindow());
    }

    @Test
    public void testCancelAllPreviewsStillReusePreviews() {
        KeyPreviewsManager underTest =
                new KeyPreviewsManager(getApplicationContext(), mKeyboardView, mTheme);
        underTest.showPreviewForKey(mTestKey, "y");

        final PopupWindow firstPopupWindow = getLatestPopupWindow();
        Assert.assertNotNull(firstPopupWindow);

        Robolectric.flushForegroundThreadScheduler();

        underTest.showPreviewForKey(mTestKey, "y");
        Assert.assertSame(firstPopupWindow, getLatestPopupWindow());

        Robolectric.flushForegroundThreadScheduler();

        underTest.cancelAllPreviews();

        Robolectric.flushForegroundThreadScheduler();

        underTest.showPreviewForKey(mTestKey, "y");
        Assert.assertSame(firstPopupWindow, getLatestPopupWindow());
    }
}
