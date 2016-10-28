package com.anysoftkeyboard;

import android.annotation.TargetApi;
import android.app.Service;
import android.os.Build;
import android.os.IBinder;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.menny.android.anysoftkeyboard.InputMethodManagerShadow;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.util.ServiceController;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public abstract class AnySoftKeyboardBaseTest {

    protected TestableAnySoftKeyboard mAnySoftKeyboardUnderTest;

    protected CandidateView mSpiedCandidateView;
    protected IBinder mMockBinder;

    private InputMethodManagerShadow mInputMethodManagerShadow;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Before
    public void setUpForAnySoftKeyboardBase() throws Exception {
        mInputMethodManagerShadow = (InputMethodManagerShadow) Shadows.shadowOf((InputMethodManager) RuntimeEnvironment.application.getSystemService(Service.INPUT_METHOD_SERVICE));
        mMockBinder = Mockito.mock(IBinder.class);

        ServiceController<TestableAnySoftKeyboard> anySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = anySoftKeyboardController.attach().create().get();

        final TestableAnySoftKeyboard.TestableSuggest spiedSuggest = (TestableAnySoftKeyboard.TestableSuggest) mAnySoftKeyboardUnderTest.getSpiedSuggest();

        Assert.assertNotNull(spiedSuggest);
        Assert.assertNotNull(spiedSuggest.getDictionaryFactory());

        spiedSuggest.setSuggestionsForWord("he", "he'll", "hell", "hello");
        spiedSuggest.setSuggestionsForWord("hel", "hell", "hello");
        spiedSuggest.setSuggestionsForWord("hell", "hell", "hello");
        spiedSuggest.setSuggestionsForWord("f", "face");
        spiedSuggest.setSuggestionsForWord("fa", "face");
        spiedSuggest.setSuggestionsForWord("fac", "face");
        spiedSuggest.setSuggestionsForWord("face", "face");

        Mockito.reset(spiedSuggest);

        final EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();

        mAnySoftKeyboardUnderTest.onCreateInputMethodInterface().attachToken(mMockBinder);

        mAnySoftKeyboardUnderTest.setInputView(mAnySoftKeyboardUnderTest.onCreateInputView());
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Robolectric.flushBackgroundThreadScheduler();

        mAnySoftKeyboardUnderTest.setCandidatesView(mAnySoftKeyboardUnderTest.onCreateCandidatesView());

        Robolectric.flushBackgroundThreadScheduler();

        mSpiedCandidateView = mAnySoftKeyboardUnderTest.getMockCandidateView();
        Assert.assertNotNull(mSpiedCandidateView);

        //simulating the first OS subtype reporting
        AnyKeyboard currentAlphabetKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        Assert.assertNotNull(currentAlphabetKeyboard);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(currentAlphabetKeyboard.getKeyboardPrefId())
                .setSubtypeLocale(currentAlphabetKeyboard.getLocale().toString())
                .build());
    }

    @After
    public void tearDownForAnySoftKeyboardBase() throws Exception {
    }

    protected final InputMethodManagerShadow getShadowInputMethodManager() {
        return mInputMethodManagerShadow;
    }

    protected EditorInfo createEditorInfoTextWithSuggestionsForSetUp() {
        return TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
    }

    protected final void verifyNoSuggestionsInteractions() {
        Mockito.verify(mSpiedCandidateView, Mockito.never()).setSuggestions(Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean());
    }

    protected final void verifySuggestions(boolean resetCandidateView, CharSequence... expectedSuggestions) {
        List actualSuggestions = verifyAndCaptureSuggestion(resetCandidateView);
        if (expectedSuggestions.length == 0) {
            Assert.assertTrue(actualSuggestions == null || actualSuggestions.size() == 0);
        } else {
            Assert.assertEquals(expectedSuggestions.length, actualSuggestions.size());
            for (int expectedSuggestionIndex = 0; expectedSuggestionIndex < expectedSuggestions.length; expectedSuggestionIndex++) {
                String expectedSuggestion = expectedSuggestions[expectedSuggestionIndex].toString();
                Assert.assertEquals(expectedSuggestion, actualSuggestions.get(expectedSuggestionIndex).toString());
            }
        }
    }

    protected List verifyAndCaptureSuggestion(boolean resetCandidateView) {
        ArgumentCaptor<List> suggestionsCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(mSpiedCandidateView, Mockito.atLeastOnce()).setSuggestions(suggestionsCaptor.capture(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyBoolean());
        List<List> allValues = suggestionsCaptor.getAllValues();


        if (resetCandidateView) mAnySoftKeyboardUnderTest.resetMockCandidateView();

        return allValues.get(allValues.size() - 1);
    }

    protected void simulateOnStartInputFlow() {
        simulateOnStartInputFlow(false, false, createEditorInfoTextWithSuggestionsForSetUp());
    }

    protected void simulateOnStartInputFlow(boolean restarting, boolean configChange, EditorInfo editorInfo) {
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, restarting);
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, configChange)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, restarting);
        }
    }

    protected void simulateFinishInputFlow(boolean restarting) {
        mAnySoftKeyboardUnderTest.onFinishInputView(restarting);
        mAnySoftKeyboardUnderTest.onFinishInput();
    }
}