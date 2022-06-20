package com.anysoftkeyboard.ime;

import static org.mockito.ArgumentMatchers.any;

import android.view.View;
import com.anysoftkeyboard.AddOnTestUtils;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.dictionaries.GetWordsCallback;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.CompositeDisposable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowSystemClock;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardGestureTypingTest extends AnySoftKeyboardBaseTest {

    private CompositeDisposable mDisposable;

    @Before
    @Override
    public void setUpForAnySoftKeyboardBase() throws Exception {
        mDisposable = new CompositeDisposable();
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, true);
        super.setUpForAnySoftKeyboardBase();
        com.anysoftkeyboard.rx.TestRxSchedulers.backgroundFlushAllJobs();
        TestRxSchedulers.foregroundFlushAllJobs();
    }

    @After
    public void tearDownDisposables() {
        mDisposable.dispose();
    }

    private Supplier<GestureTypingDetector.LoadingState> createLatestStateProvider(
            GestureTypingDetector detector) {
        final AtomicReference<GestureTypingDetector.LoadingState> currentState =
                new AtomicReference<>();
        mDisposable.add(
                detector.state()
                        .subscribe(
                                currentState::set,
                                e -> {
                                    throw new RuntimeException(e);
                                }));
        return currentState::get;
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
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.times(2))
                .setupSuggestionsForKeyboard(Mockito.anyList(), captor.capture());
        final DictionaryBackgroundLoader.Listener listener = captor.getAllValues().get(1);
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        Mockito.doAnswer(
                        invocation -> {
                            ((GetWordsCallback) invocation.getArgument(0))
                                    .onGetWordsFinished(
                                            new char[][] {"hello".toCharArray()}, new int[] {1});
                            return null;
                        })
                .when(dictionary)
                .getLoadedWords(any());
        listener.onDictionaryLoadingStarted(dictionary);
        listener.onDictionaryLoadingDone(dictionary);
        Mockito.verify(dictionary, Mockito.never()).getLoadedWords(any());
    }

    @Test
    public void testCallsGetWordsWhenGestureIsOn() {
        ArgumentCaptor<DictionaryBackgroundLoader.Listener> captor =
                ArgumentCaptor.forClass(DictionaryBackgroundLoader.Listener.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), captor.capture());
        final DictionaryBackgroundLoader.Listener listener = captor.getAllValues().get(0);
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        Mockito.doAnswer(
                        invocation -> {
                            ((GetWordsCallback) invocation.getArgument(0))
                                    .onGetWordsFinished(
                                            new char[][] {"hello".toCharArray()}, new int[] {1});
                            return null;
                        })
                .when(dictionary)
                .getLoadedWords(any());
        listener.onDictionaryLoadingStarted(dictionary);
        listener.onDictionaryLoadingDone(dictionary);
        Mockito.verify(dictionary).getLoadedWords(any());
    }

    @Test
    public void testNotCrashingWhenExceptionIsThrownInGetWordsAndGestureIsOn() {
        ArgumentCaptor<DictionaryBackgroundLoader.Listener> captor =
                ArgumentCaptor.forClass(DictionaryBackgroundLoader.Listener.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), captor.capture());
        final DictionaryBackgroundLoader.Listener listener = captor.getAllValues().get(0);
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        Mockito.doThrow(new UnsupportedOperationException()).when(dictionary).getLoadedWords(any());
        listener.onDictionaryLoadingStarted(dictionary);
        listener.onDictionaryLoadingDone(dictionary);
        Mockito.verify(dictionary).getLoadedWords(any());
    }

    @Test
    public void testOutputPrimarySuggestionOnGestureDone() {
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testOutputCapitalisedOnShiftLocked() {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT_LOCK);
        simulateGestureProcess("hello");
        Assert.assertEquals("HELLO", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testOutputTitleCaseOnShifted() {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SHIFT);
        simulateGestureProcess("hello");
        Assert.assertEquals("Hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
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
                                    "three".toCharArray(),
                                    "tree".toCharArray()
                                }),
                        Arrays.asList(
                                new int[] {50, 100, 250, 200},
                                new int[] {80, 190, 220, 140, 130, 27}));

        TestRxSchedulers.drainAllTasks();

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
    public void testOnlySingleSpaceAfterPickingGestureSuggestion() {
        simulateGestureProcess("hello");
        Assert.assertEquals("hello", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.pickSuggestionManually(0, "hello", true);
        Assert.assertEquals("hello ", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        simulateGestureProcess("welcome");
        Assert.assertEquals(
                "hello welcome", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
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
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testShowClearGestureButton() {
        simulateGestureProcess("hello");
        Assert.assertTrue(mAnySoftKeyboardUnderTest.mClearLastGestureAction.isVisible());
    }

    @Test
    public void testHideClearGestureButtonOnConfirmed() {
        simulateGestureProcess("hello");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SPACE);
        Assert.assertFalse(mAnySoftKeyboardUnderTest.mClearLastGestureAction.isVisible());
    }

    @Test
    public void testClearGestureButtonClearsGesture() {
        simulateGestureProcess("hello");
        final KeyboardViewContainerView.StripActionProvider provider =
                mAnySoftKeyboardUnderTest.mClearLastGestureAction;
        View rootActionView =
                provider.inflateActionView(mAnySoftKeyboardUnderTest.getInputViewContainer())
                        .findViewById(R.id.clear_gesture_strip_root);
        final View.OnClickListener onClickListener =
                Shadows.shadowOf(rootActionView).getOnClickListener();

        onClickListener.onClick(rootActionView);

        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testHideClearGestureButtonOnClear() {
        simulateGestureProcess("hello");
        final KeyboardViewContainerView.StripActionProvider provider =
                mAnySoftKeyboardUnderTest.mClearLastGestureAction;
        View rootActionView =
                provider.inflateActionView(mAnySoftKeyboardUnderTest.getInputViewContainer())
                        .findViewById(R.id.clear_gesture_strip_root);
        final View.OnClickListener onClickListener =
                Shadows.shadowOf(rootActionView).getOnClickListener();

        onClickListener.onClick(rootActionView);

        Assert.assertFalse(mAnySoftKeyboardUnderTest.mClearLastGestureAction.isVisible());
    }

    @Test
    public void testClearAllDetectorsWhenCriticalAddOnChange() {
        Assert.assertTrue(mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size() > 0);

        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);

        Assert.assertEquals(0, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());

        simulateOnStartInputFlow();

        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
    }

    @Test
    public void testClearDetectorsOnLowMemory() {
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);
        simulateOnStartInputFlow();
        final GestureTypingDetector detector1 = getCurrentGestureTypingDetectorFromMap();
        Supplier<GestureTypingDetector.LoadingState> detector1State =
                createLatestStateProvider(detector1);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        final GestureTypingDetector detector2 = getCurrentGestureTypingDetectorFromMap();
        Supplier<GestureTypingDetector.LoadingState> detector2State =
                createLatestStateProvider(detector2);

        // this keeps the currently used detector2, but kills the second
        mAnySoftKeyboardUnderTest.onLowMemory();
        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        Assert.assertSame(detector2, getCurrentGestureTypingDetectorFromMap());

        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, detector1State.get());
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, detector2State.get());
    }

    @Test
    public void testDoesNotCrashIfOnLowMemoryCalledBeforeLoaded() {
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);
        simulateOnStartInputFlow();
        final GestureTypingDetector detector1 = getCurrentGestureTypingDetectorFromMap();
        Assert.assertNotNull(detector1);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        final GestureTypingDetector detector2 = getCurrentGestureTypingDetectorFromMap();
        Supplier<GestureTypingDetector.LoadingState> detector2State =
                createLatestStateProvider(detector2);
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, detector2State.get());

        // this keeps the currently used detector2, but kills the second
        mAnySoftKeyboardUnderTest.onLowMemory();
        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.mGestureTypingDetectors.size());
        Assert.assertSame(detector2, getCurrentGestureTypingDetectorFromMap());

        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, detector2State.get());
    }

    @Test
    public void testCreatesDetectorOnNewKeyboard() {
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);

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
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, false);
        TestRxSchedulers.drainAllTasks();

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

        TestRxSchedulers.drainAllTasks();

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
        TestRxSchedulers.drainAllTasks();

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
        TestRxSchedulers.drainAllTasks();
        mAnySoftKeyboardUnderTest.onGestureTypingInputStart(
                startKey.centerX, startKey.centerY, (AnyKeyboard.AnyKey) startKey, time);
        TestRxSchedulers.drainAllTasks();
        for (int keyIndex = 1; keyIndex < pathKeys.length(); keyIndex++) {
            final Keyboard.Key followingKey =
                    mAnySoftKeyboardUnderTest.findKeyWithPrimaryKeyCode(pathKeys.charAt(keyIndex));
            // simulating gesture from startKey to followingKey
            final float xStep = startKey.width / 3.0f;
            final float yStep = startKey.height / 3.0f;

            final float xDistance = followingKey.centerX - startKey.centerX;
            final float yDistance = followingKey.centerY - startKey.centerY;
            int callsToMake =
                    (int) Math.ceil(((xDistance + yDistance) / 2f) / ((xStep + yStep) / 2f));

            final long timeStep = 16;

            float currentX = startKey.centerX;
            float currentY = startKey.centerY;

            TestRxSchedulers.foregroundAdvanceBy(timeStep);
            time = ShadowSystemClock.currentTimeMillis();
            mAnySoftKeyboardUnderTest.onGestureTypingInput(
                    startKey.centerX, startKey.centerY, time);

            while (callsToMake > 0) {
                callsToMake--;
                currentX += xStep;
                currentY += yStep;
                TestRxSchedulers.foregroundAdvanceBy(timeStep);
                time = ShadowSystemClock.currentTimeMillis();
                mAnySoftKeyboardUnderTest.onGestureTypingInput(
                        (int) currentX, (int) currentY, time);
            }

            TestRxSchedulers.foregroundAdvanceBy(timeStep);
            time = ShadowSystemClock.currentTimeMillis();
            mAnySoftKeyboardUnderTest.onGestureTypingInput(
                    followingKey.centerX, followingKey.centerY, time);

            startKey = followingKey;
        }
        mAnySoftKeyboardUnderTest.onGestureTypingInputDone();
        TestRxSchedulers.drainAllTasks();
    }
}
