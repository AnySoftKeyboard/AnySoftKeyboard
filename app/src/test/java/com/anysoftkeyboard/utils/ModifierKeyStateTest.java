package com.anysoftkeyboard.utils;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowSystemClock;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ModifierKeyStateTest {

    private static final int DOUBLE_TAP_TIMEOUT = 2;
    private static final int LONG_PRESS_TIMEOUT = 5;

    @Test
    public void testLongPressToLockAndUnLock() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);

        ModifierKeyState state = new ModifierKeyState(true);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.sleep(LONG_PRESS_TIMEOUT + 1);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertTrue(state.isLocked());
        Assert.assertFalse(state.isPressed());

        ShadowSystemClock.sleep(1000);

        state.onPress();
        ShadowSystemClock.setCurrentTimeMillis(LONG_PRESS_TIMEOUT + 1);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }

    @Test
    public void testLongPressToLockWhenDisabled() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);

        ModifierKeyState state = new ModifierKeyState(false);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.sleep(LONG_PRESS_TIMEOUT + 1);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }

    @Test
    public void testPressToLockedState() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);
        ModifierKeyState state = new ModifierKeyState(true);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertTrue(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        //for UI purposes, while the key is pressed, it can not be LOCKED
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }

    @Test
    public void testPressAndSkipLockedState() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);
        ModifierKeyState state = new ModifierKeyState(true);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        millis += DOUBLE_TAP_TIMEOUT + 1;
        ShadowSystemClock.setCurrentTimeMillis(millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }

    @Test
    public void testReset() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);
        ModifierKeyState state = new ModifierKeyState(true);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.reset();
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }

    @Test
    public void testPressWhenLockedStateNotSupported() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);

        ModifierKeyState state = new ModifierKeyState(false);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }

    @Test
    public void testSetActiveState() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);

        ModifierKeyState state = new ModifierKeyState(true);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.setActiveState(true);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);
        //although the state is ACTIVE before the press-release
        //sequence, we will not move to LOCKED state.
        //we can only move to LOCKED state if the user has double-clicked.
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(DOUBLE_TAP_TIMEOUT, LONG_PRESS_TIMEOUT);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }
}