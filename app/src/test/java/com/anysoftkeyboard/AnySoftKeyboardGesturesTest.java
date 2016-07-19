package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
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

    @Test
    public void testSwipeDownCustomizable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_down_action), "clear_input");
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeDown();
        Assert.assertEquals(KeyCodes.CLEAR_INPUT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeUpCustomizable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_up_action), "clear_input");
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeUp();
        Assert.assertEquals(KeyCodes.CLEAR_INPUT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeUpFromSpaceCustomizable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_up_from_spacebar_action), "clear_input");
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey(' ');
        mAnySoftKeyboardUnderTest.onSwipeUp();
        Assert.assertEquals(KeyCodes.CLEAR_INPUT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeLeftCustomizable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_left_action), "clear_input");
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeLeft(false);
        Assert.assertEquals(KeyCodes.CLEAR_INPUT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeLeftFromSpaceCustomizable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_left_space_bar_action), "clear_input");
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey(' ');
        mAnySoftKeyboardUnderTest.onSwipeLeft(false);
        Assert.assertEquals(KeyCodes.CLEAR_INPUT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeRightCustomizable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action), "clear_input");
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.CLEAR_INPUT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeRightFromSpaceCustomizable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_space_bar_action), "clear_input");
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey(' ');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.CLEAR_INPUT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }
}