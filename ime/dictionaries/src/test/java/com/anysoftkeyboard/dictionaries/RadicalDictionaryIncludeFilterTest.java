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
import java.util.HashSet;
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
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();

    Set<String> tradOnly = setOf("對");
    d.parseRadicalStream(in("a\t對\na\t对\n"), radicals, outs, tradOnly);

    Assert.assertEquals(Collections.singletonList("對"), radicals.getCandidates("a"));
    Assert.assertTrue(outs.contains("對"));
    Assert.assertFalse(outs.contains("对"));
  }

  @Test
  public void parseRadicalStream_emptyFilter_keepsAllRows() throws Exception {
    TestableDict d = new TestableDict();
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();

    d.parseRadicalStream(in("a\t對\na\t对\n"), radicals, outs);

    Assert.assertEquals(Arrays.asList("對", "对"), radicals.getCandidates("a"));
  }

  @Test
  public void parseRadicalStream_splitDictionary_filtersPerCandidate() throws Exception {
    // splitMultiCodepointCandidates=true: each CJK codepoint is its own candidate, so the filter
    // applies cp-by-cp. The "對对" line should expand into two candidates and drop 对.
    SplitTestableDict d = new SplitTestableDict();
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();

    d.parseRadicalStream(in("a\t對对\n"), radicals, outs, setOf("對"));

    Assert.assertEquals(Collections.singletonList("對"), radicals.getCandidates("a"));
  }

  @Test
  public void parseRadicalStream_kanaValueRejectedByCjkOnlySet() throws Exception {
    TestableDict d = new TestableDict();
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();

    // Trad keyboard char-set has no kana codepoints, so a kana value must be filtered out
    // even when its code prefix is not the boshiamy kana convention.
    d.parseRadicalStream(in("hi\tあ\n"), radicals, outs, setOf("對"));

    Assert.assertNull(radicals.getCandidates("hi"));
  }

  // candidate conversion (Simplified keyboard with an imported trad table) ------

  private static java.util.Map<String, String> t2s(String... pairs) {
    java.util.Map<String, String> map = new java.util.HashMap<>();
    for (int i = 0; i < pairs.length; i += 2) {
      map.put(pairs[i], pairs[i + 1]);
    }
    return map;
  }

  @Test
  public void resolveCandidate_prefersConvertedFormWhenItPassesFilter() {
    Assert.assertEquals(
        "么", RadicalDictionary.resolveCandidate("麼", setOf("么", "磁"), t2s("麼", "么")));
  }

  @Test
  public void resolveCandidate_keepsOriginalWhenConvertedFormIsRejected() {
    // 乾 is valid on its own, but its converted form is not in this keyboard's set.
    Assert.assertEquals("乾", RadicalDictionary.resolveCandidate("乾", setOf("乾"), t2s("乾", "干")));
  }

  @Test
  public void resolveCandidate_returnsNullWhenNeitherFormPasses() {
    Assert.assertNull(RadicalDictionary.resolveCandidate("們", setOf("对"), t2s("們", "们")));
  }

  @Test
  public void resolveCandidate_emptyConversion_behavesLikeIncludeFilter() {
    Assert.assertEquals(
        "對", RadicalDictionary.resolveCandidate("對", setOf("對"), Collections.emptyMap()));
    Assert.assertNull(RadicalDictionary.resolveCandidate("们", setOf("對"), Collections.emptyMap()));
  }

  @Test
  public void parseRadicalStream_conversionGivesTradPrimaryTheFirstSlot() throws Exception {
    // liu-uni order for these codes. Without conversion the Simplified keyboard drops 麼 / 們
    // and the secondary 磁 / 滚 becomes the first candidate.
    TestableDict d = new TestableDict();
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();
    Set<String> simp = setOf("么", "磁", "们", "滚");

    d.parseRadicalStream(
        in("lw\t麼\nlw\t磁\nlw\t么\nqm\t們\nqm\t滚\nqm\t们\n"),
        radicals,
        outs,
        simp,
        t2s("麼", "么", "們", "们"));

    Assert.assertEquals(Arrays.asList("么", "磁"), radicals.getCandidates("lw"));
    Assert.assertEquals(Arrays.asList("们", "滚"), radicals.getCandidates("qm"));
  }

  @Test
  public void parsePhrasesStream_conversionKeepsTriggerAndConvertsNextChars() throws Exception {
    TestableDict d = new TestableDict();
    CompactTrie.Builder phrases = new CompactTrie.Builder();

    d.parsePhrasesStream(
        in("測\t試\n測\t驗\n"), phrases, setOf("测", "试", "验"), t2s("測", "测", "試", "试", "驗", "验"));

    Assert.assertEquals(Arrays.asList("试", "验"), phrases.getCandidates("測"));
    Assert.assertNull(phrases.getCandidates("测"));
  }

  @Test
  public void parsePhrasesStream_dropsRowsRejectedByIncludeFilter() throws Exception {
    TestableDict d = new TestableDict();
    CompactTrie.Builder phrases = new CompactTrie.Builder();

    // 兒 has no conversion here and is not in the set, so the row is dropped.
    d.parsePhrasesStream(in("么\t兒\n么\t妹\n"), phrases, setOf("么", "妹"), t2s("麼", "么"));

    Assert.assertEquals(Collections.singletonList("妹"), phrases.getCandidates("么"));
  }

  @Test
  public void parsePhrasesStream_noConversion_keepsRowsAsIs() throws Exception {
    TestableDict d = new TestableDict();
    CompactTrie.Builder phrases = new CompactTrie.Builder();

    d.parsePhrasesStream(in("測\t試\n后\t羿\n後\t來\n"), phrases);

    Assert.assertEquals(Collections.singletonList("試"), phrases.getCandidates("測"));
    Assert.assertEquals(Collections.singletonList("羿"), phrases.getCandidates("后"));
    Assert.assertEquals(Collections.singletonList("來"), phrases.getCandidates("後"));
  }

  // getNextWordSuggestions on a converting keyboard ------------------------------

  /** Phrase rows keyed by Traditional chars, as stored on the Simplified keyboard. */
  private static TestableDict simplifiedPhraseDict() {
    TestableDict d = new TestableDict();
    java.util.Map<String, String> charToRadical = new java.util.HashMap<>();
    charToRadical.put("麼", "lw");
    charToRadical.put("么", "war");
    charToRadical.put("後", "i");
    charToRadical.put("后", "foo");
    charToRadical.put("測", "ebm");
    d.installHomophoneData(
        Collections.emptyMap(),
        Collections.emptyMap(),
        charToRadical,
        Collections.emptySet(),
        Collections.emptyMap());
    CompactTrie.Builder phrases = new CompactTrie.Builder();
    phrases.addCandidate("么", "儿");
    phrases.addCandidate("么", "妹");
    phrases.addCandidate("後", "来");
    phrases.addCandidate("後", "面");
    phrases.addCandidate("后", "羿");
    phrases.addCandidate("測", "试");
    phrases.addCandidate("一", "个");
    java.util.Map<String, java.util.List<String>> convertedFrom = new java.util.HashMap<>();
    convertedFrom.put("么", Arrays.asList("麼", "麽"));
    convertedFrom.put("后", Collections.singletonList("後"));
    convertedFrom.put("测", Collections.singletonList("測"));
    d.installPhraseData(phrases, convertedFrom);
    return d;
  }

  @Test
  public void nextWords_typedCodePicksTheTraditionalSource() {
    TestableDict d = simplifiedPhraseDict();

    // lw is 麼, which has no phrase rows, exactly like the Traditional keyboard.
    Assert.assertTrue(d.getNextWordSuggestions("么", "lw").isEmpty());
    // war is 么 itself.
    Assert.assertEquals(Arrays.asList("儿", "妹"), d.getNextWordSuggestions("么", "war"));
    Assert.assertEquals(Arrays.asList("来", "面"), d.getNextWordSuggestions("后", "i"));
    Assert.assertEquals(Collections.singletonList("羿"), d.getNextWordSuggestions("后", "foo"));
    Assert.assertEquals(Collections.singletonList("试"), d.getNextWordSuggestions("测", "ebm"));
  }

  @Test
  public void nextWords_trailingSelectorKeyIsIgnored() {
    TestableDict d = simplifiedPhraseDict();

    Assert.assertTrue(d.getNextWordSuggestions("么", "lwv").isEmpty());
  }

  @Test
  public void nextWords_withoutCode_mergesSourcesConvertedFirst() {
    TestableDict d = simplifiedPhraseDict();

    Assert.assertEquals(Arrays.asList("来", "面", "羿"), d.getNextWordSuggestions("后", null));
    Assert.assertEquals(Arrays.asList("儿", "妹"), d.getNextWordSuggestions("么"));
    Assert.assertEquals(Collections.singletonList("试"), d.getNextWordSuggestions("测"));
  }

  @Test
  public void nextWords_unconvertedCharIsLookedUpDirectly() {
    TestableDict d = simplifiedPhraseDict();

    Assert.assertEquals(Collections.singletonList("个"), d.getNextWordSuggestions("一", "a"));
    Assert.assertEquals(Collections.singletonList("个"), d.getNextWordSuggestions("十一"));
  }

  @Test
  public void parseConversionStream_keepsFirstValueAndSkipsComments() throws Exception {
    java.util.HashMap<String, String> map = new java.util.HashMap<>();

    int count =
        RadicalDictionary.parseConversionStream(
            in("# comment\n\uFEFF麼\t么 麽\n們\t们\nbad-line\n"), map);

    Assert.assertEquals(2, count);
    Assert.assertEquals("么", map.get("麼"));
    Assert.assertEquals("们", map.get("們"));
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
          /* charToPhoneticResourceId= */ 0,
          /* charToRadicalResourceId= */ 0,
          /* excludeHomophoneCharsResourceId= */ 0,
          /* charFrequencyResourceId= */ 0,
          /* splitMultiCodepointCandidates= */ true);
    }

    @Override
    protected void loadAllResources() {}
  }
}
