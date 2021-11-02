package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardSelectionModificationTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testCapitalizeEntireInput() {
        final String initialText = "this should all he caps";
        final String upperCaseText = "THIS SHOULD ALL HE CAPS";
        final String capitalizedText = "This should all he caps";
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);
        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.setSelectedText(0, initialText.length(), true);
        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        // to capitalized
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals(capitalizedText, mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                capitalizedText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals(upperCaseText, mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                upperCaseText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentSelectedText());
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
        final String inputText = "this should not all he caps";
        final String capitalized = "this Should not all he caps";
        final String uppercase = "this SHOULD not all he caps";
        mAnySoftKeyboardUnderTest.simulateTextTyping(inputText);
        mAnySoftKeyboardUnderTest.setSelectedText("this ".length(), "this should".length(), true);
        // To capitalized
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("Should", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(capitalized, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("SHOULD", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(uppercase, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("should", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(inputText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testStartsCapitalized() {
        final String inputText = "this Should not all he caps";
        final String capitalized = "this Should not all he caps";
        final String lowercase = "this should not all he caps";
        final String uppercase = "this SHOULD not all he caps";
        mAnySoftKeyboardUnderTest.simulateTextTyping(inputText);
        mAnySoftKeyboardUnderTest.setSelectedText("this ".length(), "this should".length(), true);
        // To uppercase - instead of capitalized, it switches to uppercase (since it was already
        // capitalized)
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("SHOULD", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(uppercase, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        // To lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("should", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(lowercase, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        // Back to capitalized
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("Should", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(capitalized, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testCapitalizeMixedCaseWord() {
        final String inputText = "this sHoUlD not all he caps";
        final String capitalized = "this Should not all he caps";
        final String uppercase = "this SHOULD not all he caps";
        final String lowercase = "this should not all he caps";
        mAnySoftKeyboardUnderTest.simulateTextTyping(inputText.toLowerCase());
        mAnySoftKeyboardUnderTest.setSelectedText("this ".length(), "this should".length(), true);
        // To capitalized
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("Should", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(capitalized, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("SHOULD", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(uppercase, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("should", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(lowercase, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testWrapWithSpecials() {
        final String inputText = "not this but this is quoted not this";
        mAnySoftKeyboardUnderTest.simulateTextTyping(inputText.toLowerCase());
        mAnySoftKeyboardUnderTest.setSelectedText(
                "not this but ".length(), "not this but this is quoted".length(), true);
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        mAnySoftKeyboardUnderTest.simulateKeyPress('\"');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"this is quoted\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        mAnySoftKeyboardUnderTest.simulateKeyPress('\'');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'this is quoted'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        mAnySoftKeyboardUnderTest.simulateKeyPress('-');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-this is quoted-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        mAnySoftKeyboardUnderTest.simulateKeyPress('_');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_this is quoted_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        mAnySoftKeyboardUnderTest.simulateKeyPress('*');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*this is quoted*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        mAnySoftKeyboardUnderTest.simulateKeyPress('`');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*`this is quoted`*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        mAnySoftKeyboardUnderTest.simulateKeyPress('~');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*`~this is quoted~`*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        // special case () [] {}
        mAnySoftKeyboardUnderTest.simulateKeyPress('(');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*`~(this is quoted)~`*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(')');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*`~((this is quoted))~`*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress('[');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*`~(([this is quoted]))~`*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(']');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*`~(([[this is quoted]]))~`*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress('{');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*`~(([[{this is quoted}]]))~`*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress('}');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*`~(([[{{this is quoted}}]]))~`*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress('<');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*`~(([[{{<this is quoted>}}]]))~`*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress('>');
        Assert.assertEquals("this is quoted", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "not this but \"'-_*`~(([[{{<<this is quoted>>}}]]))~`*_-'\" not this",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }
}
