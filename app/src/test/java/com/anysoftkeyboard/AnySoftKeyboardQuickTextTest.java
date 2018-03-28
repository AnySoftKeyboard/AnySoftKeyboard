package com.anysoftkeyboard;

import android.view.KeyEvent;
import android.view.View;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardQuickTextTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testOutputTextKeyOutputText() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals("\uD83D\uDE03", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(mAnySoftKeyboardUnderTest.getInputView(), mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(0));

        Assert.assertEquals(View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testOutputTextKeyOverrideOutputText() {
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(overrideText, inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDeletesOnBackspace() {
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(initialText +overrideText, inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(initialText, inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDeletesOnBackspaceWhenSuggestionsOff() {
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        SharedPrefsHelper.setPrefsValue("candidates_on", false);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(initialText +overrideText, inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(initialText, inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDoesNotDeletesOnBackspaceIfCursorMoves() {
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello Xello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(initialText + overrideText, inputConnection.getCurrentTextInInputConnection());

        inputConnection.setSelection(7, 7);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals((initialText + overrideText).replace("X", ""), inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDoesNotDeletesOnBackspaceIfCursorMovesWhenSuggestionsOff() {
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        SharedPrefsHelper.setPrefsValue("candidates_on", false);

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello Xello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(initialText + overrideText, inputConnection.getCurrentTextInInputConnection());

        inputConnection.setSelection(7, 7);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals((initialText + overrideText).replace("X", ""), inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextKeySwitchKeyboardWhenFlipped() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_do_not_flip_quick_key_codes_functionality, false);

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(mAnySoftKeyboardUnderTest.getInputView(), mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(0));

        Assert.assertEquals(View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testPopupTextKeyOutputTextWhenFlipped() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_do_not_flip_quick_key_codes_functionality, false);

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals("\uD83D\uDE03", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(mAnySoftKeyboardUnderTest.getInputView(), mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(0));

        Assert.assertEquals(View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testPopupTextKeySwitchKeyboard() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(mAnySoftKeyboardUnderTest.getInputView(), mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(0));

        Assert.assertEquals(View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testSecondPressOnQuickTextKeyDoesNotCloseKeyboard() {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals(View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown(KeyEvent.KEYCODE_BACK, new AnySoftKeyboardPhysicalKeyboardTest.TestKeyEvent(time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        mAnySoftKeyboardUnderTest.onKeyUp(KeyEvent.KEYCODE_BACK, new AnySoftKeyboardPhysicalKeyboardTest.TestKeyEvent(time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

        Assert.assertEquals(View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals(View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onKeyDown(KeyEvent.KEYCODE_BACK, new AnySoftKeyboardPhysicalKeyboardTest.TestKeyEvent(time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        mAnySoftKeyboardUnderTest.onKeyUp(KeyEvent.KEYCODE_BACK, new AnySoftKeyboardPhysicalKeyboardTest.TestKeyEvent(time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

        Assert.assertEquals(View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.hideWindow();
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testHomeOnQuickTextKeyClosesKeyboard() {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals(View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());

        // hideWindow() is now essentially the same as pressing the HOME hardware key
        mAnySoftKeyboardUnderTest.hideWindow();

        Assert.assertEquals(View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

}