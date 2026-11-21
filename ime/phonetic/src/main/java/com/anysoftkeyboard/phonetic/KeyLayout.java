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

import java.util.function.IntPredicate;

/**
 * How keys turn into Bopomofo. Given the syllable being typed and a key, a layout says what to do
 * with the syllable ({@link Edit}).
 *
 * <ul>
 *   <li>{@link #STANDARD}: keys emit Bopomofo code points (大千 and 倚天 41-key keyboards).
 *   <li>{@link #HSU} and {@link #ETEN26}: 26-key layouts. Keys emit Latin letters, and several
 *       symbols share a key, chosen by context. The rules follow libchewing's {@code hsu.rs} and
 *       {@code et26.rs}.
 * </ul>
 */
public abstract class KeyLayout {

  /** What a key does to the syllable being typed. */
  public static final class Edit {
    enum Type {
      /** Replace the pending syllable. */
      SET,
      /** Close the (toned) syllable. */
      CLOSE,
      /** Close the pending syllable without a tone, then interpret the key from scratch. */
      CLOSE_AND_RETYPE,
      /** Set the tone of the previous, already closed syllable. */
      RETONE,
    }

    final Type type;
    final Syllable syllable;
    final int tone;

    private Edit(Type type, Syllable syllable, int tone) {
      this.type = type;
      this.syllable = syllable;
      this.tone = tone;
    }

    static Edit set(Syllable s) {
      return new Edit(Type.SET, s, 0);
    }

    static Edit close(Syllable s) {
      return new Edit(Type.CLOSE, s, 0);
    }

    static Edit closeAndRetype() {
      return new Edit(Type.CLOSE_AND_RETYPE, null, 0);
    }

    static Edit retone(int tone) {
      return new Edit(Type.RETONE, null, tone);
    }
  }

  /**
   * The edit for {@code codePoint}, or null if this layout does not use the key.
   *
   * @param isKnownSyllable tells whether a toneless syllable base is a real syllable; a complete
   *     syllable followed by a key that cannot extend it is closed without a tone (tones are
   *     optional).
   */
  abstract Edit onKey(Syllable pending, int codePoint, IntPredicate isKnownSyllable);

  /** Completes a syllable that is being closed (space, commit, cursor move). */
  Syllable finish(Syllable pending) {
    return pending;
  }

  public static final KeyLayout STANDARD = new Standard();
  public static final KeyLayout HSU = new Hsu();
  public static final KeyLayout ETEN26 = new Eten26();

  /** The layout for a keyboard's {@code phoneticLayout} attribute; Standard when unknown. */
  public static KeyLayout byName(String name) {
    if ("hsu".equals(name)) return HSU;
    if ("eten26".equals(name)) return ETEN26;
    return STANDARD;
  }

  /**
   * Whether {@code symbol} starts a new syllable instead of going into {@code pending}.
   *
   * <p>Only the last typed slot can be corrected in place (ㄋ then ㄇ gives ㄇ), and only while the
   * syllable is not complete yet. A symbol for an earlier slot always starts a new syllable, even
   * after an impossible one (ㄋㄧㄛ then ㄅ must not turn into ㄅㄧㄛ).
   */
  static boolean startsNewSyllable(Syllable pending, char symbol, IntPredicate isKnownSyllable) {
    final int kind = Syllable.kindOf(symbol);
    if (pending.isEmpty() || kind == Syllable.KIND_TONE) return false;
    final int highest = pending.highestFilledKind();
    return kind < highest || (kind == highest && isKnownSyllable.test(pending.base()));
  }

  /** 大千 / 倚天: one Bopomofo symbol per key; a filled slot is replaced. */
  private static final class Standard extends KeyLayout {
    @Override
    Edit onKey(Syllable pending, int codePoint, IntPredicate isKnownSyllable) {
      final int kind = Syllable.kindOf(codePoint);
      if (kind == Syllable.KIND_NONE) return null;
      if (kind == Syllable.KIND_TONE) {
        return pending.isEmpty()
            ? Edit.retone(pending.with(codePoint).tone())
            : Edit.close(pending.with(codePoint));
      }
      if (startsNewSyllable(pending, (char) codePoint, isKnownSyllable)) {
        return Edit.closeAndRetype();
      }
      return Edit.set(pending.with(codePoint));
    }
  }

  /** Shared shape of the 26-key layouts: end keys give tones, other keys map by context. */
  private abstract static class TwentySix extends KeyLayout {
    /** The tone of an end key, or 0 if the key is not an end key. */
    abstract int endKeyTone(char key);

    /** Fixes a syllable that is closed with only its initial typed (ㄇ alone means ㄢ, …). */
    abstract Syllable finishLoneInitial(Syllable s);

    /** The symbol for {@code key} in the context of {@code pending}, or 0 if not a layout key. */
    abstract char symbolFor(char key, Syllable pending);

    /** Adjusts the initial once {@code symbol} is known to follow it (ㄓ↔ㄐ, …). */
    abstract Syllable adjustInitial(Syllable pending, char symbol);

    @Override
    Syllable finish(Syllable pending) {
      return finishLoneInitial(pending);
    }

    @Override
    Edit onKey(Syllable pending, int codePoint, IntPredicate isKnownSyllable) {
      if (codePoint < 'a' || codePoint > 'z') return null;
      final char key = (char) codePoint;
      final int tone = endKeyTone(key);
      if (tone != 0 && !pending.isEmpty()) {
        return Edit.close(finishLoneInitial(pending).withTone(tone));
      }
      final char symbol = symbolFor(key, pending);
      if (symbol == 0) return null;
      if (startsNewSyllable(pending, symbol, isKnownSyllable)) return Edit.closeAndRetype();
      return Edit.set(adjustInitial(pending, symbol).with(symbol));
    }

    static boolean hasInitialOrMedial(Syllable s) {
      return s.initial() != 0 || s.medial() != 0;
    }

    /** Replaces a lone initial by a final: the 26-key layouts share keys between the two. */
    static Syllable loneInitialAsFinal(Syllable s, char initial, char fin) {
      return s.initial() == initial ? s.withoutInitial().with(fin) : s;
    }
  }

  /** 許氏 Hsu. */
  private static final class Hsu extends TwentySix {
    @Override
    int endKeyTone(char key) {
      return switch (key) {
        case 'd' -> 2;
        case 'f' -> 3;
        case 'j' -> 4;
        case 's' -> 5;
        default -> 0;
      };
    }

    @Override
    Syllable finishLoneInitial(Syllable s) {
      Syllable out = s;
      if (s.medial() == 0 && s.finalSymbol() == 0) {
        out = swapInitial(out, 'ㄐ', 'ㄓ');
        out = swapInitial(out, 'ㄑ', 'ㄔ');
        out = swapInitial(out, 'ㄒ', 'ㄕ');
        out = loneInitialAsFinal(out, 'ㄏ', 'ㄛ');
        out = loneInitialAsFinal(out, 'ㄍ', 'ㄜ');
        out = loneInitialAsFinal(out, 'ㄇ', 'ㄢ');
        out = loneInitialAsFinal(out, 'ㄋ', 'ㄣ');
        out = loneInitialAsFinal(out, 'ㄎ', 'ㄤ');
        out = loneInitialAsFinal(out, 'ㄌ', 'ㄦ');
      }
      return fixGBeforeI(out);
    }

    @Override
    char symbolFor(char key, Syllable p) {
      final boolean ctx = hasInitialOrMedial(p);
      return switch (key) {
        case 'a' -> ctx ? 'ㄟ' : 'ㄘ';
        case 'b' -> 'ㄅ';
        case 'c' -> 'ㄕ';
        case 'd' -> 'ㄉ';
        case 'e' -> p.medial() != 0 ? 'ㄝ' : 'ㄧ';
        case 'f' -> 'ㄈ';
        case 'g' -> ctx ? 'ㄜ' : 'ㄍ';
        case 'h' -> ctx ? 'ㄛ' : 'ㄏ';
        case 'i' -> 'ㄞ';
        case 'j' -> 'ㄓ';
        case 'k' -> ctx ? 'ㄤ' : 'ㄎ';
        case 'l' -> ctx ? 'ㄥ' : 'ㄌ';
        case 'm' -> ctx ? 'ㄢ' : 'ㄇ';
        case 'n' -> ctx ? 'ㄣ' : 'ㄋ';
        case 'o' -> 'ㄡ';
        case 'p' -> 'ㄆ';
        case 'r' -> 'ㄖ';
        case 's' -> 'ㄙ';
        case 't' -> 'ㄊ';
        case 'u' -> 'ㄩ';
        case 'v' -> 'ㄔ';
        case 'w' -> 'ㄠ';
        case 'x' -> 'ㄨ';
        case 'y' -> 'ㄚ';
        case 'z' -> 'ㄗ';
        default -> 0;
      };
    }

    @Override
    Syllable adjustInitial(Syllable p, char symbol) {
      Syllable out = fixGBeforeI(p);
      final int kind = Syllable.kindOf(symbol);
      if (symbol == 'ㄨ' || (kind == Syllable.KIND_FINAL && out.medial() == 0)) {
        out = swapInitial(out, 'ㄐ', 'ㄓ');
        out = swapInitial(out, 'ㄑ', 'ㄔ');
        out = swapInitial(out, 'ㄒ', 'ㄕ');
      }
      if (symbol == 'ㄧ' || symbol == 'ㄩ') {
        out = swapInitial(out, 'ㄓ', 'ㄐ');
        out = swapInitial(out, 'ㄔ', 'ㄑ');
        out = swapInitial(out, 'ㄕ', 'ㄒ');
      }
      return out;
    }

    /** ㄍ never comes before ㄧ/ㄩ; on this layout it is ㄐ. */
    private static Syllable fixGBeforeI(Syllable s) {
      return s.initial() == 'ㄍ' && (s.medial() == 'ㄧ' || s.medial() == 'ㄩ') ? s.with('ㄐ') : s;
    }
  }

  /** 倚天 26-key. */
  private static final class Eten26 extends TwentySix {
    @Override
    int endKeyTone(char key) {
      return switch (key) {
        case 'f' -> 2;
        case 'j' -> 3;
        case 'k' -> 4;
        case 'd' -> 5;
        default -> 0;
      };
    }

    @Override
    Syllable finishLoneInitial(Syllable s) {
      if (s.medial() != 0 || s.finalSymbol() != 0) return s;
      Syllable out = swapInitial(s, 'ㄐ', 'ㄓ');
      out = swapInitial(out, 'ㄒ', 'ㄕ');
      out = loneInitialAsFinal(out, 'ㄆ', 'ㄡ');
      out = loneInitialAsFinal(out, 'ㄇ', 'ㄢ');
      out = loneInitialAsFinal(out, 'ㄋ', 'ㄣ');
      out = loneInitialAsFinal(out, 'ㄊ', 'ㄤ');
      out = loneInitialAsFinal(out, 'ㄌ', 'ㄥ');
      return loneInitialAsFinal(out, 'ㄏ', 'ㄦ');
    }

    @Override
    char symbolFor(char key, Syllable p) {
      final boolean ctx = hasInitialOrMedial(p);
      return switch (key) {
        case 'a' -> 'ㄚ';
        case 'b' -> 'ㄅ';
        case 'c' -> 'ㄒ';
        case 'd' -> 'ㄉ';
        case 'e' -> 'ㄧ';
        case 'f' -> 'ㄈ';
        case 'g' -> 'ㄐ';
        case 'h' -> ctx ? 'ㄦ' : 'ㄏ';
        case 'i' -> 'ㄞ';
        case 'j' -> 'ㄖ';
        case 'k' -> 'ㄎ';
        case 'l' -> ctx ? 'ㄥ' : 'ㄌ';
        case 'm' -> ctx ? 'ㄢ' : 'ㄇ';
        case 'n' -> ctx ? 'ㄣ' : 'ㄋ';
        case 'o' -> 'ㄛ';
        case 'p' -> ctx ? 'ㄡ' : 'ㄆ';
        case 'q' -> ctx ? 'ㄟ' : 'ㄗ';
        case 'r' -> 'ㄜ';
        case 's' -> 'ㄙ';
        case 't' -> ctx ? 'ㄤ' : 'ㄊ';
        case 'u' -> 'ㄩ';
        case 'v' -> 'ㄍ';
        case 'w' -> ctx ? 'ㄝ' : 'ㄘ';
        case 'x' -> 'ㄨ';
        case 'y' -> 'ㄔ';
        case 'z' -> 'ㄠ';
        default -> 0;
      };
    }

    @Override
    Syllable adjustInitial(Syllable p, char symbol) {
      final int kind = Syllable.kindOf(symbol);
      if (kind == Syllable.KIND_MEDIAL) {
        if (symbol == 'ㄨ') {
          return swapInitial(swapInitial(p, 'ㄐ', 'ㄓ'), 'ㄒ', 'ㄕ');
        }
        return swapInitial(p, 'ㄍ', 'ㄑ');
      }
      if (kind == Syllable.KIND_FINAL && p.medial() == 0) {
        return swapInitial(swapInitial(p, 'ㄐ', 'ㄓ'), 'ㄒ', 'ㄕ');
      }
      return p;
    }
  }

  static Syllable swapInitial(Syllable s, char from, char to) {
    return s.initial() == from ? s.with(to) : s;
  }
}
