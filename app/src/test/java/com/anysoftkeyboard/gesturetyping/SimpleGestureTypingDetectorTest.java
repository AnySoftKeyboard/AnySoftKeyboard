package com.anysoftkeyboard.gesturetyping;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.disposables.Disposable;

import static com.anysoftkeyboard.keyboards.ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_NORMAL;
import static java.lang.Math.sqrt;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SimpleGestureTypingDetectorTest {
    private static final int MAX_SUGGESTIONS = 4;
    private static final int PRUNING_DISTANCE = 7;
    private List<Keyboard.Key> mKeys;
    private SimpleGestureTypingDetector mDetectorUnderTest;
    private AtomicReference<SimpleGestureTypingDetector.LoadingState> mCurrentState;
    private Disposable mSubscribeState;


    @Before
    public void setUp() {
        final Context context = ApplicationProvider.getApplicationContext();
        final AnyKeyboard keyboard =
                AnyApplication.getKeyboardFactory(context)
                        .getAddOnById(context.getString(R.string.main_english_keyboard_id))
                        .createKeyboard(KEYBOARD_ROW_MODE_NORMAL);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens);
        mKeys = keyboard.getKeys();

        Robolectric.getBackgroundThreadScheduler().pause();

        mDetectorUnderTest = new SimpleGestureTypingDetector(
            MAX_SUGGESTIONS,
            context.getResources()
                   .getDimensionPixelSize(R.dimen.gesture_typing_min_point_distance),
            PRUNING_DISTANCE,
            mKeys
        );

        mCurrentState = new AtomicReference<>();
        mSubscribeState =
                mDetectorUnderTest
                        .state()
                        .subscribe(
                                mCurrentState::set,
                                throwable -> {
                                    throw new RuntimeException(throwable);
                                });

        Assert.assertEquals(
                SimpleGestureTypingDetector.LoadingState.NOT_LOADED,
                mCurrentState.get()
        );
        mDetectorUnderTest.setWords(
                Collections.singletonList(
                        new char[][] {
                                // this list is sorted alphabetically (as in the binary dictionary)
                                "Hall".toCharArray(),
                                "hell".toCharArray(),
                                "hello".toCharArray(),
                                "help".toCharArray(),
                                "hero".toCharArray(),
                                "God".toCharArray(),
                                "gods".toCharArray(),
                                "good".toCharArray()
                        }),
                Collections.singletonList(new int[] {134, 126, 108, 120, 149, 129, 121, 170}));

        Assert.assertEquals(SimpleGestureTypingDetector.LoadingState.LOADED, mCurrentState.get());
    }

    @After
    public void tearDown() {
        mSubscribeState.dispose();
    }

    @Test
    public void testGestureGetLength() {
        char[] testWord = "ab".toCharArray();
        GestureTypingDetector.Gesture gesture = GestureTypingDetector
                .Gesture
                .generateIdealGesture(testWord, mDetectorUnderTest.mKeysByCharacter);

        Keyboard.Key key1 = mDetectorUnderTest.mKeysByCharacter.get('a');
        Keyboard.Key key2 = mDetectorUnderTest.mKeysByCharacter.get('b');
        double length = SimpleGestureTypingDetector.euclideanDistance(
                key1.centerX,
                key1.centerY,
                key2.centerX,
                key2.centerY
        );

        Assert.assertEquals(length, gesture.getLength(), 0.1);
    }

    @Test
    public void testGestureGenerateIdealGesture() {
        char[] testWord = "ab".toCharArray();
        GestureTypingDetector.Gesture gesture = GestureTypingDetector
                .Gesture
                .generateIdealGesture(testWord, mDetectorUnderTest.mKeysByCharacter);

        Keyboard.Key key1 = mDetectorUnderTest.mKeysByCharacter.get('a');
        Keyboard.Key key2 = mDetectorUnderTest.mKeysByCharacter.get('b');

        Assert.assertEquals(2, gesture.getCurrentLength());

        Assert.assertEquals(key1.centerX, gesture.getFirstX());
        Assert.assertEquals(key1.centerY, gesture.getFirstY());
        Assert.assertEquals(key2.centerX, gesture.getLastX());
        Assert.assertEquals(key2.centerY, gesture.getLastY());
    }

    @Test
    public void testGesturePointGetters() {
        char[] testWord = "abc".toCharArray();
        GestureTypingDetector.Gesture gesture = GestureTypingDetector
                .Gesture
                .generateIdealGesture(testWord, mDetectorUnderTest.mKeysByCharacter);

        Assert.assertEquals(gesture.getFirstX(), gesture.getX(0));
        Assert.assertEquals(gesture.getFirstY(), gesture.getY(0));
        Assert.assertEquals(gesture.getLastX(), gesture.getX(2));
        Assert.assertEquals(gesture.getLastY(), gesture.getY(2));
    }

    @Test
    public void testGestureResample() {
        char[] testWord = "ab".toCharArray();
        GestureTypingDetector.Gesture gesture = GestureTypingDetector
                .Gesture
                .generateIdealGesture(testWord, mDetectorUnderTest.mKeysByCharacter);

        Keyboard.Key key1 = mDetectorUnderTest.mKeysByCharacter.get('a');
        Keyboard.Key key2 = mDetectorUnderTest.mKeysByCharacter.get('b');
        double length = SimpleGestureTypingDetector.euclideanDistance(
                key1.centerX,
                key1.centerY,
                key2.centerX,
                key2.centerY
        );

        GestureTypingDetector.Gesture resampled = gesture.resample(300);

        Assert.assertEquals(2, gesture.getCurrentLength());
        Assert.assertEquals(300, resampled.getCurrentLength());

        double epsilon = 5. / 100 * length;

        Assert.assertEquals(length, gesture.getLength(), epsilon);
        Assert.assertEquals(length, resampled.getLength(), epsilon);
    }

    @Test
    public void testEuclideanDistance() {
        double expected = sqrt(2);

        double distance1 = SimpleGestureTypingDetector.euclideanDistance(0, 0, 1, 1);
        double distance2 = SimpleGestureTypingDetector.euclideanDistance(1, 0, 2, 1);

        Assert.assertEquals(expected, distance1, 0.01);
        Assert.assertEquals(expected, distance2, 0.01);
    }

}
