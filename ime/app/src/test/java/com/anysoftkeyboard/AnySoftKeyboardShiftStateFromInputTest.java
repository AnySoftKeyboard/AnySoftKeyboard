package com.anysoftkeyboard;

import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
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
    public void testShiftSentences() {
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(TextUtils.CAP_MODE_SENTENCES));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond. james bond");
        Assert.assertEquals(
                "Hello my name is bond. James bond",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShiftNever() {
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(0));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond. james bond");
        Assert.assertEquals(
                "hello my name is bond. james bond",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShiftWords() {
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(TextUtils.CAP_MODE_WORDS));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond. james bond");
        Assert.assertEquals(
                "Hello My Name Is Bond. James Bond",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShiftCaps() {
        simulateOnStartInputFlow(false, createEditorInfoWithCaps(TextUtils.CAP_MODE_CHARACTERS));
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello my name is bond. james bond");
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
