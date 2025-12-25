package com.anysoftkeyboard.utils;

import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class IMEUtilTest {

  ArrayList<CharSequence> mStringPool;

  @Before
  public void setUp() {
    mStringPool = new ArrayList<>();
  }

  @Test
  public void testRemoveDupesEmpty() throws Exception {
    ArrayList<CharSequence> list = new ArrayList<>(Collections.<CharSequence>emptyList());
    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(0, list.size());
  }

  @Test
  public void testRemoveDupesOneItem() throws Exception {
    ArrayList<CharSequence> list = new ArrayList<>(Collections.<CharSequence>singleton("typed"));
    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(1, list.size());
    Assert.assertEquals("typed", list.get(0));
  }

  @Test
  public void testRemoveDupesTwoItems() throws Exception {
    ArrayList<CharSequence> list = new ArrayList<>(Arrays.<CharSequence>asList("typed", "typed"));
    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(1, list.size());
    Assert.assertEquals("typed", list.get(0));
  }

  @Test
  public void testRemoveDupesOneItemTwoTypes() throws Exception {
    ArrayList<CharSequence> list =
        new ArrayList<>(Arrays.<CharSequence>asList("typed", "something"));
    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(2, list.size());
    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));
  }

  @Test
  public void testRemoveDupesTwoItemsTwoTypes() throws Exception {
    ArrayList<CharSequence> list =
        new ArrayList<>(Arrays.<CharSequence>asList("typed", "something", "something", "typed"));
    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(2, list.size());
    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));
  }

  @Test
  public void testRemoveDupesOnlyDupes() throws Exception {
    ArrayList<CharSequence> list =
        new ArrayList<>(Arrays.<CharSequence>asList("typed", "typed", "typed", "typed", "typed"));
    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(1, list.size());
    Assert.assertEquals("typed", list.get(0));
  }

  @Test
  public void testRemoveDupesOnlyDupesMultipleTypes() throws Exception {
    ArrayList<CharSequence> list =
        new ArrayList<>(
            Arrays.<CharSequence>asList(
                "typed",
                "something",
                "something",
                "typed",
                "banana",
                "banana",
                "something",
                "typed",
                "car",
                "typed"));
    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(4, list.size());
    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));
    Assert.assertEquals("banana", list.get(2));
    Assert.assertEquals("car", list.get(3));
  }

  @Test
  public void testRemoveDupesNoDupes() throws Exception {
    ArrayList<CharSequence> list =
        new ArrayList<>(Arrays.<CharSequence>asList("typed", "something", "banana", "car"));
    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(4, list.size());
    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));
    Assert.assertEquals("banana", list.get(2));
    Assert.assertEquals("car", list.get(3));
  }

  @Test
  public void testRemoveDupesDupeIsNotFirst() throws Exception {
    ArrayList<CharSequence> list =
        new ArrayList<>(
            Arrays.<CharSequence>asList("typed", "something", "duped", "duped", "something"));
    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(3, list.size());
    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));
    Assert.assertEquals("duped", list.get(2));
  }

  @Test
  public void testRemoveDupesDupeIsNotFirstNoRecycle() throws Exception {
    ArrayList<CharSequence> list =
        new ArrayList<>(
            Arrays.<CharSequence>asList("typed", "something", "duped", "duped", "something"));

    Assert.assertEquals(0, mStringPool.size());

    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(3, list.size());
    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));
    Assert.assertEquals("duped", list.get(2));

    Assert.assertEquals(0, mStringPool.size());
  }

  @Test
  public void testRemoveDupesDupeIsNotFirstWithRecycle() throws Exception {
    ArrayList<CharSequence> list =
        new ArrayList<>(
            Arrays.<CharSequence>asList(
                "typed",
                "something",
                "duped",
                new StringBuilder("duped"),
                "something",
                new StringBuilder("new")));

    Assert.assertEquals(0, mStringPool.size());

    IMEUtil.removeDupes(list, mStringPool);
    Assert.assertEquals(4, list.size());
    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));
    Assert.assertEquals("duped", list.get(2));
    Assert.assertEquals("new", list.get(3).toString());
    Assert.assertTrue(list.get(3) instanceof StringBuilder);

    Assert.assertEquals(1, mStringPool.size());
    Assert.assertEquals("duped", mStringPool.get(0).toString());
  }

  @Test
  public void testTrimSuggestionsWhenNoNeed() {
    ArrayList<CharSequence> list =
        new ArrayList<>(
            Arrays.<CharSequence>asList("typed", "something", "duped", "duped", "something"));
    IMEUtil.tripSuggestions(list, 10, mStringPool);
    Assert.assertEquals(5, list.size());

    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));
    Assert.assertEquals("duped", list.get(2));
    Assert.assertEquals("duped", list.get(3));
    Assert.assertEquals("something", list.get(4));
  }

  @Test
  public void testTrimSuggestionsWhenOneNeeded() {
    ArrayList<CharSequence> list =
        new ArrayList<>(
            Arrays.<CharSequence>asList("typed", "something", "duped", "duped", "something"));
    IMEUtil.tripSuggestions(list, 4, mStringPool);
    Assert.assertEquals(4, list.size());

    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));
    Assert.assertEquals("duped", list.get(2));
    Assert.assertEquals("duped", list.get(3));
  }

  @Test
  public void testTrimSuggestionsWhenThreeNeeded() {
    ArrayList<CharSequence> list =
        new ArrayList<>(
            Arrays.<CharSequence>asList("typed", "something", "duped", "duped", "something"));
    IMEUtil.tripSuggestions(list, 2, mStringPool);
    Assert.assertEquals(2, list.size());

    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));
  }

  @Test
  public void testTrimSuggestionsWithRecycleBackToPool() {
    ArrayList<CharSequence> list =
        new ArrayList<>(
            Arrays.<CharSequence>asList(
                "typed", "something", "duped", new StringBuilder("duped"), "something"));
    Assert.assertEquals(0, mStringPool.size());

    IMEUtil.tripSuggestions(list, 2, mStringPool);
    Assert.assertEquals(2, list.size());

    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));

    Assert.assertEquals(1, mStringPool.size());
    Assert.assertEquals("duped", mStringPool.get(0).toString());
    Assert.assertTrue(mStringPool.get(0) instanceof StringBuilder);
  }

  @Test
  public void testTrimSuggestionsWithMultipleRecycleBackToPool() {
    ArrayList<CharSequence> list =
        new ArrayList<>(
            Arrays.<CharSequence>asList(
                "typed",
                "something",
                "duped",
                new StringBuilder("duped"),
                new StringBuilder("new"),
                new StringBuilder("car"),
                "something"));
    Assert.assertEquals(0, mStringPool.size());

    IMEUtil.tripSuggestions(list, 2, mStringPool);
    Assert.assertEquals(2, list.size());

    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));

    Assert.assertEquals(3, mStringPool.size());
    Assert.assertEquals("duped", mStringPool.get(0).toString());
    Assert.assertTrue(mStringPool.get(0) instanceof StringBuilder);
    Assert.assertEquals("new", mStringPool.get(1).toString());
    Assert.assertTrue(mStringPool.get(1) instanceof StringBuilder);
    Assert.assertEquals("car", mStringPool.get(2).toString());
    Assert.assertTrue(mStringPool.get(2) instanceof StringBuilder);
  }

  @Test
  public void testTrimSuggestionsNoRecycleBackToPool() {
    ArrayList<CharSequence> list =
        new ArrayList<>(
            Arrays.<CharSequence>asList("typed", "something", "duped", "car", "something"));
    Assert.assertEquals(0, mStringPool.size());

    IMEUtil.tripSuggestions(list, 2, mStringPool);
    Assert.assertEquals(2, list.size());

    Assert.assertEquals("typed", list.get(0));
    Assert.assertEquals("something", list.get(1));

    Assert.assertEquals(0, mStringPool.size());
  }

  @Test
  public void testShouldHonorNoSuggestionsFlag_OnlyNoSuggestions() {
    // NO_SUGGESTIONS alone should be honored
    int flags = EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
    Assert.assertTrue(
        "Should honor NO_SUGGESTIONS when no contradictory flags are set",
        IMEUtil.shouldHonorNoSuggestionsFlag(flags));
  }

  @Test
  public void testShouldHonorNoSuggestionsFlag_NoSuggestionsWithAutoCorrect() {
    // NO_SUGGESTIONS + AUTO_CORRECT should ignore NO_SUGGESTIONS (contradictory)
    int flags = EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS | EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT;
    Assert.assertFalse(
        "Should ignore NO_SUGGESTIONS when AUTO_CORRECT is also set",
        IMEUtil.shouldHonorNoSuggestionsFlag(flags));
  }

  @Test
  public void testShouldHonorNoSuggestionsFlag_NoSuggestionsWithAutoComplete() {
    // NO_SUGGESTIONS + AUTO_COMPLETE should ignore NO_SUGGESTIONS (contradictory)
    int flags = EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS | EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE;
    Assert.assertFalse(
        "Should ignore NO_SUGGESTIONS when AUTO_COMPLETE is also set",
        IMEUtil.shouldHonorNoSuggestionsFlag(flags));
  }

  @Test
  public void testShouldHonorNoSuggestionsFlag_NoSuggestionsWithBothAutoFlags() {
    // NO_SUGGESTIONS + AUTO_CORRECT + AUTO_COMPLETE should ignore NO_SUGGESTIONS
    int flags =
        EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            | EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT
            | EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE;
    Assert.assertFalse(
        "Should ignore NO_SUGGESTIONS when both AUTO_CORRECT and AUTO_COMPLETE are set",
        IMEUtil.shouldHonorNoSuggestionsFlag(flags));
  }

  @Test
  public void testShouldHonorNoSuggestionsFlag_NoFlagsSet() {
    // No NO_SUGGESTIONS flag -> false (don't honor what isn't there)
    int flags = 0;
    Assert.assertFalse(
        "Should return false when NO_SUGGESTIONS is not set",
        IMEUtil.shouldHonorNoSuggestionsFlag(flags));
  }

  @Test
  public void testShouldHonorNoSuggestionsFlag_OnlyAutoCorrect() {
    // Only AUTO_CORRECT, no NO_SUGGESTIONS -> false
    int flags = EditorInfo.TYPE_TEXT_FLAG_AUTO_CORRECT;
    Assert.assertFalse(
        "Should return false when only AUTO_CORRECT is set",
        IMEUtil.shouldHonorNoSuggestionsFlag(flags));
  }

  @Test
  public void testShouldHonorNoSuggestionsFlag_OnlyAutoComplete() {
    // Only AUTO_COMPLETE, no NO_SUGGESTIONS -> false
    int flags = EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE;
    Assert.assertFalse(
        "Should return false when only AUTO_COMPLETE is set",
        IMEUtil.shouldHonorNoSuggestionsFlag(flags));
  }

  @Test
  public void testShouldHonorNoSuggestionsFlag_GoogleKeepScenario() {
    // Real-world scenario: Google Keep uses 0xac000
    // = NO_SUGGESTIONS (0x80000) + MULTI_LINE (0x20000) + AUTO_CORRECT (0x8000) +
    // CAP_SENTENCES
    // (0x4000)
    int flags = 0xac000;
    Assert.assertFalse(
        "Should ignore NO_SUGGESTIONS in Google Keep scenario (contradicts AUTO_CORRECT)",
        IMEUtil.shouldHonorNoSuggestionsFlag(flags));
  }

  @Test
  public void testShouldHonorNoSuggestionsFlag_NoSuggestionsWithOtherNonAutoFlags() {
    // NO_SUGGESTIONS + other flags that don't contradict (like CAP_SENTENCES,
    // MULTI_LINE)
    int flags =
        EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
            | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
    Assert.assertTrue(
        "Should honor NO_SUGGESTIONS when only non-contradictory flags are set",
        IMEUtil.shouldHonorNoSuggestionsFlag(flags));
  }

  @Test
  public void testEditDistance() {
    // delete
    Assert.assertEquals(1, IMEUtil.editDistance("kitten", "kiten".toCharArray(), 0, 5));
    // insert
    Assert.assertEquals(1, IMEUtil.editDistance("kiten", "kitten".toCharArray(), 0, 6));
    // substitute
    Assert.assertEquals(1, IMEUtil.editDistance("kitten", "kittin".toCharArray(), 0, 6));
    // transpose
    Assert.assertEquals(1, IMEUtil.editDistance("kitten", "kittne".toCharArray(), 0, 6));
    // transpose + delete
    Assert.assertEquals(2, IMEUtil.editDistance("kitten", "kitne".toCharArray(), 0, 5));
    // empty
    Assert.assertEquals(6, IMEUtil.editDistance("kitten", "".toCharArray(), 0, 0));
    Assert.assertEquals(6, IMEUtil.editDistance("", "kitten".toCharArray(), 0, 6));
    Assert.assertEquals(0, IMEUtil.editDistance("", "".toCharArray(), 0, 0));
    // equal
    Assert.assertEquals(0, IMEUtil.editDistance("kitten", "kitten".toCharArray(), 0, 6));
    // case insensitive
    Assert.assertEquals(0, IMEUtil.editDistance("kitten", "Kitten".toCharArray(), 0, 6));
    // complex
    Assert.assertEquals(3, IMEUtil.editDistance("kitten", "sitting".toCharArray(), 0, 7));
  }
}
