package com.anysoftkeyboard.ime;

import static com.anysoftkeyboard.TestableAnySoftKeyboard.createEditorInfo;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
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
        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.mWord.cursorPosition());
        Assert.assertEquals(
                "hell yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        verifySuggestions(true, "hell", "hell", "hello");
        Assert.assertEquals("hell", mAnySoftKeyboardUnderTest.mWord.getTypedWord().toString());
        Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.mWord.cursorPosition());
        Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        Assert.assertEquals(
                "hll yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals("hll", mAnySoftKeyboardUnderTest.mWord.getTypedWord().toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress('e');
        Assert.assertEquals(
                "hell yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.mWord.cursorPosition());
        Assert.assertEquals("hell", mAnySoftKeyboardUnderTest.mWord.getTypedWord().toString());
    }

    @Test
    public void testSuggestionsRestartWhenBackSpace() {
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_allow_suggestions_restart, true);
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hell ");
        Assert.assertEquals(
                "hell ", getCurrentTestInputConnection().getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.resetMockCandidateView();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verifySuggestions(true, "hell", "hell", "hello");
        Assert.assertEquals(
                "hell", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(4, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals(4, mAnySoftKeyboardUnderTest.mWord.cursorPosition());
        Assert.assertEquals("hell", mAnySoftKeyboardUnderTest.mWord.getTypedWord().toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verifySuggestions(true, "hel", "hell", "hello");
        Assert.assertEquals(
                "hel", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        Assert.assertEquals(3, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals("hel", mAnySoftKeyboardUnderTest.mWord.getTypedWord().toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress('l');
        Assert.assertEquals(
                "hell", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        verifySuggestions(true, "hell", "hell", "hello");
        Assert.assertEquals(4, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals("hell", mAnySoftKeyboardUnderTest.mWord.getTypedWord().toString());
    }

    @Test
    public void testSuggestionsRestartHappyPathWhenDisabled() {
        simulateFinishInputFlow();
        Assert.assertFalse(
                "Default is FALSE",
                SharedPrefsHelper.getPrefValue(
                        R.string.settings_key_allow_suggestions_restart, false));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hell yes");
        Assert.assertEquals(
                "hell yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.resetMockCandidateView();

        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(2, 2);
        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verifySuggestions(true);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.mWord.getTypedWord().toString());
        Assert.assertEquals(0, mAnySoftKeyboardUnderTest.mWord.cursorPosition());
        Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());

        mAnySoftKeyboardUnderTest.simulateKeyPress('r');
        Assert.assertEquals(
                "herll yes", getCurrentTestInputConnection().getCurrentTextInInputConnection());
        verifyNoSuggestionsInteractions();
        Assert.assertEquals(3, getCurrentTestInputConnection().getCurrentStartPosition());
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.mWord.getTypedWord().toString());
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
}
