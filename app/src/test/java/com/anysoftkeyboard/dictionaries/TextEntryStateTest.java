package com.anysoftkeyboard.dictionaries;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
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

    @Test
    public void testNotPunctuationAfterAcceptedIfSeparatorIsEnter() throws Exception {
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        Assert.assertEquals(TextEntryState.State.IN_WORD, TextEntryState.getState());
        TextEntryState.acceptedDefault("h");
        Assert.assertEquals(TextEntryState.State.ACCEPTED_DEFAULT, TextEntryState.getState());
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('\n', true);
        Assert.assertFalse(TextEntryState.isPredicting());
        Assert.assertEquals(TextEntryState.State.UNKNOWN, TextEntryState.getState());
    }

    @Test
    public void testPunctuationAfterAcceptedIfSeparatorIsNotEnterAndNotSpace() throws Exception {
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        Assert.assertEquals(TextEntryState.State.IN_WORD, TextEntryState.getState());
        TextEntryState.acceptedDefault("h");
        Assert.assertEquals(TextEntryState.State.ACCEPTED_DEFAULT, TextEntryState.getState());
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter(',', true);
        Assert.assertFalse(TextEntryState.isPredicting());
        Assert.assertEquals(TextEntryState.State.PUNCTUATION_AFTER_ACCEPTED, TextEntryState.getState());
    }

    @Test
    public void testSpaceAfterAcceptedIfSeparatorIsSpace() throws Exception {
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter('h', false);
        Assert.assertEquals(TextEntryState.State.IN_WORD, TextEntryState.getState());
        TextEntryState.acceptedDefault("h");
        Assert.assertEquals(TextEntryState.State.ACCEPTED_DEFAULT, TextEntryState.getState());
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.typedCharacter(' ', true);
        Assert.assertFalse(TextEntryState.isPredicting());
        Assert.assertEquals(TextEntryState.State.SPACE_AFTER_ACCEPTED, TextEntryState.getState());
    }

    @Test
    public void testGestureHappyPath() throws Exception {
        Assert.assertFalse(TextEntryState.isPredicting());
        TextEntryState.performedGesture();
        Assert.assertTrue(TextEntryState.isPredicting());
        Assert.assertEquals(TextEntryState.State.PERFORMED_GESTURE, TextEntryState.getState());
        TextEntryState.acceptedDefault("hello");
        Assert.assertFalse(TextEntryState.isPredicting());
        Assert.assertEquals(TextEntryState.State.ACCEPTED_DEFAULT, TextEntryState.getState());

        TextEntryState.performedGesture();
        Assert.assertTrue(TextEntryState.isPredicting());
        TextEntryState.typedCharacter(' ', true);
        Assert.assertFalse(TextEntryState.isPredicting());
        Assert.assertEquals(TextEntryState.State.SPACE_AFTER_PICKED, TextEntryState.getState());
    }

    @Test
    public void testGestureAndBackspace() throws Exception {
        TextEntryState.performedGesture();
        TextEntryState.backspace();
        Assert.assertTrue(TextEntryState.isPredicting());
        Assert.assertEquals(TextEntryState.State.IN_WORD, TextEntryState.getState());
    }
}