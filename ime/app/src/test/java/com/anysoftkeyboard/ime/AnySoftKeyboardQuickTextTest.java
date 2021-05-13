package com.anysoftkeyboard.ime;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.TestInputConnection;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(sdk = Config.NEWEST_SDK /*since we are sensitive to actual latest unicode emojis*/)
public class AnySoftKeyboardQuickTextTest extends AnySoftKeyboardBaseTest {
    // this is related to https://github.com/robolectric/robolectric/issues/6433
    // should be "\uD83D\uDE03"
    private static final String WRONG_KEY_OUTPUT = "\uFFFD\uFFFD";

    @Test
    public void testOutputTextKeyOutputText() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(WRONG_KEY_OUTPUT, inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(3, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());

        Assert.assertSame(
                mAnySoftKeyboardUnderTest.getInputView(),
                mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(1));

        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testOutputTextKeyOutputShiftedTextWhenShifted() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final Keyboard.Key aKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode('a');
        aKey.text = "this";
        aKey.shiftedText = "THiS";
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.mShiftKeyState.isActive());
        mAnySoftKeyboardUnderTest.onText(aKey, aKey.shiftedText);
        TestRxSchedulers.foregroundFlushAllJobs();

        Assert.assertEquals("THiS", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(4, mAnySoftKeyboardUnderTest.getCursorPosition());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
    }

    @Test
    public void testOutputTextKeyOutputTextWhenNotShifted() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.mShiftKeyState.isActive());
        final Keyboard.Key aKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode('a');
        aKey.text = "thisis";
        aKey.shiftedText = "THiS";
        mAnySoftKeyboardUnderTest.onText(aKey, aKey.text);
        TestRxSchedulers.foregroundFlushAllJobs();

        Assert.assertEquals("thisis", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(
                6,
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
    }

    @Test
    public void testOutputTextKeyOutputTextWhenShiftedButHasNoShiftedText() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.mShiftKeyState.isActive());
        final Keyboard.Key aKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode('a');
        aKey.text = "thisis";

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.mShiftKeyState.isActive());
        mAnySoftKeyboardUnderTest.onText(aKey, aKey.text);
        TestRxSchedulers.foregroundFlushAllJobs();

        Assert.assertEquals("thisis", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(
                6,
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
    }

    @Test
    public void testOutputTextKeyOutputTextWhenShiftLocked() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final Keyboard.Key aKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode('a');
        aKey.text = "thisis";
        aKey.shiftedText = "THiS";
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.mShiftKeyState.isActive());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.mShiftKeyState.isLocked());
        mAnySoftKeyboardUnderTest.onText(aKey, aKey.shiftedText);
        TestRxSchedulers.foregroundFlushAllJobs();

        Assert.assertEquals("THiS", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(
                4,
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
    }

    @Test
    public void testOutputTextKeyOutputTextAndThenBackspace() {
        final Keyboard.Key aKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode('a');
        aKey.text = "thisis";
        aKey.shiftedText = "THiS";
        mAnySoftKeyboardUnderTest.onText(aKey, aKey.text);
        TestRxSchedulers.foregroundFlushAllJobs();

        Assert.assertEquals("thisis", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertEquals(
                6,
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        // deletes all the output text
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertEquals(
                0,
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
    }

    @Test
    public void testOutputTextKeyOverrideOutputText() {
        simulateFinishInputFlow();
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                overrideText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
    }

    @Test
    public void testOutputTextDeletesOnBackspace() {
        simulateFinishInputFlow();
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        simulateOnStartInputFlow();

        final String initialText = "hello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                initialText + overrideText,
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
    }

    @Test
    public void testOutputTextDoesNotAutoCorrect() {
        simulateFinishInputFlow();
        final String overrideText = ".";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        simulateOnStartInputFlow();

        final String initialText = "hel";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());

        Assert.assertEquals(
                initialText + overrideText,
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testOutputTextDeletesOnBackspaceWhenSuggestionsOff() {
        simulateFinishInputFlow();
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        SharedPrefsHelper.setPrefsValue("candidates_on", false);
        simulateOnStartInputFlow();

        final String initialText = "hello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                initialText + overrideText,
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testOutputTextDeletesOnBackspaceWithoutSpace() {
        simulateFinishInputFlow();
        final String overrideText = "TEST";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        simulateOnStartInputFlow();
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
    public void testOutputTextDeletesOnBackspaceWhenSuggestionsOffWithoutSpace() {
        simulateFinishInputFlow();
        final String overrideText = "TEST";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_show_suggestions, false);
        simulateOnStartInputFlow();

        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        Assert.assertEquals(initialText, inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                initialText + overrideText, inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(initialText, inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDoesNotDeletesOnBackspaceIfCursorMoves() {
        simulateFinishInputFlow();
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        simulateOnStartInputFlow();
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello Xello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                initialText + overrideText, inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.moveCursorToPosition(7, true);
        Assert.assertEquals(7, inputConnection.getCurrentStartPosition());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertSame(mAnySoftKeyboardUnderTest.getCurrentInputConnection(), inputConnection);
        Assert.assertEquals(
                (initialText + overrideText).replace("X", ""),
                inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDoesNotDeletesOnBackspaceIfCursorMovesWhenSuggestionsOff() {
        simulateFinishInputFlow();
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_show_suggestions, false);
        simulateOnStartInputFlow();

        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final String initialText = "hello Xello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                initialText + overrideText, inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.moveCursorToPosition(7, true);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals(
                (initialText + overrideText).replace("X", ""),
                inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testOutputTextDoesNotDeletesOnCharacterIfCursorMoves() {
        simulateFinishInputFlow();
        final String overrideText = "TEST ";
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_emoticon_default_text, overrideText);
        simulateOnStartInputFlow();

        final String initialText = "hello Xello ";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals(
                initialText + overrideText,
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        mAnySoftKeyboardUnderTest.moveCursorToPosition(7, true);

        mAnySoftKeyboardUnderTest.simulateKeyPress('a');

        Assert.assertEquals(
                (initialText + overrideText).replace("X", "Xa"),
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testOutputTextKeySwitchKeyboardWhenFlipped() {
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_do_not_flip_quick_key_codes_functionality, false);
        simulateOnStartInputFlow();

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

        Assert.assertEquals(WRONG_KEY_OUTPUT, inputConnection.getCurrentTextInInputConnection());

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

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Assert.assertEquals(
                View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.VISIBLE,
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

        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.quick_text_pager_root));
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
        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.quick_text_pager_root)
                        .getVisibility());

        final EditorInfo editorInfo = mAnySoftKeyboardUnderTest.getCurrentInputEditorInfo();
        mAnySoftKeyboardUnderTest.onFinishInputView(false);

        Assert.assertEquals(
                View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.quick_text_pager_root)
                        .getVisibility());
        Assert.assertEquals(
                View.GONE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getCandidateView()
                        .getVisibility());

        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.quick_text_pager_root)
                        .getVisibility());
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
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.quick_text_pager_root)
                        .getVisibility());
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
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.quick_text_pager_root));
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

    @Test
    public void testOutputAsTypingKeyOutput() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final Keyboard.Key aKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode('a');
        aKey.typedText = "this";
        aKey.shiftedTypedText = "THiS";
        Assert.assertFalse(mAnySoftKeyboardUnderTest.mShiftKeyState.isActive());
        mAnySoftKeyboardUnderTest.onTyping(aKey, aKey.typedText);
        TestRxSchedulers.foregroundFlushAllJobs();

        Assert.assertEquals("this", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(
                4,
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
    }

    @Test
    public void testOutputAsTypingKeyOutputShifted() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final Keyboard.Key aKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode('a');
        aKey.typedText = "this";
        aKey.shiftedTypedText = "THiS";
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.mShiftKeyState.isActive());
        mAnySoftKeyboardUnderTest.onTyping(aKey, aKey.shiftedTypedText);
        TestRxSchedulers.foregroundFlushAllJobs();

        Assert.assertEquals("THiS", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(
                4,
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
    }

    @Test
    public void testOutputTextKeyOutputTypingAndThenBackspace() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        final Keyboard.Key aKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode('a');
        aKey.typedText = "thisis";
        aKey.shiftedTypedText = "THiS";
        mAnySoftKeyboardUnderTest.onTyping(aKey, aKey.typedText);
        TestRxSchedulers.drainAllTasksUntilEnd();

        Assert.assertEquals("thisis", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(
                6,
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        // deletes text as if was typed
        Assert.assertEquals("thisi", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(
                5,
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("this", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(
                4,
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
    }
}
