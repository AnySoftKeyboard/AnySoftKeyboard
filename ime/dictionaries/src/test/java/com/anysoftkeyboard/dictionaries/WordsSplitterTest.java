package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class WordsSplitterTest {

    private WordsSplitter mUnderTest;
    private WordComposer mWordComposer;

    @Before
    public void setUp() {
        mUnderTest = new WordsSplitter();
        mWordComposer = new WordComposer();
    }

    private void typeWord(String word, boolean... nextToSpace) {
        for (int charIndex = 0; charIndex < word.length(); charIndex++) {
            final char c = word.charAt(charIndex);
            mWordComposer.add(
                    c, nextToSpace[charIndex] ? new int[] {c, KeyCodes.SPACE} : new int[] {c});
        }
    }

    private List<List<KeyCodesProvider>> splitToLists() {
        var result = mUnderTest.split(mWordComposer);
        var lists = new ArrayList<List<KeyCodesProvider>>();
        for (var iterator : result) {
            var list = new ArrayList<KeyCodesProvider>();
            for (KeyCodesProvider keyCodesProvider : iterator) {
                list.add(keyCodesProvider);
            }
            lists.add(list);
        }

        return lists;
    }

    @Test
    public void testExampleFromComment() {
        typeWord("abcdefgh", false, false, true, false, false, true, true, false);
        var result = splitToLists();

        Assert.assertEquals(8, result.size());
        /*    [0, 8] -> [[a, b, c, d, e, f, g, h]]
         *    [0, 2, 8] -> [[a, b], [d, e, f, g, h]]
         *    [0, 5, 8] -> [[a, b, c, d, e], [g, h]]
         *    [0, 2, 5, 8] -> [[a, b], [d, e], [g, h]]
         *    [0, 6, 8] -> [[a, b, c, d, e, f], [h]]
         *    [0, 2, 6, 8] -> [[a, b], [d, e, f] , [h]]
         *    [0, 5, 6, 8] -> [[a, b, c, d, e], [h]]
         *    [0, 2, 5, 6, 8] -> [[a, b] , [d, e], [h]]
         */
        assertSplits(result.get(0), "abcdefgh");
        assertSplits(result.get(1), "ab", "defgh");
        assertSplits(result.get(2), "abcde", "gh");
        assertSplits(result.get(3), "ab", "de", "gh");
        assertSplits(result.get(4), "abcdef", "h");
        assertSplits(result.get(5), "ab", "def", "h");
        assertSplits(result.get(6), "abcde", "h");
        assertSplits(result.get(7), "ab", "de", "h");
    }

    @Test
    public void testGetEmptySubWordsWhenNoKeyIsNextToSpace() {
        typeWord("hello", false, false, false, false, false);
        var result = splitToLists();

        Assert.assertEquals(0, result.size());
    }

    private static void assertSplits(List<KeyCodesProvider> splits, String... expected) {
        Assert.assertEquals(expected.length, splits.size());
        for (int splitIndex = 0; splitIndex < splits.size(); splitIndex++) {
            Assert.assertEquals(
                    "Failed for index " + splitIndex,
                    splits.get(splitIndex).getTypedWord().toString(),
                    expected[splitIndex]);
        }
    }

    @Test
    public void testDoesNotExceedMaxSplits() {
        typeWord("abcdefgh", true, true, true, true, true, true, true, true);
        var result = splitToLists();

        // maximum 32
        Assert.assertEquals(32, result.size());
    }

    @Test
    public void testDoesNotConsiderFirstCharacterAsSpace() {
        typeWord("hello", true, false, false, false, false);
        var result = splitToLists();

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testConsiderLastCharacterAsSpace() {
        typeWord("hello", false, false, false, false, true);
        var result = splitToLists();

        Assert.assertEquals(2, result.size());
        assertSplits(result.get(0), "hello");
        assertSplits(result.get(1), "hell");
    }

    @Test
    public void testSkipEmptySplit() {
        typeWord("hello", false, false, false, false, true);
        var result = splitToLists();

        Assert.assertEquals(2, result.size());
        assertSplits(result.get(0), "hello");
        assertSplits(result.get(1), "hell");
    }

    @Test
    public void testReturnsEmptyResultIfInputTooShort() {
        Assert.assertTrue(splitToLists().isEmpty());
        typeWord("h", false);
        Assert.assertTrue(splitToLists().isEmpty());
        typeWord("e", true);
        Assert.assertFalse(splitToLists().isEmpty());
    }

    @Test
    public void testReturnsEmptyResultIfInputTooShort_2() {
        Assert.assertTrue(splitToLists().isEmpty());
        typeWord("h", true);
        Assert.assertTrue(splitToLists().isEmpty());
        typeWord("e", true);
        Assert.assertFalse(splitToLists().isEmpty());
    }

    @Test
    public void testReturnsEmptyResultIfThereAreNoSpaces() {
        Assert.assertTrue(splitToLists().isEmpty());
        typeWord("h", false);
        Assert.assertTrue(splitToLists().isEmpty());
        typeWord("e", false);
        Assert.assertTrue(splitToLists().isEmpty());
        typeWord("l", false);
        Assert.assertTrue(splitToLists().isEmpty());
    }

    @Test
    public void testReturnsEmptyResultIfThereAreNoSpacesEvenIfFirstIsSpace() {
        Assert.assertTrue(splitToLists().isEmpty());
        typeWord("h", true);
        Assert.assertTrue(splitToLists().isEmpty());
        typeWord("e", false);
        Assert.assertTrue(splitToLists().isEmpty());
        typeWord("l", false);
        Assert.assertTrue(splitToLists().isEmpty());
    }
}
