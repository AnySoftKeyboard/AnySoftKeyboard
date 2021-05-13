package com.anysoftkeyboard.dictionaries;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.support.v4.util.Pair;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class InMemoryDictionaryTest {

    private InMemoryDictionary mUnderTest;
    private InMemoryDictionary mUnderTestWithTypedWord;

    @Before
    public void setup() {
        Collection<Pair<String, Integer>> pairs = new ArrayList<>();
        pairs.add(new Pair<>("word", 4));
        pairs.add(new Pair<>("hello", 5));
        pairs.add(new Pair<>("hell", 4));
        pairs.add(new Pair<>("he", 2));
        pairs.add(new Pair<>("he'll", 4));
        pairs.add(new Pair<>("AnySoftKeyboard", 15));
        mUnderTest = new InMemoryDictionary("test", getApplicationContext(), pairs, false);
        mUnderTestWithTypedWord =
                new InMemoryDictionary("test", getApplicationContext(), pairs, true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCanNotDeleteFromStorage() {
        mUnderTest.deleteWordFromStorage("word");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCanNotAddToStorage() {
        mUnderTest.addWordToStorage("wording", 3);
    }

    @Test
    public void testGetWord() {
        mUnderTest.loadDictionary();
        KeyCodesProvider word = Mockito.mock(KeyCodesProvider.class);
        Mockito.doReturn(2).when(word).codePointCount();
        Mockito.doReturn("he").when(word).getTypedWord();
        Mockito.doReturn(new int[] {'h'}).when(word).getCodesAt(Mockito.eq(0));
        Mockito.doReturn(new int[] {'e'}).when(word).getCodesAt(Mockito.eq(1));

        MyWordCallback callback = new MyWordCallback();
        mUnderTest.getSuggestions(word, callback);

        // NOTE: does not include typed word
        Assert.assertEquals("hell", callback.capturedWords.get(0));
        Assert.assertEquals("hello", callback.capturedWords.get(1));
        Assert.assertEquals("he'll", callback.capturedWords.get(2));

        Assert.assertEquals(3, callback.capturedWords.size());
    }

    @Test
    public void testGetWordWithIncluded() {
        mUnderTestWithTypedWord.loadDictionary();
        KeyCodesProvider word = Mockito.mock(KeyCodesProvider.class);
        Mockito.doReturn(2).when(word).codePointCount();
        Mockito.doReturn("he").when(word).getTypedWord();
        Mockito.doReturn(new int[] {'h'}).when(word).getCodesAt(Mockito.eq(0));
        Mockito.doReturn(new int[] {'e'}).when(word).getCodesAt(Mockito.eq(1));

        MyWordCallback callback = new MyWordCallback();
        mUnderTestWithTypedWord.getSuggestions(word, callback);

        Assert.assertEquals(4, callback.capturedWords.size());
        Assert.assertEquals("he", callback.capturedWords.get(0));
        Assert.assertEquals("hell", callback.capturedWords.get(1));
        Assert.assertEquals("hello", callback.capturedWords.get(2));
        Assert.assertEquals("he'll", callback.capturedWords.get(3));
    }

    @Test
    public void testGetWordWithCaps() {
        mUnderTest.loadDictionary();
        KeyCodesProvider word = Mockito.mock(KeyCodesProvider.class);
        Mockito.doReturn(7).when(word).codePointCount();
        Mockito.doReturn("anysoft").when(word).getTypedWord();
        Mockito.doReturn(new int[] {'a'}).when(word).getCodesAt(Mockito.eq(0));
        Mockito.doReturn(new int[] {'n'}).when(word).getCodesAt(Mockito.eq(1));
        Mockito.doReturn(new int[] {'y'}).when(word).getCodesAt(Mockito.eq(2));
        Mockito.doReturn(new int[] {'s'}).when(word).getCodesAt(Mockito.eq(3));
        Mockito.doReturn(new int[] {'o'}).when(word).getCodesAt(Mockito.eq(4));
        Mockito.doReturn(new int[] {'f'}).when(word).getCodesAt(Mockito.eq(5));
        Mockito.doReturn(new int[] {'t'}).when(word).getCodesAt(Mockito.eq(6));

        MyWordCallback callback = new MyWordCallback();
        mUnderTest.getSuggestions(word, callback);

        // NOTE: does not include typed word
        Assert.assertEquals("AnySoftKeyboard", callback.capturedWords.get(0));

        Assert.assertEquals(1, callback.capturedWords.size());
    }

    @Test
    public void testGetWordNearBy() {
        mUnderTest.loadDictionary();
        KeyCodesProvider word = Mockito.mock(KeyCodesProvider.class);
        Mockito.doReturn(7).when(word).codePointCount();
        Mockito.doReturn("anysofy").when(word).getTypedWord();
        Mockito.doReturn(new int[] {'a'}).when(word).getCodesAt(Mockito.eq(0));
        Mockito.doReturn(new int[] {'n'}).when(word).getCodesAt(Mockito.eq(1));
        Mockito.doReturn(new int[] {'y'}).when(word).getCodesAt(Mockito.eq(2));
        Mockito.doReturn(new int[] {'s'}).when(word).getCodesAt(Mockito.eq(3));
        Mockito.doReturn(new int[] {'o'}).when(word).getCodesAt(Mockito.eq(4));
        Mockito.doReturn(new int[] {'f'}).when(word).getCodesAt(Mockito.eq(5));
        Mockito.doReturn(new int[] {'y', 'u', 't', 'h'}).when(word).getCodesAt(Mockito.eq(6));

        MyWordCallback callback = new MyWordCallback();
        mUnderTest.getSuggestions(word, callback);

        // NOTE: does not include typed word
        Assert.assertEquals("AnySoftKeyboard", callback.capturedWords.get(0));

        Assert.assertEquals(1, callback.capturedWords.size());
    }

    @Test
    public void testDoesNotRegisterToObserver() {
        ContentResolver contentResolver = Mockito.mock(ContentResolver.class);
        ContentObserver observer = Mockito.mock(ContentObserver.class);
        mUnderTest.registerObserver(observer, contentResolver);
        Mockito.verifyZeroInteractions(observer, contentResolver);
    }

    private static class MyWordCallback implements Dictionary.WordCallback {

        public final ArrayList<String> capturedWords = new ArrayList<>();

        @Override
        public boolean addWord(
                char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            capturedWords.add(new String(word, wordOffset, wordLength));
            return true;
        }
    }
}
