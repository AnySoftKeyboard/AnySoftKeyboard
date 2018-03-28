package com.anysoftkeyboard;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowSystemClock;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardGestureTypingTest extends AnySoftKeyboardBaseTest {

    @Before
    @Override
    public void setUpForAnySoftKeyboardBase() throws Exception {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, true);
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, true);
        super.setUpForAnySoftKeyboardBase();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
    }

    @Test
    public void testDoesNotOutputIfGestureTypingIsDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, false);
        simulateGestureProcess("hello");
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        //it's null, because it was forcibly cleared
        Assert.assertNull(verifyAndCaptureSuggestion(true));
    }

    @Test
    public void testOutputPrimarySuggestionOnGestureDone() {
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testConfirmsLastGesturesWhenPrintableKeyIsPressed() {
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress('a');
        Assert.assertEquals("hello a", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testConfirmsLastGesturesWhenPrintableKeyIsPressedPickSuggestion() {
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "mysuggest");
        Assert.assertEquals("mysuggest ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress('a');
        Assert.assertEquals("mysuggest a", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testConfirmsLastGestureWhenShiftIsPressed() {
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isShifted());
        simulateGestureProcess("world");
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isShifted());
        Assert.assertEquals("hello World", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShift() {
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isShifted());
        simulateGestureProcess("world");
        Assert.assertEquals("hello World", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isShifted());
    }

    @Test
    public void testShiftBeginning() {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        simulateGestureProcess("hello");
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isShifted());
        Assert.assertEquals("Hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isShifted());
        Assert.assertEquals("Hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isShifted());
        simulateGestureProcess("world");
        Assert.assertEquals("Hello World", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isShifted());
    }

    @Test
    public void testShiftWithTyping() {
        simulateGestureProcess("hello");
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isShifted());
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isShifted());
        Assert.assertEquals("hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isShifted());
        mAnySoftKeyboardUnderTest.simulateTextTyping("world");
        Assert.assertEquals("hello World", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isShifted());
    }

    @Test
    public void testConfirmsLastGesturesOnNextGestureStarts() {
        simulateGestureProcess("hello");
        simulateGestureProcess("welcome");
        Assert.assertEquals("hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testConfirmsLastGesturesOnNextGestureStartsPickSuggestion() {
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "mysuggest");
        Assert.assertEquals("mysuggest ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("welcome");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "mysuggest2");
        Assert.assertEquals("mysuggest mysuggest2 ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDoesNotAddSpaceForTextIfSpacePressed() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("welcome");
        Assert.assertEquals("hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testAddsSpaceForText() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("welcome");
        Assert.assertEquals("hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testAddsSpaceForTextPickSuggestion() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "mysuggest");
        Assert.assertEquals("mysuggest ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("welcome");
        Assert.assertEquals("mysuggest welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testAddsSpaceForTextNoAutoSpace() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);
        mAnySoftKeyboardUnderTest.simulateTextTyping("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("welcome");
        Assert.assertEquals("hellowelcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testAddsSpaceForTextUnrecognized() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("notindictionary");
        simulateGestureProcess("welcome");
        Assert.assertEquals("notindictionary welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDeleteWholeGesturedWord() {
        simulateGestureProcess("hello");
        simulateGestureProcess("welcome");
        Assert.assertEquals("hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hell", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hel", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("he", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDeleteWholeGesturedWordPickSuggestion() {
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "ms1");
        simulateGestureProcess("welcome");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "ms2");
        Assert.assertEquals("ms1 ms2 ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("ms1 ms2", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("ms1 ms", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("ms1 m", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testRewriteGesturedWord() {
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress('p');
        Assert.assertEquals("p", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("p ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("welcome");
        Assert.assertEquals("p welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("p ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateTextTyping("ing");
        Assert.assertEquals("p ing", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testRewriteGesturedWordPickSuggestion() {
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "ms1");
        Assert.assertEquals("ms1 ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("ms1", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress('p');
        Assert.assertEquals("ms1p", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("ms1p ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("welcome");
        Assert.assertEquals("ms1p welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("ms1p ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateTextTyping("ing");
        Assert.assertEquals("ms1p ing", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testSpaceAfterGestureJustConfirms() {
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("you");
        Assert.assertEquals("hello you", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateTextTyping("all");
        Assert.assertEquals("hello you all", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello you al", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDeleteGesturedWordOnWholeWord() {
        simulateGestureProcess("hello");
        simulateGestureProcess("welcome");
        Assert.assertEquals("hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDeleteGesturedWordOnWholeWordPickSuggestion() {
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.pickSuggestionManually(1, "ms");
        simulateGestureProcess("welcome");
        Assert.assertEquals("ms welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("ms ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("ms", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testOutputPrimarySuggestionOnGestureDoneNoAutoSpace() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testConfirmsLastGesturesWhenPrintableKeyIsPressedNoAutoSpace() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress('a');
        Assert.assertEquals("helloa", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testConfirmsLastGestureWhenShiftIsPressedNoAutoSpace() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testConfirmsLastGesturesOnNextGestureStartsNoAutoSpace() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);
        simulateGestureProcess("hello");
        simulateGestureProcess("welcome");
        Assert.assertEquals("hellowelcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDeleteWholeGesturedWordNoAutoSpace() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);
        simulateGestureProcess("hello");
        simulateGestureProcess("welcome");
        Assert.assertEquals("hellowelcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hell", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hel", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("he", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("h", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }


    @Test
    public void testRewriteGesturedWordNoAutoSpace() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress('p');
        Assert.assertEquals("p", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("p ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("welcome");
        Assert.assertEquals("p welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("p ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateTextTyping("ing");
        Assert.assertEquals("p ing", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testSpaceAfterGestureJustConfirmsNoAutoSpace() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("you");
        Assert.assertEquals("hello you", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateTextTyping("all");
        Assert.assertEquals("hello youall", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello youal", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDeleteGesturedWordOnWholeWordNoAutoSpace() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_auto_space, false);
        simulateGestureProcess("hello");
        simulateGestureProcess("welcome");
        Assert.assertEquals("hellowelcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }


    private void simulateGestureProcess(String pathKeys) {
        long time = ShadowSystemClock.currentTimeMillis();
        Keyboard.Key startKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode(pathKeys.charAt(0));
        mAnySoftKeyboardUnderTest.onPress(startKey.getPrimaryCode());
        int w = startKey.width/2;
        int h = startKey.height/2;
        mAnySoftKeyboardUnderTest.onGestureTypingInputStart(startKey.x + w, startKey.y + h, time);
        mAnySoftKeyboardUnderTest.onUpdateSelection(0,0,0,0,0,0);
        for (int keyIndex = 1; keyIndex < pathKeys.length(); keyIndex++) {
            final Keyboard.Key followingKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode(pathKeys.charAt(keyIndex));
            //simulating gesture from startKey to followingKey
            final long timeStep = 16;

            ShadowSystemClock.sleep(timeStep);
            time = ShadowSystemClock.currentTimeMillis();
            mAnySoftKeyboardUnderTest.onGestureTypingInput(startKey.x + w, startKey.y + h, time);

            startKey = followingKey;
        }
        mAnySoftKeyboardUnderTest.onGestureTypingInput(startKey.x + w, startKey.y + h, time);
        mAnySoftKeyboardUnderTest.onGestureTypingInputDone();
        mAnySoftKeyboardUnderTest.onRelease(startKey.getPrimaryCode());
    }
}