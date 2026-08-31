package com.anysoftkeyboard;

import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

/**
 * Static repro for two reported ASK bugs (evidence-gathering pass, no fix): - typing
 * letter+digit(+letter) can produce an extra letter (k8 -> k8k, k8s -> k8ks) - typing a word then
 * Enter can duplicate the word on the new line
 *
 * <p>Each scenario dumps every InputConnection call ASK emits, plus WordComposer state and the
 * resulting editor text/composing range after each key, then asserts the final text.
 */
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardTypingCorruptionTest extends AnySoftKeyboardBaseTest {

  private final List<String> mTrace = new ArrayList<>();
  private TestInputConnection mIc;

  @Before
  public void configurePrefs() {
    // Match the reporter's real config: suggestions on, autocorrect off,
    // auto-pick aggressiveness off, next-word suggestions on, no suggestion restart.
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_show_suggestions, true);
    SharedPrefsHelper.setPrefsValue(
        R.string.settings_key_auto_pick_suggestion_aggressiveness, "none");
    SharedPrefsHelper.setPrefsValue(
        R.string.settings_key_next_word_suggestion_aggressiveness, "medium_aggressiveness");
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_allow_suggestions_restart, false);
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_quick_fix, false);
    TestRxSchedulers.drainAllTasks();

    mIc = getCurrentTestInputConnection();
    installIcTracers(mIc);
  }

  private void installIcTracers(TestInputConnection ic) {
    Mockito.doAnswer(recording("setComposingText", ic))
        .when(ic)
        .setComposingText(Mockito.any(), Mockito.anyInt());
    Mockito.doAnswer(recording("commitText", ic))
        .when(ic)
        .commitText(Mockito.any(), Mockito.anyInt());
    Mockito.doAnswer(recording("finishComposingText", ic)).when(ic).finishComposingText();
    Mockito.doAnswer(recording("setComposingRegion", ic))
        .when(ic)
        .setComposingRegion(Mockito.anyInt(), Mockito.anyInt());
    Mockito.doAnswer(recording("beginBatchEdit", ic)).when(ic).beginBatchEdit();
    Mockito.doAnswer(recording("endBatchEdit", ic)).when(ic).endBatchEdit();
    Mockito.doAnswer(recording("sendKeyEvent", ic)).when(ic).sendKeyEvent(Mockito.any());
    Mockito.doAnswer(recording("deleteSurroundingText", ic))
        .when(ic)
        .deleteSurroundingText(Mockito.anyInt(), Mockito.anyInt());
    Mockito.doAnswer(recording("setSelection", ic))
        .when(ic)
        .setSelection(Mockito.anyInt(), Mockito.anyInt());
  }

  private org.mockito.stubbing.Answer<Object> recording(String label, TestInputConnection ic) {
    return (InvocationOnMock inv) -> {
      Object result = inv.callRealMethod();
      String args = Arrays.toString(inv.getArguments());
      TestInputConnection.CompleteState st = ic.getCurrentState();
      mTrace.add(
          String.format(
              "  IC %-22s args=%s -> text=%s sel=[%d,%d] composing=[%d,%d]",
              label,
              args,
              quote(st.text),
              st.selectionStart,
              st.selectionEnd,
              st.candidateStart,
              st.candidateEnd));
      return result;
    };
  }

  private void mark(String label) {
    mTrace.add("== " + label + " ==");
  }

  private void afterKey(String key) {
    TestInputConnection.CompleteState st = mIc.getCurrentState();
    mTrace.add(
        String.format(
            "  after key '%s' -> text=%s sel=[%d,%d] composing=[%d,%d] predictionOn=%s",
            key,
            quote(st.text),
            st.selectionStart,
            st.selectionEnd,
            st.candidateStart,
            st.candidateEnd,
            mAnySoftKeyboardUnderTest.isPredictionOn()));
  }

  private static String quote(CharSequence s) {
    if (s == null) return "null";
    return "\"" + s.toString().replace("\n", "\\n") + "\"";
  }

  private void typeChars(String s) {
    for (char c : s.toCharArray()) {
      mark("key '" + c + "'");
      mAnySoftKeyboardUnderTest.simulateKeyPress(c);
      afterKey(String.valueOf(c));
    }
  }

  private void pressEnter() {
    mark("key ENTER");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    afterKey("ENTER");
  }

  private void pressSpace() {
    mark("key SPACE");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
    afterKey("SPACE");
  }

  private void assertEditorText(String scenario, String expected) {
    String actual = mIc.getCurrentTextInInputConnection();
    Assert.assertEquals("scenario=" + scenario, expected, actual);
  }

  @Test
  public void a_k_then_8_then_s_should_produce_k8s() {
    typeChars("k8s");
    assertEditorText("k8s", "k8s");
  }

  @Test
  public void b_k_then_8_should_produce_k8() {
    typeChars("k8");
    assertEditorText("k8", "k8");
  }

  @Test
  public void c_a_then_1_should_produce_a1() {
    typeChars("a1");
    assertEditorText("a1", "a1");
  }

  @Test
  public void d_b_then_1_should_produce_b1() {
    typeChars("b1");
    assertEditorText("b1", "b1");
  }

  @Test
  public void e_i_then_1_should_produce_i1() {
    typeChars("i1");
    assertEditorText("i1", "i1");
  }

  @Test
  public void f_o_then_1_should_produce_o1() {
    typeChars("o1");
    assertEditorText("o1", "o1");
  }

  @Test
  public void g_hello_then_enter_should_produce_hello_newline() {
    typeChars("hello");
    pressEnter();
    assertEditorText("hello+ENTER", "hello\n");
  }

  @Test
  public void h_gibberish_then_enter_should_produce_gibberish_newline() {
    typeChars("qzxv");
    pressEnter();
    assertEditorText("qzxv+ENTER", "qzxv\n");
  }

  @Test
  public void i_hello_then_space_should_produce_hello_space() {
    typeChars("hello");
    pressSpace();
    assertEditorText("hello+SPACE", "hello ");
  }

  // --- Post-fix regression: separator must be committed via commitText, not sendKeyEvent. ---

  private long countTraceMatches(String needle) {
    return mTrace.stream().filter(line -> line.contains(needle)).count();
  }

  @Test
  public void j_digit_separator_uses_commitText_not_sendKeyEvent() {
    typeChars("k8s");
    Assert.assertEquals(
        "digit separator '8' must be committed via commitText",
        1L,
        countTraceMatches("IC commitText             args=[8, 1]"));
    Assert.assertEquals(
        "no sendKeyEvent should fire for the digit separator",
        0L,
        countTraceMatches("IC sendKeyEvent"));
  }

  @Test
  public void k_enter_as_newline_uses_commitText_not_sendKeyEvent() {
    typeChars("hello");
    pressEnter();
    Assert.assertEquals(
        "ENTER-as-newline must be committed via commitText(\"\\n\", 1)",
        1L,
        countTraceMatches("IC commitText             args=[\n, 1]"));
    Assert.assertEquals(
        "no sendKeyEvent should fire for ENTER-as-newline",
        0L,
        countTraceMatches("IC sendKeyEvent"));
  }

  @Test
  public void l_enter_with_ime_action_send_calls_performEditorAction_not_commitText_newline() {
    // Restart the input as a SEND-action field.
    EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
    editorInfo.imeOptions = EditorInfo.IME_ACTION_SEND;
    mAnySoftKeyboardUnderTest.onFinishInputView(true);
    mAnySoftKeyboardUnderTest.onFinishInput();
    mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
    mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
    mIc = getCurrentTestInputConnection();
    mTrace.clear();
    installIcTracers(mIc);
    Mockito.doAnswer(recording("performEditorAction", mIc))
        .when(mIc)
        .performEditorAction(Mockito.anyInt());

    typeChars("hello");
    pressEnter();

    Assert.assertEquals(
        "ENTER with IME_ACTION_SEND must call performEditorAction",
        1L,
        countTraceMatches("IC performEditorAction"));
    Assert.assertEquals(
        "ENTER with IME_ACTION_SEND must NOT commit a newline",
        0L,
        countTraceMatches("IC commitText             args=[\n, 1]"));
  }

  @Test
  public void m_manual_suggestion_pick_uses_commitText_for_autospace_not_sendKeyEvent() {
    typeChars("hel");
    mTrace.clear();
    mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hello", true);

    Assert.assertEquals(
        "auto-space after suggestion pick must be committed via commitText(\" \", 1)",
        1L,
        countTraceMatches("IC commitText             args=[ , 1]"));
    Assert.assertEquals(
        "no sendKeyEvent should fire for the auto-space after suggestion pick",
        0L,
        countTraceMatches("IC sendKeyEvent"));
  }
}
