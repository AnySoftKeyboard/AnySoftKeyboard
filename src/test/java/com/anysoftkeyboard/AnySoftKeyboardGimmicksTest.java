package com.anysoftkeyboard;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.api.KeyCodes;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.AskGradleTestRunner;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowSystemClock;

@RunWith(AskGradleTestRunner.class)
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
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

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
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

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
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('2');
        Assert.assertEquals("hell 2", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

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
        verifySuggestions(mSpiedCandidateView, true, "hell", "hell", "hello");

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
        Mockito.verify(inputConnection, Mockito.never()).commitText("\n", 1);
    }

    @Test
    public void testSendsENTERKeyEventIfShiftIsPressedButImeDoesNotHaveAction() {
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
        Mockito.verify(inputConnection, Mockito.never()).commitText("\n", 1);
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
    public void testSwapPunctuationWithAutoSpaceOnAutoCorrectedWithPunctuation() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

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
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

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
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

        //typing punctuation
        mAnySoftKeyboardUnderTest.simulateKeyPress('.');
        Assert.assertEquals("hell.", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('.');
        Assert.assertEquals("hell..", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hell.. ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwipeLeftFromBackSpace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey(KeyCodes.DELETE);
        mAnySoftKeyboardUnderTest.onSwipeLeft(false);
        Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwipeRightFromBackSpace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey(KeyCodes.DELETE);
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSwipeLeftNotFromBackSpace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeLeft(false);
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
    }
}