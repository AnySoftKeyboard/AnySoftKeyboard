package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardSelectionModificationTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testCapitalizeEntireInput() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "THIS SHOULD ALL BE CAPS";
        inputConnection.commitText(expectedText.toLowerCase(), 1);
        inputConnection.setSelection(0, expectedText.length());
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals(expectedText, inputConnection.getSelectedText(0).toString());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals(
                expectedText.toLowerCase(), inputConnection.getSelectedText(0).toString());
    }

    @Test
    public void testNoChangeIfNotSelected() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "this is not selected";
        inputConnection.commitText(expectedText, 1);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    public void testCapitalizeSingleWord() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String inputText = "this SHOULD not all be caps";
        inputConnection.commitText(inputText.toLowerCase(), 1);
        inputConnection.setSelection("this ".length(), "this should".length());
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("SHOULD", inputConnection.getSelectedText(0).toString());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("should", inputConnection.getSelectedText(0).toString());
    }

    @Test
    public void testCapitalizeSingleLetter() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String inputText = "this shOuld not all be caps";
        inputConnection.commitText(inputText.toLowerCase(), 1);
        inputConnection.setSelection("this sh".length(), "this sho".length());
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("O", inputConnection.getSelectedText(0).toString());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("o", inputConnection.getSelectedText(0).toString());
    }

    @Test
    public void testCapitalizeMixedCaseWord() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String inputText = "this sHoUlD not all be caps";
        inputConnection.commitText(inputText.toLowerCase(), 1);
        inputConnection.setSelection("this ".length(), "this should".length());
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("SHOULD", inputConnection.getSelectedText(0).toString());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("should", inputConnection.getSelectedText(0).toString());
    }

    @Test
    public void testWrapWithSpecials() {
        TestInputConnection inputConnection =
                (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String inputText = "not this but this is quoted not this";
        inputConnection.commitText(inputText.toLowerCase(), 1);
        inputConnection.setSelection(
                "not this but ".length(), "not this but this is quoted".length());
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress('\"');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"this is quoted\" not this",
                inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'this is quoted'\" not this",
                inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('-');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-this is quoted-'\" not this",
                inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('_');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_this is quoted_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('*');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*this is quoted*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('`');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*`this is quoted`*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());

        mAnySoftKeyboardUnderTest.simulateKeyPress('~');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*`~this is quoted~`*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());

        // special case () [] {}
        mAnySoftKeyboardUnderTest.simulateKeyPress('(');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*`~(this is quoted)~`*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(')');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*`~((this is quoted))~`*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('[');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*`~(([this is quoted]))~`*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(']');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*`~(([[this is quoted]]))~`*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('{');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*`~(([[{this is quoted}]]))~`*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('}');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*`~(([[{{this is quoted}}]]))~`*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('<');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*`~(([[{{<this is quoted>}}]]))~`*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress('>');
        Assert.assertEquals("this is quoted", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals(
                "not this but \"'-_*`~(([[{{<<this is quoted>>}}]]))~`*_-'\" not this",
                inputConnection.getCurrentTextInInputConnection());
    }
}
