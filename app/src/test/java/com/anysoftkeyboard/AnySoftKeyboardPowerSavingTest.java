package com.anysoftkeyboard;

import com.anysoftkeyboard.powersave.PowerSavingTest;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setupSuggestionsForKeyboard(Mockito.anyList());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        PowerSavingTest.sendBatteryState(false);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isSuggestionsEnabled());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).closeDictionaries();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        SharedPrefsHelper.setPrefsValue("candidates_on", false);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isSuggestionsEnabled());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).closeDictionaries();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setupSuggestionsForKeyboard(Mockito.anyList());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        SharedPrefsHelper.setPrefsValue("candidates_on", true);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isSuggestionsEnabled());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).closeDictionaries();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
    }
}