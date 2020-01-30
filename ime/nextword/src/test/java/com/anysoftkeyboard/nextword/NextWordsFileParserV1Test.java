package com.anysoftkeyboard.nextword;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class NextWordsFileParserV1Test {

    @Test
    public void testFlow() throws Exception {
        Map<String, NextWordsContainer> words = new HashMap<>();
        words.put("hello", new NextWordsContainer("hello", Collections.singletonList("is")));
        words.put("is", new NextWordsContainer("is", Collections.singletonList("it")));
        words.put("it", new NextWordsContainer("it", Collections.singletonList("me")));
        words.put("me", new NextWordsContainer("me", Collections.singletonList("you")));
        words.put("you", new NextWordsContainer("you", Collections.singletonList("looking")));
        words.put("looking", new NextWordsContainer("looking", Collections.singletonList("for")));
        words.put("for", new NextWordsContainer("for", Arrays.asList("hello", "me")));

        OutputStream outputStream =
                getApplicationContext().openFileOutput("next_words_test.txt", Context.MODE_PRIVATE);
        NextWordsFileParserV1 parserV1 = new NextWordsFileParserV1();
        parserV1.storeNextWords(words.values(), outputStream);

        outputStream.flush();
        outputStream.close();

        InputStream inputStream = getApplicationContext().openFileInput("next_words_test.txt");
        // reading VERSION
        Assert.assertEquals(1, inputStream.read());
        // reading the rest of the dictionary
        Iterable<NextWordsContainer> loadedWords = parserV1.loadStoredNextWords(inputStream);
        for (NextWordsContainer loadedWord : loadedWords) {
            Assert.assertTrue(words.containsKey(loadedWord.word));
            NextWordsContainer word = words.get(loadedWord.word);
            Assert.assertEquals(
                    word.getNextWordSuggestions().size(),
                    loadedWord.getNextWordSuggestions().size());
            for (int nextWordIndex = 0;
                    nextWordIndex < word.getNextWordSuggestions().size();
                    nextWordIndex++) {
                NextWord nextWord = word.getNextWordSuggestions().get(nextWordIndex);
                NextWord loadedNextWord = loadedWord.getNextWordSuggestions().get(nextWordIndex);
                Assert.assertEquals(nextWord.nextWord, loadedNextWord.nextWord);
            }
            words.remove(loadedWord.word);
        }

        Assert.assertEquals(0, words.size());
    }
}
