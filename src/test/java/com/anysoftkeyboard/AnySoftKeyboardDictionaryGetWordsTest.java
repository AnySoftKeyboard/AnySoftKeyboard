package com.anysoftkeyboard;

import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.dictionaries.DictionaryFactory;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.nextword.NextWordDictionary;
import com.menny.android.anysoftkeyboard.AskGradleTestRunner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ServiceController;

import java.util.List;

@RunWith(AskGradleTestRunner.class)
public class AnySoftKeyboardDictionaryGetWordsTest {

    private static final String[] DICTIONARY_WORDS = new String[]{
            "high", "hello", "menny", "AnySoftKeyboard", "keyboard", "google", "low"
    };

    private static final String[] DICTIONARY_NEXT_WORDS = new String[]{
            "hello", "is", "it", "me", "you", "looking", "for"
    };
    private ServiceController<TestableAnySoftKeyboard> mAnySoftKeyboardController;
    private TestableAnySoftKeyboard mAnySoftKeyboardUnderTest;

    private DictionaryFactory mSpiedDictionaryFactory;
    private CandidateView mSpiedCandidateView;

    @Before
    public void setUp() throws Exception {
        mAnySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = mAnySoftKeyboardController.attach().create().get();

        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        mSpiedDictionaryFactory = mAnySoftKeyboardUnderTest.getSpiedSuggest().getDictionaryFactory();
        Assert.assertNotNull(mSpiedDictionaryFactory);
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        //loading some user-dictionary words
        UserDictionary userDictionary = new UserDictionary(RuntimeEnvironment.application, "en");
        userDictionary.loadDictionary();
        for (int wordIndex = 0; wordIndex < DICTIONARY_WORDS.length; wordIndex++) {
            userDictionary.addWord(DICTIONARY_WORDS[wordIndex], DICTIONARY_WORDS.length - wordIndex);
        }
        userDictionary.close();

        //loading some next-word-dictionary words
        NextWordDictionary nextWordDictionary = new NextWordDictionary(RuntimeEnvironment.application, "en");
        nextWordDictionary.load();
        nextWordDictionary.clearData();
        for (String nextWord : DICTIONARY_NEXT_WORDS) {
            nextWordDictionary.getNextWords(nextWord, 1, 1);
        }
        nextWordDictionary.close();


        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Robolectric.flushBackgroundThreadScheduler();

        mAnySoftKeyboardUnderTest.onCreateCandidatesView();

        Robolectric.flushBackgroundThreadScheduler();

        mSpiedCandidateView = mAnySoftKeyboardUnderTest.getMockCandidateView();
        Assert.assertNotNull(mSpiedCandidateView);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @Ignore
    public void testAskForSuggestions() {
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("h");
        verifyNoSuggestionsInteractions(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("e");
        verifySuggestions(mSpiedCandidateView, true, "hello");
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
                CharSequence expectedSuggestion = expectedSuggestions[expectedSuggestionIndex];
                Assert.assertEquals(expectedSuggestion, actualSuggestions.get(expectedSuggestionIndex));
            }
        }

        if (resetCandidateView) Mockito.reset(candidateView);
    }
}