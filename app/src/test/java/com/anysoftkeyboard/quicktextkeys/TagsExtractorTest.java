package com.anysoftkeyboard.quicktextkeys;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class TagsExtractorTest {

    private List<Keyboard.Key> mKeysForTest;
    private List<Keyboard.Key> mKeysForTest2;
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

        mKeysForTest2 = new ArrayList<>();
        mKeysForTest2.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        mKeysForTest2.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        mKeysForTest2.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        mKeysForTest2.add(Mockito.mock(AnyKeyboard.AnyKey.class));

        mKeysForTest2.get(0).text = "CAR";
        mKeysForTest2.get(1).text = "HAPPY";
        mKeysForTest2.get(2).text = "PALM";
        mKeysForTest2.get(3).text = "FACE";

        Mockito.doReturn(Arrays.asList("car", "vehicle")).when((AnyKeyboard.AnyKey) mKeysForTest2.get(0)).getKeyTags();
        Mockito.doReturn(Arrays.asList("person", "face", "happy")).when((AnyKeyboard.AnyKey) mKeysForTest2.get(1)).getKeyTags();
        Mockito.doReturn(Arrays.asList("tree", "palm")).when((AnyKeyboard.AnyKey) mKeysForTest2.get(2)).getKeyTags();
        Mockito.doReturn(Arrays.asList("face")).when((AnyKeyboard.AnyKey) mKeysForTest2.get(3)).getKeyTags();

        mUnderTest = new TagsExtractor(Arrays.asList(mKeysForTest, mKeysForTest2));
    }

    @Test
    public void getOutputForTag() throws Exception {
        Assert.assertEquals(2, mUnderTest.getOutputForTag("happy").size());
        Assert.assertArrayEquals(new String[]{"HAPPY", "HAPPY"}, mUnderTest.getOutputForTag("happy").toArray());
        Assert.assertArrayEquals(new String[]{"PALM"}, mUnderTest.getOutputForTag("palm").toArray());
    }

    @Test
    public void getMultipleOutputsForTag() throws Exception {
        Assert.assertEquals(4, mUnderTest.getOutputForTag("face").size());
        Assert.assertArrayEquals(new String[]{"HAPPY", "SHRUG", "HAPPY", "FACE"}, mUnderTest.getOutputForTag("face").toArray());
    }

    @Test
    public void getNoneForUnknown() throws Exception {
        Assert.assertEquals(0, mUnderTest.getOutputForTag("ddd").size());
    }

    @Test
    public void testEmptyTagsListIsUnmodifiable() throws Exception {
        final List<CharSequence> list = mUnderTest.getOutputForTag("ddd");
        Assert.assertSame(Collections.EMPTY_LIST, list);
    }

    @Test
    public void testNoneEmptyTagsListIsUnmodifiable() throws Exception {
        final List<CharSequence> list = mUnderTest.getOutputForTag("face");
        Assert.assertEquals(Collections.unmodifiableList(new ArrayList<CharSequence>()).getClass(), list.getClass());
    }

}