package com.anysoftkeyboard.ime;

import android.content.ComponentName;
import android.os.Build;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlyDataCreator;
import com.anysoftkeyboard.android.PowerSavingTest;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

import androidx.test.core.app.ApplicationProvider;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardThemeOverlayTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testDefaultAppliesInvalidOverlayAndDoesNotInteractWithCreator() {
        simulateOnStartInputFlow();

        OverlayData appliedData = captureOverlay();
        Assert.assertFalse(appliedData.isValid());
        Assert.assertSame(AnySoftKeyboardThemeOverlay.INVALID_OVERLAY_DATA, appliedData);
    }

    @Test
    public void testWhenEnabledAppliesOverlayFromCreator() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, true);
        Mockito.reset(mAnySoftKeyboardUnderTest.getMockOverlayDataCreator());

        final EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
        simulateOnStartInputFlow(false, editorInfo);
        ArgumentCaptor<ComponentName> componentNameArgumentCaptor = ArgumentCaptor.forClass(ComponentName.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockOverlayDataCreator()).createOverlayData(componentNameArgumentCaptor.capture());
        Assert.assertEquals(editorInfo.packageName, componentNameArgumentCaptor.getValue().getPackageName());

        OverlayData appliedData = captureOverlay();
        Assert.assertTrue(appliedData.isValid());
    }

    @Test
    public void testStartsEnabledStopsApplyingAfterDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, true);

        simulateOnStartInputFlow(false, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertTrue(captureOverlay().isValid());

        simulateFinishInputFlow();

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, false);
        simulateOnStartInputFlow(false, createEditorInfoTextWithSuggestionsForSetUp());
        Assert.assertSame(captureOverlay(), AnySoftKeyboardThemeOverlay.INVALID_OVERLAY_DATA);
    }

    @Test
    public void testAppliesInvalidIfRemotePackageDoesNotHaveIntent() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, true);

        final EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
        editorInfo.packageName = "com.is.not.there";
        simulateOnStartInputFlow(false, editorInfo);

        OverlayData appliedData = captureOverlay();
        Assert.assertFalse(appliedData.isValid());
        Assert.assertSame(AnySoftKeyboardThemeOverlay.INVALID_OVERLAY_DATA, appliedData);
    }

    @Test
    public void testSwitchesBetweenApps() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, true);

        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        Assert.assertTrue(captureOverlay().isValid());

        simulateFinishInputFlow();

        final EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
        editorInfo.packageName = "com.is.not.there";
        simulateOnStartInputFlow(false, editorInfo);

        Assert.assertFalse(captureOverlay().isValid());

        simulateFinishInputFlow();
        //again, a valid app
        simulateOnStartInputFlow();
        Assert.assertTrue(captureOverlay().isValid());
    }

    @Test
    public void testRestartsInputField() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, true);

        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        Assert.assertTrue(captureOverlay().isValid());

        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        Assert.assertTrue(captureOverlay().isValid());

        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        Assert.assertTrue(captureOverlay().isValid());

        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        Assert.assertTrue(captureOverlay().isValid());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, false);

        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        Assert.assertFalse(captureOverlay().isValid());

        simulateFinishInputFlow();

        simulateOnStartInputFlow();

        Assert.assertFalse(captureOverlay().isValid());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.KITKAT)
    public void testDoesNotWorkPriorToLollipop() {
        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        Assert.assertFalse(captureOverlay().isValid());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, true);

        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        Assert.assertFalse(captureOverlay().isValid());

        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        Assert.assertFalse(captureOverlay().isValid());
    }

    @Test
    public void testDoesNotFailWithEmptyPackageName() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, true);

        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        Assert.assertTrue(captureOverlay().isValid());

        simulateFinishInputFlow();

        final EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
        editorInfo.packageName = null;
        simulateOnStartInputFlow(false, editorInfo);

        Assert.assertFalse(captureOverlay().isValid());

        simulateFinishInputFlow();
        //again, a valid app
        simulateOnStartInputFlow();
        Assert.assertTrue(captureOverlay().isValid());
    }

    @Test
    public void testPowerSavingPriority() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode_theme_control, true);
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, true);

        final OverlyDataCreator originalOverlayDataCreator = mAnySoftKeyboardUnderTest.getOriginalOverlayDataCreator();

        final OverlayData normal = originalOverlayDataCreator.createOverlayData(new ComponentName(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class));
        Assert.assertTrue(normal.isValid());
        Assert.assertEquals(0xFFCC99FF, normal.getPrimaryColor());
        Assert.assertEquals(0xFFAA77DD, normal.getPrimaryDarkColor());
        Assert.assertEquals(0xFF000000, normal.getPrimaryTextColor());

        PowerSavingTest.sendBatteryState(true);

        final OverlayData powerSaving = originalOverlayDataCreator.createOverlayData(new ComponentName(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class));
        Assert.assertTrue(powerSaving.isValid());
        Assert.assertEquals(0xFF000000, powerSaving.getPrimaryColor());
        Assert.assertEquals(0xFF000000, powerSaving.getPrimaryDarkColor());
        Assert.assertEquals(0xFF888888, powerSaving.getPrimaryTextColor());
    }

    private OverlayData captureOverlay() {
        return captureOverlay(mAnySoftKeyboardUnderTest);
    }

    public static OverlayData captureOverlay(TestableAnySoftKeyboard testableAnySoftKeyboard) {
        ArgumentCaptor<OverlayData> captor = ArgumentCaptor.forClass(OverlayData.class);
        Mockito.verify(testableAnySoftKeyboard.getInputView(), Mockito.atLeastOnce()).setThemeOverlay(captor.capture());

        return captor.getValue();
    }
}