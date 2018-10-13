package com.anysoftkeyboard.ime;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.powersave.PowerSavingTest;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

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
    public void testSetPowerSavingThemeWhenLowBattery() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_power_save_mode_theme_control, true);
        ArgumentCaptor<KeyboardTheme> argumentCaptor = ArgumentCaptor.forClass(KeyboardTheme.class);

        final InputViewBinder keyboardView = mAnySoftKeyboardUnderTest.getInputView();
        Assert.assertNotNull(keyboardView);

        Mockito.reset(keyboardView);

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        PowerSavingTest.sendBatteryState(true);

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(keyboardView).setKeyboardTheme(argumentCaptor.capture());
        Assert.assertEquals("b8d8d941-4e56-46a7-aa73-0ae593ca4aa3", argumentCaptor.getValue().getId());

        simulateOnStartInputFlow();
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        Mockito.reset(keyboardView);
        PowerSavingTest.sendBatteryState(false);

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(keyboardView).setKeyboardTheme(argumentCaptor.capture());
        Assert.assertEquals("2fbea491-15f6-4b40-9259-06e21d9dba95", argumentCaptor.getValue().getId());
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