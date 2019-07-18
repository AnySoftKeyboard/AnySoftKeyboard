package com.anysoftkeyboard.gesturetyping;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import io.reactivex.disposables.Disposable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GestureTypingDetectorTest {
    private Set<Keyboard.Key> mKeys;

    @Before
    public void setUp() {
        mKeys = Collections.singleton(Mockito.mock(AnyKeyboard.AnyKey.class));
    }

    @Test
    public void testCalculatesCornersInBackground() {
        Robolectric.getBackgroundThreadScheduler().pause();
        GestureTypingDetector detector = new GestureTypingDetector(5, mKeys);
        AtomicReference<GestureTypingDetector.LoadingState> currentState = new AtomicReference<>();
        final Disposable subscribe =
                detector.state()
                        .subscribe(
                                currentState::set,
                                throwable -> {
                                    throw new RuntimeException(throwable);
                                });
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, currentState.get());
        detector.setWords(Collections.singletonList(new char[][] {"hello".toCharArray()}));
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, currentState.get());

        Robolectric.getBackgroundThreadScheduler().unPause();
        Robolectric.flushBackgroundThreadScheduler();

        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, currentState.get());

        detector.destroy();

        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, currentState.get());

        subscribe.dispose();
    }

    @Test
    public void testCalculatesCornersInBackgroundWithTwoDictionaries() {
        Robolectric.getBackgroundThreadScheduler().pause();
        GestureTypingDetector detector = new GestureTypingDetector(5, mKeys);
        AtomicReference<GestureTypingDetector.LoadingState> currentState = new AtomicReference<>();
        final Disposable subscribe =
                detector.state()
                        .subscribe(
                                currentState::set,
                                throwable -> {
                                    throw new RuntimeException(throwable);
                                });
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, currentState.get());
        detector.setWords(
                Arrays.asList(
                        new char[][] {"hello".toCharArray()},
                        new char[][] {"goodbye".toCharArray()}));
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, currentState.get());
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, currentState.get());
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, currentState.get());
        detector.destroy();

        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, currentState.get());

        subscribe.dispose();
    }

    @Test
    public void testCalculatesCornersInBackgroundWithTwoDictionariesButDisposed() {
        Robolectric.getBackgroundThreadScheduler().pause();
        GestureTypingDetector detector = new GestureTypingDetector(5, mKeys);
        AtomicReference<GestureTypingDetector.LoadingState> currentState = new AtomicReference<>();
        final Disposable subscribe =
                detector.state()
                        .subscribe(
                                currentState::set,
                                throwable -> {
                                    throw new RuntimeException(throwable);
                                });
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, currentState.get());
        detector.setWords(
                Arrays.asList(
                        new char[][] {"hello".toCharArray()},
                        new char[][] {"goodbye".toCharArray()}));
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, currentState.get());
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        subscribe.dispose();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, currentState.get());
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, currentState.get());
        detector.destroy();

        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, currentState.get());
    }

    @Test
    public void testCalculatesCornersInBackgroundWithTwoDictionariesButDestroyed() {
        Robolectric.getBackgroundThreadScheduler().pause();
        GestureTypingDetector detector = new GestureTypingDetector(5, mKeys);
        AtomicReference<GestureTypingDetector.LoadingState> currentState = new AtomicReference<>();
        final Disposable subscribe =
                detector.state()
                        .subscribe(
                                currentState::set,
                                throwable -> {
                                    throw new RuntimeException(throwable);
                                });
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, currentState.get());
        detector.setWords(
                Arrays.asList(
                        new char[][] {"hello".toCharArray()},
                        new char[][] {"goodbye".toCharArray()}));
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, currentState.get());
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        detector.destroy();
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, currentState.get());
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, currentState.get());
        Robolectric.flushBackgroundThreadScheduler();
        subscribe.dispose();

        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, currentState.get());
    }
}
