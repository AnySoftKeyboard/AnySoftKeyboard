package com.anysoftkeyboard.nextword;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class NextWordComparatorTest {
  @Test
  public void testSortOrderDescending() {
    List<NextWord> words = new ArrayList<>();
    words.add(new NextWord("least", 1));
    words.add(new NextWord("most", 100));
    words.add(new NextWord("middle", 50));

    Collections.sort(words, new NextWord.NextWordComparator());

    Assert.assertEquals("most", words.get(0).nextWord);
    Assert.assertEquals(100, words.get(0).getUsedCount());

    Assert.assertEquals("middle", words.get(1).nextWord);
    Assert.assertEquals(50, words.get(1).getUsedCount());

    Assert.assertEquals("least", words.get(2).nextWord);
    Assert.assertEquals(1, words.get(2).getUsedCount());
  }
}
