package com.anysoftkeyboard;

import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.menny.android.anysoftkeyboard.AskGradleTestRunner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.util.ServiceController;

@RunWith(AskGradleTestRunner.class)
public class AnySoftKeyboardDictionarySaveWordsTest {

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
    public void testAsksToAddToDictionaryWhenTouchingTypedUnknownWordAndAdds() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        //at this point, the candidates view will show a hint
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView()).showAddToDictionaryHint("hel");
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.never()).notifyAboutWordAdded(Mockito.anyString());
        mAnySoftKeyboardUnderTest.addWordToDictionary("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).addWordToUserDictionary("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView()).notifyAboutWordAdded("hel");

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).removeWordFromUserDictionary(Mockito.anyString());
        mAnySoftKeyboardUnderTest.removeFromUserDictionary("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).removeWordFromUserDictionary("hel");
    }

    @Test
    public void testAsksToAddToDictionaryWhenTouchingTypedUnknownWordAndDoesNotAddIfContinuingTyping() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        //at this point, the candidates view will show a hint
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView()).showAddToDictionaryHint("hel");
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.never()).notifyAboutWordAdded(Mockito.anyString());

        Mockito.reset(mSpiedCandidateView);
        mAnySoftKeyboardUnderTest.simulateTextTyping("he");

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.never()).notifyAboutWordAdded(Mockito.anyString());

        Mockito.verify(mSpiedCandidateView, Mockito.times(2/*once for 'h', and the other time for 'e'*/)).setSuggestions(Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean());
    }

    @Test
    public void testAutoAddUnknownWordIfTypedFrequently() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        //first time
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.times(1)).showAddToDictionaryHint("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.never()).notifyAboutWordAdded(Mockito.anyString());
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());

        //second time
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.times(2)).showAddToDictionaryHint("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.never()).notifyAboutWordAdded(Mockito.anyString());
        Assert.assertEquals("hel hel ", inputConnection.getCurrentTextInInputConnection());

        //third time will auto-add
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.times(2/*still 2 times*/)).showAddToDictionaryHint("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).addWordToUserDictionary("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView()).notifyAboutWordAdded("hel");
        Assert.assertEquals("hel hel hel ", inputConnection.getCurrentTextInInputConnection());
    }
}