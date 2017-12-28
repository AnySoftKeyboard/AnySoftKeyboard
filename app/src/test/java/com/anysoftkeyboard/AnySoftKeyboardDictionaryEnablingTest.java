package com.anysoftkeyboard;

import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowSystemClock;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnySoftKeyboardDictionaryEnablingTest extends AnySoftKeyboardBaseTest {

    private static final String[] DICTIONATY_WORDS = new String[]{
            "high", "hello", "menny", "AnySoftKeyboard", "keyboard", "google", "low"
    };

    @Before
    public void setUp() throws Exception {
        UserDictionary userDictionary = new UserDictionary(RuntimeEnvironment.application, "en");
        userDictionary.loadDictionary();
        for (int wordIndex = 0; wordIndex < DICTIONATY_WORDS.length; wordIndex++) {
            userDictionary.addWord(DICTIONATY_WORDS[wordIndex], DICTIONATY_WORDS.length - wordIndex);
        }
        userDictionary.close();
    }

    @Test
    public void testDictionariesCreatedForText() {
        simulateFinishInputFlow(false);

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setupSuggestionsForKeyboard(Mockito.anyList());

        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesNotCreatedForTextWithOutViewCreated() {
        simulateFinishInputFlow(false);
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        //NOTE: Not creating View!
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);


    }

    @Test
    public void testDictionariesNotCreatedForPassword() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        simulateFinishInputFlow(false);
        simulateOnStartInputFlow(false, false, editorInfo);

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setupSuggestionsForKeyboard(Mockito.anyList());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesNotCreatedForVisiblePassword() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        simulateFinishInputFlow(false);
        simulateOnStartInputFlow(false, false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesNotCreatedForWebPassword() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        simulateFinishInputFlow(false);
        simulateOnStartInputFlow(false, false, editorInfo);

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesCreatedForUriInputButWithoutAutoPick() {
        simulateFinishInputFlow(false);
        simulateOnStartInputFlow(false, false, TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_URI));
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesCreatedForEmailInputButNotAutoPick() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        simulateFinishInputFlow(false);
        simulateOnStartInputFlow(false, false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesCreatedForWebEmailInputButNotAutoPick() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
        simulateFinishInputFlow(false);
        simulateOnStartInputFlow(false, false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesNotCreatedForAutoComplete() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.inputType += EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE;
        simulateFinishInputFlow(false);
        simulateOnStartInputFlow(false, false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesNotCreatedForNoSuggestions() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.inputType += EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        simulateFinishInputFlow(false);
        simulateOnStartInputFlow(false, false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesResetForPassword() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());

        final EditorInfo editorInfoPassword = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        simulateFinishInputFlow(false);
        simulateOnStartInputFlow(false, false, editorInfoPassword);

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testReleasingAllDictionariesIfPrefsSetToNoSuggestions() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        simulateFinishInputFlow(false);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).closeDictionaries();

        SharedPrefsHelper.setPrefsValue("candidates_on", false);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).closeDictionaries();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setCorrectionMode(Mockito.eq(false), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());

        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());

        simulateFinishInputFlow(false);

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        SharedPrefsHelper.setPrefsValue("candidates_on", true);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setCorrectionMode(Mockito.eq(true), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());

        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
    }

    @Test
    public void testDoesNotCloseDictionaryIfInputRestartsQuickly() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        //setting the dictionary
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList());

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        simulateFinishInputFlow(false);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).closeDictionaries();
        //waiting a bit
        ShadowSystemClock.sleep(10);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).closeDictionaries();
        //restarting the input
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).closeDictionaries();
    }

    @Test
    public void testDoesCloseDictionaryIfInputRestartsSlowly() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        //setting the dictionary
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList());

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        simulateFinishInputFlow(false);
        //waiting a long time
        ShadowSystemClock.sleep(1000);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).closeDictionaries();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        //restarting the input
        simulateOnStartInputFlow();
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList());
    }

    @Test
    public void testSettingCorrectModeFromPrefs() {
        SharedPrefsHelper.setPrefsValue("settings_key_auto_pick_suggestion_aggressiveness", "minimal_aggressiveness");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setCorrectionMode(true, 1, 1, 2);
        SharedPrefsHelper.setPrefsValue("settings_key_min_length_for_word_correction__", 4);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setCorrectionMode(true, 1, 1, 4);
    }

    @Test
    public void testSetDictionaryOnOverridePrefs() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        SharedPrefsHelper.setPrefsValue(ExternalDictionaryFactory.getDictionaryOverrideKey(currentKeyboard), "dictionary_sdfsdfsd");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).resetNextWordSentence();
    }

    @Test
    public void testNotSetDictionaryOnNonOverridePrefs() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());

        SharedPrefsHelper.setPrefsValue("bsbsbsbs", "dictionary_sdfsdfsd");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setupSuggestionsForKeyboard(Mockito.anyList());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).resetNextWordSentence();

        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        SharedPrefsHelper.setPrefsValue(/*no prefix*/currentKeyboard.getKeyboardId() + "_override_dictionary", "dictionary_sdfsdfsd");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setupSuggestionsForKeyboard(Mockito.anyList());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).resetNextWordSentence();

        SharedPrefsHelper.setPrefsValue(KeyboardFactory.PREF_ID_PREFIX + currentKeyboard.getKeyboardId() /*no postfix*/, "dictionary_sdfsdfsd");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setupSuggestionsForKeyboard(Mockito.anyList());
        //this will be called, since abortSuggestions is called (the prefix matches).
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.atLeastOnce()).resetNextWordSentence();
    }
}