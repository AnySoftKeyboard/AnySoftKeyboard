package com.anysoftkeyboard;

import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardShiftStateFromInputTest extends AnySoftKeyboardBaseTest {

    @Before
    public void setupForShiftTests() {
        simulateFinishInputFlow();
        mAnySoftKeyboardUnderTest.getTestInputConnection().setRealCapsMode(true);
    }

    @Test
    public void testShiftSentencesAutoPunctuationEnabled() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_bool_should_swap_punctuation_and_space, true);
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(TextUtils.CAP_MODE_SENTENCES));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond.james bond");
        Assert.assertEquals(
                "Hello my name is bond. James bond",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShiftSentencesAutoPunctuationDisabled() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_bool_should_swap_punctuation_and_space, false);
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(TextUtils.CAP_MODE_SENTENCES));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond. james bond");
        Assert.assertEquals(
                "Hello my name is bond. James bond",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShiftNeverAutoPunctuationDisabled() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_bool_should_swap_punctuation_and_space, false);
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(0));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond. james bond");
        Assert.assertEquals(
                "hello my name is bond. james bond",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShiftNeverAutoPunctuationEnabled() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_bool_should_swap_punctuation_and_space, true);
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(0));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond.james bond");
        Assert.assertEquals(
                "hello my name is bond. james bond",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShiftWordsAutoPunctuationEnabled() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_bool_should_swap_punctuation_and_space, true);
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(TextUtils.CAP_MODE_WORDS));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond.james bond");
        Assert.assertEquals(
                "Hello My Name Is Bond. James Bond",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShiftWordsAutoPunctuationDisabled() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_bool_should_swap_punctuation_and_space, false);
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(TextUtils.CAP_MODE_WORDS));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond. james bond");
        Assert.assertEquals(
                "Hello My Name Is Bond. James Bond",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShiftCapsAutoPunctuationDisabled() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_bool_should_swap_punctuation_and_space, false);
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(TextUtils.CAP_MODE_CHARACTERS));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond. james bond");
        Assert.assertEquals(
                "HELLO MY NAME IS BOND. JAMES BOND",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShiftCapsAutoPunctuationEnabled() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_bool_should_swap_punctuation_and_space, true);
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(TextUtils.CAP_MODE_CHARACTERS));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond.james bond");
        Assert.assertEquals(
                "HELLO MY NAME IS BOND. JAMES BOND",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    private EditorInfo createEditorInfoWithCaps(int capsFlag) {
        EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
        editorInfo.inputType |= capsFlag;
        return editorInfo;
    }
}
