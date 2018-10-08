package com.anysoftkeyboard.ime;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowSystemClock;

import java.util.Arrays;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardGestureTypingTest extends AnySoftKeyboardBaseTest {

    @Before
    @Override
    public void setUpForAnySoftKeyboardBase() throws Exception {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, true);
        super.setUpForAnySoftKeyboardBase();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
    }

    @Test
    public void testDoesNotOutputIfGestureTypingIsDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, false);
        simulateGestureProcess("hello");
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        verifyNoSuggestionsInteractions();
    }

    @Test
    public void testDoesNotCallGetWordsWhenGestureIsOff() {
        simulateFinishInputFlow();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, false);
        simulateOnStartInputFlow();
        ArgumentCaptor<DictionaryBackgroundLoader.Listener> captor = ArgumentCaptor.forClass(DictionaryBackgroundLoader.Listener.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.times(2)).setupSuggestionsForKeyboard(Mockito.anyList(), captor.capture());
        final DictionaryBackgroundLoader.Listener listener = captor.getAllValues().get(1);
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        Mockito.doReturn(new char[][]{"hello".toCharArray()}).when(dictionary).getWords();
        listener.onDictionaryLoadingStarted(dictionary);
        listener.onDictionaryLoadingDone(dictionary);
        Mockito.verify(dictionary, Mockito.never()).getWords();
    }

    @Test
    public void testCallsGetWordsWhenGestureIsOn() {
        ArgumentCaptor<DictionaryBackgroundLoader.Listener> captor = ArgumentCaptor.forClass(DictionaryBackgroundLoader.Listener.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyList(), captor.capture());
        final DictionaryBackgroundLoader.Listener listener = captor.getAllValues().get(0);
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        Mockito.doReturn(new char[][]{"hello".toCharArray()}).when(dictionary).getWords();
        listener.onDictionaryLoadingStarted(dictionary);
        listener.onDictionaryLoadingDone(dictionary);
        Mockito.verify(dictionary).getWords();
    }

    @Test
    public void testOutputPrimarySuggestionOnGestureDone() {
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testCanOutputFromBothDictionaries() {
        mAnySoftKeyboardUnderTest.mGestureTypingDetector.setWords(Arrays.asList(new char[][]{
                        "keyboard".toCharArray(),
                        "welcome".toCharArray(),
                        "is".toCharArray(),
                        "you".toCharArray(),
                },
                new char[][]{
                        "luck".toCharArray(),
                        "bye".toCharArray(),
                        "one".toCharArray(),
                        "two".toCharArray(),
                        "three".toCharArray()
                }));

        Robolectric.flushBackgroundThreadScheduler();

        simulateGestureProcess("keyboard");
        Assert.assertEquals("keyboard", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        simulateGestureProcess("luck");
        Assert.assertEquals("keyboard luck", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testConfirmsLastGesturesWhenPrintableKeyIsPressed() {
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress('a');
        Assert.assertEquals("hello a", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDoesNotConfirmLastGesturesWhenNonePrintableKeyIsPressed() {
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testConfirmsLastGesturesOnNextGestureStarts() {
        simulateGestureProcess("hello");
        simulateGestureProcess("welcome");
        Assert.assertEquals("hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDeleteGesturedWordOneCharacterAtTime() {
        simulateGestureProcess("hello");
        simulateGestureProcess("welcome");
        Assert.assertEquals("hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello welcom", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello welco", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello welc", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello wel", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello we", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hello w", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
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
    public void testRewriteGesturedWord() {
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hell", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("hel", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress('p');
        Assert.assertEquals("help", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertEquals("help ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("welcome");
        Assert.assertEquals("help welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals("help welcom", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateTextTyping("ing");
        Assert.assertEquals("help welcoming", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
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

    private void simulateGestureProcess(String pathKeys) {
        long time = ShadowSystemClock.currentTimeMillis();
        Keyboard.Key startKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode(pathKeys.charAt(0));
        mAnySoftKeyboardUnderTest.onPress(startKey.getPrimaryCode());
        mAnySoftKeyboardUnderTest.onGestureTypingInputStart(startKey.centerX, startKey.centerY, time);
        for (int keyIndex = 1; keyIndex < pathKeys.length(); keyIndex++) {
            final Keyboard.Key followingKey = mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode(pathKeys.charAt(keyIndex));
            //simulating gesture from startKey to followingKey
            final float xStep = startKey.width / 3;
            final float yStep = startKey.height / 3;

            final float xDistance = followingKey.x - startKey.x;
            final float yDistance = followingKey.y - startKey.y;
            int callsToMake = (int) Math.ceil(((xDistance + yDistance) / 2) / ((xStep + yStep) / 2));

            final long timeStep = 16;

            float currentX = startKey.x;
            float currentY = startKey.y;

            ShadowSystemClock.sleep(timeStep);
            time = ShadowSystemClock.currentTimeMillis();
            mAnySoftKeyboardUnderTest.onGestureTypingInput(startKey.centerX, startKey.centerY, time);

            while (callsToMake > 0) {
                callsToMake--;
                currentX += xStep;
                currentY += yStep;
                ShadowSystemClock.sleep(timeStep);
                time = ShadowSystemClock.currentTimeMillis();
                mAnySoftKeyboardUnderTest.onGestureTypingInput((int) currentX + 2, (int) currentY + 2, time);
            }

            startKey = followingKey;
        }
        mAnySoftKeyboardUnderTest.onGestureTypingInputDone();
    }
}