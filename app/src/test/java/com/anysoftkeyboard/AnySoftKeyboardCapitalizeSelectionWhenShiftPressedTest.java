package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardCapitalizeSelectionWhenShiftPressedTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testCapitalizeEntireInput() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "THIS SHOULD ALL BE CAPS";
        inputConnection.commitText(expectedText.toLowerCase(), 1);
        inputConnection.setSelection(0, expectedText.length());
        // To uppercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals(expectedText, inputConnection.getSelectedText(0).toString());

        // Back to lowercase
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals(expectedText.toLowerCase(), inputConnection.getSelectedText(0).toString());
    }

    @Test
    public void testCapitalizeSingleWord() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
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
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
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
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
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
}