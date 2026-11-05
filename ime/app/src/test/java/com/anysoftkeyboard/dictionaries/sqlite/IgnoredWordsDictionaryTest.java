package com.anysoftkeyboard.dictionaries.sqlite;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class IgnoredWordsDictionaryTest {

  private IgnoredWordsDictionary mDictionary;

  @Before
  public void setUp() {
    getApplicationContext().deleteDatabase(IgnoredWordsDictionary.IGNORED_WORDS_DB);
    mDictionary = new IgnoredWordsDictionary(getApplicationContext(), "en");
    mDictionary.loadDictionary();
  }

  @Test
  public void testEmptyByDefault() {
    Assert.assertTrue(mDictionary.isEmpty());
    Assert.assertFalse(mDictionary.isIgnored("hello"));
  }

  @Test
  public void testAddAndCheckIgnored() {
    mDictionary.addWord("badword", 1);
    Assert.assertFalse(mDictionary.isEmpty());
    Assert.assertTrue(mDictionary.isIgnored("badword"));
    Assert.assertTrue(mDictionary.isIgnored("BadWord"));
    Assert.assertTrue(mDictionary.isIgnored("BADWORD"));
    Assert.assertFalse(mDictionary.isIgnored("goodword"));

    char[] chars = "some badword here".toCharArray();
    Assert.assertTrue(mDictionary.isIgnored(chars, 5, 7));
    Assert.assertFalse(mDictionary.isIgnored(chars, 0, 4));
  }

  @Test
  public void testDeleteWord() {
    mDictionary.addWord("test", 1);
    Assert.assertTrue(mDictionary.isIgnored("test"));

    mDictionary.deleteWord("test");
    Assert.assertFalse(mDictionary.isIgnored("test"));
    Assert.assertTrue(mDictionary.isEmpty());
  }

  @Test
  public void testPersistsToDatabase() {
    mDictionary.addWord("persisted", 1);
    mDictionary.close();

    IgnoredWordsDictionary newDict = new IgnoredWordsDictionary(getApplicationContext(), "en");
    newDict.loadDictionary();
    Assert.assertTrue(newDict.isIgnored("persisted"));
    newDict.close();
  }
}
