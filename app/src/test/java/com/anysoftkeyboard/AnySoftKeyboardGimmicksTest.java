package com.anysoftkeyboard;

import android.content.res.Configuration;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowSystemClock;

@RunWith(RobolectricTestRunner.class)
public class AnySoftKeyboardGimmicksTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testDoubleSpace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "testing";
        inputConnection.commitText(expectedText, 1);

        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + " ", inputConnection.getCurrentTextInInputConnection());
        //double space
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + ". ", inputConnection.getCurrentTextInInputConnection());

    }

    @Test
    public void testDoubleSpaceNotDoneOnTimeOut() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "testing";
        inputConnection.commitText(expectedText, 1);

        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + " ", inputConnection.getCurrentTextInInputConnection());
        //double space very late
        ShadowSystemClock.sleep(AnyApplication.getConfig().getMultiTapTimeout() + 1);
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + "  ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoubleSpaceNotDoneOnSpaceXSpace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "testing";
        inputConnection.commitText(expectedText, 1);

        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + " ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('X');
        Assert.assertEquals(expectedText + " X", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + " X ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + " X. ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoubleSpaceReDotOnAdditionalSpace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "testing";
        inputConnection.commitText(expectedText, 1);

        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + " ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + ". ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + ".. ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals(expectedText + "... ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testManualPickWordAndAnotherSpaceAndBackspace() {
        TestableAnySoftKeyboard.TestableSuggest spiedSuggest = (TestableAnySoftKeyboard.TestableSuggest) mAnySoftKeyboardUnderTest.getSpiedSuggest();
        spiedSuggest.setSuggestionsForWord("he", "he'll", "hell", "hello");
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hell");
        //should have the picked word with an auto-added space
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //another space should add a dot
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell. ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell.. ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnManuallyPicked() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hello");
        Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());
        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('.');
        Assert.assertEquals("hello. ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        Assert.assertEquals("hello. h", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnAutoCorrected() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress(',');
        Assert.assertEquals("hell, ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        Assert.assertEquals("hell, h", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoNotSwapNonPunctuationWithAutoSpaceOnAutoCorrected() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('2');
        Assert.assertEquals("hell 2", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell 2 hell ", inputConnection.getCurrentTextInInputConnection());
        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('^');
        Assert.assertEquals("hell 2 hell ^", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoNotSwapPunctuationWithOnText() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //typing punctuation
        mAnySoftKeyboardUnderTest.onText(null, ":)");
        Assert.assertEquals("hell :)", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoNotSwapPunctuationIfSwapPrefDisabled() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_bool_should_swap_punctuation_and_space), false);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress(',');
        Assert.assertEquals("hell ,", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        Assert.assertEquals("hell ,h", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnAutoPicked() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hell");
        verifySuggestions(true, "hell", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('?');
        Assert.assertEquals("hell? ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        Assert.assertEquals("hell? h", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSendsENTERKeyEventIfShiftIsNotPressedAndImeDoesNotHaveAction() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

        ArgumentCaptor<KeyEvent> keyEventArgumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        Mockito.verify(inputConnection, Mockito.times(2)).sendKeyEvent(keyEventArgumentCaptor.capture());

        Assert.assertEquals(2/*down and up*/, keyEventArgumentCaptor.getAllValues().size());
        Assert.assertEquals(KeyEvent.KEYCODE_ENTER, keyEventArgumentCaptor.getAllValues().get(0).getKeyCode());
        Assert.assertEquals(KeyEvent.ACTION_DOWN, keyEventArgumentCaptor.getAllValues().get(0).getAction());
        Assert.assertEquals(KeyEvent.KEYCODE_ENTER, keyEventArgumentCaptor.getAllValues().get(1).getKeyCode());
        Assert.assertEquals(KeyEvent.ACTION_UP, keyEventArgumentCaptor.getAllValues().get(1).getAction());
        //and never the ENTER character
        Assert.assertEquals("\n", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSendsENTERKeyEventIfShiftIsPressedAndImeDoesNotHaveAction() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

        ArgumentCaptor<KeyEvent> keyEventArgumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        Mockito.verify(inputConnection, Mockito.times(2)).sendKeyEvent(keyEventArgumentCaptor.capture());

        Assert.assertEquals(2/*down and up*/, keyEventArgumentCaptor.getAllValues().size());
        Assert.assertEquals(KeyEvent.KEYCODE_ENTER, keyEventArgumentCaptor.getAllValues().get(0).getKeyCode());
        Assert.assertEquals(KeyEvent.ACTION_DOWN, keyEventArgumentCaptor.getAllValues().get(0).getAction());
        Assert.assertEquals(KeyEvent.KEYCODE_ENTER, keyEventArgumentCaptor.getAllValues().get(1).getKeyCode());
        Assert.assertEquals(KeyEvent.ACTION_UP, keyEventArgumentCaptor.getAllValues().get(1).getAction());
        //and we have ENTER in the input-connection
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

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

        Mockito.verify(inputConnection).commitText("\n", 1);
        //and never the key-events
        Mockito.verify(inputConnection, Mockito.never()).sendKeyEvent(Mockito.any(KeyEvent.class));
    }

    @Test
    public void testDeleteWholeWordWhenShiftAndBackSpaceArePressed() {
        Assert.assertTrue(AnyApplication.getConfig().useBackword());//default behavior

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoesNotDeleteEntireWordWhenShiftDeleteInsideWord() {
        Assert.assertTrue(AnyApplication.getConfig().useBackword());//default behavior

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("Auto");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("space");
        Assert.assertEquals("Auto space", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(7, 7);

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("Auto ace", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(5, ((TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection()).getCurrentStartPosition());
    }

    @Test
    public void testDoesNotDeleteEntireWordWhenShiftDeleteInsideWordWhenNotPredicting() {
        simulateFinishInputFlow(false);
        Assert.assertTrue(AnyApplication.getConfig().useBackword());//default behavior

        mAnySoftKeyboardUnderTest.getResources().getConfiguration().keyboard = Configuration.KEYBOARD_NOKEYS;

        simulateOnStartInputFlow(false, true, TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS));

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("Auto");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("space");
        Assert.assertEquals("Auto space", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(7, 7);

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("Auto ace", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(5, ((TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection()).getCurrentStartPosition());
    }

    @Test
    public void testHappyPathBackWordWhenNotPredicting() {
        simulateFinishInputFlow(false);
        Assert.assertTrue(AnyApplication.getConfig().useBackword());//default behavior

        mAnySoftKeyboardUnderTest.getResources().getConfiguration().keyboard = Configuration.KEYBOARD_NOKEYS;

        simulateOnStartInputFlow(false, true, TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS));

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

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
        Assert.assertTrue(AnyApplication.getConfig().useBackword());//default behavior

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

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
        Assert.assertTrue(AnyApplication.getConfig().useBackword());//default behavior

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());

    }

    @Test
    public void testDeleteWholeTextFromOnText() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

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
        Assert.assertFalse(AnyApplication.getConfig().useBackword());
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDeleteCharacterWhenShiftLockedAndBackSpaceArePressed() {
        Assert.assertTrue(AnyApplication.getConfig().useBackword());
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        //now it is locked
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDeleteCharacterWhenShiftLockedAndHeldAndBackSpaceArePressed() {
        Assert.assertTrue(AnyApplication.getConfig().useBackword());
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        //now it is locked
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShiftLocked());

        mAnySoftKeyboardUnderTest.onPress(KeyCodes.SHIFT);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDeleteCharacterWhenNoShiftAndBackSpaceArePressedAndOptionDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_use_backword, false);
        Assert.assertFalse(AnyApplication.getConfig().useBackword());
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnAutoCorrectedWithPunctuation() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('!');
        Assert.assertEquals("hell!", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hell! ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnAutoPickedWithPunctuation() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('.');
        Assert.assertEquals("hell.", inputConnection.getCurrentTextInInputConnection());
        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        Assert.assertEquals("hell.h", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwapPunctuationWithAutoSpaceOnAutoPickedWithDoublePunctuation() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('.');
        Assert.assertEquals("hell.", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('.');
        Assert.assertEquals("hell..", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hell.. ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testPrintsParenthesisAsIsWithLTRKeyboard() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress('(');
        Assert.assertEquals("(", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(')');
        Assert.assertEquals("()", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testPrintsParenthesisReversedWithRTLKeyboard() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        AnyKeyboard fakeRtlKeyboard = Mockito.spy(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        Mockito.doReturn(false).when(fakeRtlKeyboard).isLeftToRightLanguage();
        mAnySoftKeyboardUnderTest.onAlphabetKeyboardSet(fakeRtlKeyboard);

        mAnySoftKeyboardUnderTest.simulateKeyPress('(');
        Assert.assertEquals(")", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(')');
        Assert.assertEquals(")(", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testShiftBehaviorForLetters() throws Exception {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

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
    public void testShiftBehaviorForNonLetters() throws Exception {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

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

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_DONE, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        Assert.assertEquals(EditorInfo.IME_ACTION_DONE, inputConnection.getLastEditorAction());
        //did not passed the ENTER to the IC
        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testEditorPerformsActionIfActionLabelSpecified() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_UNSPECIFIED, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editorInfo.actionId = 99;
        editorInfo.actionLabel = "test label";
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        Assert.assertEquals(99, inputConnection.getLastEditorAction());
        //did not passed the ENTER to the IC
        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testEditorDoesNotPerformsActionIfNoEnterActionFlagIsSet() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_ENTER_ACTION, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        //did not perform action
        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        //passed the ENTER to the IC
        Assert.assertEquals("\n", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testEditorDoesPerformsActionImeIsUnSpecified() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_UNSPECIFIED, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        //did not perform action
        Assert.assertEquals(EditorInfo.IME_ACTION_UNSPECIFIED, inputConnection.getLastEditorAction());
        //did not passed the ENTER to the IC
        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testEditorPerformsActionIfSpecifiedButNotSendingEnter() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_DONE, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        Assert.assertEquals(" ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSendsEnterIfNoneAction() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        //passed the ENTER to the IC
        Assert.assertEquals("\n", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSendsEnterIfUnspecificAction() throws Exception {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_UNSPECIFIED, 0);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        Assert.assertEquals(0, inputConnection.getLastEditorAction());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        Assert.assertEquals(0, inputConnection.getLastEditorAction());
    }

    @Test
    public void testSplitStatesPortrait() {
        RuntimeEnvironment.application.getResources().getConfiguration().keyboard = Configuration.KEYBOARD_NOKEYS;

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_split_state_portrait, "split");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, false, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeast(1)).flushKeyboardsCache();
        assertKeyDimensions(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 0, 3, 150);

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_split_state_portrait, "compact_right");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, false, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeast(1)).flushKeyboardsCache();
        assertKeyDimensions(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 101, 3, 133);

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_split_state_portrait, "compact_left");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, false, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeast(1)).flushKeyboardsCache();
        assertKeyDimensions(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 0, 3, 133);

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_split_state_portrait, "merged");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, false, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeast(1)).flushKeyboardsCache();
        assertKeyDimensions(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 0, 3, 167);
    }

    @Test
    public void testSplitStatesLandscape() {
        final Configuration configuration = RuntimeEnvironment.application.getResources().getConfiguration();
        configuration.keyboard = Configuration.KEYBOARD_NOKEYS;

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_split_state_landscape, "split");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, false, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeast(1)).flushKeyboardsCache();
        //merged, since we are in portrait
        assertKeyDimensions(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 0, 3, 167);

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        configuration.keyboard = Configuration.KEYBOARD_NOKEYS;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        simulateOnStartInputFlow(true, true, createEditorInfoTextWithSuggestionsForSetUp());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeast(1)).flushKeyboardsCache();
        //split, since we switched to landscape
        assertKeyDimensions(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 0, 3, 150);

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_split_state_landscape, "compact_right");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeast(1)).flushKeyboardsCache();
        assertKeyDimensions(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 101, 3, 133);

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_split_state_landscape, "compact_left");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeast(1)).flushKeyboardsCache();
        assertKeyDimensions(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 0, 3, 133);

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_split_state_landscape, "merged");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        simulateOnStartInputFlow(true, true, createEditorInfoTextWithSuggestionsForSetUp());

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeast(1)).flushKeyboardsCache();
        assertKeyDimensions(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys().get(0), 0, 3, 167);
    }

    private void assertKeyDimensions(Keyboard.Key key, int x, int y, int width) {
        Assert.assertEquals(x, key.x);
        Assert.assertEquals(y, key.y);
        Assert.assertEquals(width, key.width);
    }
}