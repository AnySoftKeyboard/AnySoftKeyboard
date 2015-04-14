package com.anysoftkeyboard.utils;

import com.menny.android.anysoftkeyboard.AskGradleTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowSystemClock;

@RunWith(AskGradleTestRunner.class)
public class ModifierKeyStateTest {

    @Test
    public void testPressToLockedState() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);
        final int doubleTapTime = 2;
        ModifierKeyState state = new ModifierKeyState(true);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(doubleTapTime);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(doubleTapTime);

        Assert.assertTrue(state.isActive());
        Assert.assertTrue(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        //for UI purposes, while the key is pressed, it can not be LOCKED
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(doubleTapTime);

        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }

    @Test
    public void testPressAndSkipLockedState() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);
        final int doubleTapTime = 2;
        ModifierKeyState state = new ModifierKeyState(true);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(doubleTapTime);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        millis += doubleTapTime + 1;
        ShadowSystemClock.setCurrentTimeMillis(millis);
        state.onRelease(doubleTapTime);

        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(doubleTapTime);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }

    @Test
    public void testReset() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);
        final int doubleTapTime = 2;
        ModifierKeyState state = new ModifierKeyState(true);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(doubleTapTime);

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
        state.onRelease(doubleTapTime);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }

    @Test
    public void testPressWhenLockedStateNotSupported() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);
        final int doubleTapTime = 2;
        ModifierKeyState state = new ModifierKeyState(false);
        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(doubleTapTime);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(doubleTapTime);

        Assert.assertFalse(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());

        state.onPress();

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertTrue(state.isPressed());

        ShadowSystemClock.setCurrentTimeMillis(++millis);
        state.onRelease(doubleTapTime);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }

    @Test
    public void testSetActiveState() throws Exception {
        long millis = 1000;
        ShadowSystemClock.setCurrentTimeMillis(++millis);
        final int doubleTapTime = 2;
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
        state.onRelease(doubleTapTime);
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
        state.onRelease(doubleTapTime);

        Assert.assertTrue(state.isActive());
        Assert.assertFalse(state.isLocked());
        Assert.assertFalse(state.isPressed());
    }
}