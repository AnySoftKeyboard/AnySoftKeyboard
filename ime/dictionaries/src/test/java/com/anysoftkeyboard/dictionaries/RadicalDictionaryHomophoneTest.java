package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the homophone, OpenCC self-mapping skip, and variant-exclude filter behaviour of {@link
 * RadicalDictionary}. Complements the basic {@link RadicalDictionaryTest}.
 */
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RadicalDictionaryHomophoneTest {

  @Test
  public void getExactMatches_returnsAllRowsUnfiltered_whenIncludeFilterIsParseTime() {
    // The exclude/fallback runtime filter was removed in favour of a parse-time include filter
    // applied by parseRadicalStream. getExactMatches is now a pure lookup against the already
    // filtered radicalMap, so anything that landed there should come back as-is.
    TestableDict d = new TestableDict();
    d.addMapping("a", "對");
    d.addMapping("a", "对");
    d.loadDictionary();
    d.installExclude(setOf("对"));

    List<String> matches = d.getExactMatches("a");
    Assert.assertEquals(Arrays.asList("對", "对"), matches);
  }

  @Test
  public void getExactMatches_emptyWhenNoMappings() {
    TestableDict d = new TestableDict();
    d.loadDictionary();
    Assert.assertTrue(d.getExactMatches("a").isEmpty());
  }

  @Test
  public void getHomophones_filtersExcludeAndUntypable() {
    TestableDict d = new TestableDict();
    d.addMapping("a", "對");
    d.loadDictionary();

    Map<String, String> charToZhuyin = new HashMap<>();
    charToZhuyin.put("對", "ㄉㄨㄟˋ");
    charToZhuyin.put("队", "ㄉㄨㄟˋ"); // simp char, should be excluded
    charToZhuyin.put("隊", "ㄉㄨㄟˋ"); // not typable (no radical mapping)
    Map<String, List<String>> zhuyinToChars = new HashMap<>();
    zhuyinToChars.put("ㄉㄨㄟˋ", Arrays.asList("對", "队", "隊"));
    Map<String, String> charToRadical = new HashMap<>();
    charToRadical.put("對", "a");

    d.installHomophoneData(
        charToZhuyin, zhuyinToChars, charToRadical, setOf("队"), Collections.emptyMap());

    List<String> hom = d.getHomophones("對");
    Assert.assertEquals(1, hom.size());
    Assert.assertEquals("對", hom.get(0));
  }

  @Test
  public void getHomophones_sortedByFrequencyRank() {
    TestableDict d = new TestableDict();
    d.addMapping("a", "對");
    d.addMapping("b", "兌");
    d.addMapping("c", "懟");
    d.loadDictionary();

    Map<String, String> charToZhuyin = new HashMap<>();
    charToZhuyin.put("對", "ㄉㄨㄟˋ");
    charToZhuyin.put("兌", "ㄉㄨㄟˋ");
    charToZhuyin.put("懟", "ㄉㄨㄟˋ");
    Map<String, List<String>> zhuyinToChars = new HashMap<>();
    // Provided in arbitrary order; should be sorted by frequency rank.
    zhuyinToChars.put("ㄉㄨㄟˋ", Arrays.asList("懟", "對", "兌"));
    Map<String, String> charToRadical = new HashMap<>();
    charToRadical.put("對", "a");
    charToRadical.put("兌", "b");
    charToRadical.put("懟", "c");
    Map<String, Integer> rank = new HashMap<>();
    rank.put("對", 0); // most common
    rank.put("兌", 1);
    rank.put("懟", 2);

    d.installHomophoneData(
        charToZhuyin, zhuyinToChars, charToRadical, Collections.emptySet(), rank);

    List<String> hom = d.getHomophones("對");
    Assert.assertEquals(Arrays.asList("對", "兌", "懟"), hom);
  }

  @Test
  public void getHomophones_unknownCharReturnsEmpty() {
    TestableDict d = new TestableDict();
    d.addMapping("a", "對");
    d.loadDictionary();

    Assert.assertTrue(d.getHomophones("不存在").isEmpty());
  }

  @Test
  public void hasHomophoneData_falseWhenNoneInstalled() {
    TestableDict d = new TestableDict();
    d.addMapping("a", "對");
    d.loadDictionary();
    Assert.assertFalse(d.hasHomophoneData());
  }

  @SafeVarargs
  @SuppressWarnings("varargs")
  private static <T> Set<T> setOf(T... items) {
    return new HashSet<>(Arrays.asList(items));
  }

  /** Test variant of RadicalDictionary that bypasses Android resource loading. */
  private static class TestableDict extends RadicalDictionary {
    private final List<String[]> mPending = new ArrayList<>();

    TestableDict() {
      super("test_radical_homo", null, 0);
    }

    void addMapping(String radicals, String character) {
      mPending.add(new String[] {radicals, character});
    }

    @Override
    protected void loadAllResources() {
      for (String[] m : mPending) {
        addRadicalMapping(m[0], m[1]);
      }
    }

    void installExclude(Set<String> exclude) {
      installHomophoneData(
          Collections.emptyMap(),
          Collections.emptyMap(),
          Collections.emptyMap(),
          exclude,
          Collections.emptyMap());
    }
  }
}
