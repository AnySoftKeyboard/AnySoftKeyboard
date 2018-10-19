package com.anysoftkeyboard.nextword;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.test.TestUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class NextWordPrefsProviderTest {

    @Test
    public void testId() {
        Assert.assertEquals("NextWordPrefsProvider", new NextWordPrefsProvider(getApplicationContext(), emptyList()).providerId());
    }

    @Test
    public void testEmptyLoad() {
        final NextWordPrefsProvider underTest = new NextWordPrefsProvider(getApplicationContext(), asList("en", "fr"));

        final PrefsRoot emptyRoot = underTest.getPrefsRoot();

        Assert.assertEquals(1, emptyRoot.getVersion());
        Assert.assertEquals(0, TestUtils.convertToList(emptyRoot.getValues()).size());
        Assert.assertEquals(2, TestUtils.convertToList(emptyRoot.getChildren()).size());

        final PrefItem emptyEn = TestUtils.convertToList(emptyRoot.getChildren()).get(0);
        final PrefItem emptyFr = TestUtils.convertToList(emptyRoot.getChildren()).get(1);

        Assert.assertEquals("en", emptyEn.getValue("locale"));
        Assert.assertEquals(0, TestUtils.convertToList(emptyEn.getChildren()).size());
        Assert.assertEquals("fr", emptyFr.getValue("locale"));
        Assert.assertEquals(0, TestUtils.convertToList(emptyFr.getChildren()).size());
    }

    @Test
    public void testHappyPath() throws Exception {
        final NextWordPrefsProvider underTest = new NextWordPrefsProvider(getApplicationContext(), asList("en", "fr"));

        final PrefsRoot initialRoot = new PrefsRoot(1);

        final PrefItem enRoot = initialRoot.createChild().addValue("locale", "en");

        final PrefItem enWordHello = enRoot.createChild()
                .addValue("word", "hello");

        enWordHello.createChild()
                .addValue("nextWord", "you")
                .addValue("usedCount", "10");

        enWordHello.createChild()
                .addValue("nextWord", "there")
                .addValue("usedCount", "7");

        final PrefItem enWordBye = enRoot.createChild()
                .addValue("word", "bye");

        enWordBye.createChild()
                .addValue("nextWord", "bye")
                .addValue("usedCount", "22");

        enWordBye.createChild()
                .addValue("nextWord", "you")
                .addValue("usedCount", "4");

        final PrefItem frRoot = initialRoot.createChild().addValue("locale", "fr");

        final PrefItem frWordHello = frRoot.createChild()
                .addValue("word", "bon");

        frWordHello.createChild()
                .addValue("nextWord", "jour")
                .addValue("usedCount", "9");


        underTest.storePrefsRoot(initialRoot);

        final PrefsRoot loadedRoot = underTest.getPrefsRoot();

        Assert.assertEquals(1, loadedRoot.getVersion());
        Assert.assertEquals(0, TestUtils.convertToList(loadedRoot.getValues()).size());
        Assert.assertEquals(2, TestUtils.convertToList(loadedRoot.getChildren()).size());

        final PrefItem loadedEn = TestUtils.convertToList(loadedRoot.getChildren()).get(0);
        final PrefItem loadedFr = TestUtils.convertToList(loadedRoot.getChildren()).get(1);

        Assert.assertEquals("en", loadedEn.getValue("locale"));
        Assert.assertEquals(2, TestUtils.convertToList(loadedEn.getChildren()).size());
        Assert.assertEquals("fr", loadedFr.getValue("locale"));
        Assert.assertEquals(1, TestUtils.convertToList(loadedFr.getChildren()).size());

        final PrefItem loadedHelloWord = TestUtils.convertToList(loadedEn.getChildren()).get(0);
        Assert.assertEquals("hello", loadedHelloWord.getValue("word"));
        Assert.assertEquals(2, TestUtils.convertToList(loadedHelloWord.getChildren()).size());
        Assert.assertEquals("you", TestUtils.convertToList(loadedHelloWord.getChildren()).get(0).getValue("nextWord"));
        Assert.assertEquals("2", TestUtils.convertToList(loadedHelloWord.getChildren()).get(0).getValue("usedCount"));
        Assert.assertEquals("there", TestUtils.convertToList(loadedHelloWord.getChildren()).get(1).getValue("nextWord"));
        Assert.assertEquals("2", TestUtils.convertToList(loadedHelloWord.getChildren()).get(1).getValue("usedCount"));

        final PrefItem loadedByeWord = TestUtils.convertToList(loadedEn.getChildren()).get(1);
        Assert.assertEquals("bye", loadedByeWord.getValue("word"));
        Assert.assertEquals(2, TestUtils.convertToList(loadedHelloWord.getChildren()).size());

        final PrefItem loadedBonWord = TestUtils.convertToList(loadedFr.getChildren()).get(0);
        Assert.assertEquals("bon", loadedBonWord.getValue("word"));
        Assert.assertEquals(1, TestUtils.convertToList(loadedBonWord.getChildren()).size());
    }
}