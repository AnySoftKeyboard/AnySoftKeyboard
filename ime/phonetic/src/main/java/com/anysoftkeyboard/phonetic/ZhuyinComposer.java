/*
 * Copyright (c) 2026 AnySoftKeyboard contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.phonetic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sentence-level Zhuyin composition.
 *
 * <p>State: closed syllables ({@code mReadings}), the syllable being typed ({@code mPending}, which
 * sits at the cursor), the cursor and the user's pins. The cursor is a syllable position (0..n)
 * plus a symbol offset into that syllable, so the arrows walk the Bopomofo symbol by symbol. Inside
 * a syllable, a typed symbol sets its slot of that syllable and backspace removes the symbol before
 * the cursor.
 *
 * <p>One rule drives the strip. The <b>anchor</b> is the cursor when it is inside the composition,
 * otherwise the start. Candidates are the phrases starting at the anchor. Picking one pins it and
 * moves the cursor past it, and any pinned prefix is committed right away. So without moving the
 * cursor, every pick commits from the left (a character, a word, or the whole sentence). After
 * moving the cursor, a pick fixes a middle segment without committing it.
 *
 * <p>The composing text shows what is still undecided as typed Bopomofo, and pinned segments as
 * their Hanzi. The conversion is only offered in the strip until space, Enter or a pick commits it.
 * Backspace edits symbol by symbol; right after a pinned segment it un-pins it, turning the Hanzi
 * back into Bopomofo.
 *
 * <p>Typing follows the standard (大千) Bopomofo behavior. A tone key, or space for the first tone,
 * closes a syllable. A symbol for a slot that is already filled (or that comes before a filled
 * slot) replaces it, unless the syllable so far is complete. In that case the syllable is closed
 * without a tone and a new one starts, so tones may be skipped.
 */
public final class ZhuyinComposer {
  /** Longer compositions commit their first segment automatically. */
  static final int MAX_SYLLABLES = 20;

  /**
   * Shown after a syllable without a visible tone mark, so readings like ㄐㄧㄣ ㄊㄧㄢ stay apart. It is
   * only part of the composing text, never of committed text.
   */
  static final char SYLLABLE_SEPARATOR = '\u2009';

  private final PhraseTable mTable;
  private final UserModel mUserModel;
  private boolean mLearning = true;
  private KeyLayout mLayout = KeyLayout.STANDARD;

  private final List<Syllable> mReadings = new ArrayList<>();
  private final List<Lattice.Node> mPins = new ArrayList<>();
  private Syllable mPending = Syllable.EMPTY;
  private int mCursor;

  /**
   * How many symbols of {@code mReadings[mCursor]} are before the cursor; 0 when the cursor is
   * between syllables. While it is not 0 there is no pending syllable.
   */
  private int mSymbolOffset;

  private final StringBuilder mCommit = new StringBuilder();

  private Lattice mLattice;
  private List<Lattice.Node> mPath;

  public ZhuyinComposer(PhraseTable table, UserModel userModel) {
    mTable = table;
    mUserModel = userModel;
  }

  public UserModel userModel() {
    return mUserModel;
  }

  /** How keys map to Bopomofo (the keyboard's {@code phoneticLayout}). */
  public void setLayout(KeyLayout layout) {
    if (mLayout.equals(layout)) return;
    mLayout = layout;
    invalidate(); // an open syllable is read through the layout (see viewReadings)
  }

  /** Disables learning from picks (incognito). */
  public void setLearning(boolean learning) {
    mLearning = learning;
  }

  /** Handles a typed code point. Returns false if the composer does not use it. */
  public boolean onKey(int codePoint) {
    if (codePoint == ' ') return onSpace();
    if (mSymbolOffset > 0 && Syllable.isBopomofo(codePoint)) return editSyllableAtCursor(codePoint);
    final KeyLayout.Edit edit = mLayout.onKey(mPending, codePoint, mTable::isKnownSyllable);
    if (edit == null) return false;
    if (mSymbolOffset > 0) {
      // A 26-key layout's letter has no slot of its own to set: it continues after the syllable.
      mCursor++;
      mSymbolOffset = 0;
    }
    switch (edit.type) {
      case SET -> {
        mPending = edit.syllable;
        invalidate(); // the open syllable takes part in the conversion (see viewReadings)
      }
      case CLOSE -> {
        mPending = edit.syllable;
        closePending();
      }
      case CLOSE_AND_RETYPE -> {
        // The key starts the next syllable; pending is empty afterwards, so this ends.
        closePending();
        return onKey(codePoint);
      }
      case RETONE -> {
        // A tone right after a closed syllable (re)sets its tone. With nothing composing, the
        // key is not ours: the tone mark itself is typed.
        if (mCursor > 0) setReading(mCursor - 1, mReadings.get(mCursor - 1).withTone(edit.tone));
        return isComposing();
      }
    }
    return true;
  }

  /** Sets the slot of {@code symbol} in the syllable the cursor is inside of. */
  private boolean editSyllableAtCursor(int symbol) {
    final Syllable edited = mReadings.get(mCursor).with(symbol);
    setReading(mCursor, edited);
    final String text = edited.toString();
    mSymbolOffset = text.indexOf((char) symbol) + 1;
    normalizeCursor();
    return true;
  }

  /** A cursor after the last symbol of a syllable is the position before the next one. */
  private void normalizeCursor() {
    if (mCursor < mReadings.size() && mSymbolOffset >= mReadings.get(mCursor).toString().length()) {
      mCursor++;
      mSymbolOffset = 0;
    }
  }

  private boolean onSpace() {
    if (!mPending.isEmpty()) {
      mPending = mPending.withTone(Syllable.TONE_FIRST);
      closePending();
      return true;
    }
    if (mReadings.isEmpty()) return false;
    commitAll();
    return true;
  }

  /** Backspace. Returns false when there is nothing to delete (the IME then deletes normally). */
  public boolean onDelete() {
    if (!mPending.isEmpty()) {
      mPending = withoutLastVisibleSymbol(mPending);
      invalidate();
      return true;
    }
    if (mReadings.isEmpty()) return false;
    if (mSymbolOffset > 0) {
      // Inside a syllable: remove the symbol before the cursor.
      final Syllable syllable = mReadings.get(mCursor);
      final Syllable edited = syllable.without(syllable.toString().charAt(mSymbolOffset - 1));
      mSymbolOffset--;
      if (edited.isEmpty()) {
        mReadings.remove(mCursor);
        shiftPins(mCursor, -1);
        invalidate();
      } else {
        setReading(mCursor, edited);
      }
      return true;
    }
    if (mCursor == 0) return true; // nothing before the cursor inside the composition
    final Lattice.Node pin = pinCovering(mCursor - 1);
    if (pin != null) {
      // Undo the pick: the Hanzi turns back into its Bopomofo, and the pick is not remembered.
      mPins.remove(pin);
      if (mLearning) mUserModel.forget(mReadings, pin.start, pin.phrase.span, pin.phrase.text);
      invalidate();
      return true;
    }
    // Re-open the syllable before the cursor for editing, minus its last symbol.
    mCursor--;
    final Syllable reopened = mReadings.remove(mCursor);
    shiftPins(mCursor, -1);
    mPending = withoutLastVisibleSymbol(reopened);
    invalidate();
    return true;
  }

  /** The first tone has no mark, so deleting it would look like nothing happened. */
  private static Syllable withoutLastVisibleSymbol(Syllable syllable) {
    final Syllable s =
        syllable.tone() == Syllable.TONE_FIRST ? syllable.withTone(Syllable.TONE_NONE) : syllable;
    return s.withoutLastSymbol();
  }

  /** Moves the cursor by {@code delta} typed symbols. Returns false at the edges. */
  public boolean moveCursor(int delta) {
    if (!mPending.isEmpty()) closePending();
    if (mReadings.isEmpty()) return false;
    for (int step = 0; step < Math.abs(delta); step++) {
      if (!(delta < 0 ? stepLeft() : stepRight())) return false;
    }
    return true;
  }

  /** One symbol to the left; a picked segment (shown as Hanzi) counts one step per character. */
  private boolean stepLeft() {
    if (mSymbolOffset > 0) {
      mSymbolOffset--;
      return true;
    }
    if (mCursor == 0) return false;
    mCursor--;
    if (pinCovering(mCursor) == null) {
      mSymbolOffset = mReadings.get(mCursor).toString().length() - 1;
    }
    return true;
  }

  private boolean stepRight() {
    if (mCursor >= mReadings.size()) return false;
    if (pinCovering(mCursor) != null) {
      mCursor++;
    } else {
      mSymbolOffset++;
      normalizeCursor();
    }
    return true;
  }

  /** Picks a candidate from {@link #candidates()}. */
  public void pick(Candidate candidate) {
    if (candidate.kind == Candidate.Kind.SENTENCE) {
      commitAll();
      return;
    }
    final int anchor = anchor();
    // Candidates may cover the open syllable (see viewReadings): close it, so the readings match
    // the view the candidate came from. Nothing is auto-committed here, which would move the
    // anchor; the pick shortens the composition anyway.
    if (!mPending.isEmpty()) insertPending();
    final Phrase phrase = candidate.phrase;
    if (anchor + phrase.span > mReadings.size()) return;
    mPins.removeIf(pin -> pin.overlaps(anchor, anchor + phrase.span));
    mPins.add(new Lattice.Node(anchor, phrase));
    if (mLearning && candidate.kind != Candidate.Kind.RAW) {
      mUserModel.learn(mReadings, anchor, phrase.span, phrase.text);
    }
    if (mCursor < mReadings.size()) {
      mCursor = anchor + phrase.span;
      mSymbolOffset = 0;
    }
    invalidate();
    flushPinnedPrefix();
  }

  /**
   * Commits the whole composition as its best conversion, the first candidate (Enter, punctuation,
   * keyboard switch, field change).
   */
  public void commitAll() {
    // The conversion as the strip shows it first. A syllable that is still open is completed
    // (我想ㄑ → 我想去, ㄅ → 不); to keep it as typed, close it first (space, a cursor move).
    for (Lattice.Node node : path()) mCommit.append(node.phrase.text);
    clear();
  }

  /** Drops the composition without committing anything. */
  public void reset() {
    clear();
  }

  public boolean isComposing() {
    return !mReadings.isEmpty() || !mPending.isEmpty();
  }

  /** Text committed by the last call(s); empty if none. Cleared by this call. */
  public String takeCommitText() {
    final String text = mCommit.toString();
    mCommit.setLength(0);
    return text;
  }

  /** What the field shows while composing: Bopomofo, and Hanzi for picked segments. */
  public String composingText() {
    final StringBuilder sb = new StringBuilder();
    render(sb);
    return sb.toString();
  }

  /** The cursor's char offset in {@link #composingText()}. */
  public int cursorCharOffset() {
    return render(new StringBuilder());
  }

  /** The best conversion of the whole composition (what {@link #commitAll()} would commit). */
  String conversion() {
    final StringBuilder sb = new StringBuilder();
    for (Lattice.Node node : path()) sb.append(node.phrase.text);
    return sb.toString();
  }

  /**
   * Appends the composing text to {@code sb}: Bopomofo for unpinned syllables, Hanzi for pins, and
   * the pending syllable at the cursor. Returns the cursor's char offset.
   */
  private int render(StringBuilder sb) {
    int cursorOffset = -1;
    int i = 0;
    while (i <= mReadings.size()) {
      if (i == mCursor && cursorOffset < 0) {
        sb.append(mPending);
        cursorOffset = sb.length();
      }
      if (i == mReadings.size()) break;
      final Lattice.Node pin = Lattice.pinAt(mPins, i);
      if (pin != null) {
        sb.append(pin.phrase.text);
        // A cursor inside a pin is shown right after it.
        if (mCursor > i && mCursor < pin.end()) {
          sb.append(mPending);
          cursorOffset = sb.length();
        }
        i = pin.end();
      } else {
        final Syllable reading = mReadings.get(i);
        if (i == mCursor && mSymbolOffset > 0) cursorOffset = sb.length() + mSymbolOffset;
        sb.append(reading);
        i++;
        final boolean somethingFollows =
            i < mReadings.size() || (i == mCursor && !mPending.isEmpty());
        if (!reading.hasVisibleToneMark() && somethingFollows) sb.append(SYLLABLE_SEPARATOR);
      }
    }
    return cursorOffset < 0 ? sb.length() : cursorOffset;
  }

  /** The strip, best first: the sentence, phrases starting at the anchor, the raw Bopomofo. */
  public List<Candidate> candidates() {
    final List<Candidate> candidates = new ArrayList<>();
    final List<Syllable> readings = viewReadings();
    final int size = readings.size();
    if (size == 0) return candidates;
    final int anchor = anchor();
    final String raw = readings.get(anchor).toString();
    final Lattice lattice = lattice();
    final Set<String> seen = new HashSet<>();
    final List<Lattice.Node> path = path();
    if (anchor == 0 && path.size() > 1) {
      final StringBuilder sentence = new StringBuilder();
      for (Lattice.Node node : path) sentence.append(node.phrase.text);
      candidates.add(new Candidate(sentence.toString(), Candidate.Kind.SENTENCE, null));
      seen.add(sentence.toString());
    }
    for (int span = Math.min(PhraseTable.MAX_PHRASE_SYLLABLES, size - anchor); span >= 1; span--) {
      if (crossesPin(anchor, anchor + span)) continue;
      for (Phrase phrase : lattice.phrases(anchor, span)) {
        // The Bopomofo fallback of a reading without phrases is the raw entry below.
        if (span == 1 && phrase.text.equals(raw)) continue;
        if (seen.add(phrase.text)) {
          candidates.add(new Candidate(phrase.text, Candidate.Kind.PHRASE, phrase));
        }
      }
    }
    // Last: the syllable's own Bopomofo, so the symbols themselves can be typed (ㄅ, ㄋㄧˇ).
    if (!crossesPin(anchor, anchor + 1)) {
      candidates.add(
          new Candidate(raw, Candidate.Kind.RAW, new Phrase(raw, 1, Lattice.FALLBACK_SCORE)));
    }
    return candidates;
  }

  /** Where the strip's candidates start: the cursor inside the composition, else the start. */
  private int anchor() {
    return mCursor < mReadings.size() ? mCursor : 0;
  }

  private Lattice.Node pinCovering(int index) {
    for (Lattice.Node pin : mPins) {
      if (pin.overlaps(index, index + 1)) return pin;
    }
    return null;
  }

  private boolean crossesPin(int start, int end) {
    for (Lattice.Node pin : viewPins()) {
      if (pin.overlaps(start, end) && !(pin.start == start && pin.end() == end)) return true;
    }
    return false;
  }

  /** Closes the pending syllable; a composition that got too long commits its first segment. */
  private void closePending() {
    insertPending();
    if (mReadings.size() > MAX_SYLLABLES) commitFirstSegment();
  }

  /** Turns the pending syllable into a reading at the cursor, and steps over it. */
  private void insertPending() {
    mReadings.add(mCursor, mLayout.finish(mPending));
    shiftPins(mCursor, +1);
    mCursor++;
    mPending = Syllable.EMPTY;
    invalidate();
  }

  private void setReading(int index, Syllable syllable) {
    mReadings.set(index, syllable);
    mPins.removeIf(pin -> pin.overlaps(index, index + 1));
    invalidate();
  }

  /** Moves {@link #mPins} for a change of the readings, see {@link #shiftedPins}. */
  private void shiftPins(int index, int delta) {
    final List<Lattice.Node> shifted = shiftedPins(index, delta);
    mPins.clear();
    mPins.addAll(shifted);
  }

  /**
   * The pins as they are after {@code delta} readings are inserted at {@code index} (or, when
   * negative, removed from it on): pins before the change stay, pins after it move, pins over it
   * are dropped.
   */
  private List<Lattice.Node> shiftedPins(int index, int delta) {
    final List<Lattice.Node> shifted = new ArrayList<>(mPins.size());
    for (Lattice.Node pin : mPins) {
      if (pin.end() <= index) {
        shifted.add(pin);
      } else if (pin.start >= index + Math.max(0, -delta)) {
        shifted.add(new Lattice.Node(pin.start + delta, pin.phrase));
      }
    }
    return shifted;
  }

  private void flushPinnedPrefix() {
    Lattice.Node head;
    while ((head = Lattice.pinAt(mPins, 0)) != null) {
      dropPrefix(head);
    }
  }

  private void commitFirstSegment() {
    final List<Lattice.Node> path = path();
    if (!path.isEmpty()) dropPrefix(path.get(0));
  }

  /** Commits {@code head} (which starts at 0) and removes its readings. */
  private void dropPrefix(Lattice.Node head) {
    final int span = head.phrase.span;
    mCommit.append(head.phrase.text);
    mReadings.subList(0, span).clear();
    shiftPins(0, -span);
    if (mCursor < span) mSymbolOffset = 0;
    mCursor = Math.max(0, mCursor - span);
    invalidate();
  }

  private void clear() {
    mReadings.clear();
    mPins.clear();
    mPending = Syllable.EMPTY;
    mCursor = 0;
    mSymbolOffset = 0;
    invalidate();
  }

  private void invalidate() {
    mLattice = null;
    mPath = null;
  }

  private Lattice lattice() {
    if (mLattice == null) {
      mLattice =
          new Lattice(
              viewReadings(), mTable, mUserModel, mPending.isEmpty() ? -1 : mCursor, mPending);
    }
    return mLattice;
  }

  private List<Lattice.Node> path() {
    if (mPath == null) mPath = lattice().bestPath(viewPins());
    return mPath;
  }

  /**
   * The readings the conversion works on: the closed ones plus the syllable that is being typed.
   * That one is converted as if it were closed without a tone, and also as every syllable it can
   * still become (ㄅ → 不 把 …, ㄐㄧ → 幾 and 家), so candidates show up from the first symbol while it
   * stays open. This is only a view: once closed (space, a tone, a cursor move) the syllable is
   * exactly what was typed, so a closed lone ㄅ commits as ㄅ.
   */
  private List<Syllable> viewReadings() {
    if (mPending.isEmpty()) return mReadings;
    final List<Syllable> view = new ArrayList<>(mReadings);
    view.add(mCursor, mLayout.finish(mPending));
    return view;
  }

  /** {@link #mPins} in {@link #viewReadings()} positions. */
  private List<Lattice.Node> viewPins() {
    return mPending.isEmpty() ? mPins : shiftedPins(mCursor, +1);
  }
}
