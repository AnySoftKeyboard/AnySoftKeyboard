package com.anysoftkeyboard.phonetic;

import static com.anysoftkeyboard.phonetic.TestTables.type;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ZhuyinComposerTest {
  private static final char SEP = ZhuyinComposer.SYLLABLE_SEPARATOR;

  private ZhuyinComposer mComposer;

  @Before
  public void setUp() {
    mComposer = new ZhuyinComposer(TestTables.zhuyin(), new UserModel());
  }

  private Candidate candidate(String text) {
    for (Candidate c : mComposer.candidates()) {
      if (c.text.equals(text)) return c;
    }
    throw new AssertionError(text + " not in " + mComposer.candidates());
  }

  // --- happy path ---

  @Test
  public void testShowsZhuyinAndOffersConversion() {
    type(mComposer, "ㄋㄧˇㄏㄠˇㄇㄚ˙");
    assertEquals("ㄋㄧˇㄏㄠˇㄇㄚ˙", mComposer.composingText());
    assertEquals(9, mComposer.cursorCharOffset());
    assertEquals("你好嗎", mComposer.conversion());
    assertEquals("你好嗎", mComposer.candidates().get(0).text);
    assertEquals("", mComposer.takeCommitText());
  }

  @Test
  public void testSpaceClosesFirstToneThenCommitsConversion() {
    type(mComposer, "ㄐㄧㄣ ㄊㄧㄢ ");
    assertEquals("ㄐㄧㄣ" + SEP + "ㄊㄧㄢ", mComposer.composingText());
    assertEquals("今天", mComposer.conversion());
    assertTrue(mComposer.onKey(' '));
    assertEquals("今天", mComposer.takeCommitText());
    assertFalse(mComposer.isComposing());
    assertFalse(mComposer.onKey(' ')); // empty: a real space is typed
  }

  @Test
  public void testPendingSyllableShownAtCursor() {
    type(mComposer, "ㄋㄧˇㄏㄠ");
    assertEquals("ㄋㄧˇㄏㄠ", mComposer.composingText());
    assertEquals(5, mComposer.cursorCharOffset());
  }

  @Test
  public void testTonelessSyllablesAreSeparated() {
    type(mComposer, "ㄒㄧㄝㄒㄧㄝ");
    assertEquals("ㄒㄧㄝ" + SEP + "ㄒㄧㄝ", mComposer.composingText());
    mComposer.commitAll();
    assertEquals("謝謝", mComposer.takeCommitText());
    type(mComposer, "ㄊㄞㄨㄢ");
    mComposer.commitAll();
    assertEquals("台灣", mComposer.takeCommitText());
  }

  @Test
  public void testInitialReplacesIncompleteInitial() {
    type(mComposer, "ㄋㄇㄚˇ");
    assertEquals("ㄇㄚˇ", mComposer.composingText());
    assertEquals("馬", mComposer.conversion());
  }

  @Test
  public void testToneAfterClosedSyllableSetsItsTone() {
    type(mComposer, "ㄇㄚ ");
    type(mComposer, "ˇ");
    assertEquals("ㄇㄚˇ", mComposer.composingText());
    assertEquals("馬", mComposer.conversion());
  }

  // --- commit granularity ---

  @Test
  public void testFirstCandidateIsWholeSentence() {
    type(mComposer, "ㄐㄧㄣ ㄊㄧㄢ ㄊㄧㄢ ㄑㄧˋㄏㄣˇㄏㄠˇ");
    final Candidate first = mComposer.candidates().get(0);
    assertEquals(Candidate.Kind.SENTENCE, first.kind);
    assertEquals("今天天氣很好", first.text);
    mComposer.pick(first);
    assertEquals("今天天氣很好", mComposer.takeCommitText());
    assertFalse(mComposer.isComposing());
  }

  @Test
  public void testWholePhraseIsFirstWhenOneSegment() {
    type(mComposer, "ㄋㄧˇㄏㄠˇㄇㄚ˙");
    final Candidate first = mComposer.candidates().get(0);
    assertEquals("你好嗎", first.text);
    mComposer.pick(first);
    assertEquals("你好嗎", mComposer.takeCommitText());
    assertFalse(mComposer.isComposing());
  }

  @Test
  public void testPickingHeadPhraseCommitsItAndKeepsTheRest() {
    type(mComposer, "ㄋㄧˇㄏㄠˇㄇㄚ˙");
    mComposer.pick(candidate("妳好"));
    assertEquals("妳好", mComposer.takeCommitText());
    assertEquals("ㄇㄚ˙", mComposer.composingText());
    assertEquals("嗎", mComposer.conversion());
  }

  @Test
  public void testPickingHeadCharCommitsOneChar() {
    type(mComposer, "ㄋㄧˇㄏㄠˇㄇㄚ˙");
    mComposer.pick(candidate("妳"));
    assertEquals("妳", mComposer.takeCommitText());
    assertEquals("ㄏㄠˇㄇㄚ˙", mComposer.composingText());
    assertEquals("好嗎", mComposer.conversion());
  }

  @Test
  public void testCandidatesStartAtHeadLongestFirst() {
    type(mComposer, "ㄋㄧˇㄏㄠˇ");
    final List<Candidate> candidates = mComposer.candidates();
    // SENTENCE is not repeated when it equals the single head phrase.
    assertEquals(Candidate.Kind.PHRASE, candidates.get(0).kind);
    assertEquals("你好", candidates.get(0).text);
    assertTrue(candidates.stream().anyMatch(c -> c.text.equals("你")));
  }

  // --- cursor ---

  @Test
  public void testCursorPickPinsMiddleAsHanziWithoutCommitting() {
    type(mComposer, "ㄋㄧˇㄏㄠˇㄇㄚ˙");
    assertTrue(mComposer.moveCursor(-6)); // before ㄏㄠˇ
    assertEquals(3, mComposer.cursorCharOffset());
    mComposer.pick(candidate("郝"));
    assertEquals("", mComposer.takeCommitText());
    assertEquals("ㄋㄧˇ郝ㄇㄚ˙", mComposer.composingText());
    assertEquals(4, mComposer.cursorCharOffset());
    assertEquals("你郝嗎", mComposer.conversion());
  }

  @Test
  public void testPinnedPrefixFlushesWhenHeadIsPicked() {
    type(mComposer, "ㄋㄧˇㄏㄠˇㄇㄚ˙");
    mComposer.moveCursor(-6);
    mComposer.pick(candidate("郝"));
    mComposer.moveCursor(-4); // over 郝 and ㄋㄧˇ, to the start
    mComposer.pick(candidate("妳"));
    assertEquals("妳郝", mComposer.takeCommitText());
    assertEquals("ㄇㄚ˙", mComposer.composingText());
  }

  @Test
  public void testTypingAtCursorInsertsSyllable() {
    type(mComposer, "ㄋㄧˇㄇㄚ˙");
    mComposer.moveCursor(-3);
    type(mComposer, "ㄏㄠˇ");
    assertEquals("ㄋㄧˇㄏㄠˇㄇㄚ˙", mComposer.composingText());
    assertEquals(6, mComposer.cursorCharOffset());
    assertEquals("你好嗎", mComposer.conversion());
  }

  @Test
  public void testMoveCursorEdges() {
    assertFalse(mComposer.moveCursor(-1));
    type(mComposer, "ㄋㄧˇ");
    assertFalse(mComposer.moveCursor(1));
    assertTrue(mComposer.moveCursor(-3));
    assertFalse(mComposer.moveCursor(-1));
  }

  @Test
  public void testCursorMovesSymbolBySymbol() {
    type(mComposer, "ㄋㄧˇㄏㄠˇ");
    assertEquals(6, mComposer.cursorCharOffset());
    for (int offset = 5; offset >= 0; offset--) {
      assertTrue(mComposer.moveCursor(-1));
      assertEquals(offset, mComposer.cursorCharOffset());
    }
    assertFalse(mComposer.moveCursor(-1));
    for (int offset = 1; offset <= 6; offset++) {
      assertTrue(mComposer.moveCursor(1));
      assertEquals(offset, mComposer.cursorCharOffset());
    }
    assertFalse(mComposer.moveCursor(1));
    assertEquals("ㄋㄧˇㄏㄠˇ", mComposer.composingText());
  }

  @Test
  public void testCursorPassesSeparatorWithTheSyllable() {
    type(mComposer, "ㄐㄧㄣ ㄊㄧㄢ ");
    // ㄐㄧㄣ + separator + ㄊㄧㄢ: from the end, three steps reach the start of ㄊㄧㄢ.
    assertTrue(mComposer.moveCursor(-3));
    assertEquals(4, mComposer.cursorCharOffset());
    assertTrue(mComposer.moveCursor(-1));
    assertEquals(2, mComposer.cursorCharOffset());
  }

  @Test
  public void testCursorInsideSyllableOffersItsCandidates() {
    type(mComposer, "ㄋㄧˇㄏㄠˇㄇㄚ˙");
    assertTrue(mComposer.moveCursor(-4)); // ㄏㄠ|ˇ
    assertEquals(5, mComposer.cursorCharOffset());
    mComposer.pick(candidate("郝"));
    assertEquals("ㄋㄧˇ郝ㄇㄚ˙", mComposer.composingText());
    assertEquals(4, mComposer.cursorCharOffset());
  }

  @Test
  public void testTypingInsideSyllableSetsItsSlot() {
    type(mComposer, "ㄋㄧˇㄏㄠˇ");
    assertTrue(mComposer.moveCursor(-5)); // ㄋ|ㄧˇㄏㄠˇ
    type(mComposer, "ㄨ"); // medial ㄧ → ㄨ
    assertEquals("ㄋㄨˇㄏㄠˇ", mComposer.composingText());
    assertEquals(2, mComposer.cursorCharOffset());
    type(mComposer, "ˋ"); // tone ˇ → ˋ; the cursor is now past the syllable
    assertEquals("ㄋㄨˋㄏㄠˇ", mComposer.composingText());
    assertEquals(3, mComposer.cursorCharOffset());
    mComposer.commitAll();
    assertEquals("怒好", mComposer.takeCommitText());
  }

  @Test
  public void testDeleteInsideSyllableRemovesSymbolBeforeCursor() {
    type(mComposer, "ㄋㄧㄢˊ");
    assertTrue(mComposer.moveCursor(-2)); // ㄋㄧ|ㄢˊ
    assertTrue(mComposer.onDelete());
    assertEquals("ㄋㄢˊ", mComposer.composingText());
    assertEquals(1, mComposer.cursorCharOffset());
    assertTrue(mComposer.onDelete());
    assertEquals("ㄢˊ", mComposer.composingText());
    assertEquals(0, mComposer.cursorCharOffset());
    assertTrue(mComposer.onDelete()); // at the start: consumed, nothing to remove
    assertEquals("ㄢˊ", mComposer.composingText());
  }

  @Test
  public void testCursorStepsOverPickedSegmentPerCharacter() {
    type(mComposer, "ㄋㄧˇㄏㄠˇㄇㄚ˙");
    mComposer.moveCursor(-6);
    mComposer.pick(candidate("郝")); // ㄋㄧˇ郝|ㄇㄚ˙
    assertEquals(4, mComposer.cursorCharOffset());
    assertTrue(mComposer.moveCursor(-1));
    assertEquals(3, mComposer.cursorCharOffset());
    assertTrue(mComposer.moveCursor(1));
    assertEquals(4, mComposer.cursorCharOffset());
  }

  // --- editing ---

  @Test
  public void testDeleteEditsSymbolBySymbol() {
    type(mComposer, "ㄋㄧˇㄏㄠˇ");
    assertTrue(mComposer.onDelete()); // tone
    assertEquals("ㄋㄧˇㄏㄠ", mComposer.composingText());
    assertTrue(mComposer.onDelete());
    assertEquals("ㄋㄧˇㄏ", mComposer.composingText());
    assertTrue(mComposer.onDelete());
    assertEquals("ㄋㄧˇ", mComposer.composingText());
    type(mComposer, "ˋ"); // fix the tone of the previous syllable
    assertEquals("ㄋㄧˋ", mComposer.composingText());
  }

  @Test
  public void testDeleteReopensFirstToneSyllableVisibly() {
    type(mComposer, "ㄇㄚ ");
    assertTrue(mComposer.onDelete()); // drops the invisible first tone AND ㄚ
    assertEquals("ㄇ", mComposer.composingText());
  }

  @Test
  public void testDeleteUntilEmpty() {
    type(mComposer, "ㄋㄧˇ");
    for (int i = 0; i < 3; i++) assertTrue(mComposer.onDelete());
    assertFalse(mComposer.isComposing());
    assertFalse(mComposer.onDelete());
  }

  @Test
  public void testDeleteAtStartIsConsumed() {
    type(mComposer, "ㄋㄧˇ");
    mComposer.moveCursor(-3);
    assertTrue(mComposer.onDelete());
    assertEquals("ㄋㄧˇ", mComposer.composingText());
  }

  @Test
  public void testDeleteAfterPinTurnsHanziBackIntoZhuyin() {
    type(mComposer, "ㄋㄧˇㄏㄠˇㄇㄚ˙");
    mComposer.moveCursor(-6);
    mComposer.pick(candidate("郝")); // cursor now right after 郝
    assertTrue(mComposer.onDelete());
    assertEquals("ㄋㄧˇㄏㄠˇㄇㄚ˙", mComposer.composingText());
    assertEquals("你好嗎", mComposer.conversion());
  }

  // --- learning ---

  @Test
  public void testPickIsLearned() {
    type(mComposer, "ㄋㄧˇㄏㄠˇ");
    mComposer.pick(candidate("妳好"));
    mComposer.takeCommitText();
    type(mComposer, "ㄋㄧˇㄏㄠˇ");
    assertEquals("妳好", mComposer.conversion());
  }

  @Test
  public void testNoLearningWhenDisabled() {
    mComposer.setLearning(false);
    type(mComposer, "ㄋㄧˇㄏㄠˇ");
    mComposer.pick(candidate("妳好"));
    type(mComposer, "ㄋㄧˇㄏㄠˇ");
    assertEquals("你好", mComposer.conversion());
  }

  // --- toneless typing: the open syllable takes part in the conversion ---

  @Test
  public void testTonelessSingleSyllableHasCandidates() {
    type(mComposer, "ㄋㄧ");
    assertEquals("ㄋㄧ", mComposer.composingText()); // still open
    final List<Candidate> candidates = mComposer.candidates();
    assertTrue(candidates.size() > 3);
    assertTrue(candidates.stream().anyMatch(c -> c.text.equals("你")));
    assertEquals(candidates.get(0).text, mComposer.conversion());
  }

  @Test
  public void testTonelessSentenceIsOfferedWhileTyping() {
    type(mComposer, "ㄐㄧㄣㄊㄧㄢ"); // second syllable still open
    assertEquals("今天", mComposer.candidates().get(0).text);
    assertEquals("今天", mComposer.conversion());
  }

  @Test
  public void testOpenSyllableCanStillGrow() {
    type(mComposer, "ㄐㄧ"); // a real syllable already
    type(mComposer, "ㄚ"); // but it continues
    assertEquals("ㄐㄧㄚ", mComposer.composingText());
    assertEquals("家", mComposer.candidates().get(0).text);
  }

  @Test
  public void testPickingCandidateThatCoversTheOpenSyllable() {
    type(mComposer, "ㄋㄧ");
    mComposer.pick(candidate("你"));
    assertEquals("你", mComposer.takeCommitText());
    assertFalse(mComposer.isComposing());
  }

  @Test
  public void testPickingHeadKeepsTheOpenSyllableComposing() {
    type(mComposer, "ㄋㄧˇㄏㄠ");
    mComposer.pick(candidate("妳"));
    assertEquals("妳", mComposer.takeCommitText());
    assertEquals("ㄏㄠ", mComposer.composingText());
    assertEquals("好", mComposer.conversion());
  }

  // --- typing Bopomofo itself ---

  @Test
  public void testRawBopomofoIsTheLastCandidate() {
    type(mComposer, "ㄋㄧˇ");
    final List<Candidate> candidates = mComposer.candidates();
    final Candidate last = candidates.get(candidates.size() - 1);
    assertEquals(Candidate.Kind.RAW, last.kind);
    assertEquals("ㄋㄧˇ", last.text);
    mComposer.pick(last);
    assertEquals("ㄋㄧˇ", mComposer.takeCommitText());
  }

  @Test
  public void testRawBopomofoForASingleSymbol() {
    type(mComposer, "ㄅ"); // a lone initial
    mComposer.pick(candidate("ㄅ"));
    assertEquals("ㄅ", mComposer.takeCommitText());
    assertFalse(mComposer.isComposing());
  }

  @Test
  public void testRawPickIsNotLearned() {
    type(mComposer, "ㄋㄧˇ");
    mComposer.pick(candidate("ㄋㄧˇ"));
    mComposer.takeCommitText();
    type(mComposer, "ㄋㄧˇ");
    assertEquals("你", mComposer.conversion());
  }

  // --- impossible syllables ---

  @Test
  public void testEarlierSlotAfterImpossibleSyllableStartsNewSyllable() {
    type(mComposer, "ㄋㄧㄛ"); // not a real syllable
    type(mComposer, "ㄅㄚ"); // ㄅ must not replace ㄋ
    assertEquals("ㄋㄧㄛ" + SEP + "ㄅㄚ", mComposer.composingText());
    assertTrue(mComposer.conversion().startsWith("ㄋㄧㄛ"));
  }

  @Test
  public void testLastSlotOfIncompleteSyllableIsCorrectedInPlace() {
    type(mComposer, "ㄅㄩㄧ"); // ㄅㄩ is no syllable: the second medial replaces the first
    assertEquals("ㄅㄧ", mComposer.composingText());
    mComposer.reset();
    type(mComposer, "ㄋㄨㄧ"); // ㄋㄨ is a syllable: ㄧ starts the next one
    assertEquals("ㄋㄨ" + SEP + "ㄧ", mComposer.composingText());
  }

  // --- edge cases ---

  @Test
  public void testNonBopomofoIsNotConsumed() {
    assertFalse(mComposer.onKey('a'));
    assertFalse(mComposer.onKey('，'));
  }

  @Test
  public void testUnknownSyllableFallsBackToBopomofo() {
    type(mComposer, "ㄅㄩ ");
    assertEquals("ㄅㄩ", mComposer.composingText());
    assertEquals("ㄅㄩ", mComposer.conversion());
  }

  @Test
  public void testLongCompositionAutoCommitsHead() {
    for (int i = 0; i <= ZhuyinComposer.MAX_SYLLABLES; i++) type(mComposer, "ㄉㄜ˙");
    assertNotEquals("", mComposer.takeCommitText());
    assertTrue(mComposer.isComposing());
  }

  @Test
  public void testCommitAllIncludesPending() {
    type(mComposer, "ㄋㄧˇㄏㄠ");
    mComposer.commitAll();
    assertEquals("你好", mComposer.takeCommitText());
  }

  @Test
  public void testResetDropsEverything() {
    type(mComposer, "ㄋㄧˇ");
    mComposer.reset();
    assertFalse(mComposer.isComposing());
    assertEquals("", mComposer.takeCommitText());
    assertTrue(mComposer.candidates().isEmpty());
  }

  // --- candidates from the first symbol ---

  @Test
  public void testFirstSymbolOffersCharactersByFrequency() {
    type(mComposer, "ㄅ");
    assertEquals("ㄅ", mComposer.composingText());
    final List<Candidate> candidates = mComposer.candidates();
    assertEquals("不", candidates.get(0).text);
    assertEquals(Candidate.Kind.PHRASE, candidates.get(0).kind);
    // The symbol itself stays available, last.
    assertEquals("ㄅ", candidates.get(candidates.size() - 1).text);
    assertEquals(Candidate.Kind.RAW, candidates.get(candidates.size() - 1).kind);
  }

  @Test
  public void testPickingForLoneSymbolCommitsIt() {
    type(mComposer, "ㄅ");
    mComposer.pick(candidate("不"));
    assertEquals("不", mComposer.takeCommitText());
    assertFalse(mComposer.isComposing());
  }

  @Test
  public void testExactReadingComesBeforeLongerOnes() {
    type(mComposer, "ㄋㄧ");
    final List<Candidate> candidates = mComposer.candidates();
    assertEquals("你", candidates.get(0).text);
    candidate("年"); // ㄋㄧㄢ: offered too
  }

  @Test
  public void testOpenSyllableCompletesPhrases() {
    type(mComposer, "ㄋㄧˇㄏ");
    assertEquals("ㄋㄧˇㄏ", mComposer.composingText());
    mComposer.pick(candidate("你好"));
    assertEquals("你好", mComposer.takeCommitText());
    assertFalse(mComposer.isComposing());
  }

  @Test
  public void testSentencePickCommitsWhatIsShown() {
    type(mComposer, "ㄐㄧㄣ ㄊㄧㄢ ㄊㄧㄢ ㄑㄧˋㄏㄣˇㄏ");
    final Candidate sentence = mComposer.candidates().get(0);
    assertEquals(Candidate.Kind.SENTENCE, sentence.kind);
    assertEquals("今天天氣很好", sentence.text);
    mComposer.pick(sentence);
    assertEquals("今天天氣很好", mComposer.takeCommitText());
    assertFalse(mComposer.isComposing());
  }

  @Test
  public void testCommitAllCommitsTheFirstCandidate() {
    type(mComposer, "ㄋㄧˇㄏ");
    final String shown = mComposer.candidates().get(0).text;
    assertEquals("你會", shown);
    mComposer.commitAll();
    assertEquals(shown, mComposer.takeCommitText());
  }

  @Test
  public void testCommitAllCompletesALoneSymbol() {
    type(mComposer, "ㄅ");
    mComposer.commitAll();
    assertEquals("不", mComposer.takeCommitText());
  }

  @Test
  public void testClosedLoneSymbolCommitsAsItself() {
    type(mComposer, "ㄋㄧˇㄏㄠˇㄅ ");
    mComposer.commitAll();
    assertEquals("你好ㄅ", mComposer.takeCommitText());
  }

  @Test
  public void testMovingTheCursorMakesALoneSymbolLiteral() {
    type(mComposer, "ㄅ");
    assertTrue(mComposer.moveCursor(-1));
    final List<Candidate> candidates = mComposer.candidates();
    assertEquals(1, candidates.size());
    assertEquals("ㄅ", candidates.get(0).text);
    assertFalse(mComposer.moveCursor(-1)); // leaving the composition: the IME commits it
    mComposer.commitAll();
    assertEquals("ㄅ", mComposer.takeCommitText());
  }

  @Test
  public void testPickForOpenSyllableInTheMiddle() {
    type(mComposer, "ㄋㄧˇㄇㄚ˙");
    assertTrue(mComposer.moveCursor(-3));
    type(mComposer, "ㄏ");
    assertEquals("ㄋㄧˇㄏㄇㄚ˙", mComposer.composingText());
    mComposer.pick(candidate("好"));
    assertEquals("ㄋㄧˇ好ㄇㄚ˙", mComposer.composingText());
    mComposer.commitAll();
    assertEquals("你好嗎", mComposer.takeCommitText());
  }

  @Test
  public void testUndoingAPickForAnOpenSyllableShowsWhatWasTyped() {
    type(mComposer, "ㄋㄧˇㄇㄚ˙");
    assertTrue(mComposer.moveCursor(-3));
    type(mComposer, "ㄏ");
    mComposer.pick(candidate("好"));
    assertTrue(mComposer.onDelete());
    assertEquals("ㄋㄧˇㄏ" + SEP + "ㄇㄚ˙", mComposer.composingText());
  }

  // --- standalone tone marks ---

  @Test
  public void testLoneToneIsNotConsumed() {
    assertFalse(mComposer.onKey('ˇ'));
    assertFalse(mComposer.onKey('˙'));
    assertFalse(mComposer.isComposing());
  }

  @Test
  public void testToneAfterClosedSyllableStillRetones() {
    type(mComposer, "ㄇㄚˇ");
    assertTrue(mComposer.onKey('˙'));
    assertEquals("ㄇㄚ˙", mComposer.composingText());
  }

  @Test
  public void testToneAtCompositionStartIsSwallowed() {
    type(mComposer, "ㄇㄚˇ");
    assertTrue(mComposer.moveCursor(-3));
    assertTrue(mComposer.onKey('ˋ'));
    assertEquals("ㄇㄚˇ", mComposer.composingText());
  }
}
