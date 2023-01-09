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
            wordComposer.add(
                    c, nextToSpace[charIndex] ? new int[] {c, KeyCodes.SPACE} : new int[] {c});
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
}
