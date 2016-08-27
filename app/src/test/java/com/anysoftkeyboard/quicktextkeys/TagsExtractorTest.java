package com.anysoftkeyboard.quicktextkeys;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagsExtractorTest {

    private List<Keyboard.Key> mKeysForTest;
    private TagsExtractor mUnderTest;

    @Before
    public void setup() {
        mKeysForTest = new ArrayList<>();
        mKeysForTest.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        mKeysForTest.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        mKeysForTest.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        mKeysForTest.add(Mockito.mock(AnyKeyboard.AnyKey.class));

        mKeysForTest.get(0).text = "HAPPY";
        mKeysForTest.get(1).text = "ROSE";
        mKeysForTest.get(2).text = "PLANE";
        mKeysForTest.get(3).text = "SHRUG";

        Mockito.doReturn(Arrays.asList("face", "happy")).when((AnyKeyboard.AnyKey) mKeysForTest.get(0)).getKeyTags();
        Mockito.doReturn(Arrays.asList("flower", "rose")).when((AnyKeyboard.AnyKey) mKeysForTest.get(1)).getKeyTags();
        Mockito.doReturn(Arrays.asList("plane")).when((AnyKeyboard.AnyKey) mKeysForTest.get(2)).getKeyTags();
        Mockito.doReturn(Arrays.asList("face", "shrug")).when((AnyKeyboard.AnyKey) mKeysForTest.get(3)).getKeyTags();

        mUnderTest = new TagsExtractor(mKeysForTest);
    }

    @Test
    public void getOutputForTag() throws Exception {
        Assert.assertEquals(1, mUnderTest.getOutputForTag("happy").size());
        Assert.assertArrayEquals(new String[]{"HAPPY"}, mUnderTest.getOutputForTag("happy").toArray());
    }

    @Test
    public void getMultipleOutputsForTag() throws Exception {
        Assert.assertEquals(2, mUnderTest.getOutputForTag("face").size());
        Assert.assertArrayEquals(new String[]{"HAPPY", "SHRUG"}, mUnderTest.getOutputForTag("face").toArray());
    }

    @Test
    public void getNoneForUnknown() throws Exception {
        Assert.assertEquals(0, mUnderTest.getOutputForTag("ddd").size());
    }

}