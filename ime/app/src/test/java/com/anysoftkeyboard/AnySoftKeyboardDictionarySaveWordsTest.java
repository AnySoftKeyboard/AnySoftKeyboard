package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardDictionarySaveWordsTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testAsksToAddToDictionaryWhenTouchingTypedUnknownWordAndAdds() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        // at this point, the candidates view will show a hint
        Mockito.verify(getMockCandidateView()).showAddToDictionaryHint("hel");
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never())
                .addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(getMockCandidateView(), Mockito.never())
                .notifyAboutWordAdded(Mockito.anyString());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isAddToDictionaryHintShown());
        mAnySoftKeyboardUnderTest.addWordToDictionary("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).addWordToUserDictionary("hel");
        Mockito.verify(getMockCandidateView()).notifyAboutWordAdded("hel");
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAddToDictionaryHintShown());

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never())
                .removeWordFromUserDictionary(Mockito.anyString());
        mAnySoftKeyboardUnderTest.removeFromUserDictionary("hel");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest())
                .removeWordFromUserDictionary("hel");
    }

    @Test
    public void testAddToDictionaryHintDismissedWhenBackspace() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        // at this point, the candidates view will show a hint
        Mockito.verify(getMockCandidateView()).showAddToDictionaryHint("hel");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isAddToDictionaryHintShown());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAddToDictionaryHintShown());
    }

    @Test
    public void
            testAsksToAddToDictionaryWhenTouchingTypedUnknownWordAndDoesNotAddIfContinuingTyping() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
        // at this point, the candidates view will show a hint
        Mockito.verify(getMockCandidateView()).showAddToDictionaryHint("hel");
        Assert.assertEquals("hel ", inputConnection.getCurrentTextInInputConnection());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never())
                .addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(getMockCandidateView(), Mockito.never())
                .notifyAboutWordAdded(Mockito.anyString());

        Mockito.reset(getMockCandidateView());
        mAnySoftKeyboardUnderTest.simulateTextTyping("he");

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never())
                .addWordToUserDictionary(Mockito.anyString());
        Mockito.verify(getMockCandidateView(), Mockito.never())
                .notifyAboutWordAdded(Mockito.anyString());

        Mockito.verify(
                        getMockCandidateView(),
                        Mockito.times(2 /*once for 'h', and the other time for 'e'*/))
                .setSuggestions(Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyBoolean());
    }

    @Test
    public void testAutoAddUnknownWordIfPickedFrequently() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        StringBuilder expectedOutput = new StringBuilder();
        // it takes 3 picks to learn a new word
        for (int pickIndex = 0; pickIndex < 3; pickIndex++) {
            mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
            mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hel");
            expectedOutput.append("hel ");
            if (pickIndex != 2) {
                Mockito.verify(getMockCandidateView(), Mockito.times(1 + pickIndex))
                        .showAddToDictionaryHint("hel");
                Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never())
                        .addWordToUserDictionary(Mockito.anyString());
                Mockito.verify(getMockCandidateView(), Mockito.never())
                        .notifyAboutWordAdded(Mockito.anyString());
                Assert.assertEquals(
                        expectedOutput.toString(),
                        inputConnection.getCurrentTextInInputConnection());
            } else {
                // third time will auto-add
                Mockito.verify(getMockCandidateView(), Mockito.times(pickIndex /*still 2 times*/))
                        .showAddToDictionaryHint("hel");
                Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest())
                        .addWordToUserDictionary("hel");
                Mockito.verify(getMockCandidateView()).notifyAboutWordAdded("hel");
                Assert.assertEquals(
                        expectedOutput.toString(),
                        inputConnection.getCurrentTextInInputConnection());
            }
        }
    }

    @Test
    public void testAutoAddUnknownWordIfAutoPickedAfterUndoCommit() {
        // related to https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/580
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        StringBuilder expectedOutput = new StringBuilder();
        // it takes 5 tries to lean from typing
        for (int pickIndex = 0; pickIndex < 5; pickIndex++) {
            mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
            mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
            Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never())
                    .addWordToUserDictionary(Mockito.anyString());
            Mockito.verify(getMockCandidateView(), Mockito.never())
                    .notifyAboutWordAdded(Mockito.anyString());
            expectedOutput.append("hell ");
            Assert.assertEquals(
                    expectedOutput.toString(), inputConnection.getCurrentTextInInputConnection());
            mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
            expectedOutput.setLength(expectedOutput.length() - 2); // undo commit
            Assert.assertEquals(
                    expectedOutput.toString(), inputConnection.getCurrentTextInInputConnection());
            mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
            expectedOutput.append(" ");
            Assert.assertEquals(
                    expectedOutput.toString(), inputConnection.getCurrentTextInInputConnection());

            if (pickIndex != 4) {
                Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never())
                        .addWordToUserDictionary(Mockito.anyString());
                Mockito.verify(getMockCandidateView(), Mockito.never())
                        .notifyAboutWordAdded(Mockito.anyString());
            } else {
                Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest())
                        .addWordToUserDictionary("hel");
                Mockito.verify(getMockCandidateView()).notifyAboutWordAdded("hel");
            }
        }
    }
}
