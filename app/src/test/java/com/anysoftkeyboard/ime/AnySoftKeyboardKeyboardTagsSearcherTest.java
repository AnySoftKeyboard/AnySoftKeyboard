package com.anysoftkeyboard.ime;

import android.os.Build;
import android.preference.PreferenceManager;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.SharedPrefsHelper;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.quicktextkeys.TagsExtractorImpl;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

@Config(sdk = Build.VERSION_CODES.LOLLIPOP_MR1 /*the first API level to have support for those*/)
public class AnySoftKeyboardKeyboardTagsSearcherTest extends AnySoftKeyboardBaseTest {

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void testDefaultFalseBeforeAPI22() {
        Assert.assertSame(TagsExtractorImpl.NO_OP, mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher().isEnabled());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void testDefaultTrueAtAPI22() {
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());
        Assert.assertNotSame(TagsExtractorImpl.NO_OP, mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher().isEnabled());
    }

    @Test
    public void testOnSharedPreferenceChangedCauseLoading() throws Exception {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_search_quick_text_tags, false);
        Assert.assertSame(TagsExtractorImpl.NO_OP, mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_search_quick_text_tags, true);
        Object searcher = mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher();
        Assert.assertNotSame(TagsExtractorImpl.NO_OP, searcher);
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_search_quick_text_tags, true);
        Assert.assertSame(searcher, mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());
    }

    @Test
    public void testUnrelatedOnSharedPreferenceChangedDoesNotCreateSearcher() throws Exception {
        Object searcher = mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher();
        Assert.assertNotNull(searcher);
        //unrelated pref change, should not create a new searcher
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_allow_suggestions_restart, false);
        Assert.assertSame(searcher, mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_search_quick_text_tags, false);
        Assert.assertSame(TagsExtractorImpl.NO_OP, mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_allow_suggestions_restart, true);
        Assert.assertSame(TagsExtractorImpl.NO_OP, mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());
    }

    @Test
    public void testEnabledTypingTagProvidesSuggestionsFromTagsOnly() throws Exception {
        mAnySoftKeyboardUnderTest.simulateKeyPress(':');
        verifySuggestions(true, AnySoftKeyboardKeyboardTagsSearcher.MAGNIFYING_GLASS_CHARACTER, QuickKeyHistoryRecords.DEFAULT_EMOJI);
        mAnySoftKeyboardUnderTest.simulateTextTyping("fa");
        verifySuggestions(true, AnySoftKeyboardKeyboardTagsSearcher.MAGNIFYING_GLASS_CHARACTER + "fa");

        //now checking that suggestions will work without colon
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);

        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        mAnySoftKeyboardUnderTest.simulateTextTyping("fa");
        verifySuggestions(true, "fa", "face");
    }

    @Test
    public void testOnlyTagsAreSuggestedWhenTypingColon() throws Exception {
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateKeyPress(':');
        verifySuggestions(true, AnySoftKeyboardKeyboardTagsSearcher.MAGNIFYING_GLASS_CHARACTER, QuickKeyHistoryRecords.DEFAULT_EMOJI);
        mAnySoftKeyboardUnderTest.simulateTextTyping("face");
        List suggestions = verifyAndCaptureSuggestion(true);
        Assert.assertNotNull(suggestions);
        Assert.assertEquals(131, suggestions.size());
        Assert.assertEquals(AnySoftKeyboardKeyboardTagsSearcher.MAGNIFYING_GLASS_CHARACTER + "face", suggestions.get(0));
        Assert.assertEquals("\uD83D\uDE00", suggestions.get(1));
    }

    @Test
    public void testTagsSearchDoesNotAutoPick() throws Exception {
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");

        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');

        Assert.assertEquals(":face ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testTagsSearchThrice() throws Exception {
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        List suggestions = verifyAndCaptureSuggestion(true);
        Assert.assertNotNull(suggestions);
        Assert.assertEquals(131, suggestions.size());

        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');

        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        suggestions = verifyAndCaptureSuggestion(true);
        Assert.assertNotNull(suggestions);
        Assert.assertEquals(131, suggestions.size());

        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "\uD83D\uDE00");

        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        suggestions = verifyAndCaptureSuggestion(true);
        Assert.assertNotNull(suggestions);
        Assert.assertEquals(131, suggestions.size());
    }

    @Test
    public void testPickingEmojiOutputsToInput() throws Exception {
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "\uD83D\uDE00");

        verifySuggestions(true);
        Assert.assertEquals("\uD83D\uDE00", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        //deleting

        //correctly, this is a bug with TestInputConnection: it reports that there is one character in the input
        //but that's because it does not support deleting multi-character emojis.
        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText().length());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        //so, it was two characters, and now it's one
        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText().length());
    }

    @Test
    public void testPickingEmojiStoresInHistory() throws Exception {
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "\uD83D\uDE00");

        List<QuickKeyHistoryRecords.HistoryKey> keys = mAnySoftKeyboardUnderTest.getQuickKeyHistoryRecords().getCurrentHistory();
        Assert.assertEquals(2, keys.size());
        //added last (this will be shown in reverse on the history tab)
        Assert.assertEquals("\uD83D\uDE00", keys.get(1).name);
        Assert.assertEquals("\uD83D\uDE00", keys.get(1).value);
    }

    @Test
    public void testPickingEmojiDoesNotTryToGetNextWords() throws Exception {
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "\uD83D\uDE00");

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).getNextSuggestions(Mockito.any(CharSequence.class), Mockito.anyBoolean());
    }

    @Test
    public void testPickingTypedTagDoesNotTryToAddToAutoDictionary() throws Exception {
        verifyNoSuggestionsInteractions();
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, ":face");

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).isValidWord(Mockito.any(CharSequence.class));
    }

    @Test
    public void testPickingSearchCellInSuggestionsOutputTypedWord() throws Exception {
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");

        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, AnySoftKeyboardKeyboardTagsSearcher.MAGNIFYING_GLASS_CHARACTER + "face");

        //outputs the typed word
        Assert.assertEquals(":face ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        //clears suggestions
        verifySuggestions(true);
    }

    @Test
    public void testDisabledTypingTagDoesNotProvidesSuggestions() throws Exception {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_search_quick_text_tags, false);
        mAnySoftKeyboardUnderTest.simulateKeyPress(':');
        verifySuggestions(true);
        mAnySoftKeyboardUnderTest.simulateTextTyping("fa");
        verifySuggestions(true, "fa", "face");
    }

    @Test
    public void testQuickTextEnabledPluginsPrefsChangedCauseReload() throws Exception {
        Object searcher = mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher();
        mAnySoftKeyboardUnderTest.onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application),
                QuickTextKeyFactory.PREF_ID_PREFIX+"jksdbc");

        Assert.assertNotSame(searcher, mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());
    }

    @Test
    public void testQuickTextEnabledPluginsPrefsChangedDoesNotCauseReloadIfTagsSearchIsDisabled() throws Exception {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_search_quick_text_tags, false);
        Assert.assertSame(TagsExtractorImpl.NO_OP, mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());
        mAnySoftKeyboardUnderTest.onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application),
                QuickTextKeyFactory.PREF_ID_PREFIX+"ddddd");

        Assert.assertSame(TagsExtractorImpl.NO_OP, mAnySoftKeyboardUnderTest.getQuickTextTagsSearcher());
    }

    @Test
    public void testEnsureSuggestionsAreIterable() throws Exception {
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        List suggestions = verifyAndCaptureSuggestion(true);
        int suggestionsCount = suggestions.size();
        for (Object suggestion : suggestions) {
            Assert.assertNotNull(suggestion);
            Assert.assertTrue(suggestion instanceof CharSequence);
            suggestionsCount--;
        }
        Assert.assertEquals(0, suggestionsCount);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveIteratorUnSupported() throws Exception {
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        List suggestions = verifyAndCaptureSuggestion(true);
        suggestions.iterator().remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddingAtIndexToSuggestionsUnSupported() throws Exception {
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        List suggestions = verifyAndCaptureSuggestion(true);
        suggestions.add(0, "demo");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddingToSuggestionsUnSupported() throws Exception {
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        List suggestions = verifyAndCaptureSuggestion(true);
        suggestions.add("demo");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testListIteratorUnSupported() throws Exception {
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        List suggestions = verifyAndCaptureSuggestion(true);
        suggestions.listIterator();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoteAtIndexUnSupported() throws Exception {
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        List suggestions = verifyAndCaptureSuggestion(true);
        suggestions.remove(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoteObjectUnSupported() throws Exception {
        mAnySoftKeyboardUnderTest.simulateTextTyping(":face");
        List suggestions = verifyAndCaptureSuggestion(true);
        suggestions.remove("DEMO");
    }

}