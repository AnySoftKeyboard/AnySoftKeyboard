package com.anysoftkeyboard.quicktextkeys;

import static com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardTagsSearcher.MAGNIFYING_GLASS_CHARACTER;

import static java.util.Arrays.asList;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.dictionaries.KeyCodesProvider;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class TagsExtractorTest {

    private TagsExtractorImpl mUnderTest;
    private QuickKeyHistoryRecords mQuickKeyHistoryRecords;

    @Before
    public void setup() {
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
        mQuickKeyHistoryRecords = new QuickKeyHistoryRecords(AnyApplication.prefs(getApplicationContext()));
        mUnderTest = new TagsExtractorImpl(getApplicationContext(), asList(keysForTest, keysForTest2), mQuickKeyHistoryRecords);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertFalse(mUnderTest.mTagsDictionary.isClosed());
        Assert.assertTrue(mUnderTest.isEnabled());
    }

    @Test
    public void getOutputForTag() throws Exception {
        final List<CharSequence> happyList = setOutputForTag("happy");
        Assert.assertEquals(2, happyList.size());
        //although there are two keys that output HAPPY, they will be merged into one output.
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "happy", "HAPPY"}, happyList.toArray());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "palm", "PALM"}, setOutputForTag("palm").toArray());
    }

    @Test
    public void getOutputForTagWithCaps() throws Exception {
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "Palm", "PALM"}, setOutputForTag("Palm").toArray());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "PALM", "PALM"}, setOutputForTag("PALM").toArray());
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "paLM", "PALM"}, setOutputForTag("paLM").toArray());
    }

    @Test
    public void getMultipleOutputsForTag() throws Exception {
        Assert.assertEquals(4, setOutputForTag("face").size());
        //although there are two keys that output HAPPY, they will be merged into one output.
        Assert.assertArrayEquals(new String[]{MAGNIFYING_GLASS_CHARACTER + "face", "FACE", "HAPPY", "SHRUG"}, setOutputForTag("face").toArray());
    }

    @Test
    public void getJustTypedForUnknown() throws Exception {
        Assert.assertEquals(1, setOutputForTag("ddd").size());
    }

    @Test
    public void testShowSuggestionWhenIncompleteTyped() throws Exception {
        final List<CharSequence> outputForTag = setOutputForTag("pa");
        Assert.assertEquals(2, outputForTag.size());
        Assert.assertEquals(MAGNIFYING_GLASS_CHARACTER + "pa", outputForTag.get(0));
        Assert.assertEquals("PALM", outputForTag.get(1));
    }

    @Test
    public void testClose() throws Exception {
        Assert.assertFalse(mUnderTest.mTagsDictionary.isClosed());

        mUnderTest.close();

        Assert.assertTrue(mUnderTest.mTagsDictionary.isClosed());
    }

    @Test
    public void testShowHistoryWhenStartingTagSearch() throws Exception {
        //adding history
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        List<QuickKeyHistoryRecords.HistoryKey> history = mQuickKeyHistoryRecords.getCurrentHistory();
        Assert.assertEquals(1, history.size());
        mQuickKeyHistoryRecords.store("palm", "PALM");
        history = mQuickKeyHistoryRecords.getCurrentHistory();
        Assert.assertEquals(2, history.size());
        mQuickKeyHistoryRecords.store("tree", "TREE");
        //simulating start of tag search
        final List<CharSequence> outputForTag = setOutputForTag("");
        Assert.assertEquals(4, outputForTag.size());
        Assert.assertEquals(MAGNIFYING_GLASS_CHARACTER, outputForTag.get(0));
        Assert.assertEquals("TREE", outputForTag.get(1));
        Assert.assertEquals("PALM", outputForTag.get(2));
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, outputForTag.get(3));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEmptyTagsListIsUnmodifiable() throws Exception {
        final List<CharSequence> list = setOutputForTag("ddd");
        list.add("should fail");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNoneEmptyTagsListIsUnmodifiable() throws Exception {
        final List<CharSequence> list = setOutputForTag("face");
        list.add("should fail");
    }

    private List<CharSequence> setOutputForTag(String typedTag) {
        final String typedText = ":" + typedTag;

        final KeyCodesProvider provider = Mockito.mock(KeyCodesProvider.class);

        Mockito.doReturn(typedText).when(provider).getTypedWord();
        Mockito.doReturn(typedText.length()).when(provider).length();
        Mockito.doAnswer(invocation -> {
            int index = invocation.getArgument(0);
            return new int[]{typedText.toLowerCase(Locale.US).charAt(index)};
        }).when(provider).getCodesAt(Mockito.anyInt());

        return mUnderTest.getOutputForTag(typedTag, provider);
    }

}