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

    @Test
    public void testSwipeForActionNoneConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_none));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(0, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionNextAlphabetConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_next_alphabet));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.MODE_ALPHABET, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionNextSymbolsConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_next_symbols));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.MODE_SYMOBLS, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionCycleInModeConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_left_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_next_inside_mode));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeLeft(false);
        Assert.assertEquals(KeyCodes.KEYBOARD_CYCLE_INSIDE_MODE, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionSwitchModeConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_switch_keyboard_mode));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.KEYBOARD_MODE_CHANGE, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionCycleKeyboardsConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_cycle_keyboards));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.KEYBOARD_CYCLE, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionCycleReverseConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_reverse_cycle_keyboards));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.KEYBOARD_REVERSE_CYCLE, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionShiftConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_shift));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.SHIFT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionHideConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_hide));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.CANCEL, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionBackspaceConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_backspace));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.DELETE, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionBackWordConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_backword));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.DELETE_WORD, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionClearInputConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_clear_input));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.CLEAR_INPUT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionArrowUpConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_cursor_up));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.ARROW_UP, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionArrowDownConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_cursor_down));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.ARROW_DOWN, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionArrowLeftConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_cursor_left));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.ARROW_LEFT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionArrowRightConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_cursor_right));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.ARROW_RIGHT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionSplitLayoutConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_split_layout));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.SPLIT_LAYOUT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionMergeLayoutConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_merge_layout));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.MERGE_LAYOUT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionCompactLayoutRightConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_compact_layout_to_right));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.COMPACT_LAYOUT_TO_RIGHT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionCompactLayoutLeftConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_compact_layout_to_left));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.COMPACT_LAYOUT_TO_LEFT, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }

    @Test
    public void testSwipeForActionUtilityKeyboardConfigurable() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_swipe_right_action),
                RuntimeEnvironment.application.getString(R.string.swipe_action_value_utility_keyboard));
        simulateOnStartInputFlow();

        mAnySoftKeyboardUnderTest.onFirstDownKey('x');
        mAnySoftKeyboardUnderTest.onSwipeRight(false);
        Assert.assertEquals(KeyCodes.UTILITY_KEYBOARD, mAnySoftKeyboardUnderTest.getLastOnKeyPrimaryCode());
    }
}