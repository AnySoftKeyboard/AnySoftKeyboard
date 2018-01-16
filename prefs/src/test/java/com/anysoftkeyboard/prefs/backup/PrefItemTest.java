package com.anysoftkeyboard.prefs.backup;

import android.support.v4.util.Pair;

import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;
import com.anysoftkeyboard.test.TestUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class PrefItemTest {

    private PrefItem mPrefItem;

    @Before
    public void setup() {
        mPrefItem = new PrefItem();
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testFailsIfKeyIsEmpty() {
        mPrefItem.addValue("", "value");
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testFailsIfKeyHasNonAsciiLetterCharacters() {
        mPrefItem.addValue("$", "value");
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testFailsIfKeyHasSpaces() {
        mPrefItem.addValue("key ", "value");
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testFailsIfKeyStartsWithDigit() {
        mPrefItem.addValue("1key", "value");
    }

    @Test
    public void testProperties() {
        mPrefItem.addValue("key", "value");
        mPrefItem.createChild().addValue("keyInner", "value inner");
        final PrefItem child = mPrefItem.createChild().createChild();
        child.addValue("veryInner", "so deep");
        child.addValue("veryInner2", "so deep again");

        Assert.assertEquals(1, TestUtils.convertToList(mPrefItem.getValues()).size());
        Assert.assertEquals(Pair.create("key", "value"), TestUtils.convertToList(mPrefItem.getValues()).get(0));

        Assert.assertEquals(2, TestUtils.convertToList(mPrefItem.getChildren()).size());

        Assert.assertEquals(1, TestUtils.convertToList(TestUtils.convertToList(mPrefItem.getChildren()).get(0).getValues()).size());
        Assert.assertEquals(Pair.create("keyInner", "value inner"), TestUtils.convertToList(TestUtils.convertToList(mPrefItem.getChildren()).get(0).getValues()).get(0));
        Assert.assertEquals(0, TestUtils.convertToList(TestUtils.convertToList(mPrefItem.getChildren()).get(0).getChildren()).size());

        Assert.assertEquals(0, TestUtils.convertToList(TestUtils.convertToList(mPrefItem.getChildren()).get(1).getValues()).size());
        final List<PrefItem> innerInnerChildren = TestUtils.convertToList(TestUtils.convertToList(mPrefItem.getChildren()).get(1).getChildren());
        Assert.assertEquals(1, innerInnerChildren.size());

        Assert.assertEquals(0, TestUtils.convertToList(innerInnerChildren.get(0).getChildren()).size());
        Assert.assertEquals(2, TestUtils.convertToList(innerInnerChildren.get(0).getValues()).size());

        Assert.assertEquals(Pair.create("veryInner", "so deep"), TestUtils.convertToList(innerInnerChildren.get(0).getValues()).get(0));
        Assert.assertEquals(Pair.create("veryInner2", "so deep again"), TestUtils.convertToList(innerInnerChildren.get(0).getValues()).get(1));
    }
}