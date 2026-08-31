package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RadicalDictionaryTest {

  private TestableRadicalDictionary mUnderTest;

  @Before
  public void setUp() {
    mUnderTest = new TestableRadicalDictionary();
  }

  @Test
  public void testExactMatchHappyPath() {
    mUnderTest.addMapping("a", "對");
    mUnderTest.addMapping("aa", "寸");
    mUnderTest.addMapping("ag", "威");
    mUnderTest.loadDictionary();

    List<String> matches = mUnderTest.getExactMatches("a");
    Assert.assertEquals(1, matches.size());
    Assert.assertEquals("對", matches.get(0));

    matches = mUnderTest.getExactMatches("ag");
    Assert.assertEquals(1, matches.size());
    Assert.assertEquals("威", matches.get(0));
  }

  @Test
  public void testMultipleCandidatesForSameRadical() {
    mUnderTest.addMapping("a", "對");
    mUnderTest.addMapping("a", "的");
    mUnderTest.loadDictionary();

    List<String> matches = mUnderTest.getExactMatches("a");
    Assert.assertEquals(2, matches.size());
    Assert.assertTrue(matches.contains("對"));
    Assert.assertTrue(matches.contains("的"));
  }

  @Test
  public void testPartialMatch() {
    mUnderTest.addMapping("abc", "測");
    mUnderTest.loadDictionary();

    Assert.assertTrue(mUnderTest.hasPartialMatch("a"));
    Assert.assertTrue(mUnderTest.hasPartialMatch("ab"));
    Assert.assertTrue(mUnderTest.hasPartialMatch("abc"));
    Assert.assertFalse(mUnderTest.hasPartialMatch("abcd"));
    Assert.assertFalse(mUnderTest.hasPartialMatch("b"));
  }

  @Test
  public void testNoMatchReturnsEmptyList() {
    mUnderTest.addMapping("a", "對");
    mUnderTest.loadDictionary();

    List<String> matches = mUnderTest.getExactMatches("z");
    Assert.assertTrue(matches.isEmpty());
  }

  @Test
  public void testCaseInsensitiveRadicals() {
    mUnderTest.addMapping("Ag", "威");
    mUnderTest.loadDictionary();

    List<String> matches = mUnderTest.getExactMatches("ag");
    Assert.assertEquals(1, matches.size());
    Assert.assertEquals("威", matches.get(0));

    matches = mUnderTest.getExactMatches("AG");
    Assert.assertEquals(1, matches.size());
    Assert.assertEquals("威", matches.get(0));
  }

  @Test
  public void testIsValidWord() {
    mUnderTest.addMapping("a", "對");
    mUnderTest.addMapping("ag", "威");
    mUnderTest.loadDictionary();

    Assert.assertTrue(mUnderTest.isValidWord("對"));
    Assert.assertTrue(mUnderTest.isValidWord("威"));
    Assert.assertFalse(mUnderTest.isValidWord("foo"));
    Assert.assertFalse(mUnderTest.isValidWord("a"));
  }

  @Test
  public void testGetSuggestionsViaCallback() {
    mUnderTest.addMapping("ag", "威");
    mUnderTest.addMapping("ag", "哥");
    mUnderTest.loadDictionary();

    KeyCodesProvider composer = createComposer("ag");
    List<String> results = new ArrayList<>();
    mUnderTest.getSuggestions(
        composer,
        (word, wordOffset, wordLength, frequency, from) -> {
          results.add(new String(word, wordOffset, wordLength));
          return true;
        });

    Assert.assertEquals(2, results.size());
    Assert.assertTrue(results.contains("威"));
    Assert.assertTrue(results.contains("哥"));
  }

  @Test
  public void testEmptyInputReturnsNoSuggestions() {
    mUnderTest.addMapping("a", "對");
    mUnderTest.loadDictionary();

    KeyCodesProvider composer = createComposer("");
    List<String> results = new ArrayList<>();
    mUnderTest.getSuggestions(
        composer,
        (word, wordOffset, wordLength, frequency, from) -> {
          results.add(new String(word, wordOffset, wordLength));
          return true;
        });

    Assert.assertTrue(results.isEmpty());
  }

  @Test
  public void testCloseReleasesResources() {
    mUnderTest.addMapping("a", "對");
    mUnderTest.loadDictionary();

    Assert.assertFalse(mUnderTest.getExactMatches("a").isEmpty());

    mUnderTest.close();

    Assert.assertTrue(mUnderTest.getExactMatches("a").isEmpty());
  }

  private static KeyCodesProvider createComposer(String word) {
    return new KeyCodesProvider() {
      @Override
      public int codePointCount() {
        return word.codePointCount(0, word.length());
      }

      @Override
      public int[] getCodesAt(int index) {
        int cp = word.codePointAt(index);
        return new int[] {cp};
      }

      @Override
      public CharSequence getTypedWord() {
        return word;
      }
    };
  }

  /**
   * Regression test: Boshiamy-style dictionaries (splitMultiCodepointCandidates=false) must
   * preserve the source file's curated order even when a frequency table is installed. The
   * frequency table is shared with homophone ranking; this test pins the contract that the shared
   * table must NOT reorder Boshiamy/curated candidates (otherwise positional selector keys like
   * vrsfwlcbkj would return the wrong glyph silently).
   */
  @Test
  public void testCuratedOrderPreservedWhenSplitDisabledEvenWithFrequencyTable() {
    mUnderTest.addMapping("a", "對");
    mUnderTest.addMapping("a", "对");
    mUnderTest.loadDictionary();

    // Install a frequency table that ranks the simplified char ABOVE the traditional one.
    Map<String, Integer> rank = new HashMap<>();
    rank.put("对", 1);
    rank.put("對", 5000);
    mUnderTest.installFrequencyForTest(rank);

    List<String> matches = mUnderTest.getExactMatches("a");
    Assert.assertEquals(2, matches.size());
    Assert.assertEquals(
        "Curated file order must win over frequency for non-split dicts", "對", matches.get(0));
    Assert.assertEquals("对", matches.get(1));
  }

  /** Cangjie/Zhuyin (splitMultiCodepointCandidates=true) must reorder by frequency. */
  @Test
  public void testFrequencySortAppliedWhenSplitEnabled() {
    SplitRadicalDictionary split = new SplitRadicalDictionary();
    split.addMapping("a", "對");
    split.addMapping("a", "对");
    split.loadDictionary();

    Map<String, Integer> rank = new HashMap<>();
    rank.put("对", 1);
    rank.put("對", 5000);
    split.installFrequencyForTest(rank);

    List<String> matches = split.getExactMatches("a");
    Assert.assertEquals(2, matches.size());
    Assert.assertEquals(
        "Lower frequency rank wins for split-mode dictionaries", "对", matches.get(0));
  }

  /**
   * A testable variant of RadicalDictionary that accepts in-memory mappings instead of loading from
   * Android resources.
   */
  private static class TestableRadicalDictionary extends RadicalDictionary {
    private final List<String[]> mPendingMappings = new ArrayList<>();

    TestableRadicalDictionary() {
      super("test_radical", null, 0);
    }

    TestableRadicalDictionary(boolean splitMultiCodepointCandidates) {
      super("test_radical", null, 0, 0, 0, 0, 0, 0, 0, splitMultiCodepointCandidates);
    }

    void addMapping(String radicals, String character) {
      mPendingMappings.add(new String[] {radicals, character});
    }

    void installFrequencyForTest(Map<String, Integer> rank) {
      installHomophoneData(
          Collections.emptyMap(),
          Collections.emptyMap(),
          Collections.emptyMap(),
          Collections.emptySet(),
          rank);
    }

    @Override
    protected void loadAllResources() {
      // Instead of loading from resources, load from in-memory mappings
      for (String[] mapping : mPendingMappings) {
        addRadicalMapping(mapping[0], mapping[1]);
      }
    }
  }

  private static class SplitRadicalDictionary extends TestableRadicalDictionary {
    SplitRadicalDictionary() {
      super(true);
    }
  }
}
