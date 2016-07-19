package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

@RunWith(RobolectricGradleTestRunner.class)
public class AnySoftKeyboardGesturesTest extends AnySoftKeyboardBaseTest {

    @Before
    @Override
    public void setUpForAnySoftKeyboardBase() throws Exception {
        SharedPrefsHelper.setPrefsValue("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", true);
        super.setUpForAnySoftKeyboardBase();
    }

    @Test
    public void testSwipeLeftFromBackSpace() {
        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey(KeyCodes.DELETE);
        mAnySoftKeyboardUnderTest.onSwipeLeft(false);
        Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());
        //still same keyboard
        Assert.assertEquals(currentKeyboard.getKeyboardPrefId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

    @Test
    public void testSwipeRightFromBackSpace() {
        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey(KeyCodes.DELETE);
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals("hello ", inputConnection.getCurrentTextInInputConnection());
        //still same keyboard
        Assert.assertEquals(currentKeyboard.getKeyboardPrefId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

    @Test
    public void testSwipeLeft() {
        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeLeft(false);
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        //switched keyboard
        Assert.assertNotEquals(currentKeyboard.getKeyboardPrefId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals("symbols_keyboard", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

    @Test
    public void testSwipeRight() {
        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        //switched keyboard
        Assert.assertNotEquals(currentKeyboard.getKeyboardPrefId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

    @Test
    public void testSwipeLeftFromSpace() {
        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey(' ');
        mAnySoftKeyboardUnderTest.onSwipeLeft(false);
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        //switched keyboard
        Assert.assertNotEquals(currentKeyboard.getKeyboardPrefId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals("symbols_keyboard", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

    @Test
    public void testSwipeRightFromSpace() {
        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey(' ');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        //switched keyboard
        Assert.assertNotEquals(currentKeyboard.getKeyboardPrefId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

    @Test
    public void testSwipeUp() {
        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        Assert.assertEquals(false, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());

        mAnySoftKeyboardUnderTest.onSwipeUp();
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        //same keyboard, shift on
        Assert.assertEquals(currentKeyboard.getKeyboardPrefId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals(true, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().isShifted());
    }

    @Test
    public void testSwipeDown() {
        AnyKeyboard currentKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(' ');
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        Assert.assertEquals(false, mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onSwipeDown();
        Assert.assertEquals("hello hello", inputConnection.getCurrentTextInInputConnection());
        //same keyboard
        Assert.assertEquals(currentKeyboard.getKeyboardPrefId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals(true, mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }
}