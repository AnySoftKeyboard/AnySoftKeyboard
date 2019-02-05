package com.anysoftkeyboard.ime;

import static com.anysoftkeyboard.ime.AnySoftKeyboardThemeOverlayTest.captureOverlay;

import android.content.ComponentName;
import android.os.Build;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlyDataCreator;
import com.anysoftkeyboard.android.PowerSavingTest;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

import androidx.test.core.app.ApplicationProvider;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardPowerSavingTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testDoesNotAskForSuggestionsIfInLowBattery() {
        PowerSavingTest.sendBatteryState(true);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping(" ");

        PowerSavingTest.sendBatteryState(false);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hel", "hell", "hello");
    }

    @Test
    public void testAskForSuggestionsIfInLowBatteryButPrefIsDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode_suggestions_control, false);
        PowerSavingTest.sendBatteryState(true);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateTextTyping(" ");
        mAnySoftKeyboardUnderTest.resetMockCandidateView();

        PowerSavingTest.sendBatteryState(false);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hel", "hell", "hello");
    }

    @Test
    public void testDoesNotAskForSuggestionsIfPowerSavingAlways() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode, "always");
        PowerSavingTest.sendBatteryState(false);

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping(" ");

        PowerSavingTest.sendBatteryState(true);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifyNoSuggestionsInteractions();
    }

    @Test
    public void testAskForSuggestionsIfPowerSavingNever() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode, "never");
        PowerSavingTest.sendBatteryState(true);

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hel", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping(" ");

        PowerSavingTest.sendBatteryState(false);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hel", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping(" ");

        PowerSavingTest.sendBatteryState(true);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hel", "hell", "hello");
    }

    @Test
    public void testDictionariesStateCycle() {
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isSuggestionsEnabled());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        PowerSavingTest.sendBatteryState(true);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isSuggestionsEnabled());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).closeDictionaries();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        PowerSavingTest.sendBatteryState(false);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isSuggestionsEnabled());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).closeDictionaries();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        SharedPrefsHelper.setPrefsValue("candidates_on", false);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isSuggestionsEnabled());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).closeDictionaries();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        SharedPrefsHelper.setPrefsValue("candidates_on", true);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isSuggestionsEnabled());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).closeDictionaries();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
    }

    @Test
    public void testIconShownWhenTriggered() throws Exception {
        //initial watermark
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_power_saving);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        PowerSavingTest.sendBatteryState(true);

        ViewTestUtils.assertCurrentWatermarkHasDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_power_saving);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        PowerSavingTest.sendBatteryState(false);

        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_power_saving);
    }

    @Test
    public void testIconShownWhenAlwaysOn() throws Exception {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode, "always");
        //initial watermark
        ViewTestUtils.assertCurrentWatermarkHasDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_power_saving);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        PowerSavingTest.sendBatteryState(true);

        //does not change (since it's still `always`)
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setWatermark(Mockito.anyList());

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        PowerSavingTest.sendBatteryState(false);

        //does not change (since it's still `always`
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setWatermark(Mockito.anyList());
    }

    @Test
    public void testIconShownWhenNeverOn() throws Exception {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode, "never");
        //initial watermark
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setWatermark(Mockito.anyList());

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        PowerSavingTest.sendBatteryState(true);

        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setWatermark(Mockito.anyList());

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        PowerSavingTest.sendBatteryState(false);

        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setWatermark(Mockito.anyList());
    }

    @Test
    public void testCallOverlayOnPowerSavingSwitchEvenIfOverlaySettingOff() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_apply_remote_app_colors, false);
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode_theme_control, true);

        simulateOnStartInputFlow();
        Assert.assertFalse(captureOverlay(mAnySoftKeyboardUnderTest).isValid());

        PowerSavingTest.sendBatteryState(true);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        //switched overlay on the fly
        final OverlayData powerSaving = captureOverlay(mAnySoftKeyboardUnderTest);
        Assert.assertTrue(powerSaving.isValid());
        Assert.assertEquals(0xFF000000, powerSaving.getPrimaryColor());
        Assert.assertEquals(0xFF000000, powerSaving.getPrimaryDarkColor());
        Assert.assertEquals(0xFF888888, powerSaving.getPrimaryTextColor());

        PowerSavingTest.sendBatteryState(false);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        Assert.assertFalse(captureOverlay(mAnySoftKeyboardUnderTest).isValid());
    }

    @Test
    public void testSetPowerSavingOverlayWhenLowBattery() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode_theme_control, true);

        final OverlyDataCreator originalOverlayDataCreator = mAnySoftKeyboardUnderTest.getOriginalOverlayDataCreator();

        Assert.assertTrue(originalOverlayDataCreator instanceof AnySoftKeyboardPowerSaving.PowerSavingOverlayCreator);

        final OverlayData normal = originalOverlayDataCreator.createOverlayData(new ComponentName(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class));
        Assert.assertNotEquals(0xFF000000, normal.getPrimaryColor());

        PowerSavingTest.sendBatteryState(true);

        final OverlayData powerSaving = originalOverlayDataCreator.createOverlayData(new ComponentName(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class));
        Assert.assertTrue(powerSaving.isValid());
        Assert.assertEquals(0xFF000000, powerSaving.getPrimaryColor());
        Assert.assertEquals(0xFF000000, powerSaving.getPrimaryDarkColor());
        Assert.assertEquals(0xFF888888, powerSaving.getPrimaryTextColor());

        PowerSavingTest.sendBatteryState(false);

        final OverlayData normal2 = originalOverlayDataCreator.createOverlayData(new ComponentName(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class));
        Assert.assertNotEquals(0xFF000000, normal2.getPrimaryColor());
    }


    @Test
    @Config(sdk = Build.VERSION_CODES.KITKAT)
    public void testWorkEvenIfOverlayMechanismIsOsDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode_theme_control, true);

        final OverlyDataCreator originalOverlayDataCreator = mAnySoftKeyboardUnderTest.getOriginalOverlayDataCreator();

        Assert.assertTrue(originalOverlayDataCreator instanceof AnySoftKeyboardPowerSaving.PowerSavingOverlayCreator);

        final OverlayData normal = originalOverlayDataCreator.createOverlayData(new ComponentName(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class));
        Assert.assertFalse(normal.isValid());

        PowerSavingTest.sendBatteryState(true);

        final OverlayData powerSaving = originalOverlayDataCreator.createOverlayData(new ComponentName(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class));
        Assert.assertTrue(powerSaving.isValid());
        Assert.assertEquals(0xFF000000, powerSaving.getPrimaryColor());
        Assert.assertEquals(0xFF000000, powerSaving.getPrimaryDarkColor());
        Assert.assertEquals(0xFF888888, powerSaving.getPrimaryTextColor());

        PowerSavingTest.sendBatteryState(false);

        final OverlayData normal2 = originalOverlayDataCreator.createOverlayData(new ComponentName(ApplicationProvider.getApplicationContext(), MainSettingsActivity.class));
        Assert.assertFalse(normal2.isValid());
    }

    @Test
    public void testDoesNotSetPowerSavingThemeWhenLowBatteryIfPrefDisabled() {
        //this is the default behavior
        InputViewBinder keyboardView = mAnySoftKeyboardUnderTest.getInputView();
        Assert.assertNotNull(keyboardView);

        Mockito.reset(keyboardView);

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        PowerSavingTest.sendBatteryState(true);

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        keyboardView = mAnySoftKeyboardUnderTest.getInputView();
        Mockito.verify(keyboardView, Mockito.never()).setKeyboardTheme(Mockito.any());

        PowerSavingTest.sendBatteryState(false);

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        keyboardView = mAnySoftKeyboardUnderTest.getInputView();
        Mockito.verify(keyboardView, Mockito.never()).setKeyboardTheme(Mockito.any());
    }
}