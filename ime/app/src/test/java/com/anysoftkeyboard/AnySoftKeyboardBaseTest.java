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
import com.anysoftkeyboard.rx.TestRxSchedulers;
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
import org.robolectric.android.controller.ServiceController;
import org.robolectric.shadow.api.Shadow;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AnySoftKeyboardBaseTest {

  protected TestableAnySoftKeyboard mAnySoftKeyboardUnderTest;

  protected IBinder mMockBinder;
  protected ServiceController<? extends TestableAnySoftKeyboard> mAnySoftKeyboardController;
  private InputMethodManagerShadow mInputMethodManagerShadow;
  private AbstractInputMethodService.AbstractInputMethodImpl mAbstractInputMethod;

  protected TestInputConnection getCurrentTestInputConnection() {
    return mAnySoftKeyboardUnderTest.getTestInputConnection();
  }

  protected CandidateView getMockCandidateView() {
    return mAnySoftKeyboardUnderTest.getMockCandidateView();
  }

  protected Class<? extends TestableAnySoftKeyboard> getServiceClass() {
    return TestableAnySoftKeyboard.class;
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  @Before
  public void setUpForAnySoftKeyboardBase() throws Exception {
    final Application application = getApplicationContext();

    mInputMethodManagerShadow =
        Shadow.extract(application.getSystemService(Service.INPUT_METHOD_SERVICE));
    mMockBinder = Mockito.mock(IBinder.class);

    mAnySoftKeyboardController = Robolectric.buildService(getServiceClass());
    mAnySoftKeyboardUnderTest = mAnySoftKeyboardController.create().get();
    mAbstractInputMethod = mAnySoftKeyboardUnderTest.onCreateInputMethodInterface();
    mAnySoftKeyboardUnderTest.onCreateInputMethodSessionInterface();

    final EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();

    mAbstractInputMethod.attachToken(mMockBinder);

    mAbstractInputMethod.showSoftInput(InputMethod.SHOW_EXPLICIT, null);
    mAbstractInputMethod.startInput(mAnySoftKeyboardUnderTest.getTestInputConnection(), editorInfo);
    TestRxSchedulers.drainAllTasks();
    mAnySoftKeyboardUnderTest.showWindow(true);

    Assert.assertNotNull(getMockCandidateView());

    // simulating the first OS subtype reporting
    AnyKeyboard currentAlphabetKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
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
        .setSuggestions(Mockito.anyList(), Mockito.anyInt());
  }

  protected final void verifySuggestions(
      boolean resetCandidateView, CharSequence... expectedSuggestions) {
    // ensuring suggestions computed
    TestRxSchedulers.drainAllTasks();

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
        .setSuggestions(suggestionsCaptor.capture(), Mockito.anyInt());
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
    TestRxSchedulers.foregroundAdvanceBy(0);
  }

  protected void simulateFinishInputFlow() {
    mAbstractInputMethod.hideSoftInput(InputMethodManager.RESULT_HIDDEN, null);
    mAnySoftKeyboardUnderTest.getCreatedInputMethodSessionInterface().finishInput();
    TestRxSchedulers.foregroundAdvanceBy(0);
  }

  protected CharSequence getResText(int stringId) {
    return getApplicationContext().getText(stringId);
  }
}
