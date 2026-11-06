package com.anysoftkeyboard.dictionaries.sqlite;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.After;
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
    com.anysoftkeyboard.rx.TestRxSchedulers.drainAllTasks();
  }

  @After
  public void tearDown() {
    mDictionary.close();
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

  @Test
  public void testCrossInstanceStorageChangeNotification() {
    IgnoredWordsDictionary secondInstance =
        new IgnoredWordsDictionary(getApplicationContext(), "en");
    secondInstance.loadDictionary();
    com.anysoftkeyboard.rx.TestRxSchedulers.drainAllTasks();

    Assert.assertFalse(mDictionary.isIgnored("externally_added"));
    Assert.assertFalse(secondInstance.isIgnored("externally_added"));

    secondInstance.addWord("externally_added", 1);
    Assert.assertTrue(secondInstance.isIgnored("externally_added"));

    com.anysoftkeyboard.rx.TestRxSchedulers.drainAllTasks();

    Assert.assertTrue(mDictionary.isIgnored("externally_added"));

    secondInstance.deleteWord("externally_added");
    Assert.assertFalse(secondInstance.isIgnored("externally_added"));

    com.anysoftkeyboard.rx.TestRxSchedulers.drainAllTasks();

    Assert.assertFalse(mDictionary.isIgnored("externally_added"));

    secondInstance.close();
  }

  @Test
  public void testStorageChangeDoesNotAffectDifferentLocale() {
    IgnoredWordsDictionary frenchDict =
        new IgnoredWordsDictionary(getApplicationContext(), "fr");
    frenchDict.loadDictionary();
    com.anysoftkeyboard.rx.TestRxSchedulers.drainAllTasks();

    frenchDict.addWord("bonjour", 1);
    com.anysoftkeyboard.rx.TestRxSchedulers.drainAllTasks();

    Assert.assertTrue(frenchDict.isIgnored("bonjour"));
    Assert.assertFalse(mDictionary.isIgnored("bonjour"));

    frenchDict.close();
  }
}
