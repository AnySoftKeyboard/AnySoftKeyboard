package com.anysoftkeyboard;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Service;
import android.inputmethodservice.AbstractInputMethodService;
import android.os.Build;
import android.os.IBinder;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.menny.android.anysoftkeyboard.InputMethodManagerShadow;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ServiceController;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AnySoftKeyboardBaseTest {

    protected TestableAnySoftKeyboard mAnySoftKeyboardUnderTest;

    protected IBinder mMockBinder;

    private InputMethodManagerShadow mInputMethodManagerShadow;
    protected ServiceController<TestableAnySoftKeyboard> mAnySoftKeyboardController;
    private AbstractInputMethodService.AbstractInputMethodImpl mAbstractInputMethod;

    protected TestInputConnection getCurrentTestInputConnection() {
        return mAnySoftKeyboardUnderTest.getTestInputConnection();
    }

    protected CandidateView getMockCandidateView() {
        return mAnySoftKeyboardUnderTest.getMockCandidateView();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Before
    public void setUpForAnySoftKeyboardBase() throws Exception {
        final Application application = getApplicationContext();

        mInputMethodManagerShadow =
                (InputMethodManagerShadow)
                        Shadows.shadowOf(
                                (InputMethodManager)
                                        application.getSystemService(Service.INPUT_METHOD_SERVICE));
        mMockBinder = Mockito.mock(IBinder.class);

        mAnySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = mAnySoftKeyboardController.create().get();
        mAbstractInputMethod = mAnySoftKeyboardUnderTest.onCreateInputMethodInterface();
        mAnySoftKeyboardUnderTest.onCreateInputMethodSessionInterface();

        final TestableAnySoftKeyboard.TestableSuggest spiedSuggest =
                (TestableAnySoftKeyboard.TestableSuggest)
                        mAnySoftKeyboardUnderTest.getSpiedSuggest();

        Assert.assertNotNull(spiedSuggest);

        spiedSuggest.setSuggestionsForWord("he", "he'll", "hell", "hello");
        spiedSuggest.setSuggestionsForWord("hel", "hell", "hello");
        spiedSuggest.setSuggestionsForWord("hell", "hell", "hello");
        spiedSuggest.setSuggestionsForWord("f", "face");
        spiedSuggest.setSuggestionsForWord("fa", "face");
        spiedSuggest.setSuggestionsForWord("fac", "face");
        spiedSuggest.setSuggestionsForWord("face", "face");

        Mockito.reset(spiedSuggest);

        final EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();

        mAbstractInputMethod.attachToken(mMockBinder);

        mAbstractInputMethod.showSoftInput(InputMethod.SHOW_EXPLICIT, null);
        mAbstractInputMethod.startInput(
                mAnySoftKeyboardUnderTest.getTestInputConnection(), editorInfo);
        mAnySoftKeyboardUnderTest.showWindow(true);

        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushBackgroundThreadScheduler();

        Assert.assertNotNull(getMockCandidateView());

        // simulating the first OS subtype reporting
        AnyKeyboard currentAlphabetKeyboard =
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        Assert.assertNotNull(currentAlphabetKeyboard);
        // reporting the first keyboard. This is required to simulate the selection of the first
        // keyboard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(
                    new InputMethodSubtype.InputMethodSubtypeBuilder()
                            .setSubtypeExtraValue(currentAlphabetKeyboard.getKeyboardId())
                            .setSubtypeLocale(currentAlphabetKeyboard.getLocale().toString())
                            .build());
        }

        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushBackgroundThreadScheduler();

        // verifying that ASK was set on the candidate-view
        Mockito.verify(mAnySoftKeyboardUnderTest.getMockCandidateView())
                .setService(Mockito.same(mAnySoftKeyboardUnderTest));

        verifySuggestions(true);
    }

    @After
    public void tearDownForAnySoftKeyboardBase() throws Exception {}

    protected final InputMethodManagerShadow getShadowInputMethodManager() {
        return mInputMethodManagerShadow;
    }

    protected EditorInfo createEditorInfoTextWithSuggestionsForSetUp() {
        return TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
    }

    protected final void verifyNoSuggestionsInteractions() {
        Mockito.verify(getMockCandidateView(), Mockito.never())
                .setSuggestions(Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyBoolean());
    }

    protected final void verifySuggestions(
            boolean resetCandidateView, CharSequence... expectedSuggestions) {
        List actualSuggestions = verifyAndCaptureSuggestion(resetCandidateView);
        Assert.assertEquals(
                "Actual suggestions are " + Arrays.toString(actualSuggestions.toArray()),
                expectedSuggestions.length,
                actualSuggestions.size());
        for (int expectedSuggestionIndex = 0;
                expectedSuggestionIndex < expectedSuggestions.length;
                expectedSuggestionIndex++) {
            String expectedSuggestion = expectedSuggestions[expectedSuggestionIndex].toString();
            Assert.assertEquals(
                    expectedSuggestion, actualSuggestions.get(expectedSuggestionIndex).toString());
        }
    }

    protected List verifyAndCaptureSuggestion(boolean resetCandidateView) {
        ArgumentCaptor<List> suggestionsCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(getMockCandidateView(), Mockito.atLeastOnce())
                .setSuggestions(
                        suggestionsCaptor.capture(), Mockito.anyBoolean(), Mockito.anyBoolean());
        List<List> allValues = suggestionsCaptor.getAllValues();

        if (resetCandidateView) mAnySoftKeyboardUnderTest.resetMockCandidateView();

        return allValues.get(allValues.size() - 1);
    }

    protected void simulateOnStartInputFlow() {
        simulateOnStartInputFlow(false, createEditorInfoTextWithSuggestionsForSetUp());
    }

    protected void simulateOnStartInputFlow(boolean restarting, EditorInfo editorInfo) {
        // mAbstractInputMethod.showSoftInput(InputMethod.SHOW_EXPLICIT, null);
        if (restarting) {
            mAnySoftKeyboardUnderTest
                    .getCreatedInputMethodInterface()
                    .restartInput(getCurrentTestInputConnection(), editorInfo);
        } else {
            mAnySoftKeyboardUnderTest
                    .getCreatedInputMethodInterface()
                    .startInput(getCurrentTestInputConnection(), editorInfo);
        }
        mAnySoftKeyboardUnderTest.showWindow(true);
    }

    protected void simulateFinishInputFlow() {
        mAbstractInputMethod.hideSoftInput(InputMethodManager.RESULT_HIDDEN, null);
        mAnySoftKeyboardUnderTest.getCreatedInputMethodSessionInterface().finishInput();
    }

    protected CharSequence getResText(int stringId) {
        return getApplicationContext().getText(stringId);
    }
}
