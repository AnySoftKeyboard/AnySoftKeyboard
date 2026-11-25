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

/**
 * An immutable Bopomofo syllable: four slots (initial, medial, final, tone).
 *
 * <p>The toneless part is encoded as {@link #base()} = {@code (initial * 4 + medial) * 14 + final},
 * where each slot is 0 when empty or 1 + its index in {@link #INITIALS}, {@link #MEDIALS} or {@link
 * #FINALS}. This encoding is shared with {@code build_zhuyin_phrases.py}, which writes the phrase
 * table, so it must not change without regenerating the data.
 *
 * <p>Tones: {@link #TONE_NONE} (not typed yet, matches any tone), 1 (no mark), 2 ˊ, 3 ˇ, 4 ˋ, 5 ˙.
 */
public final class Syllable {
  static final String INITIALS = "ㄅㄆㄇㄈㄉㄊㄋㄌㄍㄎㄏㄐㄑㄒㄓㄔㄕㄖㄗㄘㄙ";
  static final String MEDIALS = "ㄧㄨㄩ";
  static final String FINALS = "ㄚㄛㄜㄝㄞㄟㄠㄡㄢㄣㄤㄥㄦ";
  private static final String TONE_MARKS = "\u0000\u0000ˊˇˋ˙";

  static final int TONE_NONE = 0;
  static final int TONE_FIRST = 1;
  static final int TONE_NEUTRAL = 5;

  /** Slot kinds, in typing order. */
  static final int KIND_NONE = 0;

  static final int KIND_INITIAL = 1;
  static final int KIND_MEDIAL = 2;
  static final int KIND_FINAL = 3;
  static final int KIND_TONE = 4;

  static final Syllable EMPTY = new Syllable(0, 0, 0, TONE_NONE);

  private final int mInitial;
  private final int mMedial;
  private final int mFinal;
  private final int mTone;

  private Syllable(int initial, int medial, int fin, int tone) {
    mInitial = initial;
    mMedial = medial;
    mFinal = fin;
    mTone = tone;
  }

  /** True for the Bopomofo symbols and tone marks a syllable is made of. */
  public static boolean isBopomofo(int codePoint) {
    return kindOf(codePoint) != KIND_NONE;
  }

  /** Which slot the given code point fills, or {@link #KIND_NONE} if it is not Bopomofo. */
  static int kindOf(int codePoint) {
    if (codePoint > 0xFFFF) return KIND_NONE;
    final char c = (char) codePoint;
    if (INITIALS.indexOf(c) >= 0) return KIND_INITIAL;
    if (MEDIALS.indexOf(c) >= 0) return KIND_MEDIAL;
    if (FINALS.indexOf(c) >= 0) return KIND_FINAL;
    if (toneOf(c) != TONE_NONE) return KIND_TONE;
    return KIND_NONE;
  }

  /** The tone a tone mark stands for, or {@link #TONE_NONE}. */
  static int toneOf(char c) {
    final int index = TONE_MARKS.indexOf(c);
    return index >= 2 ? index : TONE_NONE;
  }

  /** Parses a syllable such as {@code ㄋㄧˇ} (no tone mark = first tone). Returns null if invalid. */
  static Syllable parse(CharSequence text) {
    Syllable s = EMPTY;
    for (int i = 0; i < text.length(); i++) {
      final char c = text.charAt(i);
      final int kind = kindOf(c);
      if (kind == KIND_NONE || (kind != KIND_TONE && s.highestFilledKind() >= kind)) return null;
      s = s.with(c);
    }
    if (s.isEmpty()) return null;
    return s.hasTone() ? s : s.withTone(TONE_FIRST);
  }

  /** Returns a copy with the slot of {@code codePoint} set (replacing what was there). */
  Syllable with(int codePoint) {
    final char c = (char) codePoint;
    return switch (kindOf(codePoint)) {
      case KIND_INITIAL -> new Syllable(INITIALS.indexOf(c) + 1, mMedial, mFinal, mTone);
      case KIND_MEDIAL -> new Syllable(mInitial, MEDIALS.indexOf(c) + 1, mFinal, mTone);
      case KIND_FINAL -> new Syllable(mInitial, mMedial, FINALS.indexOf(c) + 1, mTone);
      case KIND_TONE -> withTone(toneOf(c));
      default -> throw new IllegalArgumentException("Not a Bopomofo symbol: " + codePoint);
    };
  }

  Syllable withTone(int tone) {
    return new Syllable(mInitial, mMedial, mFinal, tone);
  }

  /** Removes the last filled slot (tone, final, medial, then initial). */
  Syllable withoutLastSymbol() {
    if (mTone != TONE_NONE) return withTone(TONE_NONE);
    if (mFinal != 0) return new Syllable(mInitial, mMedial, 0, TONE_NONE);
    if (mMedial != 0) return new Syllable(mInitial, 0, 0, TONE_NONE);
    return EMPTY;
  }

  /** Returns a copy with the slot of {@code symbol} emptied. */
  Syllable without(char symbol) {
    return switch (kindOf(symbol)) {
      case KIND_INITIAL -> new Syllable(0, mMedial, mFinal, mTone);
      case KIND_MEDIAL -> new Syllable(mInitial, 0, mFinal, mTone);
      case KIND_FINAL -> new Syllable(mInitial, mMedial, 0, mTone);
      case KIND_TONE -> withTone(TONE_NONE);
      default -> this;
    };
  }

  /** The highest non-tone slot kind that is filled, or {@link #KIND_NONE}. */
  int highestFilledKind() {
    if (mFinal != 0) return KIND_FINAL;
    if (mMedial != 0) return KIND_MEDIAL;
    if (mInitial != 0) return KIND_INITIAL;
    return KIND_NONE;
  }

  boolean isEmpty() {
    return mInitial == 0 && mMedial == 0 && mFinal == 0;
  }

  boolean hasTone() {
    return mTone != TONE_NONE;
  }

  int tone() {
    return mTone;
  }

  /** The initial symbol (ㄅ…ㄙ), or 0 if none. */
  char initial() {
    return mInitial == 0 ? 0 : INITIALS.charAt(mInitial - 1);
  }

  /** The medial symbol (ㄧㄨㄩ), or 0 if none. */
  char medial() {
    return mMedial == 0 ? 0 : MEDIALS.charAt(mMedial - 1);
  }

  /** The final symbol (ㄚ…ㄦ), or 0 if none. */
  char finalSymbol() {
    return mFinal == 0 ? 0 : FINALS.charAt(mFinal - 1);
  }

  Syllable withoutInitial() {
    return new Syllable(0, mMedial, mFinal, mTone);
  }

  /** True for tones 2-5 (ˊ ˇ ˋ ˙). The first tone, and no tone, have no mark. */
  boolean hasVisibleToneMark() {
    return mTone > TONE_FIRST;
  }

  /** The toneless code, see the class doc. */
  int base() {
    return (mInitial * 4 + mMedial) * 14 + mFinal;
  }

  /**
   * The largest {@link #base()} of the syllables that start with this one: typing more symbols can
   * only fill the slots after the last filled one, and those are the low part of the code. So the
   * syllables this one can still become are exactly the bases from {@link #base()} to this value.
   */
  int maxExtensionBase() {
    if (mFinal != 0) return base();
    if (mMedial != 0) return base() + FINALS.length();
    return base() + MEDIALS.length() * (FINALS.length() + 1) + FINALS.length();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(4);
    if (mInitial != 0) sb.append(INITIALS.charAt(mInitial - 1));
    if (mMedial != 0) sb.append(MEDIALS.charAt(mMedial - 1));
    if (mFinal != 0) sb.append(FINALS.charAt(mFinal - 1));
    if (mTone >= 2) sb.append(TONE_MARKS.charAt(mTone));
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Syllable other && base() == other.base() && mTone == other.mTone;
  }

  @Override
  public int hashCode() {
    return base() * 8 + mTone;
  }
}
