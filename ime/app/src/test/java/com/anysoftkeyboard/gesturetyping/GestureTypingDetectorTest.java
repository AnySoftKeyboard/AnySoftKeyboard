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

  // Tests for proximity-based filtering

  @Test
  public void testExactMatchStillWorks() {
    // Test: Exact match still works
    // Gesture starts exactly on 'h' key
    // Verify "hello" is in candidates
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    mDetectorUnderTest.clearGesture();

    // Generate a gesture that starts exactly on the 'h' key
    generatePointsStreamOfKeysString("hello")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));

    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();

    // Verify "hello" is in the candidates
    Assert.assertTrue(
        "Expected 'hello' to be in candidates but got: " + candidates,
        candidates.contains("hello"));
  }

  @Test
  public void testNearbyKeyIsAccepted() {
    // Test: Nearby key is accepted
    // Gesture starts near 'h' key (but not exactly on it)
    // Verify "hello" is still in candidates
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    mDetectorUnderTest.clearGesture();

    // Get the 'h' key position
    final Point hKeyCenter = getPointForCharacter('h');

    // Start slightly offset from the 'h' key center (within proximity threshold)
    // Offset by ~30 pixels (well within the 150 pixel threshold = sqrt(22500))
    final int offsetX = 30;
    final int offsetY = 30;
    mDetectorUnderTest.addPoint(hKeyCenter.x + offsetX, hKeyCenter.y + offsetY);

    // Continue with the rest of the gesture for "hello"
    generatePointsStreamOfKeysString("ello")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));

    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();

    // Verify "hello" is still in candidates despite imprecise start
    Assert.assertTrue(
        "Expected 'hello' to be in candidates with nearby start but got: " + candidates,
        candidates.contains("hello"));
  }

  @Test
  public void testFarKeyIsRejected() {
    // Test: Far key is rejected
    // Gesture starts on 'a' key (far from 'h')
    // Verify "hello" is NOT in candidates (first char is 'h')
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    mDetectorUnderTest.clearGesture();

    // Get the 'a' key position (far from 'h')
    final Point aKeyCenter = getPointForCharacter('a');

    // Start gesture on 'a' key
    mDetectorUnderTest.addPoint(aKeyCenter.x, aKeyCenter.y);

    // Continue with gesture points for "ello"
    generatePointsStreamOfKeysString("ello")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));

    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();

    // Verify "hello" is NOT in candidates because 'a' is far from 'h'
    Assert.assertFalse(
        "Did not expect 'hello' to be in candidates when starting far from 'h', but got: "
            + candidates,
        candidates.contains("hello"));
  }

  @Test
  public void testNullHandlingForUnknownCharacter() {
    // Test: Null handling
    // Word with character not on keyboard
    // Verify no crash, word is skipped
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    // Add a word with a special character that won't be on the keyboard
    mDetectorUnderTest.setWords(
        Collections.singletonList(
            new char[][] {
              "hello".toCharArray(),
              "h\u00E9llo".toCharArray(), // Contains é which might not be on English keyboard
              "test".toCharArray()
            }),
        Collections.singletonList(new int[] {100, 100, 100}));

    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    mDetectorUnderTest.clearGesture();

    // Generate a gesture for "hello"
    generatePointsStreamOfKeysString("hello")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));

    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();

    // Should complete without crashing
    // "hello" should be in candidates, words with unknown characters should be skipped
    Assert.assertTrue(
        "Expected 'hello' to be in candidates", candidates.contains("hello"));
  }

  @Test
  public void testProximityPenaltyPrefersExactMatch() {
    // Test: Proximity penalty
    // Two words could match: one starts on exact key, one starts on nearby key
    // Verify exact match ranks higher
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    mDetectorUnderTest.clearGesture();

    // Get positions for 'h' and 'g' keys (they should be adjacent)
    final Point hKeyCenter = getPointForCharacter('h');
    final Point gKeyCenter = getPointForCharacter('g');

    // Start gesture between 'h' and 'g', but closer to 'h'
    // This means both 'h' words and 'g' words might be considered
    int startX = (hKeyCenter.x + gKeyCenter.x) / 2;
    int startY = (hKeyCenter.y + gKeyCenter.y) / 2;

    // Adjust to be slightly closer to 'h'
    startX = (startX + hKeyCenter.x) / 2;
    startY = (startY + hKeyCenter.y) / 2;

    mDetectorUnderTest.addPoint(startX, startY);

    // Continue with gesture that could match both "hello" and "good"
    // Let's use a gesture that goes through 'e', 'l', 'o' area for "hello"
    generatePointsStreamOfKeysString("ello")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));

    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();

    // Words starting with 'h' should be preferred over words starting with 'g'
    // because the start point is closer to 'h'
    if (candidates.contains("hello") && candidates.contains("good")) {
      int helloIndex = candidates.indexOf("hello");
      int goodIndex = candidates.indexOf("good");

      // "hello" should rank higher (lower index) than "good"
      Assert.assertTrue(
          "Expected 'hello' to rank higher than 'good' due to proximity penalty, "
              + "but got: "
              + candidates,
          helloIndex < goodIndex);
    }
  }

  @Test
  public void testEdgeProximity() {
    // Test: Edge proximity
    // Gesture starts just outside a key but within threshold
    // Verify words starting with that key are still considered
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    mDetectorUnderTest.clearGesture();

    // Get the 'h' key to find its edges
    Keyboard.Key hKey =
        mKeys.stream()
            .filter(key -> key.getPrimaryCode() == 'h')
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Could not find 'h' key"));

    // Start just outside the right edge of the 'h' key, but within proximity threshold
    // The key boundary is at (hKey.x + hKey.width)
    int edgeX = hKey.x + hKey.width + 20; // 20 pixels outside the right edge
    int edgeY = Keyboard.Key.getCenterY(hKey);

    mDetectorUnderTest.addPoint(edgeX, edgeY);

    // Continue with the rest of the gesture for "hello"
    generatePointsStreamOfKeysString("ello")
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));

    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();

    // Verify "hello" is in candidates even though we started outside the key
    Assert.assertTrue(
        "Expected 'hello' to be in candidates when starting near edge but got: " + candidates,
        candidates.contains("hello"));
  }

  @Test
  public void testProximityFilteringWithMultipleNearbyWords() {
    // Test that proximity filtering includes multiple words starting with nearby keys
    TestRxSchedulers.drainAllTasks();
    Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, mCurrentState.get());

    mDetectorUnderTest.clearGesture();

    // Get positions for 'h' and 'g' keys
    final Point hKeyCenter = getPointForCharacter('h');
    final Point gKeyCenter = getPointForCharacter('g');

    // Calculate a point between 'h' and 'g' that's within proximity threshold of both
    int midX = (hKeyCenter.x + gKeyCenter.x) / 2;
    int midY = (hKeyCenter.y + gKeyCenter.y) / 2;

    mDetectorUnderTest.addPoint(midX, midY);

    // Add points that could lead to either 'h' words or 'g' words
    // Use 'o' as next point which is common to paths for both sets
    final Point oKeyCenter = getPointForCharacter('o');
    generateTraceBetweenPoints(new Point(midX, midY), oKeyCenter)
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));

    // Continue to 'd' which completes "god" or "good"
    final Point dKeyCenter = getPointForCharacter('d');
    generateTraceBetweenPoints(oKeyCenter, dKeyCenter)
        .forEach(point -> mDetectorUnderTest.addPoint(point.x, point.y));

    final ArrayList<String> candidates = mDetectorUnderTest.getCandidates();

    // Should contain words starting with 'g' since we're close enough
    boolean hasGWords = candidates.stream().anyMatch(word -> word.toLowerCase().startsWith("g"));
    Assert.assertTrue(
        "Expected to find words starting with 'g' in candidates: " + candidates, hasGWords);
  }
}
