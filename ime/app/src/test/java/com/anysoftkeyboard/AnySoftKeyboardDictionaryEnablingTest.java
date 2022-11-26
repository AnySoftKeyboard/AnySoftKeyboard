package com.anysoftkeyboard;

import static android.os.SystemClock.sleep;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardDictionaryEnablingTest extends AnySoftKeyboardBaseTest {

    private static final String[] DICTIONARY_WORDS =
            new String[] {
                "high", "hello", "menny", "AnySoftKeyboard", "keyboard", "com/google", "low"
            };

    @Before
    public void setUp() throws Exception {
        UserDictionary userDictionary = new UserDictionary(getApplicationContext(), "en");
        userDictionary.loadDictionary();
        for (int wordIndex = 0; wordIndex < DICTIONARY_WORDS.length; wordIndex++) {
            userDictionary.addWord(
                    DICTIONARY_WORDS[wordIndex], DICTIONARY_WORDS.length - wordIndex);
        }
        userDictionary.close();
    }

    @Test
    public void testDictionariesCreatedForText() {
        simulateFinishInputFlow();

        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();

        simulateOnStartInputFlow(false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesNotCreatedForTextWithOutViewCreated() {
        simulateFinishInputFlow();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        // NOTE: Not creating View!
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
    }

    @Test
    public void testDictionariesNotCreatedForPassword() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());

        final EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        simulateFinishInputFlow();
        simulateOnStartInputFlow(false, editorInfo);

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesNotCreatedForVisiblePassword() {
        final EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT
                                + EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        simulateFinishInputFlow();
        simulateOnStartInputFlow(false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesNotCreatedForWebPassword() {
        final EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        simulateFinishInputFlow();
        simulateOnStartInputFlow(false, editorInfo);

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesCreatedForUriInputButWithoutAutoPick() {
        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_URI));
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesCreatedForEmailInputButNotAutoPick() {
        final EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        simulateFinishInputFlow();
        simulateOnStartInputFlow(false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesCreatedForWebEmailInputButNotAutoPick() {
        final EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT
                                + EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
        simulateFinishInputFlow();
        simulateOnStartInputFlow(false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesCreatedForAutoComplete() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.inputType += EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE;
        simulateFinishInputFlow();
        simulateOnStartInputFlow(false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesNotCreatedForNoSuggestions() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.inputType += EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        simulateFinishInputFlow();
        simulateOnStartInputFlow(false, editorInfo);
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testDictionariesResetForPassword() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());

        final EditorInfo editorInfoPassword =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD);
        simulateFinishInputFlow();
        simulateOnStartInputFlow(false, editorInfoPassword);

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isAutoCorrect());
    }

    @Test
    public void testReleasingAllDictionariesIfPrefsSetToNoSuggestions() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());

        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());

        simulateFinishInputFlow();

        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never()).closeDictionaries();

        SharedPrefsHelper.setPrefsValue("candidates_on", false);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest()).closeDictionaries();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setCorrectionMode(Mockito.eq(false), Mockito.anyInt(), Mockito.anyInt());

        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isPredictionOn());

        simulateFinishInputFlow();

        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());

        SharedPrefsHelper.setPrefsValue("candidates_on", true);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setCorrectionMode(Mockito.eq(true), Mockito.anyInt(), Mockito.anyInt());

        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isPredictionOn());
    }

    @Test
    public void testDoesNotCloseDictionaryIfInputRestartsQuickly() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        // setting the dictionary
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());

        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());

        simulateFinishInputFlow();

        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never()).closeDictionaries();
        // waiting a bit
        sleep(10);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never()).closeDictionaries();
        // restarting the input
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never()).closeDictionaries();
    }

    @Test
    public void testDoesCloseDictionaryIfInputRestartsSlowly() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        // setting the dictionary
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());

        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());

        simulateFinishInputFlow();
        // waiting a long time
        TestRxSchedulers.foregroundAdvanceBy(10000);
        TestRxSchedulers.drainAllTasks();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest()).closeDictionaries();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());
        // restarting the input
        simulateOnStartInputFlow();
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
    }

    @Test
    public void testSettingCorrectModeFromPrefs() {
        SharedPrefsHelper.setPrefsValue(
                "settings_key_auto_pick_suggestion_aggressiveness", "minimal_aggressiveness");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest()).setCorrectionMode(true, 1, 1);
    }

    @Test
    public void testSetDictionaryOnOverridePrefs() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());

        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        SharedPrefsHelper.setPrefsValue(
                ExternalDictionaryFactory.getDictionaryOverrideKey(currentKeyboard),
                "dictionary_sdfsdfsd");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest()).resetNextWordSentence();
    }

    @Test
    public void testNotSetDictionaryOnNonOverridePrefs() {
        mAnySoftKeyboardUnderTest.simulateKeyPress('h');

        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());

        SharedPrefsHelper.setPrefsValue("bsbsbsbs", "dictionary_sdfsdfsd");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never())
                .resetNextWordSentence();

        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        SharedPrefsHelper.setPrefsValue(
                /*no prefix*/ currentKeyboard.getKeyboardId() + "_override_dictionary",
                "dictionary_sdfsdfsd");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never())
                .resetNextWordSentence();

        SharedPrefsHelper.setPrefsValue(
                KeyboardFactory.PREF_ID_PREFIX + currentKeyboard.getKeyboardId() /*no postfix*/,
                "dictionary_sdfsdfsd");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        // this will be called, since abortSuggestions is called (the prefix matches).
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.atLeastOnce())
                .resetNextWordSentence();
    }
}
