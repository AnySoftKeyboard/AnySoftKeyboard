package com.anysoftkeyboard.dictionaries.sqlite;

import android.support.v4.util.Pair;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.dictionaries.BTreeDictionary;
import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.test.TestUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class WordsSQLiteConnectionPrefsProviderTest {
    public static final String DATABASE_FILENAME = "TESTDB";
    private WordsSQLiteConnectionPrefsProvider mUnderTest;

    @Before
    public void setUp() {
        mUnderTest = new WordsSQLiteConnectionPrefsProvider(RuntimeEnvironment.application, DATABASE_FILENAME, Arrays.asList("en", "fr"));
    }

    @Test
    public void testId() {
        Assert.assertEquals("WordsSQLiteConnectionPrefsProvider", mUnderTest.providerId());
    }

    @Test
    public void testBackupAndLoad() throws Exception {
        WordsSQLiteConnection connetionEn = new WordsSQLiteConnection(RuntimeEnvironment.application, DATABASE_FILENAME, "en");
        connetionEn.addWord("one", 1);
        connetionEn.addWord("two", 2);
        WordsSQLiteConnection connetionFr = new WordsSQLiteConnection(RuntimeEnvironment.application, DATABASE_FILENAME, "fr");
        connetionFr.addWord("un", 1);

        final PrefsRoot prefsRoot = mUnderTest.getPrefsRoot();
        Assert.assertEquals(2, TestUtils.convertToList(prefsRoot.getChildren()).size());
        PrefItem en = TestUtils.convertToList(prefsRoot.getChildren()).get(0);
        PrefItem fr = TestUtils.convertToList(prefsRoot.getChildren()).get(1);

        Assert.assertEquals("en", en.getValue("locale"));
        final Map<String, String> enWords = TestUtils.convertToMap(TestUtils.convertToList(en.getChildren()),
                prefItem -> Pair.create(prefItem.getValue("word"), prefItem.getValue("freq")));

        Assert.assertEquals(2, enWords.size());
        Assert.assertEquals("1", enWords.get("one"));
        Assert.assertEquals("2", enWords.get("two"));

        Assert.assertEquals("fr", fr.getValue("locale"));
        final Map<String, String> frWords = TestUtils.convertToMap(TestUtils.convertToList(fr.getChildren()),
                prefItem -> Pair.create(prefItem.getValue("word"), prefItem.getValue("freq")));
        Assert.assertEquals(1, frWords.size());
        Assert.assertEquals("1", frWords.get("un"));

        connetionEn.deleteWord("one");
        connetionEn.deleteWord("two");
        connetionFr.deleteWord("un");

        WordsCollector collector = new WordsCollector();
        connetionEn.loadWords(collector);
        Assert.assertEquals(0, collector.loadedWords.size());
        connetionFr.loadWords(collector);
        Assert.assertEquals(0, collector.loadedWords.size());

        mUnderTest.storePrefsRoot(prefsRoot);

        connetionEn.loadWords(collector);
        Assert.assertEquals(2, collector.loadedWords.size());
        Assert.assertEquals(Integer.valueOf(1), collector.loadedWords.get("one"));
        Assert.assertEquals(Integer.valueOf(2), collector.loadedWords.get("two"));
        collector.loadedWords.clear();

        connetionFr.loadWords(collector);
        Assert.assertEquals(1, collector.loadedWords.size());
        Assert.assertEquals(Integer.valueOf(1), collector.loadedWords.get("un"));
    }

    private static class WordsCollector implements BTreeDictionary.WordReadListener {
        public final Map<String, Integer> loadedWords = new HashMap<>();

        @Override
        public boolean onWordRead(String word, int frequency) {
            loadedWords.put(word, frequency);
            return true;
        }
    }
}