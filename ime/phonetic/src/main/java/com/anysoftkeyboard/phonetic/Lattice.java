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
import java.util.Collections;
import java.util.List;

/**
 * All phrases that fit over a reading sequence, and the best path through them (Viterbi).
 *
 * <p>Edge {@code (start, span)} holds the phrases covering {@code readings[start, start + span)},
 * best first, with the user bonus already applied. A reading with no phrase at all gets its
 * Bopomofo text as a fallback edge, so a path always exists.
 */
final class Lattice {
  /** Score of the Bopomofo fallback edge: worse than any real phrase. */
  static final float FALLBACK_SCORE = -40f;

  /** A phrase placed at a position. */
  static final class Node {
    final int start;
    final Phrase phrase;

    Node(int start, Phrase phrase) {
      this.start = start;
      this.phrase = phrase;
    }

    int end() {
      return start + phrase.span;
    }
  }

  private final int mSize;
  private final List<List<List<Phrase>>> mEdges; // [start][span - 1] -> phrases

  /** How many extension phrases an edge takes for the syllable that is still being typed. */
  static final int MAX_EXTENSIONS = 64;

  Lattice(List<Syllable> readings, PhraseTable table, UserModel userModel) {
    this(readings, table, userModel, -1, null);
  }

  /**
   * @param openIndex the position of the syllable that is still being typed, or -1. Edges ending
   *     there also hold the phrases whose last syllable starts with {@code open}, so candidates
   *     show up from the first symbol (ㄅ → 不 把 …).
   * @param open that syllable as typed (the reading at {@code openIndex} is how the layout would
   *     finish it).
   */
  Lattice(
      List<Syllable> readings,
      PhraseTable table,
      UserModel userModel,
      int openIndex,
      Syllable open) {
    mSize = readings.size();
    mEdges = new ArrayList<>(mSize);
    final List<Phrase> found = new ArrayList<>();
    for (int start = 0; start < mSize; start++) {
      final int maxSpan = Math.min(PhraseTable.MAX_PHRASE_SYLLABLES, mSize - start);
      final List<List<Phrase>> bySpan = new ArrayList<>(maxSpan);
      for (int span = 1; span <= maxSpan; span++) {
        found.clear();
        table.lookup(readings, start, span, found);
        final boolean extended = start + span - 1 == openIndex;
        if (extended) table.lookupExtensions(readings, start, span, open, MAX_EXTENSIONS, found);
        final List<Phrase> phrases = new ArrayList<>(found.size());
        boolean boosted = extended && found.size() > 1;
        for (Phrase phrase : found) {
          final float bonus = userModel.bonus(readings, start, span, phrase.text);
          boosted |= bonus != 0f;
          phrases.add(bonus == 0f ? phrase : phrase.withScore(phrase.score + bonus));
        }
        if (boosted) Collections.sort(phrases, (a, b) -> Float.compare(b.score, a.score));
        if (span == 1 && phrases.isEmpty()) {
          phrases.add(new Phrase(readings.get(start).toString(), 1, FALLBACK_SCORE));
        }
        bySpan.add(phrases);
      }
      mEdges.add(bySpan);
    }
  }

  int size() {
    return mSize;
  }

  /** Phrases covering {@code [start, start + span)}, best first. */
  List<Phrase> phrases(int start, int span) {
    if (start < 0 || start >= mSize || span < 1 || span > mEdges.get(start).size()) {
      return Collections.emptyList();
    }
    return mEdges.get(start).get(span - 1);
  }

  /**
   * The best-scoring segmentation. Each pin forces its phrase over its span: no other edge may
   * overlap it.
   */
  List<Node> bestPath(List<Node> pins) {
    final float[] best = new float[mSize + 1];
    final Node[] back = new Node[mSize + 1];
    java.util.Arrays.fill(best, Float.NEGATIVE_INFINITY);
    best[0] = 0f;
    for (int start = 0; start < mSize; start++) {
      if (best[start] == Float.NEGATIVE_INFINITY) continue;
      final Node pin = pinAt(pins, start);
      if (pin != null) {
        relax(best, back, pin);
        continue;
      }
      for (int span = 1; span <= mEdges.get(start).size(); span++) {
        final List<Phrase> phrases = mEdges.get(start).get(span - 1);
        if (phrases.isEmpty() || overlapsPin(pins, start, start + span)) continue;
        relax(best, back, new Node(start, phrases.get(0)));
      }
    }
    final List<Node> path = new ArrayList<>();
    for (int pos = mSize; pos > 0 && back[pos] != null; pos = back[pos].start) {
      path.add(back[pos]);
    }
    Collections.reverse(path);
    return path;
  }

  private static void relax(float[] best, Node[] back, Node node) {
    final float score = best[node.start] + node.phrase.score;
    if (score > best[node.end()]) {
      best[node.end()] = score;
      back[node.end()] = node;
    }
  }

  static Node pinAt(List<Node> pins, int start) {
    for (Node pin : pins) {
      if (pin.start == start) return pin;
    }
    return null;
  }

  static boolean overlapsPin(List<Node> pins, int start, int end) {
    for (Node pin : pins) {
      if (start < pin.end() && pin.start < end) return true;
    }
    return false;
  }
}
