package com.anysoftkeyboard.ime;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.TestInputConnection;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
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
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals("\uD83D\uDE03", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(3, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(
                mAnySoftKeyboardUnderTest.getInputView(),
                mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(1));

        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testOutputTextKeyOverrideOutputText() {
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(overrideText, inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDeletesOnBackspace() {
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                initialText + overrideText, inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(initialText, inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDeletesOnBackspaceWhenSuggestionsOff() {
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        SharedPrefsHelper.setPrefsValue("candidates_on", false);
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                initialText + overrideText, inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(initialText, inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDoesNotDeletesOnBackspaceIfCursorMoves() {
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello Xello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                initialText + overrideText, inputConnection.getCurrentTextInInputConnection());

        inputConnection.setSelection(7, 7);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(
                (initialText + overrideText).replace("X", ""),
                inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDoesNotDeletesOnBackspaceIfCursorMovesWhenSuggestionsOff() {
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        SharedPrefsHelper.setPrefsValue("candidates_on", false);

        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello Xello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                initialText + overrideText, inputConnection.getCurrentTextInInputConnection());

        inputConnection.setSelection(7, 7);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(
                (initialText + overrideText).replace("X", ""),
                inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextKeySwitchKeyboardWhenFlipped() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_do_not_flip_quick_key_codes_functionality, false);

        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(3, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(
                mAnySoftKeyboardUnderTest.getInputView(),
                mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(1));

        Assert.assertEquals(
                View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testPopupTextKeyOutputTextWhenFlipped() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_do_not_flip_quick_key_codes_functionality, false);

        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals("\uD83D\uDE03", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(3, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(
                mAnySoftKeyboardUnderTest.getInputView(),
                mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(1));

        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testPopupTextKeySwitchKeyboard() {
        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());

        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(3, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(
                mAnySoftKeyboardUnderTest.getInputView(),
                mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(1));

        Assert.assertEquals(
                View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.GONE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());
    }

    @Test
    public void testSecondPressOnQuickTextKeyDoesNotCloseKeyboard() {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals(
                View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.GONE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());

        mAnySoftKeyboardUnderTest.sendDownUpKeyEvents(KeyEvent.KEYCODE_BACK);

        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals(
                View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.GONE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.sendDownUpKeyEvents(KeyEvent.KEYCODE_BACK);

        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.hideWindow();
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testCloseQuickTextKeyboardOnInputReallyFinished() {
        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        simulateFinishInputFlow();

        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());
    }

    @Test
    public void testDoNotCloseQuickTextKeyboardOnInputNotReallyFinished() {
        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        mAnySoftKeyboardUnderTest.onFinishInputView(false);

        Assert.assertEquals(
                View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.GONE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());
    }

    @Test
    public void testDoesNotReShowCandidatesIfNoCandidatesToBeginWith() {
        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS));

        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.GONE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals(
                View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.GONE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());

        mAnySoftKeyboardUnderTest.sendDownUpKeyEvents(KeyEvent.KEYCODE_BACK);

        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.GONE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());
    }

    @Test
    public void testHomeOnQuickTextKeyClosesKeyboard() {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals(
                View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());

        // hideWindow() is now, essentially, the same as pressing the HOME hardware key
        mAnySoftKeyboardUnderTest.hideWindow();

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        // we switched to the main-keyboard view
        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }
}
