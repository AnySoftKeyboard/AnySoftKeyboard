package com.anysoftkeyboard.nextword;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class NextWordsContainerTest {

    @Test
    public void testGetNextWordSuggestionsSortsCorrectly() {
        NextWordsContainer container = new NextWordsContainer("hello");
        container.markWordAsUsed("world");
        container.markWordAsUsed("world"); // usage 2
        container.markWordAsUsed("there"); // usage 1

        // usage desc: world(2), there(1)
        var suggestions = container.getNextWordSuggestions();
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("world", suggestions.get(0).nextWord);
        Assert.assertEquals("there", suggestions.get(1).nextWord);

        // Update counts
        container.markWordAsUsed("there");
        container.markWordAsUsed("there"); // usage 3

        // usage desc: there(3), world(2)
        suggestions = container.getNextWordSuggestions();
        Assert.assertEquals(2, suggestions.size());
        Assert.assertEquals("there", suggestions.get(0).nextWord);
        Assert.assertEquals("world", suggestions.get(1).nextWord);
    }

    @Test
    public void testGetNextWordSuggestionsOptimized() {
        // This test primarily ensures that the caching mechanism doesn't break logic.
        // It's hard to test "performance" or "sort happened" without spying,
        // but we can verify consistency.
        NextWordsContainer container = new NextWordsContainer("hello");
        container.markWordAsUsed("A");
        container.markWordAsUsed("B");
        container.markWordAsUsed("B"); // B(2), A(1)

        Assert.assertEquals("B", container.getNextWordSuggestions().get(0).nextWord);
        // Call again, should still be B (cached)
        Assert.assertEquals("B", container.getNextWordSuggestions().get(0).nextWord);

        container.markWordAsUsed("A");
        container.markWordAsUsed("A"); // A(3), B(2)

        // Should update
        Assert.assertEquals("A", container.getNextWordSuggestions().get(0).nextWord);
    }
}
