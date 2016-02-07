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
        mAnySoftKeyboardUnderTest.onCreateInputView();
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