package com.anysoftkeyboard.dictionaries;

import android.text.TextUtils;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SuggestTest {

    private SuggestionsProvider mProvider;
    private Suggest mUnderTest;

    private static void typeWord(WordComposer wordComposer, String word) {
        for (int charIndex = 0; charIndex < word.length(); charIndex++) {
            final char c = word.charAt(charIndex);
            wordComposer.add(c, new int[] {c});
        }
    }

    @Before
    public void setUp() throws Exception {
        mProvider = Mockito.mock(SuggestionsProvider.class);
        mUnderTest = new SuggestImpl(mProvider);
    }

    @Test
    public void testDelegatesIncognito() {
        Mockito.verify(mProvider, Mockito.never()).setIncognitoMode(Mockito.anyBoolean());

        mUnderTest.setIncognitoMode(true);
        Mockito.doReturn(true).when(mProvider).isIncognitoMode();

        Mockito.verify(mProvider).setIncognitoMode(true);
        Mockito.verifyNoMoreInteractions(mProvider);

        Assert.assertTrue(mUnderTest.isIncognitoMode());
        //noinspection ResultOfMethodCallIgnored
        Mockito.verify(mProvider).isIncognitoMode();
        Mockito.verifyNoMoreInteractions(mProvider);
        Mockito.reset(mProvider);

        mUnderTest.setIncognitoMode(false);
        Mockito.doReturn(false).when(mProvider).isIncognitoMode();

        Mockito.verify(mProvider).setIncognitoMode(false);
        Mockito.verifyNoMoreInteractions(mProvider);

        Assert.assertFalse(mUnderTest.isIncognitoMode());
        //noinspection ResultOfMethodCallIgnored
        Mockito.verify(mProvider).isIncognitoMode();
        Mockito.verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testHasCorrectionWhenHaveCommonalitySuggestions() {
        Assert.assertTrue(TextUtils.isEmpty(null));
        Assert.assertTrue(TextUtils.isEmpty(""));
        Assert.assertFalse(TextUtils.isEmpty("gg"));
        mUnderTest.setCorrectionMode(true, 1, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            callback.addWord(
                                    "hello".toCharArray(),
                                    0,
                                    5,
                                    23,
                                    Mockito.mock(Dictionary.class));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        // since we asked for 2 minimum-length, the first letter will not be queried
        typeWord(wordComposer, "hel");
        Assert.assertEquals(2, mUnderTest.getSuggestions(wordComposer).size());
        Assert.assertFalse(mUnderTest.hasMinimalCorrection());
        typeWord(wordComposer, "l");
        Assert.assertEquals(2, mUnderTest.getSuggestions(wordComposer).size());
        Assert.assertTrue(mUnderTest.hasMinimalCorrection());
        typeWord(wordComposer, "0");
        Assert.assertEquals(2, mUnderTest.getSuggestions(wordComposer).size());
        Assert.assertTrue(mUnderTest.hasMinimalCorrection());
    }

    @Test
    public void testNeverQueriesWhenSuggestionsOff() {
        mUnderTest.setCorrectionMode(false, 5, 2, 2);
        WordComposer wordComposer = new WordComposer();
        typeWord(wordComposer, "hello");
        final List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertTrue(suggestions.isEmpty());
        Mockito.verifyZeroInteractions(mProvider);
        Assert.assertFalse(mUnderTest.hasMinimalCorrection());
    }

    @Test
    public void testQueriesWhenSuggestionsOn() {
        mUnderTest.setCorrectionMode(true, 5, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            callback.addWord(
                                    "hello".toCharArray(),
                                    0,
                                    5,
                                    23,
                                    Mockito.mock(Dictionary.class));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        // since we asked for 2 minimum-length, the first letter will not be queried
        typeWord(wordComposer, "h");
        final List<CharSequence> suggestions1 = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(1, suggestions1.size());
        Assert.assertEquals("h", suggestions1.get(0));
        Mockito.verify(mProvider, Mockito.never()).getSuggestions(Mockito.any(), Mockito.any());
        typeWord(wordComposer, "e");
        final List<CharSequence> suggestions2 = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions2.size());
        Assert.assertEquals("he", suggestions2.get(0).toString());
        Assert.assertEquals("hello", suggestions2.get(1).toString());
        Mockito.verify(mProvider).getSuggestions(Mockito.same(wordComposer), Mockito.any());
        Assert.assertSame(suggestions1, suggestions2);
    }

    @Test
    public void testHasCorrectionWhenHaveAbbreviation() {
        mUnderTest.setCorrectionMode(true, 5, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final WordComposer word = invocation.getArgument(0);
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            if (word.getTypedWord().equals("wfh")) {
                                callback.addWord(
                                        "work from home".toCharArray(),
                                        0,
                                        14,
                                        23,
                                        Mockito.mock(Dictionary.class));
                            }
                            return null;
                        })
                .when(mProvider)
                .getAbbreviations(Mockito.any(), Mockito.any());

        // we set the minimum length to 2, so first letter is not queried.
        typeWord(wordComposer, "w");
        Assert.assertEquals(1, mUnderTest.getSuggestions(wordComposer).size());
        Mockito.verify(mProvider, Mockito.never())
                .getAbbreviations(Mockito.same(wordComposer), Mockito.any());
        Assert.assertFalse(mUnderTest.hasMinimalCorrection());

        // this is the second letter, it should be queried.
        typeWord(wordComposer, "f");
        final List<CharSequence> suggestions1 = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(1, suggestions1.size());
        Assert.assertEquals("wf", suggestions1.get(0));
        Mockito.verify(mProvider).getAbbreviations(Mockito.same(wordComposer), Mockito.any());
        Assert.assertFalse(mUnderTest.hasMinimalCorrection());
        typeWord(wordComposer, "h");
        final List<CharSequence> suggestions2 = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions2.size());
        Assert.assertEquals("wfh", suggestions2.get(0).toString());
        Assert.assertEquals("work from home", suggestions2.get(1).toString());
        Mockito.verify(mProvider, Mockito.times(2))
                .getAbbreviations(Mockito.same(wordComposer), Mockito.any());
        Assert.assertSame(suggestions1, suggestions2);
        Assert.assertTrue(mUnderTest.hasMinimalCorrection());
    }
}
