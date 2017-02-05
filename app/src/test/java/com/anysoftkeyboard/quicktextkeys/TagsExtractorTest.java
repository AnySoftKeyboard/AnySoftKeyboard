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
import java.util.List;

import static com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardTagsSearcher.MAGNIFYING_GLASS_CHARACTER;

@RunWith(RobolectricTestRunner.class)
public class TagsExtractorTest {

    private TagsExtractor mUnderTest;

    @Before
    public void setup() {
        List<Keyboard.Key> mKeysForTest = new ArrayList<>();
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

        List<Keyboard.Key> mKeysForTest2 = new ArrayList<>();
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
        Assert.assertEquals(3, mUnderTest.getOutputForTag("happy").size());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER+"happy", "HAPPY", "HAPPY"}, mUnderTest.getOutputForTag("happy").toArray());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER+"palm", "PALM"}, mUnderTest.getOutputForTag("palm").toArray());
    }

    @Test
    public void getOutputForTagWithCaps() throws Exception {
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER+"Palm", "PALM"}, mUnderTest.getOutputForTag("Palm").toArray());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER+"PALM", "PALM"}, mUnderTest.getOutputForTag("PALM").toArray());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER+"paLM", "PALM"}, mUnderTest.getOutputForTag("paLM").toArray());
    }

    @Test
    public void getMultipleOutputsForTag() throws Exception {
        Assert.assertEquals(5, mUnderTest.getOutputForTag("face").size());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER+"face", "HAPPY", "SHRUG", "HAPPY", "FACE"}, mUnderTest.getOutputForTag("face").toArray());
    }

    @Test
    public void getJustTypedForUnknown() throws Exception {
        Assert.assertEquals(1, mUnderTest.getOutputForTag("ddd").size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEmptyTagsListIsUnmodifiable() throws Exception {
        final List<CharSequence> list = mUnderTest.getOutputForTag("ddd");
        list.add("should fail");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNoneEmptyTagsListIsUnmodifiable() throws Exception {
        final List<CharSequence> list = mUnderTest.getOutputForTag("face");
        list.add("should fail");
    }

}