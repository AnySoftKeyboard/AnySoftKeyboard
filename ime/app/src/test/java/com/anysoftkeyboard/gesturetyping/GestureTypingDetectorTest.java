package com.anysoftkeyboard.gesturetyping;

import static com.anysoftkeyboard.keyboards.ExternalAnyKeyboardTest.SIMPLE_KeyboardDimens;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_NORMAL;

import android.content.Context;
import android.graphics.Point;
import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GestureTypingDetectorTest {
    private static final int MAX_SUGGESTIONS = 4;
    private List<Keyboard.Key> mKeys;
    private GestureTypingDetector mDetectorUnderTest;
    private AtomicReference<GestureTypingDetector.LoadingState> mCurrentState;
    private Disposable mSubscribeState;

    private static Stream<Point> generateTraceBetweenPoints(final Point start, final Point end) {
        int callsToMake = 16;
        final float stepX = (end.x - start.x) / (float) callsToMake;
        final float stepY = (end.y - start.y) / (float) callsToMake;

        List<Point> points = new ArrayList<>(1 + callsToMake);
        while (callsToMake >= 0) {
            points.add(
                    new Point(
                            end.x - (int) (callsToMake * stepX),
                            end.y - (int) (callsToMake * stepY)));

            callsToMake--;
        }

        return points.stream();
    }

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
        final Context context = ApplicationProvider.getApplicationContext();
        final AnyKeyboard keyboard =
                AnyApplication.getKeyboardFactory(context)
                        .getAddOnById(context.getString(R.string.main_english_keyboard_id))
                        .createKeyboard(KEYBOARD_ROW_MODE_NORMAL);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens);
        TestRxSchedulers.drainAllTasks();
        mKeys = keyboard.getKeys();

        mDetectorUnderTest =
                new GestureTypingDetector(
                        context.getResources()
                                .getDimension(R.dimen.gesture_typing_frequency_factor),
                        MAX_SUGGESTIONS,
                        context.getResources()
                                .getDimensionPixelSize(R.dimen.gesture_typing_min_point_distance),
                        mKeys);

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

        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());
    }

    @After
    public void tearDown() {
        mSubscribeState.dispose();
    }

    @Test
    public void testHappyPath() {
        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

        mDetectorUnderTest.clearGesture();

        AtomicInteger distance = new AtomicInteger();
        generatePointsStreamOfKeysString("helo")
                .forEach(
                        point -> distance.addAndGet(mDetectorUnderTest.addPoint(point.x, point.y)));
        Assert.assertEquals(8016, distance.get());
        final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();

        Assert.assertEquals(MAX_SUGGESTIONS, candidates.size());
        // "harp" is removed due to MAX_SUGGESTIONS limit
        Arrays.asList("hero", "hello", "hell", "Hall")
                .forEach(
                        word ->
                                Assert.assertTrue(
                                        "Missing the word " + word + ". has " + candidates,
                                        candidates.remove(word)));
        // ensuring we asserted all words
        Assert.assertTrue("Still has " + candidates, candidates.isEmpty());
    }

    @Test
    public void testTakesWordFrequencyIntoAccount() {
        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

        mDetectorUnderTest.clearGesture();

        generatePointsStreamOfKeysString("help")
                .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
        final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();

        Assert.assertEquals(MAX_SUGGESTIONS, candidates.size());
        Assert.assertEquals("help", candidates.get(0));
        Assert.assertEquals("hell", candidates.get(1));
        Assert.assertEquals("hero", candidates.get(2));
        Assert.assertEquals("Hall", candidates.get(3));
    }

    @Test
    public void testFilterOutWordsThatDoNotStartsWithFirstPress() {
        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

        mDetectorUnderTest.clearGesture();

        generatePointsStreamOfKeysString("to")
                .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
        final ArrayList<String> candidates = new ArrayList<>(mDetectorUnderTest.getCandidates());

        Assert.assertEquals(0, candidates.size());

        candidates.clear();
        mDetectorUnderTest.clearGesture();
        generatePointsStreamOfKeysString("god")
                .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
        candidates.addAll(mDetectorUnderTest.getCandidates());

        Assert.assertEquals(3, candidates.size());
        Arrays.asList("good", "God", "gods")
                .forEach(
                        word ->
                                Assert.assertTrue(
                                        "Missing the word " + word, candidates.remove(word)));
        Assert.assertTrue("Still has " + candidates.toString(), candidates.isEmpty());
    }

    @Test
    public void testCalculatesCornersInBackground() {
        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

        mDetectorUnderTest.destroy();
        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
    }

    @Test
    @Ignore("I'm not sure how this is two dictionaries")
    public void testCalculatesCornersInBackgroundWithTwoDictionaries() {
        TestRxSchedulers.backgroundRunOneJob();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());
        TestRxSchedulers.backgroundRunOneJob();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());
        mDetectorUnderTest.destroy();
        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
    }

    @Test
    @Ignore("I'm not sure how this is two dictionaries")
    public void testCalculatesCornersInBackgroundWithTwoDictionariesButDisposed() {
        TestRxSchedulers.backgroundRunOneJob();
        mSubscribeState.dispose();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());
        TestRxSchedulers.backgroundRunOneJob();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());
        mDetectorUnderTest.destroy();
        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());
    }

    @Test
    public void testCalculatesCornersInBackgroundWithTwoDictionariesButDestroyed() {
        TestRxSchedulers.drainAllTasks();
        mDetectorUnderTest.destroy();
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
        TestRxSchedulers.drainAllTasks();
        mSubscribeState.dispose();

        TestRxSchedulers.drainAllTasks();
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, mCurrentState.get());
    }

    @Test
    public void testHasEnoughCurvatureStraight() {
        final int[] Xs = new int[3];
        final int[] Ys = new int[3];

        Xs[0] = -100;
        Ys[0] = 0;

        Xs[1] = 0;
        Ys[1] = 0;

        Xs[2] = 100;
        Ys[2] = 0;
        Assert.assertFalse(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));

        Xs[0] = 0;
        Ys[0] = -100;

        Xs[1] = 0;
        Ys[1] = 0;

        Xs[2] = 0;
        Ys[2] = 100;
        Assert.assertFalse(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));

        Xs[0] = 50;
        Ys[0] = -50;

        Xs[1] = 0;
        Ys[1] = 0;

        Xs[2] = -50;
        Ys[2] = 50;
        Assert.assertFalse(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));

        Xs[0] = -50;
        Ys[0] = 50;

        Xs[1] = 0;
        Ys[1] = 0;

        Xs[2] = 50;
        Ys[2] = -50;
        Assert.assertFalse(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));

        Xs[0] = -41;
        Ys[0] = 50;

        Xs[1] = 9;
        Ys[1] = 0;

        Xs[2] = 59;
        Ys[2] = -50;
        Assert.assertFalse(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));
    }

    @Test
    public void testHasEnoughCurvature90Degrees() {
        final int[] Xs = new int[3];
        final int[] Ys = new int[3];

        Xs[0] = -50;
        Ys[0] = 0;

        Xs[1] = 0;
        Ys[1] = 0;

        Xs[2] = 0;
        Ys[2] = -50;
        Assert.assertTrue(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));

        Xs[0] = -50;
        Ys[0] = 0;

        Xs[1] = 0;
        Ys[1] = 0;

        Xs[2] = 0;
        Ys[2] = 50;
        Assert.assertTrue(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));

        Xs[0] = 0;
        Ys[0] = -50;

        Xs[1] = 0;
        Ys[1] = 0;

        Xs[2] = 50;
        Ys[2] = 0;
        Assert.assertTrue(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));
    }

    @Test
    public void testHasEnoughCurvature180Degrees() {
        final int[] Xs = new int[3];
        final int[] Ys = new int[3];

        Xs[0] = 0;
        Ys[0] = -50;

        Xs[1] = 0;
        Ys[1] = 0;

        Xs[2] = 0;
        Ys[2] = -50;
        Assert.assertTrue(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));

        Xs[0] = -50;
        Ys[0] = 0;

        Xs[1] = 0;
        Ys[1] = 0;

        Xs[2] = -50;
        Ys[2] = 0;
        Assert.assertTrue(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));
    }

    @Test
    public void testHasEnoughCurvature15Degrees() {
        final int[] Xs = new int[3];
        final int[] Ys = new int[3];

        // https://www.triangle-calculator.com/?what=&q=A%3D165%2C+b%3D100%2C+c%3D100&submit=Solve
        // A[100; 0] B[0; 0] C[196.593; 25.882]

        Xs[0] = 0;
        Ys[0] = 0;

        Xs[1] = 100;
        Ys[1] = 0;

        Xs[2] = 196;
        Ys[2] = 26;
        Assert.assertTrue(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));

        Xs[0] = 0;
        Ys[0] = 0;

        Xs[1] = 100;
        Ys[1] = 0;

        Xs[2] = 196;
        Ys[2] = -26;
        Assert.assertTrue(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));
    }

    @Test
    public void testHasEnoughCurvature9Degrees() {
        final int[] Xs = new int[3];
        final int[] Ys = new int[3];

        // https://www.triangle-calculator.com/?what=&q=A%3D171%2C+b%3D100%2C+c%3D100&submit=Solve
        // A[100; 0] B[0; 0] C[198.769; 15.643]

        Xs[0] = 0;
        Ys[0] = 0;

        Xs[1] = 100;
        Ys[1] = 0;

        Xs[2] = 198;
        Ys[2] = 16;
        Assert.assertFalse(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));

        Xs[0] = 0;
        Ys[0] = 0;

        Xs[1] = 100;
        Ys[1] = 0;

        Xs[2] = 198;
        Ys[2] = -16;
        Assert.assertFalse(GestureTypingDetector.hasEnoughCurvature(Xs, Ys, 1));
    }

    private Stream<Point> generatePointsStreamOfKeysString(String path) {
        return path.chars()
                .boxed()
                .map(this::getPointForCharacter)
                .map(
                        new Function<Point, Pair<Point, Point>>() {
                            private Point mPrevious = new Point();

                            @Override
                            public Pair<Point, Point> apply(Point point) {
                                final Point previous = mPrevious;
                                mPrevious = point;

                                return new Pair<>(previous, mPrevious);
                            }
                        })
                .skip(1 /*the first one is just wrong*/)
                .map(pair -> generateTraceBetweenPoints(pair.first, pair.second))
                .flatMap(pointStream -> pointStream);
    }
}
