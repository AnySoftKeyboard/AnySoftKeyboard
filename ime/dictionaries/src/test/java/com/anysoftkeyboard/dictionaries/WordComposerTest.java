package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class WordComposerTest {

  private static void typeWord(WordComposer wordComposer, String word, boolean[] nextToSpace) {
    for (int charIndex = 0; charIndex < word.length(); charIndex++) {
      final char c = word.charAt(charIndex);
      wordComposer.add(c, nextToSpace[charIndex] ? new int[] {c, KeyCodes.SPACE} : new int[] {c});
    }
  }

  @Test
  public void testPointCountHappyPath() {
    final var underTest = new WordComposer();
    Assert.assertTrue(underTest.isEmpty());
    typeWord(underTest, "hello", new boolean[] {false, true, false, false, false});
    Assert.assertFalse(underTest.isEmpty());
    Assert.assertEquals(5, underTest.codePointCount());
    Assert.assertEquals(5, underTest.charCount());
    Assert.assertEquals(5, underTest.cursorPosition());
    Assert.assertEquals(1, underTest.deleteCodePointAtCurrentPosition());
    Assert.assertEquals(4, underTest.codePointCount());
    Assert.assertEquals(4, underTest.charCount());
    Assert.assertEquals(4, underTest.cursorPosition());

    underTest.add("\uD83D\uDE3C".codePointAt(0), new int[] {"\uD83D\uDE3C".codePointAt(0)});
    Assert.assertEquals(5, underTest.codePointCount());
    Assert.assertEquals(6, underTest.charCount());
    Assert.assertEquals(6, underTest.cursorPosition());

    Assert.assertEquals(2, underTest.deleteCodePointAtCurrentPosition());
    Assert.assertEquals(4, underTest.codePointCount());
    Assert.assertEquals(4, underTest.charCount());
    Assert.assertEquals(4, underTest.cursorPosition());

    Assert.assertArrayEquals(new int[] {'h'}, underTest.getCodesAt(0));
    Assert.assertArrayEquals(new int[] {'e', KeyCodes.SPACE}, underTest.getCodesAt(1));
  }

  @Test
  public void testComplexCodePoint() {
    final var underTest = new WordComposer();

    underTest.add('h', new int[] {'h'});
    underTest.add(68137, new int[] {68137});
    underTest.add('l', new int[] {'l'});

    Assert.assertEquals(4, underTest.charCount());
    Assert.assertEquals(3, underTest.codePointCount());

    underTest.deleteCodePointAtCurrentPosition();
    Assert.assertEquals(3, underTest.charCount());
    Assert.assertEquals(2, underTest.codePointCount());
    underTest.deleteCodePointAtCurrentPosition();
    Assert.assertEquals(1, underTest.charCount());
    Assert.assertEquals(1, underTest.codePointCount());
    underTest.deleteCodePointAtCurrentPosition();
    Assert.assertEquals(0, underTest.charCount());
    Assert.assertEquals(0, underTest.codePointCount());
  }

  @Test
  public void testSimulateTypedWord() {
    final var underTest = new WordComposer();

    underTest.simulateTypedWord("hello");
    Assert.assertEquals("hello", underTest.getTypedWord());
    Assert.assertEquals(5, underTest.charCount());
    Assert.assertEquals(5, underTest.codePointCount());
    Assert.assertEquals(5, underTest.cursorPosition());

    Assert.assertArrayEquals(new int[] {'h'}, underTest.getCodesAt(0));
    Assert.assertArrayEquals(new int[] {'o'}, underTest.getCodesAt(4));

    underTest.simulateTypedWord("there");
    Assert.assertEquals("hellothere", underTest.getTypedWord());
    Assert.assertEquals(10, underTest.charCount());
    Assert.assertEquals(10, underTest.codePointCount());
    Assert.assertEquals(10, underTest.cursorPosition());

    Assert.assertArrayEquals(new int[] {'h'}, underTest.getCodesAt(0));
    Assert.assertArrayEquals(new int[] {'o'}, underTest.getCodesAt(4));
    Assert.assertArrayEquals(new int[] {'t'}, underTest.getCodesAt(5));
    Assert.assertArrayEquals(new int[] {'e'}, underTest.getCodesAt(9));

    underTest.setCursorPosition(2);

    underTest.simulateTypedWord("wr");
    Assert.assertEquals("hewrllothere", underTest.getTypedWord());
    Assert.assertEquals(12, underTest.charCount());
    Assert.assertEquals(12, underTest.codePointCount());
    Assert.assertEquals(4, underTest.cursorPosition());

    Assert.assertArrayEquals(new int[] {'h'}, underTest.getCodesAt(0));
    Assert.assertArrayEquals(new int[] {'o'}, underTest.getCodesAt(6));
    Assert.assertArrayEquals(new int[] {'t'}, underTest.getCodesAt(7));
    Assert.assertArrayEquals(new int[] {'e'}, underTest.getCodesAt(11));
    Assert.assertArrayEquals(new int[] {'w'}, underTest.getCodesAt(2));
    Assert.assertArrayEquals(new int[] {'r'}, underTest.getCodesAt(3));
  }

  @Test
  public void testRePositionPrimaryCode() {
    final var underTest = new WordComposer();
    underTest.add('h', new int[] {'h', 'j', 'u', 'y', 't', 'g'});
    Assert.assertEquals('h', underTest.getCodesAt(0)[0]);
    Assert.assertEquals('j', underTest.getCodesAt(0)[1]);
    Assert.assertEquals('u', underTest.getCodesAt(0)[2]);
    underTest.add('u', new int[] {'h', 'j', 'u', 'y', 't', 'g'});
    Assert.assertEquals('u', underTest.getCodesAt(1)[0]);
    Assert.assertEquals('j', underTest.getCodesAt(1)[1]);
    Assert.assertEquals('h', underTest.getCodesAt(1)[2]);
    underTest.add('l', new int[] {'h', 'j', 'u', 'y', 't', 'g'});
    Assert.assertEquals('h', underTest.getCodesAt(2)[0]);
    Assert.assertEquals('j', underTest.getCodesAt(2)[1]);
    Assert.assertEquals('u', underTest.getCodesAt(2)[2]);
  }
}
