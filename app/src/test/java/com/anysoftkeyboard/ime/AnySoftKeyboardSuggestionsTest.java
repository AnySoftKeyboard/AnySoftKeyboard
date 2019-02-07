package com.anysoftkeyboard.ime;

import static com.anysoftkeyboard.TestableAnySoftKeyboard.createEditorInfo;

import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
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
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));

        simulateFinishInputFlow();

        Assert.assertNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));
    }

    @Test
    public void testStripActionRemovedWhenAbortingPrediction() {
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));

        mAnySoftKeyboardUnderTest.abortCorrectionAndResetPredictionState(true);

        Assert.assertNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));
    }

    @Test
    public void testStripActionNotRemovedWhenAbortingPredictionNotForever() {
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));

        mAnySoftKeyboardUnderTest.abortCorrectionAndResetPredictionState(false);

        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));
    }

    @Test
    public void testStripActionNotAddedWhenInNonPredictiveField() {
        simulateFinishInputFlow();

        Assert.assertNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));

        final EditorInfo editorInfo = createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        simulateOnStartInputFlow(false, editorInfo);

        Assert.assertNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));
    }

    @Test
    public void testStripActionNotAddedWhenInSuggestionsDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_show_suggestions, false);
        simulateFinishInputFlow();
        Assert.assertNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));

        simulateOnStartInputFlow();

        Assert.assertNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));
    }

    @Test
    public void testClickingCancelPredicationHappyPath() {
        final KeyboardViewContainerView.StripActionProvider provider = ((AnySoftKeyboardSuggestions) mAnySoftKeyboardUnderTest).mCancelSuggestionsAction;
        View rootActionView = provider.inflateActionView(mAnySoftKeyboardUnderTest.getInputViewContainer());

        final View image = rootActionView.findViewById(R.id.close_suggestions_strip_icon);
        final View text = rootActionView.findViewById(R.id.close_suggestions_strip_text);

        Assert.assertEquals(View.VISIBLE, image.getVisibility());
        Assert.assertEquals(View.GONE, text.getVisibility());

        image.performClick();

        //should be shown for some time
        Assert.assertEquals(View.VISIBLE, text.getVisibility());
        //strip is not removed
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertEquals(View.GONE, text.getVisibility());

        image.performClick();
        Assert.assertEquals(View.VISIBLE, text.getVisibility());
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));

        //removing
        text.performClick();
        Assert.assertNull(mAnySoftKeyboardUnderTest.getInputViewContainer().findViewById(R.id.close_suggestions_strip_text));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
    }
}