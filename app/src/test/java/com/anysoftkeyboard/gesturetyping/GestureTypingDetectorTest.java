package com.anysoftkeyboard.gesturetyping;

import static com.anysoftkeyboard.keyboards.ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_NORMAL;

import android.graphics.Point;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GestureTypingDetectorTest {
    private static final int MAX_SUGGESTIONS = 4;
    private List<Keyboard.Key> mKeys;
    private GestureTypingDetector mDetectorUnderTest;
    private AtomicReference<GestureTypingDetector.LoadingState> mCurrentState;
    private Disposable mSubscribeState;

    private Point getPointForCharacter(final int character) {
        return mKeys.stream()
                .filter(key -> key.getPrimaryCode() == character)
                .findFirst()
                .map(key -> new Point(key.centerX, key.centerY))
                .orElseGet(
                        () -> {
                            throw new RuntimeException(
                                    "Could not find key for character " + character);
                        });
    }

    @Before
    public void setUp() {
        final AnyKeyboard keyboard =
                AnyApplication.getKeyboardFactory(ApplicationProvider.getApplicationContext())
                        .getEnabledAddOns()
                        .get(0)
                        .createKeyboard(KEYBOARD_ROW_MODE_NORMAL);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens);
        mKeys = keyboard.getKeys();

        Robolectric.getBackgroundThreadScheduler().pause();

        mDetectorUnderTest = new GestureTypingDetector(2.5, MAX_SUGGESTIONS, 5, mKeys);
        mCurrentState = new AtomicReference<>();
        mSubscribeState =
                mDetectorUnderTest
                        .state()
                        .subscribe(
                                mCurrentState::set,
                                throwable -> {
                                    throw new RuntimeException(throwable);
                                });

        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
        mDetectorUnderTest.setWords(
                Collections.singletonList(
                        new char[][] {
                            "hell".toCharArray(),
                            "hello".toCharArray(),
                            "heello".toCharArray(),
                            "heko".toCharArray(),
                            "help".toCharArray(),
                            "good".toCharArray(),
                            "god".toCharArray(),
                            "gods".toCharArray(),
                        }),
                Collections.singletonList(new int[] {20, 250, 1, 1, 50, 190, 120, 100}));

        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());
    }

    @After
    public void tearDown() {
        mSubscribeState.dispose();
    }

    @Test
    public void testHappyPath() {
        Robolectric.flushBackgroundThreadScheduler();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

        mDetectorUnderTest.clearGesture();

        "helo"
                .chars()
                .boxed()
                .map(this::getPointForCharacter)
                .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
        final ArrayList<CharSequence> candidates = mDetectorUnderTest.getCandidates();

        Assert.assertEquals(MAX_SUGGESTIONS, candidates.size());
        Arrays.asList("hello", "heello", "hell", "heko")
                .forEach(
                        word ->
                                Assert.assertTrue(
                                        "Missing the word " + word, candidates.contains(word)));
    }

    @Test
    public void testTakesWordFrequencyIntoAccount() {
        Robolectric.flushBackgroundThreadScheduler();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

        mDetectorUnderTest.clearGesture();

        "help"
                .chars()
                .boxed()
                .map(this::getPointForCharacter)
                .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
        final ArrayList<CharSequence> candidates = mDetectorUnderTest.getCandidates();

        Assert.assertEquals(MAX_SUGGESTIONS, candidates.size());
        Assert.assertEquals("help", candidates.get(0));
        Assert.assertEquals("hello", candidates.get(1));
        Assert.assertEquals("hell", candidates.get(2));
        Assert.assertEquals("heello", candidates.get(3));
    }

    @Test
    public void testFilterOutWordsThatDoNotStartsWithFirstPress() {
        final ArrayList<CharSequence> candidates = new ArrayList<>();
        Robolectric.flushBackgroundThreadScheduler();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

        mDetectorUnderTest.clearGesture();

        "to"
                .chars()
                .boxed()
                .map(this::getPointForCharacter)
                .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
        candidates.addAll(mDetectorUnderTest.getCandidates());

        Assert.assertEquals(0, candidates.size());

        candidates.clear();
        mDetectorUnderTest.clearGesture();
        "god"
                .chars()
                .boxed()
                .map(this::getPointForCharacter)
                .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
        candidates.addAll(mDetectorUnderTest.getCandidates());

        Assert.assertEquals(3, candidates.size());
        Arrays.asList("good", "god", "gods")
                .forEach(
                        word ->
                                Assert.assertTrue(
                                        "Missing the word " + word, candidates.contains(word)));
    }

    @Test
    public void testCalculatesCornersInBackground() {
        Robolectric.getBackgroundThreadScheduler().unPause();
        Robolectric.flushBackgroundThreadScheduler();

        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

        mDetectorUnderTest.destroy();

        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
    }

    @Test
    public void testCalculatesCornersInBackgroundWithTwoDictionaries() {
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());
        mDetectorUnderTest.destroy();

        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
    }

    @Test
    public void testCalculatesCornersInBackgroundWithTwoDictionariesButDisposed() {
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        mSubscribeState.dispose();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());
        mDetectorUnderTest.destroy();

        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());
    }

    @Test
    public void testCalculatesCornersInBackgroundWithTwoDictionariesButDestroyed() {
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        mDetectorUnderTest.destroy();
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
        Robolectric.flushBackgroundThreadScheduler();
        mSubscribeState.dispose();

        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
    }
}
