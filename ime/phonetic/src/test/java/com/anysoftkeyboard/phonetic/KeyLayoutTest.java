package com.anysoftkeyboard.phonetic;

import static com.anysoftkeyboard.phonetic.TestTables.type;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class KeyLayoutTest {
  private static final char SEP = ZhuyinComposer.SYLLABLE_SEPARATOR;

  private static ZhuyinComposer composer(KeyLayout layout) {
    final ZhuyinComposer composer = new ZhuyinComposer(TestTables.zhuyin(), new UserModel());
    composer.setLayout(layout);
    return composer;
  }

  @Test
  public void testByName() {
    assertEquals(KeyLayout.HSU, KeyLayout.byName("hsu"));
    assertEquals(KeyLayout.ETEN26, KeyLayout.byName("eten26"));
    assertEquals(KeyLayout.STANDARD, KeyLayout.byName(null));
    assertEquals(KeyLayout.STANDARD, KeyLayout.byName("standard"));
  }

  // --- 許氏 Hsu ---

  @Test
  public void testHsuNiHao() {
    final ZhuyinComposer c = composer(KeyLayout.HSU);
    type(c, "nefhwf"); // ㄋㄧˇ ㄏㄠˇ, f = third tone
    assertEquals("ㄋㄧˇㄏㄠˇ", c.composingText());
    assertEquals("你好", c.conversion());
  }

  @Test
  public void testHsuZhBecomesJBeforeI() {
    final ZhuyinComposer c = composer(KeyLayout.HSU);
    type(c, "jey "); // j = ㄓ, but ㄓㄧ is impossible: ㄐㄧㄚ
    assertEquals("ㄐㄧㄚ", c.composingText());
    assertEquals("家", c.conversion());
  }

  @Test
  public void testHsuContextKeys() {
    final ZhuyinComposer c = composer(KeyLayout.HSU);
    type(c, "ggj"); // g = ㄍ, then ㄜ after an initial; j ends with the fourth tone
    assertEquals("ㄍㄜˋ", c.composingText());
  }

  @Test
  public void testHsuLoneInitialIsAFinal() {
    final ZhuyinComposer c = composer(KeyLayout.HSU);
    type(c, "m "); // ㄇ alone becomes ㄢ
    assertEquals("ㄢ", c.composingText());
  }

  @Test
  public void testHsuEndKeyOnEmptyIsAnInitial() {
    final ZhuyinComposer c = composer(KeyLayout.HSU);
    type(c, "dy "); // d starts with ㄉ when nothing is typed
    assertEquals("ㄉㄚ", c.composingText());
  }

  @Test
  public void testHsuTonelessNextSyllable() {
    final ZhuyinComposer c = composer(KeyLayout.HSU);
    type(c, "byby"); // ㄅㄚ, then b cannot extend it: a new syllable
    assertEquals("ㄅㄚ" + SEP + "ㄅㄚ", c.composingText());
  }

  @Test
  public void testHsuIgnoresNonLetters() {
    assertFalse(composer(KeyLayout.HSU).onKey('ㄅ'));
    assertFalse(composer(KeyLayout.HSU).onKey('1'));
    // Shift + letter: the capital passes through, so it types Latin text.
    assertFalse(composer(KeyLayout.HSU).onKey('A'));
    assertFalse(composer(KeyLayout.ETEN26).onKey('Z'));
  }

  // --- 倚天 26 ---

  @Test
  public void testEten26NiHao() {
    final ZhuyinComposer c = composer(KeyLayout.ETEN26);
    type(c, "nejhzj"); // ㄋㄧˇ ㄏㄠˇ, j = third tone
    assertEquals("ㄋㄧˇㄏㄠˇ", c.composingText());
    assertEquals("你好", c.conversion());
  }

  @Test
  public void testEten26JBecomesZhBeforeU() {
    final ZhuyinComposer c = composer(KeyLayout.ETEN26);
    type(c, "gxl "); // g = ㄐ, ㄨ makes it ㄓ; l = ㄥ after an initial
    assertEquals("ㄓㄨㄥ", c.composingText());
  }

  @Test
  public void testEten26GBecomesQBeforeIu() {
    final ZhuyinComposer c = composer(KeyLayout.ETEN26);
    type(c, "vuk"); // v = ㄍ, ㄩ makes it ㄑ; k = fourth tone
    assertEquals("ㄑㄩˋ", c.composingText());
    assertEquals("去", c.conversion());
  }

  // --- Standard keeps working with Bopomofo code points ---

  @Test
  public void testStandardIgnoresLatinLetters() {
    assertFalse(composer(KeyLayout.STANDARD).onKey('a'));
  }
}
