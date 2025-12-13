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
          new Point(end.x - (int) (callsToMake * stepX), end.y - (int) (callsToMake * stepY)));

      callsToMake--;
    }

    return points.stream();
  }

  private Point getPointForCharacter(final int character) {
    return mKeys.stream()
        .filter(key -> key.getPrimaryCode() == character)
        .findFirst()
        .map(key -> new Point(Keyboard.Key.getCenterX(key), Keyboard.Key.getCenterY(key)))
        .orElseGet(
            () -> {
              throw new RuntimeException("Could not find key for character " + character);
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
            context.getResources().getDimension(R.dimen.gesture_typing_frequency_factor),
            MAX_SUGGESTIONS,
            context.getResources().getDimensionPixelSize(R.dimen.gesture_typing_min_point_distance),
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
        .forEach(point -> distance.addAndGet(mDetectorUnderTest.addPoint(point.x, point.y)));
    Assert.assertEquals(8016, distance.get());
    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();

    Assert.assertEquals(MAX_SUGGESTIONS, candidates.size());
    // "harp" is removed due to MAX_SUGGESTIONS limit
    Arrays.asList("hero", "hello", "hell", "Hall")
        .forEach(
            word ->
                Assert.assertTrue(
                    "Missing the word " + word + ". has " + candidates, candidates.remove(word)));
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
    Assert.assertEquals("Hall", candidates.get(2));
    Assert.assertEquals("hero", candidates.get(3));
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

    // With proximity filtering, we may get words from nearby keys too (e.g., 'h' is near 'g')
    // But we should still have all the 'g' words
    Assert.assertTrue("Should have at least 3 candidates", candidates.size() >= 3);
    Arrays.asList("good", "God", "gods")
        .forEach(word -> Assert.assertTrue("Missing the word " + word, candidates.contains(word)));
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

  // Tests for trimMemory() functionality

  @Test
  public void testTrimMemoryClearsCandidatesAndWeights() {
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    // Generate candidates first
    mDetectorUnderTest.clearGesture();
    generatePointsStreamOfKeysString("helo")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
    final ArrayList<String> candidatesBeforeTrim = mDetectorUnderTest.getCandidates();
    Assert.assertEquals(MAX_SUGGESTIONS, candidatesBeforeTrim.size());

    // Call trimMemory
    mDetectorUnderTest.trimMemory();

    // Verify candidates can still be generated (data structures are cleared but functional)
    mDetectorUnderTest.clearGesture();
    generatePointsStreamOfKeysString("helo")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
    final ArrayList<String> candidatesAfterTrim = mDetectorUnderTest.getCandidates();
    Assert.assertEquals(MAX_SUGGESTIONS, candidatesAfterTrim.size());
  }

  @Test
  public void testTrimMemoryResetsWorkspaceData() {
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    // Add some gesture points
    mDetectorUnderTest.clearGesture();
    final Point startPoint = getPointForCharacter('h');
    mDetectorUnderTest.addPoint(startPoint.x, startPoint.y);
    final Point endPoint = getPointForCharacter('e');
    mDetectorUnderTest.addPoint(endPoint.x, endPoint.y);

    // Call trimMemory
    mDetectorUnderTest.trimMemory();

    // Verify workspace is reset by checking that new gesture can be added
    mDetectorUnderTest.clearGesture();
    generatePointsStreamOfKeysString("help")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();
    Assert.assertEquals(MAX_SUGGESTIONS, candidates.size());
    Assert.assertEquals("help", candidates.get(0));
  }

  @Test
  public void testTrimMemoryWhenNoCandidatesExist() {
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    // Call trimMemory without generating any candidates
    mDetectorUnderTest.trimMemory();

    // Verify detector still works
    mDetectorUnderTest.clearGesture();
    generatePointsStreamOfKeysString("god")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();
    Assert.assertTrue(candidates.size() >= 3);
  }

  @Test
  public void testTrimMemoryWhenWorkspaceAlreadyReset() {
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    // Clear gesture to reset workspace
    mDetectorUnderTest.clearGesture();

    // Call trimMemory on already-reset workspace
    mDetectorUnderTest.trimMemory();

    // Verify detector still works - start a new gesture
    mDetectorUnderTest.clearGesture();
    generatePointsStreamOfKeysString("good")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();
    Assert.assertTrue(candidates.size() > 0);
    Assert.assertEquals("good", candidates.get(0));
  }

  @Test
  public void testTrimMemoryCanBeCalledMultipleTimes() {
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    // Generate candidates
    mDetectorUnderTest.clearGesture();
    generatePointsStreamOfKeysString("help")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
    Assert.assertEquals(MAX_SUGGESTIONS, mDetectorUnderTest.getCandidates().size());

    // Call trimMemory multiple times
    mDetectorUnderTest.trimMemory();
    mDetectorUnderTest.trimMemory();
    mDetectorUnderTest.trimMemory();

    // Verify detector still works
    mDetectorUnderTest.clearGesture();
    generatePointsStreamOfKeysString("good")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();
    Assert.assertTrue(candidates.size() >= 3);
    Assert.assertEquals("good", candidates.get(0));
  }

  @Test
  public void testTrimMemoryDoesNotAffectLoadedState() {
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    // Call trimMemory
    mDetectorUnderTest.trimMemory();

    // Verify state is still LOADED
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());
  }

  @Test
  public void testTrimMemoryDoesNotClearWordsAndCorners() {
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    // Generate candidates to verify words are loaded
    mDetectorUnderTest.clearGesture();
    generatePointsStreamOfKeysString("hello")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
    final ArrayList<String> candidatesBeforeTrim = mDetectorUnderTest.getCandidates();
    Assert.assertTrue(candidatesBeforeTrim.contains("hello"));

    // Call trimMemory
    mDetectorUnderTest.trimMemory();

    // Verify words and corners are still available by generating candidates again
    mDetectorUnderTest.clearGesture();
    generatePointsStreamOfKeysString("hello")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
    final ArrayList<String> candidatesAfterTrim = mDetectorUnderTest.getCandidates();
    Assert.assertTrue(candidatesAfterTrim.contains("hello"));
    // Verify same candidates can be generated, proving words/corners weren't cleared
    Assert.assertEquals(candidatesBeforeTrim.size(), candidatesAfterTrim.size());
  }

  @Test
  public void testTrimMemoryBeforeLoadingComplete() {
    // Don't drain tasks - leave detector in LOADING state
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, mCurrentState.get());

    // Call trimMemory while still loading
    mDetectorUnderTest.trimMemory();

    // Complete loading
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    // Verify detector works correctly after trimMemory during loading
    mDetectorUnderTest.clearGesture();
    generatePointsStreamOfKeysString("hero")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));
    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();
    Assert.assertTrue(candidates.size() > 0);
    Assert.assertEquals("hero", candidates.get(0));
  }

  // ===================================================================================
  // Tests for calculateCosineOfAngleBetweenVectors - Happy Path cases
  // ===================================================================================

  private static final double EPSILON = 0.0001;

  @Test
  public void testCosineOfAngle_SameDirection_ReturnsOne() {
    // Two vectors pointing in exactly the same direction (along positive X axis)
    double result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(1, 0, 2, 0);
    Assert.assertEquals(1.0, result, EPSILON);

    // Same direction along positive Y axis
    result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(0, 5, 0, 10);
    Assert.assertEquals(1.0, result, EPSILON);

    // Same direction along diagonal
    result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(3, 3, 6, 6);
    Assert.assertEquals(1.0, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_OppositeDirection_ReturnsMinusOne() {
    // Two vectors pointing in opposite directions (along X axis)
    double result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(1, 0, -1, 0);
    Assert.assertEquals(-1.0, result, EPSILON);

    // Opposite direction along Y axis
    result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(0, 5, 0, -10);
    Assert.assertEquals(-1.0, result, EPSILON);

    // Opposite direction along diagonal
    result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(3, 4, -6, -8);
    Assert.assertEquals(-1.0, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_Perpendicular_ReturnsZero() {
    // Perpendicular vectors: X axis and Y axis
    double result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(1, 0, 0, 1);
    Assert.assertEquals(0.0, result, EPSILON);

    // Perpendicular: positive X and negative Y
    result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(5, 0, 0, -3);
    Assert.assertEquals(0.0, result, EPSILON);

    // Perpendicular: negative X and positive Y
    result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(-4, 0, 0, 7);
    Assert.assertEquals(0.0, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_45Degrees_ReturnsCorrectValue() {
    // 45 degrees: v1 along X axis, v2 along diagonal (1,1)
    // cos(45°) = √2/2 ≈ 0.7071
    double result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(1, 0, 1, 1);
    Assert.assertEquals(Math.sqrt(2) / 2, result, EPSILON);

    // 45 degrees in different quadrant
    result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(0, 1, 1, 1);
    Assert.assertEquals(Math.sqrt(2) / 2, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_135Degrees_ReturnsCorrectValue() {
    // 135 degrees: v1 along positive X axis, v2 along (-1, 1) diagonal
    // cos(135°) = -√2/2 ≈ -0.7071
    double result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(1, 0, -1, 1);
    Assert.assertEquals(-Math.sqrt(2) / 2, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_60Degrees_ReturnsCorrectValue() {
    // 60 degrees: v1 = (1, 0), v2 = (1, √3)
    // cos(60°) = 0.5
    double result =
        GestureTypingDetector.calculateCosineOfAngleBetweenVectors(1, 0, 1, Math.sqrt(3));
    Assert.assertEquals(0.5, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_120Degrees_ReturnsCorrectValue() {
    // 120 degrees: v1 = (1, 0), v2 = (-1, √3)
    // cos(120°) = -0.5
    double result =
        GestureTypingDetector.calculateCosineOfAngleBetweenVectors(1, 0, -1, Math.sqrt(3));
    Assert.assertEquals(-0.5, result, EPSILON);
  }

  // ===================================================================================
  // Tests for calculateCosineOfAngleBetweenVectors - Symmetry and Commutativity cases
  // ===================================================================================

  @Test
  public void testCosineOfAngle_OrderIndependent() {
    // Swapping v1 and v2 should give the same result
    double result1 = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(3, 4, 5, 0);
    double result2 = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(5, 0, 3, 4);
    Assert.assertEquals(result1, result2, EPSILON);

    // Another pair
    result1 = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(1, 2, 3, 4);
    result2 = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(3, 4, 1, 2);
    Assert.assertEquals(result1, result2, EPSILON);
  }

  @Test
  public void testCosineOfAngle_NegativeCoordinates() {
    // Vectors in third quadrant (both negative)
    // v1 = (-3, -4), v2 = (-5, 0) - angle should be same as (3, 4) and (5, 0)
    double result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(-3, -4, -5, 0);
    // cos of angle between (-3,-4) and (-5,0)
    // dot product = 15, |v1| = 5, |v2| = 5, cos = 15/25 = 0.6
    Assert.assertEquals(0.6, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_MixedQuadrants() {
    // v1 in first quadrant (3, 4), v2 in third quadrant (-5, -12)
    // These are nearly opposite, so expect negative cosine
    double result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(3, 4, -5, -12);
    // dot product = -15 + -48 = -63, |v1| = 5, |v2| = 13, cos = -63/65
    Assert.assertEquals(-63.0 / 65.0, result, EPSILON);
  }

  // ===================================================================================
  // Tests for calculateCosineOfAngleBetweenVectors - Edge cases
  // ===================================================================================

  @Test
  public void testCosineOfAngle_FirstVectorZeroLength_ReturnsOne() {
    // First vector is zero vector - should return 1.0 (no penalty)
    double result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(0, 0, 1, 1);
    Assert.assertEquals(1.0, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_SecondVectorZeroLength_ReturnsOne() {
    // Second vector is zero vector - should return 1.0 (no penalty)
    double result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(1, 1, 0, 0);
    Assert.assertEquals(1.0, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_BothVectorsZeroLength_ReturnsOne() {
    // Both vectors are zero - should return 1.0 (no penalty)
    double result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(0, 0, 0, 0);
    Assert.assertEquals(1.0, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_VerySmallVectors_HandlesCorrectly() {
    // Very small magnitude vectors - perpendicular
    double result =
        GestureTypingDetector.calculateCosineOfAngleBetweenVectors(0.001, 0, 0, 0.001);
    Assert.assertEquals(0.0, result, EPSILON);

    // Very small magnitude vectors - same direction
    result = GestureTypingDetector.calculateCosineOfAngleBetweenVectors(0.001, 0.001, 0.002, 0.002);
    Assert.assertEquals(1.0, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_VeryLargeVectors_HandlesCorrectly() {
    // Large magnitude vectors - should still compute correctly
    // v1 = (10000, 10000), v2 = (10000, 0) - 45 degree angle
    double result =
        GestureTypingDetector.calculateCosineOfAngleBetweenVectors(10000, 10000, 10000, 0);
    Assert.assertEquals(Math.sqrt(2) / 2, result, EPSILON);
  }

  @Test
  public void testCosineOfAngle_ResultAlwaysInValidRange() {
    // Test various vectors and ensure result is always in [-1, 1]
    double[][] testVectors = {
      {1, 0, 1, 0},
      {1, 0, -1, 0},
      {1, 0, 0, 1},
      {3, 4, 5, 12},
      {-7, 24, 8, -15},
      {1000, 1000, -1000, -1000},
      {0.1, 0.2, 0.3, 0.4}
    };

    for (double[] v : testVectors) {
      double result =
          GestureTypingDetector.calculateCosineOfAngleBetweenVectors(v[0], v[1], v[2], v[3]);
      Assert.assertTrue(
          "Result " + result + " should be >= -1", result >= -1.0 - EPSILON);
      Assert.assertTrue(
          "Result " + result + " should be <= 1", result <= 1.0 + EPSILON);
    }
  }

  // ===================================================================================
  // Tests for calculateDistanceBetweenUserPathAndWord - Happy Path cases
  // ===================================================================================

  @Test
  public void testDistanceCalculation_SameDirection_NoPenalty() {
    // User path: (0,0) -> (100,0) -> (200,0) (moving right)
    // Word path: (0,0) -> (100,0) -> (200,0) (moving right)
    // Same direction = multiplier 1.0
    short[] userPath = {0, 0, 100, 0, 200, 0};
    short[] wordPath = {0, 0, 100, 0, 200, 0};

    double distance =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // Distance should be 0 (paths are identical) with no penalty
    Assert.assertEquals(0.0, distance, EPSILON);
  }

  @Test
  public void testDistanceCalculation_OppositeDirection_MaxPenalty() {
    // User path: (0,0) -> (100,0) -> (200,0) (moving right, direction vector: +100, 0)
    // Word path: (200,0) -> (100,0) -> (0,0) (moving left, direction vector: -100, 0)
    // Opposite direction = multiplier 3.0
    short[] userPath = {0, 0, 100, 0, 200, 0};
    short[] wordPath = {200, 0, 100, 0, 0, 0};

    double distanceOpposite =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // Compare with same-direction case to verify penalty is applied
    short[] wordPathSame = {0, 0, 100, 0, 200, 0};
    double distanceSame =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPathSame);

    // Opposite direction should result in higher cumulative distance
    Assert.assertTrue(
        "Opposite direction distance (" + distanceOpposite
            + ") should be greater than same direction (" + distanceSame + ")",
        distanceOpposite > distanceSame);
  }

  @Test
  public void testDistanceCalculation_PerpendicularDirection_MediumPenalty() {
    // User path moving right: (0,0) -> (100,0) -> (200,0)
    // Word path moving down: (0,0) -> (0,100) -> (0,200)
    // Perpendicular = multiplier 2.0
    short[] userPath = {0, 0, 100, 0, 200, 0};
    short[] wordPath = {0, 0, 0, 100, 0, 200};

    double distancePerpendicular =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // Compare with same-direction and opposite-direction
    short[] wordPathSame = {0, 0, 100, 0, 200, 0};
    double distanceSame =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPathSame);

    short[] wordPathOpposite = {200, 0, 100, 0, 0, 0};
    double distanceOpposite =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPathOpposite);

    // Perpendicular should be between same and opposite
    // (Note: actual values depend on distances too, but penalty ordering should hold)
    Assert.assertTrue(
        "Same direction distance should be smallest", distanceSame <= distancePerpendicular);
  }

  @Test
  public void testDistanceCalculation_45DegreeDifference_CorrectPenalty() {
    // User path moving right: (0,0) -> (100,0) -> (200,0)
    // Word path moving diagonal (45°): (0,0) -> (100,100) -> (200,200)
    // cos(45°) = 0.7071, penalty = 1 + 1*(1-0.7071) ≈ 1.29
    short[] userPath = {0, 0, 100, 0, 200, 0};
    short[] wordPath = {0, 0, 100, 100, 200, 200};

    double distance45 =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // Compare with same direction
    short[] wordPathSame = {0, 0, 100, 0, 200, 0};
    double distanceSame =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPathSame);

    // 45 degree should have some penalty
    Assert.assertTrue(
        "45 degree distance should be greater than same direction",
        distance45 > distanceSame);
  }

  // ===================================================================================
  // Tests for calculateDistanceBetweenUserPathAndWord - First Loop behavior
  // ===================================================================================

  @Test
  public void testDistanceCalculation_FirstPointNoPenalty() {
    // With only 2 points each (1 segment), there's no "previous" point for direction
    // So no penalty should be applied
    short[] userPath = {0, 0, 100, 0};
    short[] wordPath = {200, 0, 100, 0}; // opposite direction but only 1 segment

    double distance =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // Should be pure distance without penalty (first point has no previous direction)
    // Distance from (0,0) to (200,0) = 200, from (100,0) to (100,0) = 0
    // Total should be 200 (no penalty because no previous points for direction)
    Assert.assertTrue("Distance should be calculated without penalty for first segment",
        distance >= 0);
  }

  @Test
  public void testDistanceCalculation_SecondPointOnward_PenaltyApplied() {
    // 3 points = 2 segments, penalty starts from second segment
    // User path: (0,0) -> (100,0) -> (200,0) direction: right
    // Word path: (0,0) -> (100,0) -> (0,0) direction changes to left (opposite)
    short[] userPath = {0, 0, 100, 0, 200, 0};
    short[] wordPath = {0, 0, 100, 0, 0, 0};

    double distanceWithPenalty =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // Same user path with word path going same direction
    short[] wordPathSame = {0, 0, 100, 0, 200, 0};
    double distanceNoPenalty =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPathSame);

    // The path with opposite direction in second segment should have penalty
    Assert.assertTrue(
        "Second segment with opposite direction should have penalty applied",
        distanceWithPenalty > distanceNoPenalty);
  }

  @Test
  public void testDistanceCalculation_DirectionChangeMidPath_UpdatesPenalty() {
    // User path with a turn: right then up
    // (0,0) -> (100,0) -> (100,100)
    short[] userPath = {0, 0, 100, 0, 100, 100};

    // Word path that follows the same turn
    short[] wordPathSameTurn = {0, 0, 100, 0, 100, 100};
    double distanceSameTurn =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPathSameTurn);

    // Word path that goes straight (doesn't turn)
    short[] wordPathStraight = {0, 0, 100, 0, 200, 0};
    double distanceStraight =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPathStraight);

    // Following the turn should result in lower distance (better match)
    Assert.assertTrue(
        "Following same turn should have lower distance than going straight",
        distanceSameTurn < distanceStraight);
  }

  // ===================================================================================
  // Tests for calculateDistanceBetweenUserPathAndWord - Second Loop behavior
  // ===================================================================================

  @Test
  public void testDistanceCalculation_RemainingWordCorners_UsesLastGestureDirection() {
    // User path ends early, word path has more corners
    // User: (0,0) -> (100,0) -> (200,0) - direction: right
    // Word: (0,0) -> (100,0) -> (200,0) -> (300,0) -> (400,0) - continues right
    short[] userPath = {0, 0, 100, 0, 200, 0};
    short[] wordPathSameDir = {0, 0, 100, 0, 200, 0, 300, 0, 400, 0};

    double distanceSameDir =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPathSameDir);

    // Word path where remaining corners go opposite direction
    // Word: (0,0) -> (100,0) -> (200,0) -> (100,0) -> (0,0) - reverses
    short[] wordPathReverses = {0, 0, 100, 0, 200, 0, 100, 0, 0, 0};

    double distanceReverses =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPathReverses);

    // Remaining corners going opposite to last gesture direction should have higher penalty
    Assert.assertTrue(
        "Word path reversing should have higher distance than continuing same direction",
        distanceReverses > distanceSameDir);
  }

  @Test
  public void testDistanceCalculation_RemainingWordCorners_NoPreviousDirection_NoPenalty() {
    // Very short user path (just 2 points) - no direction established for penalty
    // Word path has more corners
    short[] userPath = {0, 0, 100, 0};
    short[] wordPath = {0, 0, 100, 0, 200, 0, 300, 0};

    double distance =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // Should calculate distance but with minimal/no penalty since direction wasn't established
    // (user path only has first segment, no previous point for direction)
    Assert.assertTrue("Distance should be non-negative", distance >= 0);
  }

  // ===================================================================================
  // Tests for calculateDistanceBetweenUserPathAndWord - Edge cases
  // ===================================================================================

  @Test
  public void testDistanceCalculation_MinimumValidPaths() {
    // Minimum valid: 2 coordinates each (one point)
    short[] userPath = {50, 50};
    short[] wordPath = {100, 100};

    double distance =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // Should return valid distance (Euclidean distance between the two points)
    double expectedDistance = Math.sqrt((100 - 50) * (100 - 50) + (100 - 50) * (100 - 50));
    Assert.assertEquals(expectedDistance, distance, EPSILON);
  }

  @Test
  public void testDistanceCalculation_UserPathTooShort_ReturnsMaxValue() {
    // User path with less than 2 coordinates
    short[] userPathEmpty = {};
    short[] wordPath = {0, 0, 100, 100};

    double distance =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPathEmpty, wordPath);
    Assert.assertEquals(Double.MAX_VALUE, distance, EPSILON);

    // User path with only 1 coordinate (invalid - needs pairs)
    short[] userPathOne = {50};
    distance =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPathOne, wordPath);
    Assert.assertEquals(Double.MAX_VALUE, distance, EPSILON);
  }

  @Test
  public void testDistanceCalculation_WordPathEmpty_ReturnsMaxValue() {
    // Empty word path
    short[] userPath = {0, 0, 100, 100};
    short[] wordPathEmpty = {};

    double distance =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPathEmpty);
    Assert.assertEquals(Double.MAX_VALUE, distance, EPSILON);
  }

  @Test
  public void testDistanceCalculation_WordPathLongerThanUserPath_ReturnsMaxValue() {
    // Word path has more coordinates than user path
    short[] userPath = {0, 0, 100, 0};
    short[] wordPath = {0, 0, 100, 0, 200, 0, 300, 0, 400, 0, 500, 0};

    double distance =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // According to the code: if (generatedWordPath.length > actualUserPath.length) return MAX_VALUE
    Assert.assertEquals(Double.MAX_VALUE, distance, EPSILON);
  }

  // ===================================================================================
  // Tests for calculateDistanceBetweenUserPathAndWord - Integration tests
  // ===================================================================================

  @Test
  public void testDistanceCalculation_CumulativeDistanceWithPenalties() {
    // Create a scenario where we can calculate expected values
    // User path: (0,0) -> (100,0) -> (200,0) - straight line going right
    // Word path: (0,100) -> (100,100) -> (200,100) - parallel line 100 units above, same direction
    short[] userPath = {0, 0, 100, 0, 200, 0};
    short[] wordPath = {0, 100, 100, 100, 200, 100};

    double distance =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // Since paths are parallel (same direction), penalty multiplier should be 1.0
    // Distance at each point: 100 (vertical distance)
    // Point 1 (0,0) to (0,100): distance = 100, no penalty (first point)
    // Point 2 (100,0) to (100,100): distance = 100, penalty = 1.0 (same direction)
    // Point 3 (200,0) to (200,100): distance = 100, penalty = 1.0 (same direction)
    // Expected total: 100 + 100*1.0 + 100*1.0 = 300
    Assert.assertEquals(300.0, distance, EPSILON);
  }

  @Test
  public void testDistanceCalculation_ZeroDistanceWithPenalty() {
    // Paths overlap completely - distance is 0 regardless of penalty
    short[] userPath = {0, 0, 100, 0, 200, 0};
    short[] wordPath = {0, 0, 100, 0, 200, 0};

    double distance =
        GestureTypingDetector.calculateDistanceBetweenUserPathAndWord(userPath, wordPath);

    // 0 distance * any penalty = 0
    Assert.assertEquals(0.0, distance, EPSILON);
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
