package com.anysoftkeyboard.dictionaries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class CompactTrieTest {

  @Test
  public void testEmptyTrie() {
    CompactTrie trie = CompactTrie.EMPTY;
    Assert.assertTrue(trie.isEmpty());
    Assert.assertEquals(0, trie.size());
    Assert.assertTrue(trie.get("a").isEmpty());
    Assert.assertFalse(trie.hasPrefix("a"));
  }

  @Test
  public void testSingleEntry() {
    CompactTrie.Builder builder = new CompactTrie.Builder();
    builder.addCandidate("a", "對");
    CompactTrie trie = builder.build();

    Assert.assertFalse(trie.isEmpty());
    Assert.assertEquals(1, trie.size());

    List<String> results = trie.get("a");
    Assert.assertEquals(1, results.size());
    Assert.assertEquals("對", results.get(0));

    Assert.assertTrue(trie.hasPrefix("a"));
    Assert.assertFalse(trie.hasPrefix("b"));
    Assert.assertFalse(trie.hasPrefix("aa"));
    Assert.assertTrue(trie.get("b").isEmpty());
  }

  @Test
  public void testMultipleCandidatesSameKey() {
    CompactTrie.Builder builder = new CompactTrie.Builder();
    builder.addCandidate("a", "對");
    builder.addCandidate("a", "的");
    builder.addCandidate("a", "對"); // duplicate should be ignored

    CompactTrie trie = builder.build();
    Assert.assertEquals(1, trie.size());

    List<String> results = trie.get("a");
    Assert.assertEquals(2, results.size());
    Assert.assertEquals("對", results.get(0));
    Assert.assertEquals("的", results.get(1));
  }

  @Test
  public void testMultipleKeysSharingPrefixes() {
    CompactTrie.Builder builder = new CompactTrie.Builder();
    builder.addCandidate("a", "對");
    builder.addCandidate("aa", "寸");
    builder.addCandidate("ag", "威");
    builder.addCandidate("abc", "測");

    CompactTrie trie = builder.build();
    Assert.assertEquals(4, trie.size());

    Assert.assertEquals("對", trie.get("a").get(0));
    Assert.assertEquals("寸", trie.get("aa").get(0));
    Assert.assertEquals("威", trie.get("ag").get(0));
    Assert.assertEquals("測", trie.get("abc").get(0));

    // Prefixes
    Assert.assertTrue(trie.hasPrefix("a"));
    Assert.assertTrue(trie.hasPrefix("aa"));
    Assert.assertTrue(trie.hasPrefix("ag"));
    Assert.assertTrue(trie.hasPrefix("ab"));
    Assert.assertTrue(trie.hasPrefix("abc"));

    // Non-prefixes
    Assert.assertFalse(trie.hasPrefix("b"));
    Assert.assertFalse(trie.hasPrefix("ax"));
    Assert.assertFalse(trie.hasPrefix("abcd"));
  }

  @Test
  public void testFrequencySorting() {
    CompactTrie.Builder builder = new CompactTrie.Builder();
    builder.addCandidate("a", "對");
    builder.addCandidate("a", "对");

    Map<String, Integer> rank = new HashMap<>();
    rank.put("对", 1);
    rank.put("對", 5000);

    builder.applyFrequencyOrdering(rank);
    CompactTrie trie = builder.build();

    List<String> results = trie.get("a");
    Assert.assertEquals(2, results.size());
    Assert.assertEquals("对", results.get(0));
    Assert.assertEquals("對", results.get(1));
  }

  @Test
  public void testToBuilderRoundTrip() {
    CompactTrie.Builder builder = new CompactTrie.Builder();
    builder.addCandidate("a", "對");
    builder.addCandidate("aa", "寸");
    CompactTrie trie = builder.build();

    CompactTrie.Builder mutatedBuilder = trie.toBuilder();
    mutatedBuilder.addCandidate("ag", "威");
    CompactTrie updatedTrie = mutatedBuilder.build();

    Assert.assertEquals(3, updatedTrie.size());
    Assert.assertEquals("對", updatedTrie.get("a").get(0));
    Assert.assertEquals("寸", updatedTrie.get("aa").get(0));
    Assert.assertEquals("威", updatedTrie.get("ag").get(0));
  }
}
