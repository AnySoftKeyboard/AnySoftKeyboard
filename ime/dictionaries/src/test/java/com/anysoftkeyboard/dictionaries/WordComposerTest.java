package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class WordComposerTest {

    private static void typeWord(WordComposer wordComposer, String word) {
        final boolean[] noSpace = new boolean[word.length()];
        Arrays.fill(noSpace, false);
        typeWord(wordComposer, word, noSpace);
    }

    private static void typeWord(WordComposer wordComposer, String word, boolean[] nextToSpace) {
        for (int charIndex = 0; charIndex < word.length(); charIndex++) {
            final char c = word.charAt(charIndex);
            wordComposer.add(
                    c, nextToSpace[charIndex] ? new int[] {c, KeyCodes.SPACE} : new int[] {c});
        }
    }

    @Test
    public void testGetPossibleSubWordsWhenNoKeyIsNextToSpace() {
        WordComposer wordComposer = new WordComposer();
        typeWord(wordComposer, "hello");
        final List<? extends KeyCodesProvider> possibleSubWords =
                wordComposer.getPossibleSubWords();
        Assert.assertEquals(0, possibleSubWords.size());
    }

    @Test
    public void testGetPossibleSubWordsWhenLastKeyIsNextToSpace() {
        WordComposer wordComposer = new WordComposer();
        typeWord(wordComposer, "hellon", new boolean[] {false, false, false, false, false, true});
        final List<? extends KeyCodesProvider> possibleSubWords =
                wordComposer.getPossibleSubWords();
        Assert.assertEquals(1, possibleSubWords.size());
        final KeyCodesProvider helloKeysProvider = possibleSubWords.get(0);
        Assert.assertEquals("hello", helloKeysProvider.getTypedWord().toString());
        Assert.assertEquals(5, helloKeysProvider.codePointCount());
        for (int keyIndex = 0; keyIndex < 5; keyIndex++) {
            Assert.assertSame(
                    wordComposer.getCodesAt(keyIndex), helloKeysProvider.getCodesAt(keyIndex));
        }
    }

    @Test
    public void testGetPossibleSubWordsWhenFirstKeyIsNextToSpace() {
        WordComposer wordComposer = new WordComposer();
        typeWord(wordComposer, "nhello", new boolean[] {true, false, false, false, false, false});
        final List<? extends KeyCodesProvider> possibleSubWords =
                wordComposer.getPossibleSubWords();
        Assert.assertEquals(1, possibleSubWords.size());
        final KeyCodesProvider helloKeysProvider = possibleSubWords.get(0);
        Assert.assertEquals("hello", helloKeysProvider.getTypedWord().toString());
        Assert.assertEquals(5, helloKeysProvider.codePointCount());
        for (int keyIndex = 0; keyIndex < 5; keyIndex++) {
            Assert.assertSame(
                    wordComposer.getCodesAt(keyIndex + 1), helloKeysProvider.getCodesAt(keyIndex));
        }
    }

    @Test
    public void testGetPossibleSubWordsWhenTwoFirstKeyIsNextToSpace() {
        WordComposer wordComposer = new WordComposer();
        typeWord(
                wordComposer,
                "nnhello",
                new boolean[] {true, true, false, false, false, false, false});
        final List<? extends KeyCodesProvider> possibleSubWords =
                wordComposer.getPossibleSubWords();
        Assert.assertEquals(1, possibleSubWords.size());
        final KeyCodesProvider helloKeysProvider = possibleSubWords.get(0);
        Assert.assertEquals("hello", helloKeysProvider.getTypedWord().toString());
        Assert.assertEquals(5, helloKeysProvider.codePointCount());
        for (int keyIndex = 0; keyIndex < 5; keyIndex++) {
            Assert.assertSame(
                    wordComposer.getCodesAt(keyIndex + 2), helloKeysProvider.getCodesAt(keyIndex));
        }
    }

    @Test
    public void testGetPossibleSubWordsWhenKeyIsNextToSpace() {
        WordComposer wordComposer = new WordComposer();
        typeWord(
                wordComposer,
                "hellonyellow",
                new boolean[] {
                    false, false, false, false, false, true, false, false, false, false, false,
                    false
                });
        final List<? extends KeyCodesProvider> possibleSubWords =
                wordComposer.getPossibleSubWords();
        Assert.assertEquals(2, possibleSubWords.size());
        final KeyCodesProvider helloKeysProvider = possibleSubWords.get(0);
        Assert.assertEquals("hello", helloKeysProvider.getTypedWord().toString());
        Assert.assertEquals(5, helloKeysProvider.codePointCount());
        for (int keyIndex = 0; keyIndex < 5; keyIndex++) {
            Assert.assertSame(
                    wordComposer.getCodesAt(keyIndex), helloKeysProvider.getCodesAt(keyIndex));
        }
        final KeyCodesProvider yellowKeysProvider = possibleSubWords.get(1);
        Assert.assertEquals("yellow", yellowKeysProvider.getTypedWord().toString());
        Assert.assertEquals(6, yellowKeysProvider.codePointCount());
        for (int keyIndex = 0; keyIndex < 6; keyIndex++) {
            Assert.assertSame(
                    wordComposer.getCodesAt(keyIndex + 6), yellowKeysProvider.getCodesAt(keyIndex));
        }
    }

    @Test
    public void testGetPossibleSubWordsReturnsMax() {
        WordComposer wordComposer = new WordComposer();
        typeWord(
                wordComposer,
                "hellonyellownbelow",
                new boolean[] {
                    false, false, false, false, false, true, false, false, false, false, false,
                    false, true, false, false, false, false, false
                });
        final List<? extends KeyCodesProvider> possibleSubWords =
                wordComposer.getPossibleSubWords();
        Assert.assertEquals(2, possibleSubWords.size());
        final KeyCodesProvider helloKeysProvider = possibleSubWords.get(0);
        Assert.assertEquals("hello", helloKeysProvider.getTypedWord().toString());
        Assert.assertEquals(5, helloKeysProvider.codePointCount());
        for (int keyIndex = 0; keyIndex < 5; keyIndex++) {
            Assert.assertSame(
                    wordComposer.getCodesAt(keyIndex), helloKeysProvider.getCodesAt(keyIndex));
        }
        final KeyCodesProvider yellowKeysProvider = possibleSubWords.get(1);
        Assert.assertEquals("yellow", yellowKeysProvider.getTypedWord().toString());
        Assert.assertEquals(6, yellowKeysProvider.codePointCount());
        for (int keyIndex = 6; keyIndex < 12; keyIndex++) {
            Assert.assertSame(
                    wordComposer.getCodesAt(keyIndex), yellowKeysProvider.getCodesAt(keyIndex - 6));
        }
    }
}
