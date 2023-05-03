package com.anysoftkeyboard;

import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardDictionaryGetWordsTest extends AnySoftKeyboardBaseTest {

  @Test
  public void testAskForSuggestions() {
    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("h");
    verifySuggestions(true, "h", "he");
    mAnySoftKeyboardUnderTest.simulateTextTyping("e");
    verifySuggestions(true, "he", "hell", "hello", "he'll");
    mAnySoftKeyboardUnderTest.simulateTextTyping("l");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");
  }

  @Test
  public void testPerformUpdateSuggestionsOnSeparatorQuickly() {
    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("h");
    verifySuggestions(true, "h", "he");
    mAnySoftKeyboardUnderTest.simulateTextTyping("e");
    verifySuggestions(true, "he", "hell", "hello", "he'll");
    mAnySoftKeyboardUnderTest.simulateKeyPress('l', false);
    Assert.assertEquals("hel", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ', false);
    // correctly auto-picked
    Assert.assertEquals("he'll ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
  }

  @Test
  public void testAskForSuggestionsWithoutInputConnectionUpdates() {
    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateKeyPress('h');
    verifySuggestions(true, "h", "he");
    mAnySoftKeyboardUnderTest.simulateKeyPress('e');
    verifySuggestions(true, "he", "hell", "hello", "he'll");
    mAnySoftKeyboardUnderTest.simulateKeyPress('l');
    verifySuggestions(true, "hel", "he'll", "hello", "hell");
  }

  @Test
  public void testAskForSuggestionsWithDelayedInputConnectionUpdates() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    inputConnection.setUpdateSelectionDelay(1000000L);
    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateKeyPress('h');
    verifySuggestions(true, "h", "he");
    mAnySoftKeyboardUnderTest.simulateKeyPress('e');
    verifySuggestions(true, "he", "hell", "hello", "he'll");
    // sending a delayed event from the input-connection.
    // this can happen when the user is clicking fast (in ASK thread), but the other side (the
    // app thread)
    // is too slow, or busy with something to send out events.
    inputConnection.setUpdateSelectionDelay(1L);
    TestRxSchedulers.foregroundFlushAllJobs();

    mAnySoftKeyboardUnderTest.simulateKeyPress('l');
    verifySuggestions(true, "hel", "he'll", "hello", "hell");
  }

  @Test
  public void testAskForSuggestionsWhenCursorInsideWord() {
    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("h");
    verifySuggestions(true, "h", "he");
    mAnySoftKeyboardUnderTest.simulateTextTyping("l");
    verifySuggestions(true, "hl");
    // moving one character back, and fixing the word to 'hel'
    mAnySoftKeyboardUnderTest.setSelectedText(1, 1, true);
    mAnySoftKeyboardUnderTest.simulateTextTyping("e");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");
  }

  @Test
  public void testAutoPickWordWhenCursorAtTheEndOfTheWord() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("h");
    verifySuggestions(true, "h", "he");
    mAnySoftKeyboardUnderTest.simulateTextTyping("e");
    verifySuggestions(true, "he", "hell", "hello", "he'll");
    mAnySoftKeyboardUnderTest.simulateTextTyping("l");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");

    Assert.assertEquals("", inputConnection.getLastCommitCorrection());
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("he'll", inputConnection.getLastCommitCorrection());
    // we should also see the space
    Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());
    // now, if we press DELETE, the word should be reverted
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
  }

  @Test
  public void testManualPickWordAndShouldNotRevert() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    mAnySoftKeyboardUnderTest.simulateTextTyping("h");
    mAnySoftKeyboardUnderTest.simulateTextTyping("e");
    mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hell");
    Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
    // now, if we press DELETE, the word should be reverted
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
  }

  @Test
  public void testManualPickWordAndAnotherSpaceAndBackspace() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    mAnySoftKeyboardUnderTest.simulateTextTyping("h");
    mAnySoftKeyboardUnderTest.simulateTextTyping("e");
    mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hell");
    // another space
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
    Assert.assertEquals("hell. ", inputConnection.getCurrentTextInInputConnection());
    // now, if we press DELETE, the word should NOT be reverted
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hell.", inputConnection.getCurrentTextInInputConnection());
  }

  @Test
  public void testManualPickUnknownWordAndThenBackspace() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    mAnySoftKeyboardUnderTest.simulateTextTyping("hellp");
    mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hellp");

    Assert.assertEquals("hellp ", inputConnection.getCurrentTextInInputConnection());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hellp", inputConnection.getCurrentTextInInputConnection());
  }

  @Test
  public void testManualPickUnknownWordAndPunctuationAndThenBackspace() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    mAnySoftKeyboardUnderTest.simulateTextTyping("hellp");
    mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hellp");

    Assert.assertEquals("hellp ", inputConnection.getCurrentTextInInputConnection());

    mAnySoftKeyboardUnderTest.simulateTextTyping("!");

    Assert.assertEquals("hellp! ", inputConnection.getCurrentTextInInputConnection());
    // now, if we press DELETE, the word should NOT be reverted
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hellp!", inputConnection.getCurrentTextInInputConnection());
  }

  @Test
  public void testSpaceAutoPickWordAndAnotherSpaceAndBackspace() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    mAnySoftKeyboardUnderTest.simulateTextTyping("h");
    mAnySoftKeyboardUnderTest.simulateTextTyping("e");
    mAnySoftKeyboardUnderTest.simulateTextTyping("l");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
    Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());
    // another space
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
    Assert.assertEquals("he'll. ", inputConnection.getCurrentTextInInputConnection());
    // now, if we press DELETE, the word should NOT be reverted
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("he'll.", inputConnection.getCurrentTextInInputConnection());
  }

  @Test
  public void testSpaceAutoDisabledAutoCorrectAndBackSpace() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    mAnySoftKeyboardUnderTest.simulateTextTyping("h");
    mAnySoftKeyboardUnderTest.simulateTextTyping("e");
    mAnySoftKeyboardUnderTest.simulateTextTyping("l");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
    Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());
    // another space
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
    Assert.assertEquals("he'll. ", inputConnection.getCurrentTextInInputConnection());
    // now, if we press DELETE, the word should NOT be reverted
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("he'll.", inputConnection.getCurrentTextInInputConnection());
  }

  @Test
  public void testAutoPickWordWhenCursorAtTheEndOfTheWordWithWordSeparator() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("h");
    verifySuggestions(true, "h", "he");
    mAnySoftKeyboardUnderTest.simulateTextTyping("e");
    verifySuggestions(true, "he", "hell", "hello", "he'll");
    mAnySoftKeyboardUnderTest.simulateTextTyping("l");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");

    Assert.assertEquals("", inputConnection.getLastCommitCorrection());
    mAnySoftKeyboardUnderTest.simulateKeyPress('?');
    Assert.assertEquals("he'll", inputConnection.getLastCommitCorrection());
    // we should also see the question mark
    Assert.assertEquals("he'll?", inputConnection.getCurrentTextInInputConnection());
    // now, if we press DELETE, the word should be reverted
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
  }

  @Test
  public void testDoesNotAutoPickWordWhenCursorNotAtTheEndOfTheWord() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("h");
    verifySuggestions(true, "h", "he");
    mAnySoftKeyboardUnderTest.simulateTextTyping("l");
    verifySuggestions(true, "hl");
    Assert.assertEquals("hl", inputConnection.getCurrentTextInInputConnection());
    // moving one character back, and fixing the word to 'hel'
    mAnySoftKeyboardUnderTest.setSelectedText(1, 1, true);
    mAnySoftKeyboardUnderTest.simulateKeyPress('e');
    Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
    verifySuggestions(true, "hel", "he'll", "hello", "hell");

    Mockito.reset(inputConnection); // clearing any previous interactions with finishComposingText
    Assert.assertEquals("", inputConnection.getLastCommitCorrection());
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    // this time, it will not auto-pick since the cursor is inside the word (and not at the end)
    Assert.assertEquals("", inputConnection.getLastCommitCorrection());
    // will stop composing in the input-connection
    Mockito.verify(inputConnection).finishComposingText();
    // also, it will abort suggestions
    verifySuggestions(true);
  }

  @Test
  public void testBackSpaceCorrectlyWhenEditingManuallyPickedWord() {
    // related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/585
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");

    Assert.assertEquals("", inputConnection.getLastCommitCorrection());
    mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
    // at this point, the candidates view will show a hint
    Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
    // now, navigating to to the 'e'
    mAnySoftKeyboardUnderTest.setSelectedText(2, 2, true);
    Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
    Assert.assertEquals(2, inputConnection.getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hl ", inputConnection.getCurrentTextInInputConnection());
    Assert.assertEquals(1, inputConnection.getCurrentStartPosition());
  }

  @Test
  public void testBackSpaceCorrectlyAfterEnter() {
    // related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/920
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");
    Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
    Assert.assertEquals("", inputConnection.getLastCommitCorrection());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    // note: there shouldn't be any correction possible here.
    Assert.assertEquals("", inputConnection.getLastCommitCorrection());
    Assert.assertEquals("hel\n", inputConnection.getCurrentTextInInputConnection());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
    Assert.assertEquals(3, inputConnection.getCurrentStartPosition());
  }

  @Test
  public void testBackSpaceCorrectlyAfterAutoSpaceAndEnter() {
    // related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/920
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");
    Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
    mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hello");
    Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());

    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    Assert.assertEquals("hello hel", inputConnection.getCurrentTextInInputConnection());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    // note: there shouldn't be any correction possible here.
    Assert.assertEquals("", inputConnection.getLastCommitCorrection());
    Assert.assertEquals("hello hel\n", inputConnection.getCurrentTextInInputConnection());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hello hel", inputConnection.getCurrentTextInInputConnection());
    Assert.assertEquals(9, inputConnection.getCurrentStartPosition());
  }

  @Test
  public void testBackSpaceCorrectlyAfterAutoSpaceAndEnterWithDelayedUpdates() throws Exception {
    // related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/920
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    inputConnection.setUpdateSelectionDelay(10000000L);

    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");
    Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
    mAnySoftKeyboardUnderTest.pickSuggestionManually(2, "hello");
    Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());

    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    Assert.assertEquals("hello hel", inputConnection.getCurrentTextInInputConnection());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    // note: there shouldn't be any correction possible here.
    Assert.assertEquals("", inputConnection.getLastCommitCorrection());
    Assert.assertEquals("hello hel\n", inputConnection.getCurrentTextInInputConnection());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, false);
    Assert.assertEquals("hello hel", inputConnection.getCurrentTextInInputConnection());

    TestRxSchedulers.foregroundFlushAllJobs();
    Assert.assertEquals("hello hel", inputConnection.getCurrentTextInInputConnection());
    Assert.assertEquals(9, inputConnection.getCurrentStartPosition());
  }

  @Test
  public void testBackSpaceCorrectlyWhenEditingAutoCorrectedWord() {
    // related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/585
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");

    Assert.assertEquals("", inputConnection.getLastCommitCorrection());
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());
    // now, navigating to to the 'e'
    mAnySoftKeyboardUnderTest.setSelectedText(2, 2, true);
    Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());
    Assert.assertEquals(2, inputConnection.getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("h'll ", inputConnection.getCurrentTextInInputConnection());
    Assert.assertEquals(1, inputConnection.getCurrentStartPosition());
  }

  @Test
  public void testBackSpaceAfterAutoPickingAutoSpaceAndEnter() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");

    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');

    Assert.assertEquals("he'll ", inputConnection.getCurrentTextInInputConnection());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

    Assert.assertEquals("he'll\n", inputConnection.getCurrentTextInInputConnection());
    Assert.assertEquals(6, inputConnection.getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("he'll", inputConnection.getCurrentTextInInputConnection());
    Assert.assertEquals(5, inputConnection.getCurrentStartPosition());
  }

  @Test
  public void testBackSpaceAfterAutoPickingAndEnterWithoutAutoSpace() {
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);

    simulateFinishInputFlow();
    simulateOnStartInputFlow(
        false, TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, 0));

    verifySuggestions(true);
    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');

    Assert.assertEquals("hel ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

    Assert.assertEquals("hel\n", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    Assert.assertEquals(
        4, mAnySoftKeyboardUnderTest.getTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hel", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    Assert.assertEquals(
        3, mAnySoftKeyboardUnderTest.getTestInputConnection().getCurrentStartPosition());
  }

  @Test
  public void testBackSpaceAfterAutoPickingWithoutAutoSpace() {
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);

    simulateFinishInputFlow();
    simulateOnStartInputFlow();

    mAnySoftKeyboardUnderTest.simulateTextTyping("hell hell");

    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');

    Assert.assertEquals("hell hell ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    Assert.assertEquals(
        "hell hell ".length(),
        mAnySoftKeyboardUnderTest.getTestInputConnection().getCurrentStartPosition());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

    Assert.assertEquals("hell hell", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    Assert.assertEquals(
        "hell hell".length(),
        mAnySoftKeyboardUnderTest.getTestInputConnection().getCurrentStartPosition());

    mAnySoftKeyboardUnderTest.simulateKeyPress('l');
    Assert.assertEquals("hell helll", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    Assert.assertEquals(
        "hell helll".length(),
        mAnySoftKeyboardUnderTest.getTestInputConnection().getCurrentStartPosition());
  }

  @Test
  public void testBackSpaceAfterManualPickingAutoSpaceAndEnter() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

    verifyNoSuggestionsInteractions();
    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");

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

    simulateFinishInputFlow();
    simulateOnStartInputFlow();

    verifySuggestions(true);
    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");

    mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "hell");

    Assert.assertEquals("hell", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);

    Assert.assertEquals("hell\n", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    Assert.assertEquals(
        5, mAnySoftKeyboardUnderTest.getTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hell", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    Assert.assertEquals(
        4, mAnySoftKeyboardUnderTest.getTestInputConnection().getCurrentStartPosition());
  }

  @Test
  public void testManualPickWordLongerWordAndBackspaceAndTypeCharacter() {
    TestInputConnection inputConnection =
        (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifySuggestions(true, "hel", "he'll", "hello", "hell");
    mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "hell");
    Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
    // backspace
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hell", inputConnection.getCurrentTextInInputConnection());
    // some random character now
    mAnySoftKeyboardUnderTest.simulateKeyPress('k');
    Assert.assertEquals("hellk", inputConnection.getCurrentTextInInputConnection());
  }

  @Test
  public void testDoesNotSuggestInPasswordField() {
    simulateFinishInputFlow();

    EditorInfo editorInfo =
        TestableAnySoftKeyboard.createEditorInfo(
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);

    simulateOnStartInputFlow(false, editorInfo);

    mAnySoftKeyboardUnderTest.resetMockCandidateView();

    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifyNoSuggestionsInteractions();
    Assert.assertEquals("hel", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    verifySuggestions(true);
    Assert.assertEquals("hel ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

    mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
    verifyNoSuggestionsInteractions();
    Assert.assertEquals("hel hel", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
  }

  @Test
  public void testForwardDelete() {
    mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
    Assert.assertEquals("hello", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(5, getCurrentTestInputConnection().getCurrentStartPosition());

    mAnySoftKeyboardUnderTest.setSelectedText(2, 2, true);
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.FORWARD_DELETE);
    Assert.assertEquals("helo", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.FORWARD_DELETE);
    Assert.assertEquals("heo", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.FORWARD_DELETE);
    Assert.assertEquals("he", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

    verifySuggestions(true, "he", "hell", "hello", "he'll");

    // should not do anything
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.FORWARD_DELETE);
    Assert.assertEquals("he", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
  }

  @Test
  public void testForwardDeleteAcrossWords() {
    mAnySoftKeyboardUnderTest.simulateTextTyping("hello you all");

    mAnySoftKeyboardUnderTest.setSelectedText(2, 2, true);
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.FORWARD_DELETE);
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.FORWARD_DELETE);
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.FORWARD_DELETE);
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.FORWARD_DELETE);
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.FORWARD_DELETE);
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.FORWARD_DELETE);

    Assert.assertEquals(
        "heu all", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
  }

  @Test
  public void testTypeWordFixInnerMoveToEndAndDelete() {
    mAnySoftKeyboardUnderTest.simulateTextTyping("hllo");
    Assert.assertEquals("hllo", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    mAnySoftKeyboardUnderTest.setSelectedText(1, 1, true);
    Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress('e');
    Assert.assertEquals("hello", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

    mAnySoftKeyboardUnderTest.setSelectedText(5, 5, true);
    Assert.assertEquals("hello", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(5, getCurrentTestInputConnection().getCurrentStartPosition());

    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hell", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(4, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("hel", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(3, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("he", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("h", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(0, getCurrentTestInputConnection().getCurrentStartPosition());

    mAnySoftKeyboardUnderTest.simulateKeyPress('d');
    Assert.assertEquals("d", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
  }

  @Test
  public void testJumpToMiddleAndThenBackToEnd() {
    mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
    Assert.assertEquals("hello", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    mAnySoftKeyboardUnderTest.setSelectedText(1, 1, true);
    Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.setSelectedText(5, 5, true);
    Assert.assertEquals(5, getCurrentTestInputConnection().getCurrentStartPosition());
    Assert.assertEquals("hello", getCurrentTestInputConnection().getCurrentTextInInputConnection());

    mAnySoftKeyboardUnderTest.simulateKeyPress('d');
    Assert.assertEquals(
        "hellod", getCurrentTestInputConnection().getCurrentTextInInputConnection());
    Assert.assertEquals(6, getCurrentTestInputConnection().getCurrentStartPosition());
  }
}
