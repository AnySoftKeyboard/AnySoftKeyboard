package com.anysoftkeyboard;

import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.api.KeyCodes;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnySoftKeyboardDictionaryGetWordsTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testAskForSuggestions() {
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hel", "hell", "hello");
    }

    @Test
    public void testAskForSuggestionsWithoutInputConnectionUpdates() {
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateKeyPress('e');
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress('l');
        verifySuggestions(true, "hel", "hell", "hello");
    }

    @Test
    public void testAskForSuggestionsWithDelayedInputConnectionUpdates() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        inputConnection.setSendUpdates(false);
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateKeyPress('e');
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        //sending a delayed event from the input-connection.
        //this can happen when the user is clicking fast (in ASK thread), but the other side (the app thread)
        //is too slow, or busy with something to send out events.
        inputConnection.sendUpdateNow();

        mAnySoftKeyboardUnderTest.simulateKeyPress('l');
        verifySuggestions(true, "hel", "hell", "hello");
    }

    @Test
    public void testAskForSuggestionsWhenCursorInsideWord() {
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hl");
        //moving one character back, and fixing the word to 'hel'
        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(1, 1);
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(true, "hel", "hell", "hello");
    }

    @Test
    public void testAutoPickWordWhenCursorAtTheEndOfTheWord() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hel", "hell", "hello");

        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hell", inputConnection.getLastCommitCorrection());
        //we should also see the space
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //now, if we press DELETE, the word should be reverted
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testManualPickWordAndShouldNotRevert() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hell");
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //now, if we press DELETE, the word should be reverted
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testManualPickWordAndAnotherSpaceAndBackspace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hell");
        //another space
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hell. ", inputConnection.getCurrentTextInInputConnection());
        //now, if we press DELETE, the word should NOT be reverted
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hell.", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testManualPickUnknownWordAndThenBackspace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hellp");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hellp");

        Assert.assertEquals("hellp ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hellp", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testManualPickUnknownWordAndPunctuationAndThenBackspace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hellp");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hellp");

        Assert.assertEquals("hellp ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateTextTyping("!");

        Assert.assertEquals("hellp! ", inputConnection.getCurrentTextInInputConnection());
        //now, if we press DELETE, the word should NOT be reverted
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hellp!", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testSpaceAutoPickWordAndAnotherSpaceAndBackspace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());
        //another space
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("he'll. ", inputConnection.getCurrentTextInInputConnection());
        //now, if we press DELETE, the word should NOT be reverted
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("he'll.", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testAutoPickWordWhenCursorAtTheEndOfTheWordWithWordSeparator() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hel", "hell", "hello");

        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('?');
        Assert.assertEquals("hell", inputConnection.getLastCommitCorrection());
        //we should also see the question mark
        Assert.assertEquals("hell?", inputConnection.getCurrentTextInInputConnection());
        //now, if we press DELETE, the word should be reverted
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoesNotAutoPickWordWhenCursorNotAtTheEndOfTheWord() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(true, "hl");
        //moving one character back, and fixing the word to 'hel'
        inputConnection.setSelection(1, 1);
        mAnySoftKeyboardUnderTest.simulateKeyPress('e');
        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(2, 2);
        verifySuggestions(true, "hel", "hell", "hello");

        Mockito.reset(inputConnection);//clearing any previous interactions with finishComposingText
        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        //this time, it will not auto-pick since the cursor is inside the word (and not at the end)
        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        //will stop composing in the input-connection
        Mockito.verify(inputConnection).finishComposingText();
        //also, it will abort suggestions
        verifySuggestions(true);
    }

    @Test
    public void testBackSpaceCorrectlyWhenEditingManuallyPickedWord() {
        //related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/585
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        //at this point, the candidates view will show a hint
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
        //now, navigating to to the 'e'
        inputConnection.setSelection(2, 2);
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(2, inputConnection.getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hl ", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(1, inputConnection.getCurrentStartPosition());
    }

    @Test
    public void testBackSpaceCorrectlyAfterEnter() {
        //related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/920
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals("", inputConnection.getLastCommitCorrection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        //note: there shouldn't be any correction possible here.
        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        Assert.assertEquals("hel\n", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(3, inputConnection.getCurrentStartPosition());
    }

    @Test
    public void testBackSpaceCorrectlyAfterAutoSpaceAndEnter() {
        //related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/920
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hello");
        Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        Assert.assertEquals("hello hel", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        //note: there shouldn't be any correction possible here.
        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        Assert.assertEquals("hello hel\n", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello hel", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(9, inputConnection.getCurrentStartPosition());
    }

    @Test
    public void testBackSpaceCorrectlyAfterAutoSpaceAndEnterWithDelayedUpdates() throws Exception {
        //related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/920
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        inputConnection.setSendUpdates(false);

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hello");
        Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        Assert.assertEquals("hello hel", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
        //note: there shouldn't be any correction possible here.
        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        Assert.assertEquals("hello hel\n", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, false);
        inputConnection.sendUpdateNow();
        Assert.assertEquals("hello hel", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(9, inputConnection.getCurrentStartPosition());
    }

    @Test
    public void testBackSpaceCorrectlyWhenEditingAutoCorrectedWord() {
        //related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/585
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //now, navigating to to the 'e'
        inputConnection.setSelection(2, 2);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(2, inputConnection.getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hll ", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(1, inputConnection.getCurrentStartPosition());
    }

    @Test
    public void testBackSpaceAfterAutoPickingAutoSpaceAndEnter() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');

        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

        Assert.assertEquals("hell\n", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(5, inputConnection.getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(4, inputConnection.getCurrentStartPosition());
    }

    @Test
    public void testBackSpaceAfterAutoPickingWithoutAutoSpaceAndEnter() {
        SharedPrefsHelper.setPrefsValue("insert_space_after_word_suggestion_selection", false);
        simulateFinishInputFlow(false);
        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, 0);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');

        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

        Assert.assertEquals("hel \n", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(5, inputConnection.getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(4, inputConnection.getCurrentStartPosition());
    }

    @Test
    public void testBackSpaceAfterManualPickingAutoSpaceAndEnter() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "hell");

        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

        Assert.assertEquals("hell\n", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(5, inputConnection.getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(4, inputConnection.getCurrentStartPosition());
    }

    @Test
    public void testBackSpaceAfterManualPickingWithoutAutoSpaceAndEnter() {
        SharedPrefsHelper.setPrefsValue("insert_space_after_word_suggestion_selection", false);
        simulateFinishInputFlow(false);
        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "hell");

        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

        Assert.assertEquals("hell\n", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(5, inputConnection.getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(4, inputConnection.getCurrentStartPosition());
    }

    @Test
    public void testManualPickWordLongerWordAndBackspaceAndTypeCharacter() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true, "hel", "hell", "hello");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "hell");
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //backspace
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
        //some random character now
        mAnySoftKeyboardUnderTest.simulateKeyPress('k');
        Assert.assertEquals("hellk", inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testDoesNotSuggestInPasswordField() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NEXT, EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);

        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        mAnySoftKeyboardUnderTest.resetMockCandidateView();
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(true/*empty suggestions passed*/);
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        verifyNoSuggestionsInteractions();
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifyNoSuggestionsInteractions();
        Assert.assertEquals("hel hel", inputConnection.getCurrentTextInInputConnection());
    }
}