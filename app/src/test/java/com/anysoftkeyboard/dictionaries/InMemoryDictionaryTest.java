package com.anysoftkeyboard.dictionaries;

import android.content.ContentResolver;
import android.database.ContentObserver;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class InMemoryDictionaryTest {

    private InMemoryDictionary mUnderTest;

    @Before
    public void setup() {
        Collection<String> mWordsInDictionary = new ArrayList<>();
        mWordsInDictionary.add("word");
        mWordsInDictionary.add("hello");
        mWordsInDictionary.add("hell");
        mWordsInDictionary.add("he");
        mWordsInDictionary.add("he'll");
        mWordsInDictionary.add("AnySoftKeyboard");
        mUnderTest = new InMemoryDictionary("test", RuntimeEnvironment.application, mWordsInDictionary);
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
        Mockito.doReturn(2).when(word).length();
        Mockito.doReturn("he").when(word).getTypedWord();
        Mockito.doReturn(new int[]{'h'}).when(word).getCodesAt(Mockito.eq(0));
        Mockito.doReturn(new int[]{'e'}).when(word).getCodesAt(Mockito.eq(1));


        MyWordCallback callback = new MyWordCallback();
        mUnderTest.getWords(word, callback);

        //NOTE: does not include typed word
        Assert.assertEquals("hell", callback.capturedWords.get(0));
        Assert.assertEquals("hello", callback.capturedWords.get(1));
        Assert.assertEquals("he'll", callback.capturedWords.get(2));

        Assert.assertEquals(3, callback.capturedWords.size());
    }

    @Test
    public void testGetWordWithCaps() {
        mUnderTest.loadDictionary();
        KeyCodesProvider word = Mockito.mock(KeyCodesProvider.class);
        Mockito.doReturn(7).when(word).length();
        Mockito.doReturn("anysoft").when(word).getTypedWord();
        Mockito.doReturn(new int[]{'a'}).when(word).getCodesAt(Mockito.eq(0));
        Mockito.doReturn(new int[]{'n'}).when(word).getCodesAt(Mockito.eq(1));
        Mockito.doReturn(new int[]{'y'}).when(word).getCodesAt(Mockito.eq(2));
        Mockito.doReturn(new int[]{'s'}).when(word).getCodesAt(Mockito.eq(3));
        Mockito.doReturn(new int[]{'o'}).when(word).getCodesAt(Mockito.eq(4));
        Mockito.doReturn(new int[]{'f'}).when(word).getCodesAt(Mockito.eq(5));
        Mockito.doReturn(new int[]{'t'}).when(word).getCodesAt(Mockito.eq(6));

        MyWordCallback callback = new MyWordCallback();
        mUnderTest.getWords(word, callback);

        //NOTE: does not include typed word
        Assert.assertEquals("AnySoftKeyboard", callback.capturedWords.get(0));

        Assert.assertEquals(1, callback.capturedWords.size());
    }

    @Test
    public void testGetWordNearBy() {
        mUnderTest.loadDictionary();
        KeyCodesProvider word = Mockito.mock(KeyCodesProvider.class);
        Mockito.doReturn(7).when(word).length();
        Mockito.doReturn("anysofy").when(word).getTypedWord();
        Mockito.doReturn(new int[]{'a'}).when(word).getCodesAt(Mockito.eq(0));
        Mockito.doReturn(new int[]{'n'}).when(word).getCodesAt(Mockito.eq(1));
        Mockito.doReturn(new int[]{'y'}).when(word).getCodesAt(Mockito.eq(2));
        Mockito.doReturn(new int[]{'s'}).when(word).getCodesAt(Mockito.eq(3));
        Mockito.doReturn(new int[]{'o'}).when(word).getCodesAt(Mockito.eq(4));
        Mockito.doReturn(new int[]{'f'}).when(word).getCodesAt(Mockito.eq(5));
        Mockito.doReturn(new int[]{'y', 'u', 't', 'h'}).when(word).getCodesAt(Mockito.eq(6));

        MyWordCallback callback = new MyWordCallback();
        mUnderTest.getWords(word, callback);

        //NOTE: does not include typed word
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
        public boolean addWord(char[] word, int wordOffset, int wordLength, int frequency, Dictionary from) {
            capturedWords.add(new String(word, wordOffset, wordLength));
            return true;
        }
    }
}