package com.anysoftkeyboard.prefs.backup;

import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;
import com.anysoftkeyboard.test.TestUtils;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        child.addValue("veryInner", "so deep override");

        Assert.assertEquals(1, TestUtils.convertToList(mPrefItem.getValues()).size());
        Assert.assertEquals("value", mPrefItem.getValue("key"));

        Assert.assertEquals(2, TestUtils.convertToList(mPrefItem.getChildren()).size());

        Assert.assertEquals(
                1,
                TestUtils.convertToList(
                                TestUtils.convertToList(mPrefItem.getChildren()).get(0).getValues())
                        .size());
        Assert.assertEquals(
                "value inner",
                TestUtils.convertToList(mPrefItem.getChildren()).get(0).getValue("keyInner"));
        Assert.assertEquals(
                0,
                TestUtils.convertToList(
                                TestUtils.convertToList(mPrefItem.getChildren())
                                        .get(0)
                                        .getChildren())
                        .size());

        Assert.assertEquals(
                0,
                TestUtils.convertToList(
                                TestUtils.convertToList(mPrefItem.getChildren()).get(1).getValues())
                        .size());
        final List<PrefItem> innerInnerChildren =
                TestUtils.convertToList(
                        TestUtils.convertToList(mPrefItem.getChildren()).get(1).getChildren());
        Assert.assertEquals(1, innerInnerChildren.size());

        Assert.assertEquals(
                0, TestUtils.convertToList(innerInnerChildren.get(0).getChildren()).size());
        Assert.assertEquals(
                2, TestUtils.convertToList(innerInnerChildren.get(0).getValues()).size());

        Assert.assertEquals("so deep override", innerInnerChildren.get(0).getValue("veryInner"));
        Assert.assertEquals("so deep again", innerInnerChildren.get(0).getValue("veryInner2"));

        Assert.assertEquals("value", mPrefItem.getValue("key"));
        Assert.assertNull(mPrefItem.getValue("none"));
    }
}
