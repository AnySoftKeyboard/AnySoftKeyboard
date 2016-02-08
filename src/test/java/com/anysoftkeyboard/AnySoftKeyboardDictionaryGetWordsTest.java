package com.anysoftkeyboard;

import android.view.inputmethod.EditorInfo;

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
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');
        verifySuggestions(mSpiedCandidateView, true, "h");
        mAnySoftKeyboardUnderTest.simulateKeyPress('e');
        verifySuggestions(mSpiedCandidateView, true, "he", "he'll", "hell", "hello");
        //sending a delayed event from the input-connection.
        //this can happen when the user is clicking fast (in ASK thread), but the other side (the app thread)
        //is too slow, or busy with something to send out events.
        mAnySoftKeyboardUnderTest.updateInputConnection('h');

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
        TestableAnySoftKeyboard.TestInputConnection inputConnection = (TestableAnySoftKeyboard.TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifySuggestions(mSpiedCandidateView, true, "h");
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(mSpiedCandidateView, true, "he", "he'll", "hell", "hello");
        mAnySoftKeyboardUnderTest.simulateTextTyping("l");
        verifySuggestions(mSpiedCandidateView, true, "hel", "hell", "hello");

        Assert.assertEquals("", inputConnection.getLastCommitText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hell", inputConnection.getLastCommitText());
    }

    @Test
    public void testDoesNotAutoPickWordWhenCursorNotAtTheEndOfTheWord() {
        TestableAnySoftKeyboard.TestInputConnection inputConnection = (TestableAnySoftKeyboard.TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
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
        Assert.assertEquals("", inputConnection.getLastCommitText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        //this time, it will not auto-pick since the cursor is inside the word (and not at the end)
        Assert.assertEquals("", inputConnection.getLastCommitText());
        //will stop composing in the input-connection
        Mockito.verify(inputConnection).finishComposingText();
        //also, it will abort suggestions
        verifySuggestions(mSpiedCandidateView, true);
    }

    private void verifyNoSuggestionsInteractions(CandidateView candidateView) {
        Mockito.verify(candidateView, Mockito.never()).setSuggestions(Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean());
    }

    private void verifySuggestions(CandidateView candidateView, boolean resetCandidateView, CharSequence... expectedSuggestions) {
        ArgumentCaptor<List> suggestionsCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(candidateView).setSuggestions(suggestionsCaptor.capture(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean());
        List actualSuggestions = suggestionsCaptor.getValue();
        if (expectedSuggestions.length == 0) {
            Assert.assertTrue(actualSuggestions == null || actualSuggestions.size() == 0);
        } else {
            Assert.assertEquals(expectedSuggestions.length, actualSuggestions.size());
            for (int expectedSuggestionIndex = 0; expectedSuggestionIndex < expectedSuggestions.length; expectedSuggestionIndex++) {
                String expectedSuggestion = expectedSuggestions[expectedSuggestionIndex].toString();
                Assert.assertEquals(expectedSuggestion, actualSuggestions.get(expectedSuggestionIndex).toString());
            }
        }

        if (resetCandidateView) Mockito.reset(candidateView);
    }
}