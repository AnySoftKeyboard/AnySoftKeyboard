package com.anysoftkeyboard.phonetic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SyllableTest {

  @Test
  public void testParseAndToStringRoundTrip() {
    for (String text : new String[] {"ㄋㄧˇ", "ㄏㄠˇ", "ㄇㄚ˙", "ㄓ", "ㄦˊ", "ㄐㄩㄢˋ", "ㄨㄛˇ"}) {
      assertEquals(text, Syllable.parse(text).toString());
    }
  }

  @Test
  public void testParseWithoutMarkIsFirstTone() {
    assertEquals(Syllable.TONE_FIRST, Syllable.parse("ㄇㄚ").tone());
  }

  @Test
  public void testParseRejectsInvalid() {
    assertNull(Syllable.parse(""));
    assertNull(Syllable.parse("ˇ"));
    assertNull(Syllable.parse("ㄧㄋ")); // medial before initial
    assertNull(Syllable.parse("a"));
  }

  @Test
  public void testBaseEncodingMatchesDataScript() {
    // (initial * 4 + medial) * 14 + final; ㄋ=7, ㄧ=1, no final.
    assertEquals((7 * 4 + 1) * 14, Syllable.parse("ㄋㄧˇ").base());
    // ㄏ=11, ㄠ=7
    assertEquals((11 * 4) * 14 + 7, Syllable.parse("ㄏㄠˇ").base());
  }

  @Test
  public void testWithReplacesSlot() {
    final Syllable s = Syllable.EMPTY.with('ㄋ').with('ㄇ');
    assertEquals("ㄇ", s.toString());
  }

  @Test
  public void testWithoutLastSymbol() {
    Syllable s = Syllable.parse("ㄐㄩㄢˋ");
    s = s.withoutLastSymbol();
    assertEquals("ㄐㄩㄢ", s.toString());
    assertFalse(s.hasTone());
    assertEquals("ㄐㄩ", s.withoutLastSymbol().toString());
    assertEquals("ㄐ", s.withoutLastSymbol().withoutLastSymbol().toString());
    assertTrue(s.withoutLastSymbol().withoutLastSymbol().withoutLastSymbol().isEmpty());
  }

  @Test
  public void testKindOf() {
    assertEquals(Syllable.KIND_INITIAL, Syllable.kindOf('ㄅ'));
    assertEquals(Syllable.KIND_MEDIAL, Syllable.kindOf('ㄩ'));
    assertEquals(Syllable.KIND_FINAL, Syllable.kindOf('ㄦ'));
    assertEquals(Syllable.KIND_TONE, Syllable.kindOf('˙'));
    assertEquals(Syllable.KIND_NONE, Syllable.kindOf('a'));
    assertEquals(Syllable.KIND_NONE, Syllable.kindOf(' '));
  }

  @Test
  public void testMaxExtensionBase() {
    // A final is the last slot: nothing can follow.
    final Syllable full = Syllable.parse("ㄋㄧㄢ");
    assertEquals(full.base(), full.maxExtensionBase());
    // ㄋㄧ can become ㄋㄧㄚ … ㄋㄧㄦ, but not ㄋㄨ.
    final Syllable ni = Syllable.EMPTY.with('ㄋ').with('ㄧ');
    assertEquals(Syllable.parse("ㄋㄧㄦ").base(), ni.maxExtensionBase());
    assertTrue(Syllable.EMPTY.with('ㄋ').with('ㄨ').base() > ni.maxExtensionBase());
    // ㄋ can become anything up to ㄋㄩㄦ, but not ㄌ.
    final Syllable n = Syllable.EMPTY.with('ㄋ');
    assertEquals(Syllable.parse("ㄋㄩㄦ").base(), n.maxExtensionBase());
    assertTrue(Syllable.EMPTY.with('ㄌ').base() > n.maxExtensionBase());
  }
}
