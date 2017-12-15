package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnySoftKeyboardPopTextTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testDefaultPopTextOutOfKeyOnCorrection() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");
        //pressing SPACE will auto-correct and pop the text out of the key
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).popTextOutOfKey(Mockito.any());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        verifyPopText("hell");

        //regular-word
        mAnySoftKeyboardUnderTest.simulateTextTyping("gggg");
        verifySuggestions(true, "gggg");
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).popTextOutOfKey(Mockito.any());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell gggg ", inputConnection.getCurrentTextInInputConnection());
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).popTextOutOfKey(Mockito.any());
    }

    @Test
    public void testAllPopTextOutOfKeyOnKeyPressAndCorrection() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "any_key");

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        InputViewBinder inputView = mAnySoftKeyboardUnderTest.getInputView();
        InOrder popTextInOrder = Mockito.inOrder(inputView);
        popTextInOrder.verify(inputView).popTextOutOfKey("h");
        popTextInOrder.verify(inputView).popTextOutOfKey("e");
        popTextInOrder.verify(inputView).popTextOutOfKey("l");
        popTextInOrder.verify(inputView).popTextOutOfKey("hell");
        popTextInOrder.verify(inputView, Mockito.never()).popTextOutOfKey(Mockito.any());
    }

    @Test
    public void testAllWordsPopTextOutOfKeyOnKeyPressAndCorrection() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "on_word");
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");
        //pressing SPACE will auto-correct and pop the text out of the key
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).popTextOutOfKey(Mockito.any());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        verifyPopText("hell");
        //regular-word
        mAnySoftKeyboardUnderTest.simulateTextTyping("gggg");
        verifySuggestions(true, "gggg");
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).popTextOutOfKey(Mockito.any());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell gggg ", inputConnection.getCurrentTextInInputConnection());

        verifyPopText("gggg");
    }

    @Test
    public void testNeverPopTextOut() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "never");

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        InputViewBinder inputView = mAnySoftKeyboardUnderTest.getInputView();

        Mockito.verify(inputView, Mockito.never()).popTextOutOfKey(Mockito.any());
    }

    @Test
    public void testDefaultSwitchCaseSameAsNever() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_pop_text_option, "blahblah");

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        InputViewBinder inputView = mAnySoftKeyboardUnderTest.getInputView();

        Mockito.verify(inputView, Mockito.never()).popTextOutOfKey(Mockito.any());
    }

    private void verifyPopText(String text) {
        ArgumentCaptor<CharSequence> popTextCaptor = ArgumentCaptor.forClass(CharSequence.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView()).popTextOutOfKey(popTextCaptor.capture());
        Assert.assertEquals(text, popTextCaptor.getValue().toString());
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());
    }
}