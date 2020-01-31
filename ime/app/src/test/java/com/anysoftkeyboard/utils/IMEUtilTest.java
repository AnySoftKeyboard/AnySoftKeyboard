package com.anysoftkeyboard.utils;

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
        ArrayList<CharSequence> list =
                new ArrayList<>(Collections.<CharSequence>singleton("typed"));
        IMEUtil.removeDupes(list, mStringPool);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("typed", list.get(0));
    }

    @Test
    public void testRemoveDupesTwoItems() throws Exception {
        ArrayList<CharSequence> list =
                new ArrayList<>(Arrays.<CharSequence>asList("typed", "typed"));
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
                new ArrayList<>(
                        Arrays.<CharSequence>asList("typed", "something", "something", "typed"));
        IMEUtil.removeDupes(list, mStringPool);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("typed", list.get(0));
        Assert.assertEquals("something", list.get(1));
    }

    @Test
    public void testRemoveDupesOnlyDupes() throws Exception {
        ArrayList<CharSequence> list =
                new ArrayList<>(
                        Arrays.<CharSequence>asList("typed", "typed", "typed", "typed", "typed"));
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
                        Arrays.<CharSequence>asList(
                                "typed", "something", "duped", "duped", "something"));
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
                        Arrays.<CharSequence>asList(
                                "typed", "something", "duped", "duped", "something"));

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
                        Arrays.<CharSequence>asList(
                                "typed", "something", "duped", "duped", "something"));
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
                        Arrays.<CharSequence>asList(
                                "typed", "something", "duped", "duped", "something"));
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
                        Arrays.<CharSequence>asList(
                                "typed", "something", "duped", "duped", "something"));
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
                                "typed",
                                "something",
                                "duped",
                                new StringBuilder("duped"),
                                "something"));
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
                        Arrays.<CharSequence>asList(
                                "typed", "something", "duped", "car", "something"));
        Assert.assertEquals(0, mStringPool.size());

        IMEUtil.tripSuggestions(list, 2, mStringPool);
        Assert.assertEquals(2, list.size());

        Assert.assertEquals("typed", list.get(0));
        Assert.assertEquals("something", list.get(1));

        Assert.assertEquals(0, mStringPool.size());
    }
}
