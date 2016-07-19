package com.anysoftkeyboard.nextword;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Iterator;

@RunWith(RobolectricTestRunner.class)
public class NextWordDictionaryTest {
    private NextWordDictionary mNextWordDictionaryUnderTest;

    @Before
    public void setup() {
        mNextWordDictionaryUnderTest = new NextWordDictionary(RuntimeEnvironment.application, "en");
    }

    @Test
    public void testLoadEmpty() throws Exception {
        mNextWordDictionaryUnderTest.load();

        Assert.assertEquals(0, mNextWordDictionaryUnderTest.dumpDictionaryStatistics().firstWordCount);
        Assert.assertEquals(0, mNextWordDictionaryUnderTest.dumpDictionaryStatistics().secondWordCount);

        mNextWordDictionaryUnderTest.close();
    }

    @Test
    public void testLyrics() throws Exception {
        mNextWordDictionaryUnderTest.load();

        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "hello");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "is");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "it");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "me");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "you");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "looking");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "for");

        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "hello", "is");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "is", "it");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "it", "me");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "me", "you");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "you", "looking");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "looking", "for");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "for", "hello");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "bye");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "for", "hello", "bye");

        Assert.assertEquals(8, mNextWordDictionaryUnderTest.dumpDictionaryStatistics().firstWordCount);
        Assert.assertEquals(9, mNextWordDictionaryUnderTest.dumpDictionaryStatistics().secondWordCount);

        mNextWordDictionaryUnderTest.close();
    }

    @Test
    public void testResetSentence() throws Exception {
        mNextWordDictionaryUnderTest.load();

        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "hello");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "menny");
        mNextWordDictionaryUnderTest.resetSentence();
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "hello", "menny");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "menny");

        mNextWordDictionaryUnderTest.close();
    }

    @Test
    public void testLoadAgain() throws Exception {
        mNextWordDictionaryUnderTest.load();

        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "hello");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "menny");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "hello", "menny");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "menny", "hello");

        Assert.assertEquals(2, mNextWordDictionaryUnderTest.dumpDictionaryStatistics().firstWordCount);
        Assert.assertEquals(2, mNextWordDictionaryUnderTest.dumpDictionaryStatistics().secondWordCount);

        mNextWordDictionaryUnderTest.close();
        mNextWordDictionaryUnderTest = null;

        NextWordDictionary loadedDictionary = new NextWordDictionary(RuntimeEnvironment.application, "en");
        loadedDictionary.load();

        assertHasNextWordsForWord(loadedDictionary, "hello", "menny");
        assertHasNextWordsForWord(loadedDictionary, "menny", "hello");

        Assert.assertEquals(2, loadedDictionary.dumpDictionaryStatistics().firstWordCount);
        Assert.assertEquals(2, loadedDictionary.dumpDictionaryStatistics().secondWordCount);

        loadedDictionary.close();
    }

    @Test
    public void testClearData() throws Exception {
        mNextWordDictionaryUnderTest.load();

        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "hello");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "menny");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "hello", "menny");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "menny", "hello");
        mNextWordDictionaryUnderTest.clearData();
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "hello");
        assertHasNextWordsForWord(mNextWordDictionaryUnderTest, "menny");

        mNextWordDictionaryUnderTest.close();
    }

    private static void assertHasNextWordsForWord(NextWordDictionary nextWordDictionaryUnderTest, String word, String... expectedNextWords) throws Exception {
        Iterator<String> nextWordsIterator = nextWordDictionaryUnderTest.getNextWords(word, 8, 0).iterator();
        for (String expectedNextWord : expectedNextWords) {
            Assert.assertTrue(nextWordsIterator.hasNext());
            Assert.assertEquals(expectedNextWord, nextWordsIterator.next());
        }
        Assert.assertFalse(nextWordsIterator.hasNext());
    }
}
