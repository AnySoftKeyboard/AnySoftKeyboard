package com.anysoftkeyboard.ime;

import android.os.SystemClock;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.addons.SupportTest;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowSystemClock;

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
        ArgumentCaptor<DictionaryBackgroundLoader.Listener> captor =
                ArgumentCaptor.forClass(DictionaryBackgroundLoader.Listener.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.times(2))
                .setupSuggestionsForKeyboard(Mockito.anyList(), captor.capture());
        final DictionaryBackgroundLoader.Listener listener = captor.getAllValues().get(1);
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        Mockito.doReturn(new char[][] {"hello".toCharArray()}).when(dictionary).getWords();
        listener.onDictionaryLoadingStarted(dictionary);
        listener.onDictionaryLoadingDone(dictionary);
        Mockito.verify(dictionary, Mockito.never()).getWords();
    }

    @Test
    public void testCallsGetWordsWhenGestureIsOn() {
        ArgumentCaptor<DictionaryBackgroundLoader.Listener> captor =
                ArgumentCaptor.forClass(DictionaryBackgroundLoader.Listener.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), captor.capture());
        final DictionaryBackgroundLoader.Listener listener = captor.getAllValues().get(0);
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        Mockito.doReturn(new char[][] {"hello".toCharArray()}).when(dictionary).getWords();
        listener.onDictionaryLoadingStarted(dictionary);
        listener.onDictionaryLoadingDone(dictionary);
        Mockito.verify(dictionary).getWords();
    }

    @Test
    public void testNotCrashingWhenExceptionIsThrownInGetWordsAndGestureIsOn() {
        ArgumentCaptor<DictionaryBackgroundLoader.Listener> captor =
                ArgumentCaptor.forClass(DictionaryBackgroundLoader.Listener.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), captor.capture());
        final DictionaryBackgroundLoader.Listener listener = captor.getAllValues().get(0);
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        Mockito.doThrow(new UnsupportedOperationException()).when(dictionary).getWords();
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
        mAnySoftKeyboardUnderTest
                .mGestureTypingDetectors
                .get(
                        AnySoftKeyboardWithGestureTyping.getKeyForDetector(
                                mAnySoftKeyboardUnderTest.getCurrentKeyboard()))
                .setWords(
                        Arrays.asList(
                                new char[][] {
                                    "keyboard".toCharArray(),
                                    "welcome".toCharArray(),
                                    "is".toCharArray(),
                                    "you".toCharArray(),
                                },
                                new char[][] {
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
        Assert.assertEquals(
                "keyboard luck", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        simulateGestureProcess("bye");
        Assert.assertEquals(
                "keyboard luck bye", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
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
        Assert.assertEquals(
                "hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDeleteGesturedWordOneCharacterAtTime() {
        simulateGestureProcess("hello");
        simulateGestureProcess("welcome");
        Assert.assertEquals(
                "hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals(
                "hello welcom", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals(
                "hello welco", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals(
                "hello welc", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
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
        Assert.assertEquals(
                "help welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals(
                "help welcom", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateTextTyping("ing");
        Assert.assertEquals(
                "help welcoming", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
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
        Assert.assertEquals(
                "hello you all", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testDeleteGesturedWordOnWholeWord() {
        simulateGestureProcess("hello");
        simulateGestureProcess("welcome");
        Assert.assertEquals(
                "hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE_WORD);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testClearAllDetectorsWhenCriticalAddOnChange() {
        Assert.assertTrue(mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size() > 0);

        SupportTest.ensureKeyboardAtIndexEnabled(1, true);

        Assert.assertEquals(0, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());

        simulateOnStartInputFlow();

        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
    }

    @Test
    public void testClearDetectorsOnLowMemory() {
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);
        simulateOnStartInputFlow();
        final GestureTypingDetector detector1 = getCurrentGestureTypingDetectorFromMap();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        final GestureTypingDetector detector2 = getCurrentGestureTypingDetectorFromMap();

        // this keeps the currently used detector2, but kills the second
        mAnySoftKeyboardUnderTest.onLowMemory();
        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        Assert.assertSame(detector2, getCurrentGestureTypingDetectorFromMap());

        Assert.assertEquals(
                GestureTypingDetector.LoadingState.NOT_LOADED, detector1.state().blockingFirst());
        Assert.assertEquals(
                GestureTypingDetector.LoadingState.LOADED, detector2.state().blockingFirst());
    }

    @Test
    public void testDoesNotCrashIfOnLowMemoryCalledBeforeLoaded() {
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);
        simulateOnStartInputFlow();
        final GestureTypingDetector detector1 = getCurrentGestureTypingDetectorFromMap();
        Assert.assertNotNull(detector1);

        Robolectric.getBackgroundThreadScheduler().pause();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        final GestureTypingDetector detector2 = getCurrentGestureTypingDetectorFromMap();
        Assert.assertEquals(
                GestureTypingDetector.LoadingState.LOADING, detector2.state().blockingFirst());

        // this keeps the currently used detector2, but kills the second
        mAnySoftKeyboardUnderTest.onLowMemory();
        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        Assert.assertSame(detector2, getCurrentGestureTypingDetectorFromMap());

        Robolectric.getBackgroundThreadScheduler().unPause();
        Robolectric.flushBackgroundThreadScheduler();
        Assert.assertEquals(
                GestureTypingDetector.LoadingState.LOADED, detector2.state().blockingFirst());
    }

    @Test
    public void testCreatesDetectorOnNewKeyboard() {
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);

        Assert.assertEquals(0, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());

        simulateOnStartInputFlow();

        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        final GestureTypingDetector detector1 = getCurrentGestureTypingDetectorFromMap();
        Assert.assertNotNull(detector1);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);

        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        final GestureTypingDetector detector2 = getCurrentGestureTypingDetectorFromMap();
        Assert.assertNotNull(detector2);
        Assert.assertNotSame(detector1, detector2);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);

        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        // cached now
        final GestureTypingDetector detector1Again = getCurrentGestureTypingDetectorFromMap();
        Assert.assertNotNull(detector1Again);
        Assert.assertSame(detector1, detector1Again);
    }

    private GestureTypingDetector getCurrentGestureTypingDetectorFromMap() {
        return mAnySoftKeyboardUnderTest.mGestureTypingDetectors.get(
                AnySoftKeyboardWithGestureTyping.getKeyForDetector(
                        mAnySoftKeyboardUnderTest.getCurrentKeyboard()));
    }

    @Test
    public void testBadgeGestureLifeCycle() {
        Robolectric.getBackgroundThreadScheduler().pause();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, false);
        Robolectric.flushBackgroundThreadScheduler();

        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_gesture);
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(),
                R.drawable.ic_watermark_gesture_not_loaded);

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, true);

        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_gesture);
        ViewTestUtils.assertCurrentWatermarkHasDrawable(
                mAnySoftKeyboardUnderTest.getInputView(),
                R.drawable.ic_watermark_gesture_not_loaded);

        simulateOnStartInputFlow();

        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_gesture);
        ViewTestUtils.assertCurrentWatermarkHasDrawable(
                mAnySoftKeyboardUnderTest.getInputView(),
                R.drawable.ic_watermark_gesture_not_loaded);

        Robolectric.flushBackgroundThreadScheduler();

        ViewTestUtils.assertCurrentWatermarkHasDrawable(
                mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_gesture);
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(),
                R.drawable.ic_watermark_gesture_not_loaded);
    }

    @Test
    public void testBadgeClearedWhenPrefDisabled() {
        ViewTestUtils.assertCurrentWatermarkHasDrawable(
                mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_gesture);
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(),
                R.drawable.ic_watermark_gesture_not_loaded);

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, false);

        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_gesture);
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(),
                R.drawable.ic_watermark_gesture_not_loaded);

        Assert.assertEquals(0, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
    }

    @Test
    public void testBadgeClearedWhenSwitchingToSymbols() {
        ViewTestUtils.assertCurrentWatermarkHasDrawable(
                mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_gesture);
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(),
                R.drawable.ic_watermark_gesture_not_loaded);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);

        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_gesture);
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(),
                R.drawable.ic_watermark_gesture_not_loaded);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        ViewTestUtils.assertCurrentWatermarkHasDrawable(
                mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_gesture);
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(
                mAnySoftKeyboardUnderTest.getInputView(),
                R.drawable.ic_watermark_gesture_not_loaded);
    }

    private void simulateGestureProcess(String pathKeys) {
        long time = ShadowSystemClock.currentTimeMillis();
        Keyboard.Key startKey =
                mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode(pathKeys.charAt(0));
        mAnySoftKeyboardUnderTest.onPress(startKey.getPrimaryCode());
        mAnySoftKeyboardUnderTest.onGestureTypingInputStart(
                startKey.centerX, startKey.centerY, (AnyKeyboard.AnyKey) startKey, time);
        for (int keyIndex = 1; keyIndex < pathKeys.length(); keyIndex++) {
            final Keyboard.Key followingKey =
                    mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode(pathKeys.charAt(keyIndex));
            // simulating gesture from startKey to followingKey
            final float xStep = startKey.width / 3;
            final float yStep = startKey.height / 3;

            final float xDistance = followingKey.x - startKey.x;
            final float yDistance = followingKey.y - startKey.y;
            int callsToMake =
                    (int) Math.ceil(((xDistance + yDistance) / 2) / ((xStep + yStep) / 2));

            final long timeStep = 16;

            float currentX = startKey.x;
            float currentY = startKey.y;

            SystemClock.sleep(timeStep);
            time = ShadowSystemClock.currentTimeMillis();
            mAnySoftKeyboardUnderTest.onGestureTypingInput(
                    startKey.centerX, startKey.centerY, time);

            while (callsToMake > 0) {
                callsToMake--;
                currentX += xStep;
                currentY += yStep;
                SystemClock.sleep(timeStep);
                time = ShadowSystemClock.currentTimeMillis();
                mAnySoftKeyboardUnderTest.onGestureTypingInput(
                        (int) currentX + 2, (int) currentY + 2, time);
            }

            startKey = followingKey;
        }
        mAnySoftKeyboardUnderTest.onGestureTypingInputDone();
    }
}
