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
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that {@link RadicalDictionary#parseRadicalStream} correctly accumulates entries across
 * multiple streams, the mechanism the loader uses to merge an overlay file on top of the bundled
 * main table.
 */
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RadicalDictionaryOverlayMergeTest {

  private TestableDict mDict;

  @Before
  public void setUp() {
    mDict = new TestableDict();
  }

  private static InputStream in(String s) {
    return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  public void overlayAddsNewCandidatesToExistingCode() throws Exception {
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();
    mDict.parseRadicalStream(in("a\t對\n"), radicals, outs);
    mDict.parseRadicalStream(in("a\t隊\n"), radicals, outs);
    Assert.assertEquals(2, radicals.getCandidates("a").size());
    Assert.assertEquals("對", radicals.getCandidates("a").get(0));
    Assert.assertEquals("隊", radicals.getCandidates("a").get(1));
  }

  @Test
  public void overlayIntroducesNewCodeAndPrefix() throws Exception {
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();
    mDict.parseRadicalStream(in("a\t對\n"), radicals, outs);
    mDict.parseRadicalStream(in(",a\tㄇ\n"), radicals, outs);
    Assert.assertTrue(radicals.getCandidates(",a") != null);
    Assert.assertTrue(outs.contains("ㄇ"));
  }

  @Test
  public void overlayIsLowercasedLikeBundled() throws Exception {
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();
    mDict.parseRadicalStream(in("AG\t威\n"), radicals, outs);
    Assert.assertTrue(radicals.getCandidates("ag") != null);
    Assert.assertFalse(radicals.getCandidates("AG") != null);
  }

  @Test
  public void mergingTheSameEntryTwiceDoesNotDuplicateIt() throws Exception {
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();
    // A user can hand us liu-uni, liu-uni2, liu-uni3 and liu-uni4 at once and those overlap
    // heavily, which used to render as "趟趟趟趟" in the candidate strip.
    for (int i = 0; i < 4; i++) {
      mDict.parseRadicalStream(in("yzso\t趟\n"), radicals, outs);
    }
    Assert.assertEquals(1, radicals.getCandidates("yzso").size());
    Assert.assertEquals("趟", radicals.getCandidates("yzso").get(0));
  }

  @Test
  public void mergingOverlappingTablesKeepsFirstSeenOrder() throws Exception {
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();
    mDict.parseRadicalStream(in("pox\t假\npox\t儼\n"), radicals, outs);
    // Second table repeats 假 and adds 囟; only the new character may be appended.
    mDict.parseRadicalStream(in("pox\t假\npox\t囟\n"), radicals, outs);
    Assert.assertEquals(3, radicals.getCandidates("pox").size());
    Assert.assertEquals("假", radicals.getCandidates("pox").get(0));
    Assert.assertEquals("儼", radicals.getCandidates("pox").get(1));
    Assert.assertEquals("囟", radicals.getCandidates("pox").get(2));
  }

  @Test
  public void firstSourceLoadedDecidesThePrimaryCandidate() throws Exception {
    CompactTrie.Builder radicals = new CompactTrie.Builder();
    HashSet<String> outs = new HashSet<>();
    // loadAllResources feeds overlays before the bundled table precisely so an imported
    // Boshiamy table decides the primary. Here the "overlay" ranks 到 first and the
    // "bundled" table would have ranked the rare 佽 first.
    mDict.parseRadicalStream(in("pri\t到\npri\t佽\n"), radicals, outs);
    mDict.parseRadicalStream(in("pri\t佽\npri\t到\npri\t侕\n"), radicals, outs);
    Assert.assertEquals("到", radicals.getCandidates("pri").get(0));
    Assert.assertEquals("佽", radicals.getCandidates("pri").get(1));
    // The bundled pass still contributes characters the overlay lacked.
    Assert.assertEquals("侕", radicals.getCandidates("pri").get(2));
  }

  /** Exposes the package-private @VisibleForTesting parseRadicalStream. */
  private static class TestableDict extends RadicalDictionary {
    TestableDict() {
      super("test", null, 0);
    }

    @Override
    protected void loadAllResources() {
      // not used in this test
    }
  }
}
