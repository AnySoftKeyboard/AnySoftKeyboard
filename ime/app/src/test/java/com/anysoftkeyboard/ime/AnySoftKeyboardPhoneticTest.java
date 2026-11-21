package com.anysoftkeyboard.ime;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.keyboards.ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.CandidatesPanelView;
import com.anysoftkeyboard.phonetic.PhraseTable;
import com.menny.android.anysoftkeyboard.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** Zhuyin composition through the IME: key routing, field sync, picks and commits. */
public class AnySoftKeyboardPhoneticTest extends AnySoftKeyboardBaseTest {
  private static final String TABLE_PATH =
      "addons/languages/chinese_traditional/pack/src/main/res/raw/zhuyin_phrases";
  private static PhraseTable sTable;

  /** Forces phonetic mode with the real Traditional Chinese table on the default keyboard. */
  public static class PhoneticTestableAnySoftKeyboard extends TestableAnySoftKeyboard {
    @Override
    protected boolean isPhoneticInputMode() {
      return true;
    }

    @Override
    protected PhraseTable getPhoneticTable() {
      return table();
    }
  }

  @Override
  protected Class<? extends TestableAnySoftKeyboard> getServiceClass() {
    return PhoneticTestableAnySoftKeyboard.class;
  }

  private static synchronized PhraseTable table() {
    if (sTable == null) {
      File dir = new File("").getAbsoluteFile();
      while (!new File(dir, TABLE_PATH).exists()) dir = dir.getParentFile();
      try (InputStream in = new FileInputStream(new File(dir, TABLE_PATH))) {
        sTable = PhraseTable.read(in);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return sTable;
  }

  private void type(String bopomofo) {
    bopomofo.codePoints().forEach(cp -> mAnySoftKeyboardUnderTest.simulateKeyPress(cp));
  }

  private String text() {
    return mAnySoftKeyboardUnderTest.getCurrentInputConnectionText();
  }

  private void pick(String candidate) {
    final List<?> shown = verifyAndCaptureSuggestion(false);
    final int index = shown.indexOf(candidate);
    Assert.assertTrue(candidate + " not in " + shown, index >= 0);
    mAnySoftKeyboardUnderTest.pickSuggestionManually(index, candidate);
  }

  @Test
  public void testTypingShowsZhuyinAndOffersConversion() {
    type("ㄋㄧˇㄏㄠˇㄇㄚ˙");
    Assert.assertEquals("ㄋㄧˇㄏㄠˇㄇㄚ˙", text());
    Assert.assertEquals(9, getCurrentTestInputConnection().getCurrentStartPosition());
    Assert.assertEquals("你好嗎", verifyAndCaptureSuggestion(false).get(0).toString());
  }

  @Test
  public void testTonelessSyllableShowsCandidates() {
    type("ㄋㄧ");
    Assert.assertEquals("ㄋㄧ", text());
    final List<?> shown = verifyAndCaptureSuggestion(false);
    Assert.assertTrue(shown.toString(), shown.contains("你"));
    pick("你");
    Assert.assertEquals("你", text());
  }

  @Test
  public void testPendingSyllableIsShownAsTyped() {
    type("ㄋㄧˇㄏㄠ");
    Assert.assertEquals("ㄋㄧˇㄏㄠ", text());
  }

  @Test
  public void testSpaceCommitsThenTypesSpace() {
    type("ㄋㄧˇㄏㄠˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("你好", text());
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("你好 ", text());
  }

  @Test
  public void testEnterCommitsWithoutNewLine() {
    type("ㄋㄧˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    Assert.assertEquals("你", text());
  }

  @Test
  public void testPickingHeadPhraseCommitsItAndKeepsComposing() {
    type("ㄋㄧˇㄏㄠˇㄇㄚ˙");
    pick("妳好");
    Assert.assertEquals("妳好ㄇㄚ˙", text());
    // ㄇㄚ˙ is still composing: deleting edits it, not the committed text.
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("妳好ㄇㄚ", text());
  }

  @Test
  public void testDeleteEditsSymbolBySymbol() {
    type("ㄋㄧˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("ㄋㄧ", text());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("", text());
    // Nothing composing: delete works on the field as usual.
    mAnySoftKeyboardUnderTest.simulateTextTyping("ab");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("a", text());
  }

  @Test
  public void testPunctuationCommitsCompositionFirst() {
    type("ㄋㄧˇㄏㄠˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress('，');
    Assert.assertEquals("你好，", text());
  }

  @Test
  public void testCursorPickFixesMiddleWord() {
    type("ㄋㄧˇㄏㄠˇㄇㄚ˙");
    // The arrows walk the Bopomofo symbol by symbol: six steps back to the start of ㄏㄠˇ.
    for (int position = 8; position >= 3; position--) {
      mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
      Assert.assertEquals(position, getCurrentTestInputConnection().getCurrentStartPosition());
    }
    pick("郝");
    Assert.assertEquals("ㄋㄧˇ郝ㄇㄚ˙", text());
    Assert.assertEquals(4, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    Assert.assertEquals("你郝嗎", text());
  }

  private CandidatesPanelView panel() {
    return mAnySoftKeyboardUnderTest
        .getInputViewContainer()
        .findViewById(R.id.candidates_panel_root);
  }

  @Test
  public void testSwipeUpOpensCandidatesPanelAndPickCommits() {
    type("ㄋㄧˇㄏㄠˇ");
    Assert.assertNull(panel());
    Assert.assertTrue(mAnySoftKeyboardUnderTest.onCandidateStripSwipeUp());
    final CandidatesPanelView panel = panel();
    Assert.assertNotNull(panel);
    Assert.assertEquals(
        android.view.View.GONE,
        ((android.view.View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());

    // The grid holds every candidate; pick the 2nd one (你).
    final android.view.ViewGroup scroll = (android.view.ViewGroup) panel.getChildAt(1);
    final android.view.ViewGroup grid = (android.view.ViewGroup) scroll.getChildAt(0);
    final android.widget.TextView you = findText(grid, "你");
    you.performClick();

    Assert.assertNull(panel());
    Assert.assertEquals(
        android.view.View.VISIBLE,
        ((android.view.View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    Assert.assertEquals("你ㄏㄠˇ", text());
  }

  @Test
  public void testSwipeUpWithoutCompositionKeepsStripBehavior() {
    Assert.assertFalse(mAnySoftKeyboardUnderTest.onCandidateStripSwipeUp());
    Assert.assertNull(panel());
  }

  @Test
  public void testClosingCandidatesPanelKeepsTheStrip() {
    type("ㄋㄧˇㄏㄠˇ");
    final android.view.View strip =
        mAnySoftKeyboardUnderTest.getInputViewContainer().getCandidateView();
    Assert.assertEquals(android.view.View.VISIBLE, strip.getVisibility());
    mAnySoftKeyboardUnderTest.onCandidateStripSwipeUp();
    final android.view.ViewGroup panel = panel();
    // The close button closes the panel.
    panel.getChildAt(0).performClick();
    Assert.assertNull(panel());
    Assert.assertEquals(android.view.View.VISIBLE, strip.getVisibility());
    Assert.assertEquals("你好", verifyAndCaptureSuggestion(false).get(0).toString());
  }

  @Test
  public void testBackClosesCandidatesPanel() {
    type("ㄋㄧˇ");
    mAnySoftKeyboardUnderTest.onCandidateStripSwipeUp();
    Assert.assertNotNull(panel());
    mAnySoftKeyboardUnderTest.hideWindow();
    Assert.assertNull(panel());
  }

  private static android.widget.TextView findText(android.view.ViewGroup grid, String text) {
    for (int i = 0; i < grid.getChildCount(); i++) {
      final android.widget.TextView item = (android.widget.TextView) grid.getChildAt(i);
      if (item.getText().toString().equals(text)) return item;
    }
    throw new AssertionError(text + " not in the panel");
  }

  // --- state-machine scenarios: commit, cancel, delete, move, switch ---

  @Test
  public void testDeleteAfterCommitDeletesExactlyOneChar() {
    type("ㄋㄧˇㄏㄠˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("你好", text());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("你", text());
    // and a new composition starts cleanly after it
    type("ㄇㄚ˙");
    Assert.assertEquals("你ㄇㄚ˙", text());
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("你嗎", text());
  }

  @Test
  public void testDeleteAfterPickedHeadDeletesOnlyComposingSymbols() {
    type("ㄋㄧˇㄏㄠˇ");
    pick("你");
    Assert.assertEquals("你ㄏㄠˇ", text());
    for (int i = 0; i < 3; i++) mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("你", text());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("", text());
  }

  @Test
  public void testCancelStripKeepsTypedZhuyin() {
    type("ㄋㄧˇㄏㄠˇ");
    ((AnySoftKeyboardSuggestions) mAnySoftKeyboardUnderTest).cancelSuggestionsStrip();
    Assert.assertEquals("ㄋㄧˇㄏㄠˇ", text());
    // nothing is composing anymore: delete is a plain delete
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("ㄋㄧˇㄏㄠ", text());
  }

  @Test
  public void testFieldClosingCommitsConversion() {
    type("ㄋㄧˇㄏㄠ");
    simulateFinishInputFlow();
    Assert.assertEquals("你好", text());
  }

  @Test
  public void testAppRestartingInputDoesNotCommitConversion() {
    type("ㄋㄧˇ");
    // The app restarts the input on the same field (as it does after changing the text itself).
    simulateOnStartInputFlow(true, createEditorInfoTextWithSuggestionsForSetUp());
    Assert.assertEquals("ㄋㄧˇ", text());
    // The old composition is gone: new keys start a fresh one.
    type("ㄏㄠˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    Assert.assertEquals("ㄋㄧˇ好", text());
  }

  @Test
  public void testSymbolsKeyboardCommitsConversion() {
    type("ㄋㄧˇㄏㄠˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMBOLS);
    Assert.assertEquals("你好", text());
  }

  @Test
  public void testSwitchingToTheSameKeyboardKeepsComposition() {
    // Only one alphabet keyboard is enabled here, so "next language" lands on the same keyboard
    // (a real switch commits, see onAlphabetKeyboardSet / commitPendingPhonetic).
    type("ㄋㄧˇㄏㄠˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
    Assert.assertEquals("ㄋㄧˇㄏㄠˇ", text());
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("你好", text());
  }

  @Test
  public void testTappingElsewhereKeepsTypedTextAndCursor() {
    mAnySoftKeyboardUnderTest.simulateTextTyping("ab ");
    type("ㄋㄧˇ");
    Assert.assertEquals("ab ㄋㄧˇ", text());
    com.anysoftkeyboard.rx.TestRxSchedulers.foregroundAdvanceBy(2000);
    mAnySoftKeyboardUnderTest.moveCursorToPosition(1, true);
    Assert.assertEquals("ab ㄋㄧˇ", text());
    // a new composition starts where the user tapped, the old one is left alone
    type("ㄇㄚ˙");
    Assert.assertEquals("aㄇㄚ˙b ㄋㄧˇ", text());
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("a嗎b ㄋㄧˇ", text());
  }

  @Test
  public void testTappingElsewhereRightAfterTyping() {
    mAnySoftKeyboardUnderTest.simulateTextTyping("ab ");
    type("ㄋㄧˇ");
    mAnySoftKeyboardUnderTest.moveCursorToPosition(1, true); // within ASK's 1.5 s window
    type("ㄇㄚ˙");
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("a嗎b ㄋㄧˇ", text());
  }

  @Test
  public void testBackAndForthThenInsertAndCommit() {
    type("ㄋㄧˇㄇㄚ˙");
    for (int i = 0; i < 5; i++) mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
    for (int i = 0; i < 2; i++) mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
    Assert.assertEquals(3, getCurrentTestInputConnection().getCurrentStartPosition());
    type("ㄏㄠˇ");
    Assert.assertEquals("ㄋㄧˇㄏㄠˇㄇㄚ˙", text());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    Assert.assertEquals("你好嗎", text());
  }

  @Test
  public void testArrowPastTheEndCommitsAndMovesOn() {
    mAnySoftKeyboardUnderTest.simulateTextTyping("ab");
    mAnySoftKeyboardUnderTest.moveCursorToPosition(1, true);
    com.anysoftkeyboard.rx.TestRxSchedulers.foregroundAdvanceBy(2000);
    type("ㄋㄧˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
    Assert.assertEquals("a你b", text());
  }

  @Test
  public void testShiftDoesNotCommit() {
    type("ㄋㄧˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
    Assert.assertEquals("ㄋㄧˇ", text());
    type("ㄏㄠˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("你好", text());
  }

  @Test
  public void testPopupTextCommitsFirst() {
    type("ㄋㄧˇㄏㄠˇ");
    mAnySoftKeyboardUnderTest.onText(null, "1", true);
    Assert.assertEquals("你好1", text());
  }

  @Test
  public void testFullWidthPunctuationAfterComposition() {
    type("ㄋㄧˇㄏㄠˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress('？');
    Assert.assertEquals("你好？", text());
  }

  @Test
  public void testDoubleSpaceAfterCommitDoesNotEatText() {
    type("ㄋㄧˇ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
    Assert.assertEquals("你  ", text());
  }

  @Test
  public void testNonBopomofoWithoutCompositionIsTypedNormally() {
    mAnySoftKeyboardUnderTest.simulateKeyPress('a');
    Assert.assertEquals("a", text());
  }

  // --- candidates from the first symbol, and typing the symbols themselves ---

  /** A key of the long-press popup that holds {@code popupCharacters}. */
  private static Keyboard.Key popupKey(String popupCharacters, int code) {
    final AnyPopupKeyboard popup =
        new AnyPopupKeyboard(
            new DefaultAddOn(getApplicationContext(), getApplicationContext()),
            getApplicationContext(),
            popupCharacters,
            SIMPLE_KeyboardDimens,
            "POP_KEYBOARD");
    for (Keyboard.Key key : popup.getKeys()) {
      if (key.getPrimaryCode() == code) return key;
    }
    throw new AssertionError("no key " + code + " in popup " + popupCharacters);
  }

  private void longPressPick(String popupCharacters, int code) {
    mAnySoftKeyboardUnderTest.onKey(code, popupKey(popupCharacters, code), 0, null, true);
  }

  @Test
  public void testFirstSymbolShowsCandidates() {
    type("ㄅ");
    Assert.assertEquals("ㄅ", text());
    Assert.assertEquals("不", verifyAndCaptureSuggestion(false).get(0).toString());
    pick("不");
    Assert.assertEquals("不", text());
  }

  @Test
  public void testOpenSyllableCompletesPhrase() {
    type("ㄋㄧˇㄏ");
    pick("你好");
    Assert.assertEquals("你好", text());
  }

  @Test
  public void testEnterCommitsTheFirstCandidate() {
    type("ㄋㄧˇㄏ");
    final String shown = verifyAndCaptureSuggestion(false).get(0).toString();
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    Assert.assertEquals("你會", shown);
    Assert.assertEquals(shown, text());
  }

  @Test
  public void testPunctuationCommitsTheFirstCandidate() {
    type("ㄅ");
    mAnySoftKeyboardUnderTest.simulateKeyPress('，');
    Assert.assertEquals("不，", text());
  }

  @Test
  public void testClosedLoneSymbolStaysItselfOnEnter() {
    type("ㄅ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(' '); // closes the syllable as typed
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    Assert.assertEquals("ㄅ", text());
  }

  @Test
  public void testMovingTheCursorKeepsLoneSymbol() {
    type("ㄅ");
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
    Assert.assertEquals("ㄅ", text());
    // The composition is over: the next symbol starts a new one after it.
    type("ㄋㄧˇ");
    Assert.assertEquals("ㄅㄋㄧˇ", text());
  }

  @Test
  public void testEditingInsideASyllableWithTheArrows() {
    type("ㄋㄧˇㄏㄠˇ");
    for (int i = 0; i < 5; i++) mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
    Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
    type("ㄨ"); // ㄋ|ㄧˇ: the medial becomes ㄨ
    Assert.assertEquals("ㄋㄨˇㄏㄠˇ", text());
    Assert.assertEquals(2, getCurrentTestInputConnection().getCurrentStartPosition());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
    Assert.assertEquals("ㄋˇㄏㄠˇ", text());
    Assert.assertEquals(1, getCurrentTestInputConnection().getCurrentStartPosition());
  }

  @Test
  public void testLongPressTypesSymbolItself() {
    longPressPick("1ㄅ", 'ㄅ');
    Assert.assertEquals("ㄅ", text());
    longPressPick("1ㄅ", 'ㄅ');
    Assert.assertEquals("ㄅㄅ", text());
    // Not composing: the next key starts a syllable of its own.
    type("ㄋㄧˇ");
    Assert.assertEquals("ㄅㄅㄋㄧˇ", text());
  }

  @Test
  public void testLongPressWhileComposingCommitsFirst() {
    type("ㄋㄧˇㄏㄠˇ");
    longPressPick("1ㄅ", 'ㄅ');
    Assert.assertEquals("你好ㄅ", text());
  }

  @Test
  public void testLongPressHintStillTypesIt() {
    type("ㄋㄧˇ");
    longPressPick("1ㄅ", '1');
    Assert.assertEquals("你1", text());
  }

  @Test
  public void testLoneToneMarkIsTyped() {
    type("ˇ");
    Assert.assertEquals("ˇ", text());
    type("ˊ˙");
    Assert.assertEquals("ˇˊ˙", text());
    type("ㄋㄧˇ");
    Assert.assertEquals("ˇˊ˙ㄋㄧˇ", text());
    mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ENTER);
    Assert.assertEquals("ˇˊ˙你", text());
  }

  @Test
  public void testLoneToneMarkAfterLatinWordKeepsTheWord() {
    mAnySoftKeyboardUnderTest.simulateTextTyping("ab");
    type("ˋ");
    Assert.assertEquals("abˋ", text());
  }

  @Test
  public void testToneAfterSyllableStillRetones() {
    type("ㄇㄚˇ˙");
    Assert.assertEquals("ㄇㄚ˙", text());
  }
}
