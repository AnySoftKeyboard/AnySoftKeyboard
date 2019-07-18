package com.anysoftkeyboard;

import android.view.inputmethod.EditorInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardIncognitoTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testSetsIncognitoWhenInputFieldRequestsIt() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE | EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING,
                        0));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testSetsIncognitoWhenInputFieldRequestsItWithSendAction() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING,
                        0));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testSetsIncognitoWhenPasswordInputFieldNumber() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testDoesNotSetIncognitoWhenInputFieldNumberButNotPassword() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_NORMAL));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testDoesNotSetIncognitoWhenInputFieldNumberButNotNumberPassword() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testSetsIncognitoWhenPasswordInputField() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testDoesNotSetIncognitoWhenPasswordInputFieldButNotText() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_TEXT_VARIATION_PASSWORD));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testDoesNotSetIncognitoWhenInputFieldTextButNormal() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_NORMAL));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testDoesNotSetIncognitoWhenInputFieldTextButNotPassword() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_LONG_MESSAGE));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testDoesNotSetIncognitoWhenInputFieldTextButNotTextPassword() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testSetsIncognitoWhenPasswordInputFieldVisible() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT
                                | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testSetsIncognitoWhenPasswordInputFieldWeb() {
        simulateFinishInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testClearsIncognitoOnNewFieldAfterMomentary() {
        simulateFinishInputFlow();

        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE | EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING,
                        0));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());

        simulateFinishInputFlow();
        simulateOnStartInputFlow(
                false, TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, 0));

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testClearsIncognitoWhileInMomentaryInputFieldWhenUserRequestsToClear() {
        simulateFinishInputFlow();

        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE + EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING,
                        0));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());

        mAnySoftKeyboardUnderTest.setIncognito(false, true);

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testDoesNotClearIncognitoOnNewFieldUserRequestIncognito() {
        mAnySoftKeyboardUnderTest.setIncognito(true, true);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());

        simulateFinishInputFlow();

        simulateOnStartInputFlow(
                false, TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, 0));
        // still incognito
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testDoesNotClearIncognitoOnNewFieldUserRequestIncognitoAfterMomentary() {
        mAnySoftKeyboardUnderTest.setIncognito(true, true);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());

        simulateFinishInputFlow();

        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE + EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING,
                        0));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());

        simulateFinishInputFlow();

        simulateOnStartInputFlow(
                false, TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, 0));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }

    @Test
    public void testMomentaryIncognitoAfterUserClearsPreviousInputField() {
        simulateFinishInputFlow();

        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE + EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING,
                        0));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());

        mAnySoftKeyboardUnderTest.setIncognito(false, true);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());

        simulateFinishInputFlow();

        simulateOnStartInputFlow(
                false,
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE + EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING,
                        0));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
    }
}
