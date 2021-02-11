package com.anysoftkeyboard.ime;

import static com.anysoftkeyboard.TestableAnySoftKeyboard.createEditorInfo;
import static com.anysoftkeyboard.ime.KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS;

import android.os.SystemClock;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardSuggestionsTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testStripActionLifeCycle() {
        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));

        simulateFinishInputFlow();

        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));
    }

    @Test
    public void testStripActionRemovedWhenAbortingPrediction() {
        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));

        mAnySoftKeyboardUnderTest.abortCorrectionAndResetPredictionState(true);

        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));
    }

    @Test
    public void testStripActionNotRemovedWhenAbortingPredictionNotForever() {
        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));

        mAnySoftKeyboardUnderTest.abortCorrectionAndResetPredictionState(false);

        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));
    }

    @Test
    public void testStripActionNotAddedWhenInNonPredictiveField() {
        simulateFinishInputFlow();

        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));

        final EditorInfo editorInfo =
                createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        simulateOnStartInputFlow(false, editorInfo);

        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));
    }

    @Test
    public void testStripActionNotAddedWhenInSuggestionsDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_show_suggestions, false);
        simulateFinishInputFlow();
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));

        simulateOnStartInputFlow();

        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));
    }

    @Test
    public void testClickingCancelPredicationHappyPath() {
        final KeyboardViewContainerView.StripActionProvider provider =
                ((AnySoftKeyboardSuggestions) mAnySoftKeyboardUnderTest).mCancelSuggestionsAction;
        View rootActionView =
                provider.inflateActionView(mAnySoftKeyboardUnderTest.getInputViewContainer());

        final View image = rootActionView.findViewById(R.id.close_suggestions_strip_icon);
        final View text = rootActionView.findViewById(R.id.close_suggestions_strip_text);

        Assert.assertEquals(View.VISIBLE, image.getVisibility());
        Assert.assertEquals(View.GONE, text.getVisibility());

        rootActionView.performClick();

        // should be shown for some time
        Assert.assertEquals(View.VISIBLE, text.getVisibility());
        // strip is not removed
        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertEquals(View.GONE, text.getVisibility());

        rootActionView.performClick();
        Assert.assertEquals(View.VISIBLE, text.getVisibility());
        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));

        // removing
        rootActionView.performClick();
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.close_suggestions_strip_text));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
    }

    @Test
    public void testSuggestionsRestartWhenMovingCursor() {
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_allow_suggestions_restart, true);
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hell yes");
        Assert.assertEquals(
                "hell yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.resetMockCandidateView();
        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(2, 2);
        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.getCurrentComposedWord().cursorPosition());
        Assert.assertEquals(
                "hell yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        verifySuggestions(true, "hell", "hell", "hello");
        Assert.assertEquals(
                "hell",
                mAnySoftKeyboardUnderTest.getCurrentComposedWord().getTypedWord().toString());
        Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.getCurrentComposedWord().cursorPosition());
        Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertEquals(
                "hll yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals(
                "hll",
                mAnySoftKeyboardUnderTest.getCurrentComposedWord().getTypedWord().toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress('e');
        Assert.assertEquals(
                "hell yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.getCurrentComposedWord().cursorPosition());
        Assert.assertEquals(
                "hell",
                mAnySoftKeyboardUnderTest.getCurrentComposedWord().getTypedWord().toString());
    }

    @Test
    public void testDoesNotPostRestartOnBackspaceWhilePredicting() {
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_allow_suggestions_restart, true);
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hel");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
        Assert.assertFalse(
                ((AnySoftKeyboardSuggestions) mAnySoftKeyboardUnderTest)
                        .mKeyboardHandler.hasMessages(MSG_RESTART_NEW_WORD_SUGGESTIONS));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, false);
        Assert.assertFalse(
                ((AnySoftKeyboardSuggestions) mAnySoftKeyboardUnderTest)
                        .mKeyboardHandler.hasMessages(MSG_RESTART_NEW_WORD_SUGGESTIONS));
        SystemClock.sleep(5);
        Assert.assertFalse(
                ((AnySoftKeyboardSuggestions) mAnySoftKeyboardUnderTest)
                        .mKeyboardHandler.hasMessages(MSG_RESTART_NEW_WORD_SUGGESTIONS));
    }

    @Test
    public void testDeletesCorrectlyIfPredictingButDelayedPositionUpdate() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("abcd efgh");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isCurrentlyPredicting());
        mAnySoftKeyboardUnderTest.setUpdateSelectionDelay(500);
        Assert.assertEquals("abcd efgh", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, false);
        Assert.assertEquals("abcd efg", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, true);
        Assert.assertEquals("abcd ef", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, false);
        Assert.assertEquals("abcd e", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, true);
        Assert.assertEquals("abcd ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, false);
        Assert.assertEquals("abcd", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, true);
        Assert.assertEquals("abc", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, false);
        Assert.assertEquals("ab", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, true);
        Assert.assertEquals("a", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, false);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        // extra
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, true);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testSuggestionsRestartWhenBackSpace() {
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_allow_suggestions_restart, true);
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hell face ");
        Assert.assertEquals(
                "hell face ", getCurrentTestInputConnection().getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.resetMockCandidateView();
        for (int deleteKeyPress = 6; deleteKeyPress > 0; deleteKeyPress--) {
            // really quickly
            mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE, false);
            SystemClock.sleep(5);
        }
        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verifySuggestions(true, "hell", "hell", "hello");
        Assert.assertEquals(
                "hell", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(4, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals(4, mAnySoftKeyboardUnderTest.getCurrentComposedWord().cursorPosition());
        Assert.assertEquals(
                "hell",
                mAnySoftKeyboardUnderTest.getCurrentComposedWord().getTypedWord().toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        verifySuggestions(true, "hel", "hell", "hello");
        Assert.assertEquals(
                "hel", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(3, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals(
                "hel",
                mAnySoftKeyboardUnderTest.getCurrentComposedWord().getTypedWord().toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress('l');
        Assert.assertEquals(
                "hell", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        verifySuggestions(true, "hell", "hell", "hello");
        Assert.assertEquals(4, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals(
                "hell",
                mAnySoftKeyboardUnderTest.getCurrentComposedWord().getTypedWord().toString());
    }

    @Test
    public void testSuggestionsRestartHappyPathWhenDisabled() {
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_allow_suggestions_restart, false);
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hell yes");
        Assert.assertEquals(
                "hell yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.resetMockCandidateView();

        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(2, 2);
        verifySuggestions(true);
        Assert.assertEquals(
                "", mAnySoftKeyboardUnderTest.getCurrentComposedWord().getTypedWord().toString());
        Assert.assertEquals(0, mAnySoftKeyboardUnderTest.getCurrentComposedWord().cursorPosition());
        Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

        mAnySoftKeyboardUnderTest.simulateKeyPress('r');
        Assert.assertEquals(
                "herll yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        verifySuggestions(true, "r");
        Assert.assertEquals(3, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals(
                "r", mAnySoftKeyboardUnderTest.getCurrentComposedWord().getTypedWord().toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress('d');
        Assert.assertEquals(
                "herdll yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        verifySuggestions(true, "rd");
        Assert.assertEquals(4, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals(
                "rd", mAnySoftKeyboardUnderTest.getCurrentComposedWord().getTypedWord().toString());
    }

    @Test
    public void testCorrectlyOutputCharactersWhenCongestedCursorUpdates() {
        Assert.assertEquals(0, getCurrentTestInputConnection().getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress('g');
        Assert.assertEquals("g", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress('o');
        Assert.assertEquals(
                "go", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

        getCurrentTestInputConnection().setCongested(true);
        mAnySoftKeyboardUnderTest.simulateKeyPress('i');
        Assert.assertEquals(
                "go", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('n');
        Assert.assertEquals(
                "go", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        getCurrentTestInputConnection().popCongestedAction();
        Assert.assertEquals(
                "goi", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        getCurrentTestInputConnection().popCongestedAction();
        Assert.assertEquals(
                "goin", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('g');
        getCurrentTestInputConnection().setCongested(false);
        Assert.assertEquals(
                "going", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(5, getCurrentTestInputConnection().getCurrentStartPosition());
    }

    @Test
    public void testCorrectlyOutputCharactersWhenVeryCongestedCursorUpdates() {
        Assert.assertEquals(0, getCurrentTestInputConnection().getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateTextTyping("go");
        Assert.assertEquals(
                "go", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

        getCurrentTestInputConnection().setCongested(true);
        mAnySoftKeyboardUnderTest.simulateTextTyping("ing to work");
        getCurrentTestInputConnection().popCongestedAction();
        mAnySoftKeyboardUnderTest.simulateTextTyping("ing");
        getCurrentTestInputConnection().setCongested(false);
        Assert.assertEquals(
                "going to working",
                getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(
                "going to working".length(),
                getCurrentTestInputConnection().getCurrentStartPosition());
    }

    @Test
    public void testCorrectlyOutputCharactersWhenExtremelyCongestedCursorUpdates() {
        Assert.assertEquals(0, getCurrentTestInputConnection().getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress('g');
        Assert.assertEquals("g", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress('o');
        Assert.assertEquals(
                "go", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

        getCurrentTestInputConnection().setCongested(true);
        mAnySoftKeyboardUnderTest.simulateKeyPress('i');
        Assert.assertEquals(
                "go", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('n');
        Assert.assertEquals(
                "go", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        getCurrentTestInputConnection().popCongestedAction();
        Assert.assertEquals(
                "goi", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('g');
        getCurrentTestInputConnection().setCongested(false);
        Assert.assertEquals(
                "going", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(5, getCurrentTestInputConnection().getCurrentStartPosition());
    }

    @Test
    public void testCorrectlyOutputCharactersWhenDelayedCursorUpdates() {
        Assert.assertEquals(0, getCurrentTestInputConnection().getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress('g');
        Assert.assertEquals("g", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());

        getCurrentTestInputConnection().setSendUpdates(false);
        mAnySoftKeyboardUnderTest.simulateKeyPress('o');
        Assert.assertEquals(
                "go", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress('i');
        Assert.assertEquals(
                "goi", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(3, getCurrentTestInputConnection().getCurrentStartPosition());

        getCurrentTestInputConnection().setSendUpdates(true);
        mAnySoftKeyboardUnderTest.simulateKeyPress('n');
        Assert.assertEquals(
                "goin", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(4, getCurrentTestInputConnection().getCurrentStartPosition());
        mAnySoftKeyboardUnderTest.simulateKeyPress('g');
        Assert.assertEquals(
                "going", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(5, getCurrentTestInputConnection().getCurrentStartPosition());

        getCurrentTestInputConnection().setSendUpdates(false);
        mAnySoftKeyboardUnderTest.simulateKeyPress('g');
        Assert.assertEquals(
                "goingg", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(6, getCurrentTestInputConnection().getCurrentStartPosition());
    }

    private void testDelayedOnSelectionUpdate(long delay) {
        final String testText =
                "typing 1 2 3 working hel kjasldkjalskdjasd hel fac ksdjflksd smile fac fac hel hel aklsjdas gggggg hello fac hel face hel";
        final String expectedText =
                "typing 1 2 3 working hell kjasldkjalskdjasd hell face ksdjflksd smile face face hell hell aklsjdas gggggg hello face hell face hel";
        mAnySoftKeyboardUnderTest.setUpdateSelectionDelay(delay + 1);
        mAnySoftKeyboardUnderTest.simulateTextTyping(testText);
        Robolectric.flushForegroundThreadScheduler();
        // the first two hel are corrected
        Assert.assertEquals(
                expectedText, getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(
                expectedText.length(), getCurrentTestInputConnection().getCurrentStartPosition());
    }

    @Test
    public void testSmallDelayedOnSelectionUpdate() {
        testDelayedOnSelectionUpdate(TestableAnySoftKeyboard.DELAY_BETWEEN_TYPING);
    }

    @Test
    public void testAnnoyingDelayedOnSelectionUpdate() {
        testDelayedOnSelectionUpdate(TestableAnySoftKeyboard.DELAY_BETWEEN_TYPING * 3);
    }

    @Test
    public void testCrazyDelayedOnSelectionUpdate() {
        testDelayedOnSelectionUpdate(TestableAnySoftKeyboard.DELAY_BETWEEN_TYPING * 6);
    }

    @Test
    public void testOverExpectedDelayedOnSelectionUpdate() {
        testDelayedOnSelectionUpdate(TestableAnySoftKeyboard.MAX_TIME_TO_EXPECT_SELECTION_UPDATE);
    }

    @Test
    public void testWayOverExpectedDelayedOnSelectionUpdate() {
        testDelayedOnSelectionUpdate(
                TestableAnySoftKeyboard.MAX_TIME_TO_EXPECT_SELECTION_UPDATE * 2);
    }
}
