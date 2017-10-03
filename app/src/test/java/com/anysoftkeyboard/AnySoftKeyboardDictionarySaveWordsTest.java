package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnySoftKeyboardDictionarySaveWordsTest extends AnySoftKeyboardBaseTest {

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
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isAddToDictionaryHintShown());
        mAnySoftKeyboardUnderTest.addWordToDictionary("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).addWordToUserDictionary("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView()).notifyAboutWordAdded("hel");
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAddToDictionaryHintShown());

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).removeWordFromUserDictionary(Mockito.anyString());
        mAnySoftKeyboardUnderTest.removeFromUserDictionary("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).removeWordFromUserDictionary("hel");
    }

    @Test
    public void testAddToDictionaryHintDismissedWhenBackspace() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        //at this point, the candidates view will show a hint
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView()).showAddToDictionaryHint("hel");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isAddToDictionaryHintShown());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAddToDictionaryHintShown());
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

        Mockito.verify(mSpiedCandidateView, Mockito.times(2/*once for 'h', and the other time for 'e'*/)).setSuggestions(Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyBoolean());
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

    @Test
    public void testAutoAddUnknownWordIfAutoPickedAfterUndoCommit() {
        //related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/580
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        //first time
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.never()).notifyAboutWordAdded(Mockito.anyString());
        Assert.assertEquals("hell ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hel", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.never()).notifyAboutWordAdded(Mockito.anyString());

        //second time
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.never()).notifyAboutWordAdded(Mockito.anyString());
        Assert.assertEquals("hel hell ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hel hel", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hel hel ", inputConnection.getCurrentTextInInputConnection());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.never()).notifyAboutWordAdded(Mockito.anyString());

        //third time will auto-add
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView(), Mockito.never()).notifyAboutWordAdded(Mockito.anyString());
        Assert.assertEquals("hel hel hell ", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hel hel hel", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        Assert.assertEquals("hel hel hel ", inputConnection.getCurrentTextInInputConnection());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).addWordToUserDictionary("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView()).notifyAboutWordAdded("hel");
    }
}