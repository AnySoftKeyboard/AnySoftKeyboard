package com.anysoftkeyboard;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.keyboards.ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens;

import android.content.res.Configuration;
import android.os.SystemClock;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.ExternalAnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardGimmicksTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testDoubleSpace() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();
        final String expectedText = "testing";
        inputConnection.commitText(expectedText, 1);

        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + " ", inputConnection.getCurrentTextInInputConnection());
        // double space
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + ". ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoubleSpaceNotDoneOnTimeOut() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();
        final String expectedText = "testing";
        inputConnection.commitText(expectedText, 1);

        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + " ", inputConnection.getCurrentTextInInputConnection());
        // double space very late
        SystemClock.sleep(
                Integer.parseInt(getResText(R.string.settings_default_multitap_timeout).toString())
                        + 1);
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + "  ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoubleSpaceNotDoneOnSpaceXSpace() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();
        final String expectedText = "testing";
        inputConnection.commitText(expectedText, 1);

        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + " ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('X');
        Assert.assertEquals(expectedText + " X", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(
                expectedText + " X ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(
                expectedText + " X. ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoubleSpaceReDotOnAdditionalSpace() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();
        final String expectedText = "testing";
        inputConnection.commitText(expectedText, 1);

        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + " ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + ". ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(
                expectedText + ".. ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(
                expectedText + "... ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testManualPickWordAndAnotherSpaceAndBackspace() {
        TestableAnySoftKeyboard.TestableSuggest spiedSuggest =
                (TestableAnySoftKeyboard.TestableSuggest)
                        mAnySoftKeyboardUnderTest.getSpiedSuggest();
        spiedSuggest.setSuggestionsForWord("he", "he'll", "hell", "hello");
        TestInputConnection inputConnection = getCurrentTestInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hell");
        // should have the picked word with an auto-added space
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        // another space should add a dot
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell. ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell.. ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnManuallyPicked() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hello");
        Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('.');
        Assert.assertEquals("hello. ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        Assert.assertEquals("hello. h", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnAutoCorrected() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress(',');
        Assert.assertEquals("hell, ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        Assert.assertEquals("hell, h", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoNotSwapNonPunctuationWithAutoSpaceOnAutoCorrected() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('2');
        Assert.assertEquals("hell 2", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell 2 hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('^');
        Assert.assertEquals("hell 2 hell ^", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoNotSwapPunctuationWithOnText() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals(
                "hell ", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.onText(null, ":)");
        Assert.assertEquals(
                "hell :)", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    }

    @Test
    public void testDoNotSwapPunctuationIfSwapPrefDisabled() {
        SharedPrefsHelper.setPrefsValue(
                getApplicationContext()
                        .getString(R.string.settings_key_bool_should_swap_punctuation_and_space),
                false);
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress(',');
        Assert.assertEquals("hell ,", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        Assert.assertEquals("hell ,h", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnAutoPicked() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hell");
        verifySuggestions(true, "hell", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('?');
        Assert.assertEquals("hell? ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        Assert.assertEquals("hell? h", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSendsENTERKeyEventIfShiftIsNotPressedAndImeDoesNotHaveAction() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

        ArgumentCaptor<KeyEvent> keyEventArgumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        Mockito.verify(inputConnection, Mockito.times(2))
                .sendKeyEvent(keyEventArgumentCaptor.capture());

        Assert.assertEquals(2 /*down and up*/, keyEventArgumentCaptor.getAllValues().size());
        Assert.assertEquals(
                KeyEvent.KEYCODE_ENTER, keyEventArgumentCaptor.getAllValues().get(0).getKeyCode());
        Assert.assertEquals(
                KeyEvent.ACTION_DOWN, keyEventArgumentCaptor.getAllValues().get(0).getAction());
        Assert.assertEquals(
                KeyEvent.KEYCODE_ENTER, keyEventArgumentCaptor.getAllValues().get(1).getKeyCode());
        Assert.assertEquals(
                KeyEvent.ACTION_UP, keyEventArgumentCaptor.getAllValues().get(1).getAction());
        // and never the ENTER character
        Assert.assertEquals("\n", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSendsENTERKeyEventIfShiftIsPressedAndImeDoesNotHaveAction() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

        ArgumentCaptor<KeyEvent> keyEventArgumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        Mockito.verify(inputConnection, Mockito.times(2))
                .sendKeyEvent(keyEventArgumentCaptor.capture());

        Assert.assertEquals(2 /*down and up*/, keyEventArgumentCaptor.getAllValues().size());
        Assert.assertEquals(
                KeyEvent.KEYCODE_ENTER, keyEventArgumentCaptor.getAllValues().get(0).getKeyCode());
        Assert.assertEquals(
                KeyEvent.ACTION_DOWN, keyEventArgumentCaptor.getAllValues().get(0).getAction());
        Assert.assertEquals(
                KeyEvent.KEYCODE_ENTER, keyEventArgumentCaptor.getAllValues().get(1).getKeyCode());
        Assert.assertEquals(
                KeyEvent.ACTION_UP, keyEventArgumentCaptor.getAllValues().get(1).getAction());
        // and we have ENTER in the input-connection
        Assert.assertEquals("\n", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSendsENTERCharacterIfShiftIsPressedAndImeHasAction() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
        editorInfo.imeOptions = EditorInfo.IME_ACTION_GO;
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        TestInputConnection inputConnection = getCurrentTestInputConnection();
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

        Mockito.verify(inputConnection).commitText("\n", 1);
        // and never the key-events
        Mockito.verify(inputConnection, Mockito.never()).sendKeyEvent(Mockito.any(KeyEvent.class));
    }

    @Test
    public void testDeleteWholeWordWhenShiftAndBackSpaceArePressed() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoesNotDeleteEntireWordWhenShiftDeleteInsideWord() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("Auto");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("space");
        Assert.assertEquals("Auto space", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(7, 7);

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("Auto ace", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(5, getCurrentTestInputConnection().getCurrentStartPosition());
    }

    @Test
    public void testDoesNotDeleteEntireWordWhenShiftDeleteInsideWordWhenNotPredicting() {
        simulateFinishInputFlow();

        mAnySoftKeyboardUnderTest.getResources().getConfiguration().keyboard =
                Configuration.KEYBOARD_NOKEYS;

        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS));

        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("Auto");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("space");
        Assert.assertEquals("Auto space", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(7, 7);

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("Auto ace", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(5, getCurrentTestInputConnection().getCurrentStartPosition());
    }

    @Test
    public void testHappyPathBackWordWhenNotPredicting() {
        simulateFinishInputFlow();

        mAnySoftKeyboardUnderTest.getResources().getConfiguration().keyboard =
                Configuration.KEYBOARD_NOKEYS;

        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS));

        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("Auto");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("space");
        Assert.assertEquals("Auto space", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("Auto ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("Auto", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testHappyPathBackWordWhenPredicting() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("Auto");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("space");
        Assert.assertEquals("Auto space", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("Auto ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("Auto", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDeleteCharacterWhenNoShiftAndBackSpaceArePressed() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDeleteWholeTextFromOnText() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello ");
        Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onText(null, "text");

        Assert.assertEquals("hello text", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDeleteCharacterWhenShiftAndBackSpaceArePressedAndOptionDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_use_backword, false);

        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDeleteCharacterWhenShiftLockedAndBackSpaceArePressed() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        // now it is locked
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDeleteCharacterWhenShiftLockedAndHeldAndBackSpaceArePressed() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        // now it is locked
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDeleteCharacterWhenNoShiftAndBackSpaceArePressedAndOptionDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_use_backword, false);
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnAutoCorrectedWithPunctuation() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('!');
        Assert.assertEquals("hell!", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hell! ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnAutoPickedWithPunctuation() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('.');
        Assert.assertEquals("hell.", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        Assert.assertEquals("hell.h", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnAutoPickedWithDoublePunctuation() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('.');
        Assert.assertEquals("hell.", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('.');
        Assert.assertEquals("hell..", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hell.. ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testPrintsParenthesisAsIsWithLTRKeyboard() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress('(');
        Assert.assertEquals("(", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(')');
        Assert.assertEquals("()", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testPrintsParenthesisReversedWithRTLKeyboard() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        AnyKeyboard fakeRtlKeyboard =
                Mockito.spy(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        Mockito.doReturn(false).when(fakeRtlKeyboard).isLeftToRightLanguage();
        mAnySoftKeyboardUnderTest.onAlphabetKeyboardSet(fakeRtlKeyboard);

        mAnySoftKeyboardUnderTest.simulateKeyPress('(');
        Assert.assertEquals(")", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(')');
        Assert.assertEquals(")(", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testShiftBehaviorForLetters() throws Exception {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("q", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQq", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQqQ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQqQQ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQqQQq", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQqQQqQ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQqQQqQQ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onRelease(KeyCodes.SHIFT);

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQqQQqQQq", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testLongShiftBehaviorForLetters() throws Exception {
        final int longPressTime =
                Integer.parseInt(
                                getResText(R.string.settings_default_long_press_timeout).toString())
                        + 20;

        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("q", inputConnection.getCurrentTextInInputConnection());

        // long press should switch to caps-lock
        AnyKeyboard.AnyKey shiftKey =
                (AnyKeyboard.AnyKey)
                        mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode(KeyCodes.SHIFT);
        Assert.assertNotNull(shiftKey);

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        SystemClock.sleep(longPressTime);
        mAnySoftKeyboardUnderTest.onRelease(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQq", inputConnection.getCurrentTextInInputConnection());

        // now from lock to unlock with just shift
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT_LOCK);
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQqQ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQqQq", inputConnection.getCurrentTextInInputConnection());

        // and now long-press but multi-touch typing
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        SystemClock.sleep(longPressTime);

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQqQqQ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQqQqQQ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onRelease(KeyCodes.SHIFT);

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQqQqQQq", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        SystemClock.sleep(longPressTime);
        mAnySoftKeyboardUnderTest.onRelease(KeyCodes.SHIFT);

        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQqQqQQqQ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQqQqQQqQQ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQqQqQQqQQq", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('q');
        Assert.assertEquals("qQQQqQqQQqQQqq", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testShiftBehaviorForNonLetters() throws Exception {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("'", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);

        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("''", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("'''", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);

        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("''''", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("'''''", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);

        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("''''''", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getShiftKey().onPressed();

        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("''''''\"", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("''''''\"\"", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onRelease(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getShiftKey().onReleased();

        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("''''''\"\"'", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testEditorPerformsActionIfImeOptionsSpecified() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_DONE,
                        InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        Assert.assertEquals(EditorInfo.IME_ACTION_DONE, inputConnection.getLastEditorAction());
        // did not passed the ENTER to the IC
        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testEditorPerformsActionIfActionLabelSpecified() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_UNSPECIFIED,
                        InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editorInfo.actionId = 99;
        editorInfo.actionLabel = "test label";
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        Assert.assertEquals(99, inputConnection.getLastEditorAction());
        // did not passed the ENTER to the IC
        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testEditorDoesNotPerformsActionIfNoEnterActionFlagIsSet() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_ENTER_ACTION,
                        InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        // did not perform action
        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        // passed the ENTER to the IC
        Assert.assertEquals("\n", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testEditorDoesPerformsActionImeIsUnSpecified() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_UNSPECIFIED,
                        InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        // did not perform action
        Assert.assertEquals(
                EditorInfo.IME_ACTION_UNSPECIFIED, inputConnection.getLastEditorAction());
        // did not passed the ENTER to the IC
        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testEditorPerformsActionIfSpecifiedButNotSendingEnter() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_DONE,
                        InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        Assert.assertEquals(" ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSendsEnterIfNoneAction() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        // passed the ENTER to the IC
        Assert.assertEquals("\n", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSendsEnterIfUnspecificAction() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_UNSPECIFIED, 0);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        Assert.assertEquals(0, inputConnection.getLastEditorAction());
    }

    @Test
    @Config(qualifiers = "w480dp-h640dp-port-mdpi")
    public void testSplitStatesPortrait() {
        getApplicationContext().getResources().getConfiguration().keyboard =
                Configuration.KEYBOARD_NOKEYS;

        // verify device config, to ensure test is valid
        Assert.assertEquals(
                160, getApplicationContext().getResources().getConfiguration().densityDpi);
        Assert.assertEquals(
                480, getApplicationContext().getResources().getConfiguration().screenWidthDp);
        Assert.assertEquals(
                640, getApplicationContext().getResources().getConfiguration().screenHeightDp);
        Assert.assertEquals(
                Configuration.ORIENTATION_PORTRAIT,
                getApplicationContext().getResources().getConfiguration().orientation);

        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_default_split_state_portrait, "split");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();

        assertKeyDimensions(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 2, 5, 130);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_default_split_state_portrait, "compact_right");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        assertKeyDimensions(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0),
                168,
                5,
                105);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_default_split_state_portrait, "compact_left");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        assertKeyDimensions(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 2, 5, 105);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_default_split_state_portrait, "merged");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        assertKeyDimensions(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 2, 5, 163);
    }

    @Test
    @Config(qualifiers = "w480dp-h640dp-land-mdpi")
    public void testSplitStatesLandscape() {
        getApplicationContext().getResources().getConfiguration().keyboard =
                Configuration.KEYBOARD_NOKEYS;

        // verify device config, to ensure test is valid
        Assert.assertEquals(
                160, getApplicationContext().getResources().getConfiguration().densityDpi);
        Assert.assertEquals(
                640, getApplicationContext().getResources().getConfiguration().screenWidthDp);
        Assert.assertEquals(
                480, getApplicationContext().getResources().getConfiguration().screenHeightDp);
        Assert.assertEquals(
                Configuration.ORIENTATION_LANDSCAPE,
                getApplicationContext().getResources().getConfiguration().orientation);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_default_split_state_landscape, "split");

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        // split, since we switched to landscape
        assertKeyDimensions(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 2, 5, 131);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_default_split_state_landscape, "compact_right");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        assertKeyDimensions(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0),
                378,
                5,
                87);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_default_split_state_landscape, "compact_left");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        assertKeyDimensions(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 2, 5, 87);

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_default_split_state_landscape, "merged");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        assertKeyDimensions(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 2, 5, 219);
    }

    @Test
    public void testSwapDoublePunctuationsWhenNotInFrLocale() {
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('!');
        Assert.assertEquals("hell! ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell! hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('?');
        Assert.assertEquals("hell! hell? ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell! hell? hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress(':');
        Assert.assertEquals(
                "hell! hell? hell: ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoNotSwapDoublePunctuationsWhenInFrLocale() {
        final AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        ExternalAnyKeyboard keyboard =
                new ExternalAnyKeyboard(
                        currentKeyboard.getKeyboardAddOn(),
                        ApplicationProvider.getApplicationContext(),
                        R.xml.qwerty,
                        R.xml.qwerty,
                        "fr",
                        R.drawable.ic_status_english,
                        0,
                        "fr",
                        "",
                        new String(currentKeyboard.getSentenceSeparators()),
                        currentKeyboard.getKeyboardMode());
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens);

        mAnySoftKeyboardUnderTest.onAlphabetKeyboardSet(keyboard);
        TestInputConnection inputConnection = getCurrentTestInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('!');
        Assert.assertEquals("hell !", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ! hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('?');
        Assert.assertEquals("hell ! hell ?", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals(
                "hell ! hell ? hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress(':');
        Assert.assertEquals(
                "hell ! hell ? hell :", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals(
                "hell ! hell ? hell : hell ", inputConnection.getCurrentTextInInputConnection());
        // typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress(';');
        Assert.assertEquals(
                "hell ! hell ? hell : hell ;", inputConnection.getCurrentTextInInputConnection());
    }

    private void assertKeyDimensions(Keyboard.Key key, int x, int y, int width) {
        Assert.assertEquals("X position is wrong", x, key.x);
        Assert.assertEquals("Y position is wrong", y, key.y);
        Assert.assertEquals("Key width is wrong", width, key.width);
    }
}
