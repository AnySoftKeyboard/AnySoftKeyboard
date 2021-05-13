package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewWithExtraDraw;
import com.anysoftkeyboard.keyboards.views.extradraw.ExtraDraw;
import com.anysoftkeyboard.keyboards.views.extradraw.PopTextExtraDraw;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardPopTextTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testDefaultPopTextOutOfKeyOnCorrection() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "he'll", "hello", "hell");
        // pressing SPACE will auto-correct and pop the text out of the key
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        verifyNothingAddedInteractions();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());
        verifyPopText("he'll");

        // regular-word
        mAnySoftKeyboardUnderTest.simulateTextTyping("gggg");
        verifySuggestions(true, "gggg");
        verifyNothingAddedInteractions();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll gggg ", inputConnection.getCurrentTextInInputConnection());
        verifyNothingAddedInteractions();
    }

    @Test
    public void testWordRevert() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "he'll", "hello", "hell");
        // pressing SPACE will auto-correct and pop the text out of the key
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        verifyNothingAddedInteractions();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());

        ArgumentCaptor<ExtraDraw> popTextCaptor = ArgumentCaptor.forClass(ExtraDraw.class);
        Mockito.verify(((AnyKeyboardViewWithExtraDraw) mAnySoftKeyboardUnderTest.getInputView()))
                .addExtraDraw(popTextCaptor.capture());
        Assert.assertTrue(popTextCaptor.getValue() instanceof PopTextExtraDraw.PopOut);
        PopTextExtraDraw popTextExtraDraw = (PopTextExtraDraw) popTextCaptor.getValue();
        Assert.assertEquals("he'll", popTextExtraDraw.getPopText().toString());
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        popTextCaptor = ArgumentCaptor.forClass(ExtraDraw.class);
        Mockito.verify(((AnyKeyboardViewWithExtraDraw) mAnySoftKeyboardUnderTest.getInputView()))
                .addExtraDraw(popTextCaptor.capture());
        Assert.assertTrue(popTextCaptor.getValue() instanceof PopTextExtraDraw.PopIn);
        Assert.assertEquals(
                "he'll", ((PopTextExtraDraw) popTextCaptor.getValue()).getPopText().toString());
    }

    @Test
    public void testAllPopTextOutOfKeyOnKeyPressAndCorrection() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "any_key");

        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "he'll", "hello", "hell");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());

        ArgumentCaptor<ExtraDraw> popTextCaptor = ArgumentCaptor.forClass(ExtraDraw.class);
        Mockito.verify(
                        ((AnyKeyboardViewWithExtraDraw) mAnySoftKeyboardUnderTest.getInputView()),
                        Mockito.times(4))
                .addExtraDraw(popTextCaptor.capture());

        Assert.assertEquals(4, popTextCaptor.getAllValues().size());
        for (ExtraDraw extraDraw : popTextCaptor.getAllValues()) {
            Assert.assertTrue(extraDraw instanceof PopTextExtraDraw.PopOut);
        }
        Assert.assertEquals(
                "h",
                ((PopTextExtraDraw) popTextCaptor.getAllValues().get(0)).getPopText().toString());
        Assert.assertEquals(
                "e",
                ((PopTextExtraDraw) popTextCaptor.getAllValues().get(1)).getPopText().toString());
        Assert.assertEquals(
                "l",
                ((PopTextExtraDraw) popTextCaptor.getAllValues().get(2)).getPopText().toString());
        Assert.assertEquals(
                "he'll",
                ((PopTextExtraDraw) popTextCaptor.getAllValues().get(3)).getPopText().toString());
    }

    @Test
    public void testAllWordsPopTextOutOfKeyOnKeyPressAndCorrection() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "on_word");
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "he'll", "hello", "hell");
        // pressing SPACE will auto-correct and pop the text out of the key
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        verifyNothingAddedInteractions();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());
        verifyPopText("he'll");
        // regular-word
        mAnySoftKeyboardUnderTest.simulateTextTyping("gggg");
        verifySuggestions(true, "gggg");
        verifyNothingAddedInteractions();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll gggg ", inputConnection.getCurrentTextInInputConnection());

        verifyPopText("gggg");
    }

    @Test
    public void testRestorePrefOnServiceRestart() throws Exception {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "on_word");
        mAnySoftKeyboardController.destroy();
        // restarting
        setUpForAnySoftKeyboardBase();

        simulateOnStartInputFlow();

        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "he'll", "hello", "hell");
        // pressing SPACE will auto-correct and pop the text out of the key
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        verifyNothingAddedInteractions();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());
        verifyPopText("he'll");
        // regular-word
        mAnySoftKeyboardUnderTest.simulateTextTyping("gggg");
        verifySuggestions(true, "gggg");
        verifyNothingAddedInteractions();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll gggg ", inputConnection.getCurrentTextInInputConnection());

        verifyPopText("gggg");
    }

    @Test
    public void testDoesNotPopTextWhenManuallyPicked() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "on_word");
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "he'll", "hello", "hell");
        verifyNothingAddedInteractions();
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "hell");
        verifyNothingAddedInteractions();
    }

    @Test
    public void testDoesNotCrashOnPopTextWhenFunctionalKeyPress() {
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "any_key");
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);

        Assert.assertEquals(
                "symbols_keyboard",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        verifyNothingAddedInteractions();
    }

    @Test
    public void testNeverPopTextOut() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "never");

        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "he'll", "hello", "hell");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());

        verifyNothingAddedInteractions();
    }

    @Test
    public void testDefaultSwitchCaseSameAsNever() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "blahblah");

        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "he'll", "hello", "hell");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());

        verifyNothingAddedInteractions();
    }

    private void verifyNothingAddedInteractions() {
        Mockito.verify(
                        ((AnyKeyboardViewWithExtraDraw) mAnySoftKeyboardUnderTest.getInputView()),
                        Mockito.never())
                .addExtraDraw(Mockito.any());
    }

    private void verifyPopText(String text) {
        ArgumentCaptor<ExtraDraw> popTextCaptor = ArgumentCaptor.forClass(ExtraDraw.class);
        Mockito.verify(((AnyKeyboardViewWithExtraDraw) mAnySoftKeyboardUnderTest.getInputView()))
                .addExtraDraw(popTextCaptor.capture());
        Assert.assertTrue(popTextCaptor.getValue() instanceof PopTextExtraDraw.PopOut);
        Assert.assertEquals(
                text, ((PopTextExtraDraw) popTextCaptor.getValue()).getPopText().toString());
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    }
}
