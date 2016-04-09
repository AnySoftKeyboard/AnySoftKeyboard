package com.anysoftkeyboard;

import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.menny.android.anysoftkeyboard.AskGradleTestRunner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.util.ServiceController;

import java.util.List;

@RunWith(AskGradleTestRunner.class)
public class AnySoftKeyboardDictionaryGetWordsTest {

    private TestableAnySoftKeyboard mAnySoftKeyboardUnderTest;

    private CandidateView mSpiedCandidateView;

    @Before
    public void setUp() throws Exception {
        ServiceController<TestableAnySoftKeyboard> anySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = anySoftKeyboardController.attach().create().get();

        final TestableAnySoftKeyboard.TestableSuggest spiedSuggest = (TestableAnySoftKeyboard.TestableSuggest) mAnySoftKeyboardUnderTest.getSpiedSuggest();

        Assert.assertNotNull(spiedSuggest);
        Assert.assertNotNull(spiedSuggest.getDictionaryFactory());

        spiedSuggest.setSuggestionsForWord("he", "he'll", "hell", "hello");
        spiedSuggest.setSuggestionsForWord("hel", "hell", "hello");
        spiedSuggest.setSuggestionsForWord("hell", "hell", "hello");

        Mockito.reset(spiedSuggest);

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.setInputView(mAnySoftKeyboardUnderTest.onCreateInputView());
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Robolectric.flushBackgroundThreadScheduler();

        mAnySoftKeyboardUnderTest.setCandidatesView(mAnySoftKeyboardUnderTest.onCreateCandidatesView());

        Robolectric.flushBackgroundThreadScheduler();

        mSpiedCandidateView = mAnySoftKeyboardUnderTest.getMockCandidateView();
        Assert.assertNotNull(mSpiedCandidateView);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAskForSuggestions() {
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(mSpiedCandidateView, true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(mSpiedCandidateView, true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");
    }

    @Test
    public void testAskForSuggestionsWithoutInputConnectionUpdates() {
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        verifySuggestions(mSpiedCandidateView, true, "h");
        mAnySoftKeyboardUnderTest.simulateKeyPress('e');
        verifySuggestions(mSpiedCandidateView, true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress('l');
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");
    }

    @Test
    public void testAskForSuggestionsWithDelayedInputConnectionUpdates() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        inputConnection.setSendUpdates(false);
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        verifySuggestions(mSpiedCandidateView, true, "h");
        mAnySoftKeyboardUnderTest.simulateKeyPress('e');
        verifySuggestions(mSpiedCandidateView, true, "he", "he'll", "hell", "hello");
        //sending a delayed event from the input-connection.
        //this can happen when the user is clicking fast (in ASK thread), but the other side (the app thread)
        //is too slow, or busy with something to send out events.
        inputConnection.sendUpdateNow();

        mAnySoftKeyboardUnderTest.simulateKeyPress('l');
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");
    }

    @Test
    public void testAskForSuggestionsWhenCursorInsideWord() {
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(mSpiedCandidateView, true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(mSpiedCandidateView, true, "hl");
        //moving one character back, and fixing the word to 'hel'
        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(1, 1);
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");
    }

    @Test
    public void testAutoPickWordWhenCursorAtTheEndOfTheWord() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(mSpiedCandidateView, true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(mSpiedCandidateView, true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

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
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(mSpiedCandidateView, true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(mSpiedCandidateView, true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

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
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(mSpiedCandidateView, true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(mSpiedCandidateView, true, "hl");
        //moving one character back, and fixing the word to 'hel'
        inputConnection.setSelection(1, 1);
        mAnySoftKeyboardUnderTest.simulateKeyPress('e');
        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(2, 2);
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

        Mockito.reset(inputConnection);//clearing any previous interactions with finishComposingText
        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        //this time, it will not auto-pick since the cursor is inside the word (and not at the end)
        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        //will stop composing in the input-connection
        Mockito.verify(inputConnection).finishComposingText();
        //also, it will abort suggestions
        verifySuggestions(mSpiedCandidateView, true);
    }

    @Test
    public void testBackSpaceCorrectlyWhenEditingManuallyPickedWord() {
        //related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/585
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        //at this point, the candidates view will show a hint
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
        //now, navigating to to the 'e'
        inputConnection.setSelection(2, 2);
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(2, inputConnection.getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, true);
        Assert.assertEquals("hl ", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(1, inputConnection.getCurrentStartPosition());
    }

    @Test
    public void testBackSpaceCorrectlyWhenEditingAutoCorrectedWord() {
        //related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/585
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

        Assert.assertEquals("", inputConnection.getLastCommitCorrection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        //now, navigating to to the 'e'
        inputConnection.setSelection(2, 2);
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(2, inputConnection.getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, true);
        Assert.assertEquals("hll ", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(1, inputConnection.getCurrentStartPosition());
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

    private void verifyNoSuggestionsInteractions(CandidateView candidateView) {
        Mockito.verify(candidateView, Mockito.never()).setSuggestions(Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean());
    }

    private void verifySuggestions(CandidateView candidateView, boolean resetCandidateView, CharSequence... expectedSuggestions) {
        ArgumentCaptor<List> suggestionsCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(candidateView, Mockito.atLeastOnce()).setSuggestions(suggestionsCaptor.capture(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean());
        List<List> allValues = suggestionsCaptor.getAllValues();
        List actualSuggestions = allValues.get(allValues.size()-1);
        if (expectedSuggestions.length == 0) {
            Assert.assertTrue(actualSuggestions == null || actualSuggestions.size() == 0);
        } else {
            Assert.assertEquals(expectedSuggestions.length, actualSuggestions.size());
            for (int expectedSuggestionIndex = 0; expectedSuggestionIndex < expectedSuggestions.length; expectedSuggestionIndex++) {
                String expectedSuggestion = expectedSuggestions[expectedSuggestionIndex].toString();
                Assert.assertEquals(expectedSuggestion, actualSuggestions.get(expectedSuggestionIndex).toString());
            }
        }

        if (resetCandidateView) mAnySoftKeyboardUnderTest.resetMockCandidateView();
    }
}