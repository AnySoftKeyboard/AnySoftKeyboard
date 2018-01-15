package com.anysoftkeyboard.quicktextkeys;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.base.dictionaries.KeyCodesProvider;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardTagsSearcher.MAGNIFYING_GLASS_CHARACTER;

@RunWith(AnySoftKeyboardTestRunner.class)
public class TagsExtractorTest {

    private TagsExtractor mUnderTest;
    private KeyCodesProvider mWordComposer;
    private QuickKeyHistoryRecords mQuickKeyHistoryRecords;

    @Before
    public void setup() {
        mWordComposer = Mockito.mock(KeyCodesProvider.class);

        List<Keyboard.Key> keysForTest = new ArrayList<>();
        keysForTest.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        keysForTest.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        keysForTest.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        keysForTest.add(Mockito.mock(AnyKeyboard.AnyKey.class));

        keysForTest.get(0).text = "HAPPY";
        keysForTest.get(1).text = "ROSE";
        keysForTest.get(2).text = "PLANE";
        keysForTest.get(3).text = "SHRUG";

        Mockito.doReturn(Arrays.asList("face", "happy")).when((AnyKeyboard.AnyKey) keysForTest.get(0)).getKeyTags();
        Mockito.doReturn(Arrays.asList("flower", "rose")).when((AnyKeyboard.AnyKey) keysForTest.get(1)).getKeyTags();
        Mockito.doReturn(Arrays.asList("plane")).when((AnyKeyboard.AnyKey) keysForTest.get(2)).getKeyTags();
        Mockito.doReturn(Arrays.asList("face", "shrug")).when((AnyKeyboard.AnyKey) keysForTest.get(3)).getKeyTags();

        List<Keyboard.Key> keysForTest2 = new ArrayList<>();
        keysForTest2.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        keysForTest2.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        keysForTest2.add(Mockito.mock(AnyKeyboard.AnyKey.class));
        keysForTest2.add(Mockito.mock(AnyKeyboard.AnyKey.class));

        keysForTest2.get(0).text = "CAR";
        keysForTest2.get(1).text = "HAPPY";
        keysForTest2.get(2).text = "PALM";
        keysForTest2.get(3).text = "FACE";

        Mockito.doReturn(Arrays.asList("car", "vehicle")).when((AnyKeyboard.AnyKey) keysForTest2.get(0)).getKeyTags();
        Mockito.doReturn(Arrays.asList("person", "face", "happy")).when((AnyKeyboard.AnyKey) keysForTest2.get(1)).getKeyTags();
        Mockito.doReturn(Arrays.asList("tree", "palm")).when((AnyKeyboard.AnyKey) keysForTest2.get(2)).getKeyTags();
        Mockito.doReturn(Arrays.asList("face")).when((AnyKeyboard.AnyKey) keysForTest2.get(3)).getKeyTags();
        mQuickKeyHistoryRecords = new QuickKeyHistoryRecords(AnyApplication.prefs(RuntimeEnvironment.application));
        mUnderTest = new TagsExtractorImpl(RuntimeEnvironment.application, Arrays.asList(keysForTest, keysForTest2), mQuickKeyHistoryRecords);
    }

    @Test
    public void getOutputForTag() throws Exception {
        Assert.assertEquals(3, mUnderTest.getOutputForTag("happy", mWordComposer).size());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "happy", "HAPPY", "HAPPY"}, mUnderTest.getOutputForTag("happy", mWordComposer).toArray());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "palm", "PALM"}, mUnderTest.getOutputForTag("palm", mWordComposer).toArray());
    }

    @Test
    public void getOutputForTagWithCaps() throws Exception {
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "Palm", "PALM"}, mUnderTest.getOutputForTag("Palm", mWordComposer).toArray());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "PALM", "PALM"}, mUnderTest.getOutputForTag("PALM", mWordComposer).toArray());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "paLM", "PALM"}, mUnderTest.getOutputForTag("paLM", mWordComposer).toArray());
    }

    @Test
    public void getMultipleOutputsForTag() throws Exception {
        Assert.assertEquals(5, mUnderTest.getOutputForTag("face", mWordComposer).size());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "face", "HAPPY", "SHRUG", "HAPPY", "FACE"}, mUnderTest.getOutputForTag("face", mWordComposer).toArray());
    }

    @Test
    public void getJustTypedForUnknown() throws Exception {
        setupWordComposerFor("ddd");
        Assert.assertEquals(1, mUnderTest.getOutputForTag("ddd", mWordComposer).size());
    }

    @Test
    public void testShowSuggestionWhenIncompleteTyped() throws Exception {
        setupWordComposerFor("pa");
        final List<CharSequence> outputForTag = mUnderTest.getOutputForTag("pa", mWordComposer);
        Assert.assertEquals(2, outputForTag.size());
        Assert.assertEquals(MAGNIFYING_GLASS_CHARACTER + "pa", outputForTag.get(0));
        Assert.assertEquals("PALM", outputForTag.get(1));
    }

    @Test
    public void testShowHistoryWhenStartingTagSearch() throws Exception {
        //adding history
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        List<QuickKeyHistoryRecords.HistoryKey> history = mQuickKeyHistoryRecords.getCurrentHistory();
        Assert.assertEquals(1, history.size());
        mQuickKeyHistoryRecords.store("palm", "PALM");
        history = mQuickKeyHistoryRecords.getCurrentHistory();
        Assert.assertEquals(2, history.size());
        mQuickKeyHistoryRecords.store("tree", "TREE");
        //simulating start of tag search
        setupWordComposerFor("");
        final List<CharSequence> outputForTag = mUnderTest.getOutputForTag("", mWordComposer);
        Assert.assertEquals(4, outputForTag.size());
        Assert.assertEquals(MAGNIFYING_GLASS_CHARACTER, outputForTag.get(0));
        Assert.assertEquals("TREE", outputForTag.get(1));
        Assert.assertEquals("PALM", outputForTag.get(2));
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, outputForTag.get(3));
    }

    private void setupWordComposerFor(String typedTag) {
        String typedText = ":" + typedTag;
        Mockito.doReturn(typedText).when(mWordComposer).getTypedWord();
        Mockito.doReturn(typedText.length()).when(mWordComposer).length();
        for (int charIndex = 0; charIndex < typedText.length(); charIndex++) {
            Mockito.doReturn(new int[]{typedText.charAt(charIndex)}).when(mWordComposer).getCodesAt(Mockito.eq(charIndex));
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEmptyTagsListIsUnmodifiable() throws Exception {
        setupWordComposerFor("ddd");
        final List<CharSequence> list = mUnderTest.getOutputForTag("ddd", mWordComposer);
        list.add("should fail");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNoneEmptyTagsListIsUnmodifiable() throws Exception {
        final List<CharSequence> list = mUnderTest.getOutputForTag("face", mWordComposer);
        list.add("should fail");
    }

}