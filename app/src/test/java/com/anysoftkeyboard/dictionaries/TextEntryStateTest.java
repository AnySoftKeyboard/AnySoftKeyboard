package com.anysoftkeyboard.dictionaries;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TextEntryStateTest {
    @Before
    public void setUp() throws Exception {
        TextEntryState.newSession(true);
    }

    @After
    public void tearDown() throws Exception {
        TextEntryState.newSession(false);
    }

    @Test
    public void testRestartSession() throws Exception {
        Assert.assertEquals(TextEntryState.State.START, TextEntryState.getState());
        TextEntryState.typedCharacter('h', false);
        Assert.assertNotEquals(TextEntryState.State.START, TextEntryState.getState());

        TextEntryState.restartSession();
        Assert.assertEquals(TextEntryState.State.START, TextEntryState.getState());
        Assert.assertFalse(TextEntryState.isPredicting());
    }

    @Test
    public void testRestartSessionKeepsPredictionFlagEnabled() throws Exception {
        TextEntryState.newSession(true);
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        Assert.assertTrue(TextEntryState.isPredicting());

        TextEntryState.restartSession();
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        Assert.assertTrue(TextEntryState.isPredicting());
    }

    @Test
    public void testRestartSessionKeepsPredictionFlagDisabled() throws Exception {
        TextEntryState.newSession(false);
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        Assert.assertFalse(TextEntryState.isPredicting());

        TextEntryState.restartSession();
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        Assert.assertFalse(TextEntryState.isPredicting());
    }

    @Test
    public void testAlwaysNotPredictingIfSessionIsDisabled() throws Exception {
        TextEntryState.newSession(false);
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        Assert.assertFalse(TextEntryState.isPredicting());
    }

    @Test
    public void testIsPredictingIfStartTyping() throws Exception {
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        Assert.assertTrue(TextEntryState.isPredicting());
        TextEntryState.acceptedDefault("hello");
        Assert.assertFalse(TextEntryState.isPredicting());
    }

    @Test
    public void testNotIsPredictingAfterAcceptedTyped() throws Exception {
        TextEntryState.typedCharacter('h', false);
        Assert.assertTrue(TextEntryState.isPredicting());
        TextEntryState.acceptedTyped();
        Assert.assertFalse(TextEntryState.isPredicting());
    }

    @Test
    public void testNotIsPredictingIfStartTypingAndThenSeparator() throws Exception {
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        TextEntryState.typedCharacter(' ', true);
        Assert.assertFalse(TextEntryState.isPredicting());
    }
}