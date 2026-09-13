/*
 * Copyright (c) 2026 AnySoftKeyboard contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies the per-keyboard include filter that {@link RadicalDictionary#parseRadicalStream}
 * applies to both bundled and user-imported (SAF) rows. The rule is: a row is kept only when every
 * CJK or kana codepoint in its value is present in the keyboard's char-set. ASCII / punctuation /
 * kaomoji pass freely; empty char-set disables the filter.
 */
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RadicalDictionaryIncludeFilterTest {

  private static InputStream in(String s) {
    return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
  }

  private static Set<String> setOf(String... items) {
    return new HashSet<>(Arrays.asList(items));
  }

  // valuePassesIncludeFilter -------------------------------------------------

  @Test
  public void valuePassesIncludeFilter_emptyIncludeChars_allowsEverything() {
    Assert.assertTrue(RadicalDictionary.valuePassesIncludeFilter("對", Collections.emptySet()));
    Assert.assertTrue(RadicalDictionary.valuePassesIncludeFilter("あ", Collections.emptySet()));
    Assert.assertTrue(
        RadicalDictionary.valuePassesIncludeFilter("(o´∀`o)", Collections.emptySet()));
  }

  @Test
  public void valuePassesIncludeFilter_cjkInSet_passes() {
    Assert.assertTrue(RadicalDictionary.valuePassesIncludeFilter("對", setOf("對")));
  }

  @Test
  public void valuePassesIncludeFilter_cjkNotInSet_fails() {
    Assert.assertFalse(RadicalDictionary.valuePassesIncludeFilter("对", setOf("對")));
  }

  @Test
  public void valuePassesIncludeFilter_kanaNotInSet_fails() {
    // Trad/simp keyboards ship CJK-only char-sets; a kana value must be rejected.
    Assert.assertFalse(RadicalDictionary.valuePassesIncludeFilter("あ", setOf("對")));
  }

  @Test
  public void valuePassesIncludeFilter_kanaInSet_passes() {
    // JP keyboard ships CJK + kana in its char-set.
    Assert.assertTrue(RadicalDictionary.valuePassesIncludeFilter("あ", setOf("あ")));
  }

  @Test
  public void valuePassesIncludeFilter_asciiAlwaysPasses() {
    Assert.assertTrue(RadicalDictionary.valuePassesIncludeFilter("abc!", setOf("對")));
  }

  @Test
  public void valuePassesIncludeFilter_kaomojiSymbolsAlwaysPass() {
    // (´▽｀), the half-width corner brackets are CJK punctuation, not ideographs.
    Assert.assertTrue(RadicalDictionary.valuePassesIncludeFilter("(´▽｀)", setOf("對")));
  }

  @Test
  public void valuePassesIncludeFilter_mixedRequiresAllMembership() {
    // Multi-char value: every CJK cp must be in set. 對 yes, 对 no -> reject.
    Assert.assertFalse(RadicalDictionary.valuePassesIncludeFilter("對对", setOf("對")));
    Assert.assertTrue(RadicalDictionary.valuePassesIncludeFilter("對對", setOf("對")));
  }

  // parseRadicalStream integration ------------------------------------------

  @Test
  public void parseRadicalStream_dropsRowsRejectedByIncludeFilter() throws Exception {
    TestableDict d = new TestableDict();
    HashMap<String, List<String>> radicals = new HashMap<>();
    HashSet<String> prefixes = new HashSet<>();
    HashSet<String> outs = new HashSet<>();

    Set<String> tradOnly = setOf("對");
    d.parseRadicalStream(in("a\t對\na\t对\n"), radicals, prefixes, outs, tradOnly);

    Assert.assertEquals(Collections.singletonList("對"), radicals.get("a"));
    Assert.assertTrue(outs.contains("對"));
    Assert.assertFalse(outs.contains("对"));
  }

  @Test
  public void parseRadicalStream_emptyFilter_keepsAllRows() throws Exception {
    TestableDict d = new TestableDict();
    HashMap<String, List<String>> radicals = new HashMap<>();
    HashSet<String> prefixes = new HashSet<>();
    HashSet<String> outs = new HashSet<>();

    d.parseRadicalStream(in("a\t對\na\t对\n"), radicals, prefixes, outs);

    Assert.assertEquals(Arrays.asList("對", "对"), radicals.get("a"));
  }

  @Test
  public void parseRadicalStream_splitDictionary_filtersPerCandidate() throws Exception {
    // splitMultiCodepointCandidates=true: each CJK codepoint is its own candidate, so the filter
    // applies cp-by-cp. The "對对" line should expand into two candidates and drop 对.
    SplitTestableDict d = new SplitTestableDict();
    HashMap<String, List<String>> radicals = new HashMap<>();
    HashSet<String> prefixes = new HashSet<>();
    HashSet<String> outs = new HashSet<>();

    d.parseRadicalStream(in("a\t對对\n"), radicals, prefixes, outs, setOf("對"));

    Assert.assertEquals(Collections.singletonList("對"), radicals.get("a"));
    Assert.assertTrue(prefixes.contains("a"));
  }

  @Test
  public void parseRadicalStream_kanaValueRejectedByCjkOnlySet() throws Exception {
    TestableDict d = new TestableDict();
    HashMap<String, List<String>> radicals = new HashMap<>();
    HashSet<String> prefixes = new HashSet<>();
    HashSet<String> outs = new HashSet<>();

    // Trad keyboard char-set has no kana codepoints, so a kana value must be filtered out
    // even when its code prefix is not the boshiamy kana convention.
    d.parseRadicalStream(in("hi\tあ\n"), radicals, prefixes, outs, setOf("對"));

    Assert.assertNull(radicals.get("hi"));
    Assert.assertFalse(prefixes.contains("hi"));
  }

  // Test scaffolding ---------------------------------------------------------

  private static class TestableDict extends RadicalDictionary {
    TestableDict() {
      super("test_include", null, 0);
    }

    @Override
    protected void loadAllResources() {}
  }

  private static class SplitTestableDict extends RadicalDictionary {
    SplitTestableDict() {
      super(
          "test_include_split",
          null,
          /* resourceId= */ 0,
          /* phrasesResourceId= */ 0,
          /* homophonesResourceId= */ 0,
          /* charToZhuyinResourceId= */ 0,
          /* charToRadicalResourceId= */ 0,
          /* excludeHomophoneCharsResourceId= */ 0,
          /* charFrequencyResourceId= */ 0,
          /* splitMultiCodepointCandidates= */ true);
    }

    @Override
    protected void loadAllResources() {}
  }
}
