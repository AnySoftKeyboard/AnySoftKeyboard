package com.anysoftkeyboard.dictionaries;

import androidx.core.util.Pair;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SuggestTest {

    private SuggestionsProvider mProvider;
    private Suggest mUnderTest;

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
        Mockito.verify(mProvider).isIncognitoMode();
        Mockito.verifyNoMoreInteractions(mProvider);
        Mockito.reset(mProvider);

        mUnderTest.setIncognitoMode(false);
        Mockito.doReturn(false).when(mProvider).isIncognitoMode();

        Mockito.verify(mProvider).setIncognitoMode(false);
        Mockito.verifyNoMoreInteractions(mProvider);

        Assert.assertFalse(mUnderTest.isIncognitoMode());
        Mockito.verify(mProvider).isIncognitoMode();
        Mockito.verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testHasCorrectionWhenHaveCommonalitySuggestions() {
        mUnderTest.setCorrectionMode(true, 1, 2);
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
        // no close correction
        Assert.assertEquals(-1, mUnderTest.getLastValidSuggestionIndex());
        typeWord(wordComposer, "l");
        Assert.assertEquals(2, mUnderTest.getSuggestions(wordComposer).size());
        // we have a close correction for you at index 1
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());
        typeWord(wordComposer, "o");
        // the same word typed as received from the dictionary, so pruned.
        Assert.assertEquals(1, mUnderTest.getSuggestions(wordComposer).size());
        // the typed word is valid and is at index 0
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testDoesNotSuggestFixWhenLengthIsOne() {
        mUnderTest.setCorrectionMode(true, 1, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            callback.addWord(
                                    "he".toCharArray(), 0, 2, 23, Mockito.mock(Dictionary.class));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        typeWord(wordComposer, "h");
        List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("h", suggestions.get(0).toString());
        Assert.assertEquals("he", suggestions.get(1).toString());
        Assert.assertEquals(-1, mUnderTest.getLastValidSuggestionIndex());

        typeWord(wordComposer, "e");
        suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(1, suggestions.size());
        Assert.assertEquals("he", suggestions.get(0).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testPrefersValidTypedToSuggestedFix() {
        mUnderTest.setCorrectionMode(true, 1, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            callback.addWord(
                                    "works".toCharArray(),
                                    0,
                                    5,
                                    23,
                                    Mockito.mock(Dictionary.class));
                            callback.addWord(
                                    "Works".toCharArray(),
                                    0,
                                    5,
                                    23,
                                    Mockito.mock(Dictionary.class));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        typeWord(wordComposer, "works");
        List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("works", suggestions.get(0).toString());
        Assert.assertEquals("Works", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());

        Mockito.doAnswer(
                        invocation -> {
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            callback.addWord(
                                    "Works".toCharArray(),
                                    0,
                                    5,
                                    50,
                                    Mockito.mock(Dictionary.class));
                            callback.addWord(
                                    "works".toCharArray(),
                                    0,
                                    5,
                                    23,
                                    Mockito.mock(Dictionary.class));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());
        suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("works", suggestions.get(0).toString());
        Assert.assertEquals("Works", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());

        wordComposer.reset();
        typeWord(wordComposer, "Works");
        suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("Works", suggestions.get(0).toString());
        Assert.assertEquals("works", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());

        wordComposer.reset();
        typeWord(wordComposer, "eorks");
        suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals("eorks", suggestions.get(0).toString());
        Assert.assertEquals("Works", suggestions.get(1).toString());
        Assert.assertEquals("works", suggestions.get(2).toString());
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testNeverQueriesWhenSuggestionsOff() {
        mUnderTest.setCorrectionMode(false, 5, 2);
        WordComposer wordComposer = new WordComposer();
        typeWord(wordComposer, "hello");
        final List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertTrue(suggestions.isEmpty());
        Mockito.verifyZeroInteractions(mProvider);
        Assert.assertEquals(-1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testQueriesWhenSuggestionsOn() {
        mUnderTest.setCorrectionMode(true, 5, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
                            if (word.codePointCount() > 1) {
                                final Dictionary.WordCallback callback = invocation.getArgument(1);
                                callback.addWord(
                                        "hello".toCharArray(),
                                        0,
                                        5,
                                        23,
                                        Mockito.mock(Dictionary.class));
                            }
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        // since we asked for 2 minimum-length, the first letter will not be queried
        typeWord(wordComposer, "h");
        final List<CharSequence> suggestions1 = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(1, suggestions1.size());
        Assert.assertEquals("h", suggestions1.get(0));
        Mockito.verify(mProvider).getSuggestions(Mockito.any(), Mockito.any());
        typeWord(wordComposer, "e");
        final List<CharSequence> suggestions2 = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions2.size());
        Assert.assertEquals("he", suggestions2.get(0).toString());
        Assert.assertEquals("hello", suggestions2.get(1).toString());
        Mockito.verify(mProvider, Mockito.times(2))
                .getSuggestions(Mockito.same(wordComposer), Mockito.any());
        Assert.assertSame(suggestions1, suggestions2);
    }

    @Test
    public void testHasCorrectionWhenHaveAbbreviation() {
        mUnderTest.setCorrectionMode(true, 5, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
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

        typeWord(wordComposer, "w");
        Assert.assertEquals(1, mUnderTest.getSuggestions(wordComposer).size());
        Mockito.verify(mProvider).getAbbreviations(Mockito.same(wordComposer), Mockito.any());
        Assert.assertEquals(-1, mUnderTest.getLastValidSuggestionIndex());

        // this is the second letter, it should be queried.
        typeWord(wordComposer, "f");
        final List<CharSequence> suggestions1 = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(1, suggestions1.size());
        Assert.assertEquals("wf", suggestions1.get(0));
        Mockito.verify(mProvider, Mockito.times(2))
                .getAbbreviations(Mockito.same(wordComposer), Mockito.any());
        Assert.assertEquals(-1, mUnderTest.getLastValidSuggestionIndex());
        typeWord(wordComposer, "h");
        final List<CharSequence> suggestions2 = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions2.size());
        Assert.assertEquals("wfh", suggestions2.get(0).toString());
        Assert.assertEquals("work from home", suggestions2.get(1).toString());
        Mockito.verify(mProvider, Mockito.times(3))
                .getAbbreviations(Mockito.same(wordComposer), Mockito.any());
        Assert.assertSame(suggestions1, suggestions2);
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testAbbreviationsOverTakeDictionarySuggestions() {
        mUnderTest.setCorrectionMode(true, 5, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            if (word.getTypedWord().equals("hate")) {
                                callback.addWord(
                                        "love".toCharArray(),
                                        0,
                                        4,
                                        23,
                                        Mockito.mock(Dictionary.class));
                            }
                            return null;
                        })
                .when(mProvider)
                .getAbbreviations(Mockito.any(), Mockito.any());
        Mockito.doAnswer(
                        invocation -> {
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            callback.addWord(
                                    "hate".toCharArray(), 0, 4, 23, Mockito.mock(Dictionary.class));
                            callback.addWord(
                                    "gate".toCharArray(), 0, 4, 25, Mockito.mock(Dictionary.class));
                            callback.addWord(
                                    "bate".toCharArray(), 0, 4, 20, Mockito.mock(Dictionary.class));
                            callback.addWord(
                                    "date".toCharArray(), 0, 4, 50, Mockito.mock(Dictionary.class));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        typeWord(wordComposer, "hat");
        final InOrder inOrder = Mockito.inOrder(mProvider);
        final List<CharSequence> suggestions1 = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(5, suggestions1.size());
        Assert.assertEquals("hat", suggestions1.get(0).toString());
        Assert.assertEquals("date", suggestions1.get(1).toString());
        Assert.assertEquals("gate", suggestions1.get(2).toString());
        Assert.assertEquals("hate", suggestions1.get(3).toString());
        Assert.assertEquals("bate", suggestions1.get(4).toString());
        // ensuring abbr are called first, so the max-suggestions will not hide the exploded abbr.
        inOrder.verify(mProvider).getAbbreviations(Mockito.same(wordComposer), Mockito.any());
        inOrder.verify(mProvider).getSuggestions(Mockito.same(wordComposer), Mockito.any());
        // suggesting "hate" as a correction (from dictionary)
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());

        // hate should be converted to love
        typeWord(wordComposer, "e");
        final List<CharSequence> suggestions2 = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(5, suggestions2.size());
        Assert.assertEquals("hate", suggestions2.get(0).toString());
        Assert.assertEquals("love", suggestions2.get(1).toString());
        Assert.assertEquals("date", suggestions2.get(2).toString());
        Assert.assertEquals("gate", suggestions2.get(3).toString());
        Assert.assertEquals("bate", suggestions2.get(4).toString());
        inOrder.verify(mProvider).getAbbreviations(Mockito.same(wordComposer), Mockito.any());
        inOrder.verify(mProvider).getSuggestions(Mockito.same(wordComposer), Mockito.any());
        // suggestion "love" as a correction (abbr)
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testAutoTextIsQueriedEvenWithOneLetter() {
        mUnderTest.setCorrectionMode(true, 5, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            if (word.getTypedWord().equals("i")) {
                                callback.addWord("I".toCharArray(), 0, 1, 23, null);
                            }
                            return null;
                        })
                .when(mProvider)
                .getAutoText(Mockito.any(), Mockito.any());

        typeWord(wordComposer, "i");
        List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        InOrder inOrder = Mockito.inOrder(mProvider);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("i", suggestions.get(0).toString());
        Assert.assertEquals("I", suggestions.get(1).toString());
        // ensuring abbr are called first, so the max-suggestions will not hide the exploded abbr.
        inOrder.verify(mProvider).getAbbreviations(Mockito.same(wordComposer), Mockito.any());
        inOrder.verify(mProvider).getAutoText(Mockito.same(wordComposer), Mockito.any());
        inOrder.verify(mProvider).getSuggestions(Mockito.same(wordComposer), Mockito.any());
        // suggesting "I" as a correction (from dictionary)
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());

        typeWord(wordComposer, "ll");
        suggestions = mUnderTest.getSuggestions(wordComposer);
        inOrder = Mockito.inOrder(mProvider);
        Assert.assertEquals(1, suggestions.size());
        Assert.assertEquals("ill", suggestions.get(0).toString());
        inOrder.verify(mProvider).getAbbreviations(Mockito.same(wordComposer), Mockito.any());
        inOrder.verify(mProvider).getAutoText(Mockito.same(wordComposer), Mockito.any());
        inOrder.verify(mProvider).getSuggestions(Mockito.same(wordComposer), Mockito.any());
        Assert.assertEquals(
                -1 /*ill is not a valid word in the test*/,
                mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testCorrectlyPrioritizeFixes_1() {
        mUnderTest.setCorrectionMode(true, 5, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            callback.addWord(
                                    "Jello".toCharArray(),
                                    0,
                                    5,
                                    24,
                                    Mockito.mock(Dictionary.class));
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
        typeWord(wordComposer, "hello");
        final List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("hello", suggestions.get(0).toString());
        Assert.assertEquals("Jello", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testCorrectlyPrioritizeFixes_2() {
        mUnderTest.setCorrectionMode(true, 5, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            callback.addWord(
                                    "Jello".toCharArray(),
                                    0,
                                    5,
                                    22,
                                    Mockito.mock(Dictionary.class));
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
        typeWord(wordComposer, "hello");
        final List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("hello", suggestions.get(0).toString());
        Assert.assertEquals("Jello", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testCorrectlyPrioritizeFixes_3() {
        mUnderTest.setCorrectionMode(true, 5, 2);
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
                            callback.addWord(
                                    "Jello".toCharArray(),
                                    0,
                                    5,
                                    22,
                                    Mockito.mock(Dictionary.class));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        // since we asked for 2 minimum-length, the first letter will not be queried
        typeWord(wordComposer, "hello");
        final List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("hello", suggestions.get(0).toString());
        Assert.assertEquals("Jello", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testCorrectlyPrioritizeFixes_4() {
        mUnderTest.setCorrectionMode(true, 5, 2);
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
                            callback.addWord(
                                    "Jello".toCharArray(),
                                    0,
                                    5,
                                    24,
                                    Mockito.mock(Dictionary.class));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        // since we asked for 2 minimum-length, the first letter will not be queried
        typeWord(wordComposer, "hello");
        final List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("hello", suggestions.get(0).toString());
        Assert.assertEquals("Jello", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testCorrectlyPrioritizeFixes_5() {
        mUnderTest.setCorrectionMode(true, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            callback.addWord(
                                    "Jello".toCharArray(),
                                    0,
                                    5,
                                    24,
                                    Mockito.mock(Dictionary.class));
                            callback.addWord(
                                    "hello".toCharArray(),
                                    0,
                                    5,
                                    23,
                                    Mockito.mock(Dictionary.class));
                            callback.addWord(
                                    "cello".toCharArray(),
                                    0,
                                    5,
                                    25,
                                    Mockito.mock(Dictionary.class));
                            callback.addWord(
                                    "following".toCharArray(),
                                    0,
                                    9,
                                    29,
                                    Mockito.mock(Dictionary.class));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        // typing a typo
        typeWord(wordComposer, "aello");
        final List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        // all the suggestions
        Assert.assertEquals(5, suggestions.size());
        // typed always first
        Assert.assertEquals("aello", suggestions.get(0).toString());
        // these are possible corrections, sorted by frequency
        Assert.assertEquals("cello", suggestions.get(1).toString());
        Assert.assertEquals("Jello", suggestions.get(2).toString());
        Assert.assertEquals("hello", suggestions.get(3).toString());
        // this is a possible suggestion, but not close
        Assert.assertEquals("following", suggestions.get(4).toString());
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testIgnoreLetterNextToSpaceWhenAtEnd() {
        Map<String, List<Pair<String, Integer>>> map = new HashMap<>();
        map.put("hello", Arrays.asList(Pair.create("notevenhello", 13), Pair.create("hello", 23)));
        map.put("hellon", Collections.singletonList(Pair.create("notevenhello", 13)));
        mUnderTest.setCorrectionMode(true, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            final Dictionary dictionary = Mockito.mock(Dictionary.class);
                            map.get(word.getTypedWord().toString())
                                    .forEach(
                                            pair ->
                                                    callback.addWord(
                                                            pair.first.toCharArray(),
                                                            0,
                                                            pair.first.length(),
                                                            pair.second,
                                                            dictionary));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        typeWord(wordComposer, "hello");
        List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("hello", suggestions.get(0).toString());
        Assert.assertEquals("notevenhello", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());

        // typing a letter next to space
        typeWord(wordComposer, "n", new boolean[] {true});
        suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals("hellon", suggestions.get(0).toString());
        Assert.assertEquals("hello", suggestions.get(1).toString());
        Assert.assertEquals("notevenhello", suggestions.get(2).toString());
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());

        // same typed word, different frequencies and has common letters
        map.put("hellon", Collections.singletonList(Pair.create("bellon", 33)));
        suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals("hellon", suggestions.get(0).toString());
        Assert.assertEquals("bellon", suggestions.get(1).toString());
        Assert.assertEquals("hello", suggestions.get(2).toString());
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testIgnoreLetterNextToSpaceWhenAtEndButAlsoSuggestIfValid() {
        Map<String, List<Pair<String, Integer>>> map = new HashMap<>();
        map.put("hello", Arrays.asList(Pair.create("notevenhello", 13), Pair.create("hello", 23)));
        map.put("hellon", Collections.singletonList(Pair.create("notevenhello", 13)));
        mUnderTest.setCorrectionMode(true, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            final Dictionary dictionary = Mockito.mock(Dictionary.class);
                            map.get(word.getTypedWord().toString())
                                    .forEach(
                                            pair ->
                                                    callback.addWord(
                                                            pair.first.toCharArray(),
                                                            0,
                                                            pair.first.length(),
                                                            pair.second,
                                                            dictionary));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        typeWord(wordComposer, "hello");
        List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("hello", suggestions.get(0).toString());
        Assert.assertEquals("notevenhello", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());

        // typing a letter next to space
        typeWord(wordComposer, "n", new boolean[] {true});
        suggestions = mUnderTest.getSuggestions(wordComposer);
        // note: here we see that duplication are removed:
        // 'notevenhello' is suggested from two sources
        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals("hellon", suggestions.get(0).toString());
        Assert.assertEquals("hello", suggestions.get(1).toString());
        Assert.assertEquals("notevenhello", suggestions.get(2).toString());
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());

        // same typed word, different frequencies, no commons. Still, typed word wins
        map.put("hellon", Collections.singletonList(Pair.create("notevenhello", 33)));
        suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals("hellon", suggestions.get(0).toString());
        Assert.assertEquals("hello", suggestions.get(1).toString());
        Assert.assertEquals("notevenhello", suggestions.get(2).toString());
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testDoesNotIgnoreLetterNotNextToSpaceWhenAtEnd() {
        Map<String, List<Pair<String, Integer>>> map = new HashMap<>();
        map.put("hello", Arrays.asList(Pair.create("notevenhello", 13), Pair.create("hello", 23)));
        map.put("hellon", Collections.singletonList(Pair.create("notevenhello", 13)));
        mUnderTest.setCorrectionMode(true, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            final Dictionary dictionary = Mockito.mock(Dictionary.class);
                            map.get(word.getTypedWord().toString())
                                    .forEach(
                                            pair ->
                                                    callback.addWord(
                                                            pair.first.toCharArray(),
                                                            0,
                                                            pair.first.length(),
                                                            pair.second,
                                                            dictionary));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        typeWord(wordComposer, "hello");
        List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("hello", suggestions.get(0).toString());
        Assert.assertEquals("notevenhello", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());

        // typing a letter NOT next to space
        typeWord(wordComposer, "n", new boolean[] {false});
        suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("hellon", suggestions.get(0).toString());
        Assert.assertEquals("notevenhello", suggestions.get(1).toString());
        Assert.assertEquals(-1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testDoesNotIgnoreLetterNextToSpaceWhenAtStart() {
        // we do not assume the user pressed SPACE to begin typing a word
        Map<String, List<Pair<String, Integer>>> map = new HashMap<>();
        map.put("hello", Arrays.asList(Pair.create("notevenhello", 13), Pair.create("hello", 23)));
        map.put("nhello", Collections.emptyList());
        mUnderTest.setCorrectionMode(true, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            final Dictionary dictionary = Mockito.mock(Dictionary.class);
                            map.get(word.getTypedWord().toString())
                                    .forEach(
                                            pair ->
                                                    callback.addWord(
                                                            pair.first.toCharArray(),
                                                            0,
                                                            pair.first.length(),
                                                            pair.second,
                                                            dictionary));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        typeWord(wordComposer, "nhello");
        List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(1, suggestions.size());
        Assert.assertEquals("nhello", suggestions.get(0).toString());
        Assert.assertEquals(-1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testOnlyReturnsOneExactlyMatchingSubWord() {
        Map<String, List<Pair<String, Integer>>> map = new HashMap<>();
        map.put("hello", Arrays.asList(Pair.create("notevenhello", 13), Pair.create("hello", 23)));
        map.put("hellon", Collections.singletonList(Pair.create("notevenhello", 13)));
        mUnderTest.setCorrectionMode(true, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            final Dictionary dictionary = Mockito.mock(Dictionary.class);
                            map.get(word.getTypedWord().toString())
                                    .forEach(
                                            pair ->
                                                    callback.addWord(
                                                            pair.first.toCharArray(),
                                                            0,
                                                            pair.first.length(),
                                                            pair.second,
                                                            dictionary));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        typeWord(wordComposer, "hello");
        List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("hello", suggestions.get(0).toString());
        Assert.assertEquals("notevenhello", suggestions.get(1).toString());
        Assert.assertEquals(0, mUnderTest.getLastValidSuggestionIndex());

        // typing a letter next to space - replacing the 'hello', to verify that only
        // the valid word is returned
        map.put("hello", Arrays.asList(Pair.create("gello", 13), Pair.create("hello", 23)));
        typeWord(wordComposer, "n", new boolean[] {true});
        suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals("hellon", suggestions.get(0).toString());
        Assert.assertEquals("hello", suggestions.get(1).toString());
        Assert.assertEquals("notevenhello", suggestions.get(2).toString());
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());

        // same typed word, different frequencies, without common letter
        map.put("hellon", Collections.singletonList(Pair.create("notevenhello", 33)));
        suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals("hellon", suggestions.get(0).toString());
        Assert.assertEquals("hello", suggestions.get(1).toString());
        Assert.assertEquals("notevenhello", suggestions.get(2).toString());
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testMatchTwoSubWords() {
        Map<String, List<Pair<String, Integer>>> map = new HashMap<>();
        map.put("hello", Arrays.asList(Pair.create("notevenhello", 13), Pair.create("hello", 23)));
        map.put("world", Arrays.asList(Pair.create("world", 13), Pair.create("worlds", 23)));
        map.put("hellonworld", Collections.singletonList(Pair.create("hellobworld", 13)));
        mUnderTest.setCorrectionMode(true, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            final Dictionary dictionary = Mockito.mock(Dictionary.class);
                            map.get(word.getTypedWord().toString())
                                    .forEach(
                                            pair ->
                                                    callback.addWord(
                                                            pair.first.toCharArray(),
                                                            0,
                                                            pair.first.length(),
                                                            pair.second,
                                                            dictionary));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        typeWord(
                wordComposer,
                "hellonworld",
                new boolean[] {
                    false, false, false, false, false, true, false, false, false, false, false
                });
        List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals("hellonworld", suggestions.get(0).toString());
        Assert.assertEquals("hello world", suggestions.get(1).toString());
        Assert.assertEquals("hellobworld", suggestions.get(2).toString());
        Assert.assertEquals(3, suggestions.size());
        Assert.assertEquals(1, mUnderTest.getLastValidSuggestionIndex());
    }

    @Test
    public void testDoesNotSuggestIfNotAllSubWordsMatch() {
        Map<String, List<Pair<String, Integer>>> map = new HashMap<>();
        map.put("hello", Arrays.asList(Pair.create("notevenhello", 13), Pair.create("hello", 23)));
        map.put("hellon", Collections.singletonList(Pair.create("notevenhello", 13)));
        map.put("world", Collections.emptyList());
        map.put("hellonworld", Collections.emptyList());
        mUnderTest.setCorrectionMode(true, 2, 2);
        WordComposer wordComposer = new WordComposer();
        Mockito.doAnswer(
                        invocation -> {
                            final KeyCodesProvider word = invocation.getArgument(0);
                            final Dictionary.WordCallback callback = invocation.getArgument(1);
                            final Dictionary dictionary = Mockito.mock(Dictionary.class);
                            map.get(word.getTypedWord().toString())
                                    .forEach(
                                            pair ->
                                                    callback.addWord(
                                                            pair.first.toCharArray(),
                                                            0,
                                                            pair.first.length(),
                                                            pair.second,
                                                            dictionary));
                            return null;
                        })
                .when(mProvider)
                .getSuggestions(Mockito.any(), Mockito.any());

        typeWord(
                wordComposer,
                "hellonworld",
                new boolean[] {
                    false, false, false, false, false, true, false, false, false, false, false
                });
        List<CharSequence> suggestions = mUnderTest.getSuggestions(wordComposer);
        Assert.assertEquals(1, suggestions.size());
        Assert.assertEquals("hellonworld", suggestions.get(0).toString());
        Assert.assertEquals(-1, mUnderTest.getLastValidSuggestionIndex());
    }
}
