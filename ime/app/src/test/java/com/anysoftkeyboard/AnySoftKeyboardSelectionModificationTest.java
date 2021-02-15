package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardSelectionModificationTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testCapitalizeEntireInput() {
        final String expectedText = "THIS SHOULD ALL BE CAPS";
        final String initialText = expectedText.toLowerCase(Locale.ENGLISH);
        mAnySoftKeyboardUnderTest.simulateTextTyping(initialText);
        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.setSelectedText(0, expectedText.length(), true);
        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertEquals(initialText, mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals(expectedText, mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                expectedText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

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
        final String inputText = "this SHOULD not all be caps";
        mAnySoftKeyboardUnderTest.simulateTextTyping(inputText.toLowerCase());
        mAnySoftKeyboardUnderTest.setSelectedText("this ".length(), "this should".length(), true);
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("SHOULD", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("should", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
    }

    @Test
    public void testCapitalizeSingleLetter() {
        final String inputText = "this shOuld not all be caps";
        mAnySoftKeyboardUnderTest.simulateTextTyping(inputText.toLowerCase());
        mAnySoftKeyboardUnderTest.setSelectedText("this sh".length(), "this sho".length(), true);
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("O", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("o", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
    }

    @Test
    public void testCapitalizeMixedCaseWord() {
        final String inputText = "this sHoUlD not all be caps";
        mAnySoftKeyboardUnderTest.simulateTextTyping(inputText.toLowerCase());
        mAnySoftKeyboardUnderTest.setSelectedText("this ".length(), "this should".length(), true);
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("SHOULD", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("should", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
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
