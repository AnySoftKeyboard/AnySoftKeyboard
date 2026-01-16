package com.anysoftkeyboard.gesturetyping;

import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.menny.android.anysoftkeyboard.BuildConfig;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.subjects.ReplaySubject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestureTypingDetector {
  private static final String TAG = "ASKGestureTypingDetector";

  private static final double CURVATURE_THRESHOLD = Math.toRadians(170);
  // How many points away from the current point do we use when calculating hasEnoughCurvature?
  private static final int CURVATURE_NEIGHBORHOOD = 1;
  private static final double MINIMUM_DISTANCE_FILTER = 1000000;

  /**
   * Factor controlling how much direction mismatch penalizes distance in path comparison. The
   * penalty multiplier is calculated as: 1 + DIRECTION_PENALTY_FACTOR * (1 - cos(θ)) where θ is the
   * angle between gesture direction and generated word direction.
   *
   * <p>At factor 1.0: same direction = 1×, perpendicular = 2×, opposite = 3× At factor 0.5: same
   * direction = 1×, perpendicular = 1.5×, opposite = 2×
   */
  private static final double DIRECTION_PENALTY_FACTOR = 1.0;

  /**
   * Penalty factor for words that start near but not on the exact starting key. Applied to the
   * squared distance, creating a quadratic penalty that is lenient for small offsets (adjacent key
   * touches) but strongly penalizes distant starts. Lower value = less penalty, higher value = more
   * penalty.
   *
   * <p>Value of ~0.000667 (approximately 1/1500) gives: - Adjacent key edge (~20px, 400 squared):
   * ~0.3 penalty units - Adjacent key center (~100px, 10000 squared): ~6.7 penalty units - Maximum
   * threshold (~150px, 22500 squared): ~15 penalty units
   */
  private static final double PROXIMITY_PENALTY_FACTOR = 0.000667;

  // How far away do two points of the gesture have to be (distance squared)?
  private final int mMinPointDistanceSquared;

  /**
   * Maximum squared distance from gesture start point to accept a word's starting key. This allows
   * for imprecise gesture starts while filtering obviously wrong candidates. Value is approximately
   * 1.5 key widths squared (assuming ~100 pixel keys).
   */
  private final int mStartKeyProximityThresholdSquared;

  private final ArrayList<String> mCandidates;
  private final double mFrequencyFactor;

  private final ArrayList<Double> mCandidateWeights;

  private final WorkspaceData mWorkspaceData = new WorkspaceData();

  @NonNull private final Iterable<Keyboard.Key> mKeys;

  @NonNull private SparseArray<Keyboard.Key> mKeysByCharacter = new SparseArray<>();

  @NonNull private List<char[][]> mWords = Collections.emptyList();
  @NonNull private List<int[]> mWordFrequencies = Collections.emptyList();

  @NonNull private Disposable mGeneratingDisposable = Disposables.empty();
  private int mMaxSuggestions;

  public enum LoadingState {
    NOT_LOADED,
    LOADING,
    LOADED
  }

  private final ReplaySubject<LoadingState> mGenerateStateSubject = ReplaySubject.createWithSize(1);
  private ArrayList<short[]> mWordsCorners = new ArrayList<>();

  /**
   * Groups word indices by their starting key for efficient proximity-based lookup. Instead of
   * iterating all words, we can iterate only words that start with keys near the gesture start.
   */
  @NonNull private Map<Keyboard.Key, CompactWordList> mWordsByStartKey = new HashMap<>();

  /**
   * Memory-efficient storage for word references using Structure of Arrays (SoA) pattern. Instead
   * of creating many small WordReference objects, we store data in parallel primitive arrays. This
   * reduces memory overhead from ~32 bytes/word to ~12 bytes/word and minimizes GC pressure.
   */
  private static class CompactWordList {
    // Average ~1600 words/key; 256 reduces reallocations from 5 to 3 during loading
    private static final int INITIAL_CAPACITY = 256;

    int[] dictIndices;
    int[] wordIndices;
    int[] cornersIndices;
    int size;

    CompactWordList() {
      dictIndices = new int[INITIAL_CAPACITY];
      wordIndices = new int[INITIAL_CAPACITY];
      cornersIndices = new int[INITIAL_CAPACITY];
      size = 0;
    }

    void add(int dictIndex, int wordIndex, int cornersIndex) {
      if (size == dictIndices.length) {
        grow();
      }
      dictIndices[size] = dictIndex;
      wordIndices[size] = wordIndex;
      cornersIndices[size] = cornersIndex;
      size++;
    }

    private void grow() {
      int newCapacity = dictIndices.length * 2;
      int[] newDictIndices = new int[newCapacity];
      int[] newWordIndices = new int[newCapacity];
      int[] newCornersIndices = new int[newCapacity];
      System.arraycopy(dictIndices, 0, newDictIndices, 0, size);
      System.arraycopy(wordIndices, 0, newWordIndices, 0, size);
      System.arraycopy(cornersIndices, 0, newCornersIndices, 0, size);
      dictIndices = newDictIndices;
      wordIndices = newWordIndices;
      cornersIndices = newCornersIndices;
    }

    /** Shrinks arrays to exact size, reclaiming unused memory after loading is complete. */
    void trim() {
      if (size < dictIndices.length) {
        int[] newDictIndices = new int[size];
        int[] newWordIndices = new int[size];
        int[] newCornersIndices = new int[size];
        System.arraycopy(dictIndices, 0, newDictIndices, 0, size);
        System.arraycopy(wordIndices, 0, newWordIndices, 0, size);
        System.arraycopy(cornersIndices, 0, newCornersIndices, 0, size);
        dictIndices = newDictIndices;
        wordIndices = newWordIndices;
        cornersIndices = newCornersIndices;
      }
    }
  }

  public GestureTypingDetector(
      double frequencyFactor,
      int maxSuggestions,
      int minPointDistance,
      int startKeyProximityThreshold,
      @NonNull Iterable<Keyboard.Key> keys) {
    mFrequencyFactor = frequencyFactor;
    mMaxSuggestions = maxSuggestions;
    mCandidates = new ArrayList<>(mMaxSuggestions * 3);
    mCandidateWeights = new ArrayList<>(mMaxSuggestions * 3);
    mMinPointDistanceSquared = minPointDistance * minPointDistance;
    mStartKeyProximityThresholdSquared = startKeyProximityThreshold * startKeyProximityThreshold;
    mKeys = keys;

    mGenerateStateSubject.onNext(LoadingState.NOT_LOADED);
  }

  @NonNull
  public Observable<LoadingState> state() {
    return mGenerateStateSubject;
  }

  public void setWords(@NonNull List<char[][]> words, @NonNull List<int[]> wordFrequencies) {
    mWords = words;
    mWordFrequencies = wordFrequencies;

    Logger.d(TAG, "starting generateCorners");
    mGeneratingDisposable.dispose();
    mGenerateStateSubject.onNext(LoadingState.LOADING);
    mGeneratingDisposable =
        generateCornersInBackground(
                mWords, mWordsCorners, mKeys, mKeysByCharacter, mWordsByStartKey, mWorkspaceData)
            .subscribe(mGenerateStateSubject::onNext, mGenerateStateSubject::onError);
  }

  public void destroy() {
    mGeneratingDisposable.dispose();
    mGenerateStateSubject.onNext(LoadingState.NOT_LOADED);
    mGenerateStateSubject.onComplete();
    mWords = Collections.emptyList();
    mWordFrequencies = Collections.emptyList();
    mWordsCorners = new ArrayList<>();
    mKeysByCharacter = new SparseArray<>();
    mWordsByStartKey = new HashMap<>();
  }

  /**
   * Called when system is under memory pressure. Clears temporary data that can be regenerated.
   * Does NOT clear the word list or pre-computed corners.
   */
  public void trimMemory() {
    Logger.d(TAG, "trimMemory() called, clearing temporary data");

    // Clear candidate results (can be regenerated on next gesture)
    mCandidates.clear();
    mCandidateWeights.clear();

    // Trim ArrayList capacity to actual size
    mCandidates.trimToSize();
    mCandidateWeights.trimToSize();

    // Reset gesture workspace
    mWorkspaceData.reset();

    Logger.d(TAG, "trimMemory() completed");
  }

  private static Single<LoadingState> generateCornersInBackground(
      List<char[][]> words,
      Collection<short[]> wordsCorners,
      Iterable<Keyboard.Key> keys,
      SparseArray<Keyboard.Key> keysByCharacter,
      Map<Keyboard.Key, CompactWordList> wordsByStartKey,
      WorkspaceData workspaceData) {

    workspaceData.reset();
    wordsCorners.clear();
    keysByCharacter.clear();
    wordsByStartKey.clear();

    // Use concatMap (not flatMap) to process dictionaries sequentially.
    // This is required because we share mutable state (workspaceData, wordsByStartKey,
    // wordsCorners) across dictionaries, and Schedulers.io() uses multiple threads.
    return Observable.range(0, words.size())
        .subscribeOn(RxSchedulers.background())
        .concatMap(
            dictIndex ->
                Observable.<LoadingState>create(
                    e -> {
                      try {
                        Logger.d(TAG, "generating in BG.");

                        // Fill keysByCharacter map for faster path generation.
                        // This is called for each dictionary, but we only need to do it once.
                        if (keysByCharacter.size() == 0) {
                          for (Keyboard.Key key : keys) {
                            for (int i = 0; i < key.getCodesCount(); ++i) {
                              char c = Character.toLowerCase((char) key.getCodeAtIndex(i, false));
                              keysByCharacter.put(c, key);
                            }
                          }
                        }

                        final char[][] dictionary = words.get(dictIndex);
                        final int cornersOffsetForDict = wordsCorners.size();

                        for (int wordIndex = 0; wordIndex < dictionary.length; wordIndex++) {
                          if (e.isDisposed()) {
                            Logger.d(TAG, "generation cancelled during word processing");
                            return;
                          }
                          char[] word = dictionary[wordIndex];
                          short[] path = generatePath(word, keysByCharacter, workspaceData);
                          wordsCorners.add(path);

                          // Add word to the start-key index for efficient lookup
                          if (word.length > 0) {
                            char firstChar = Dictionary.toLowerCase(word[0]);
                            Keyboard.Key startKey = keysByCharacter.get(firstChar);
                            if (startKey != null) {
                              CompactWordList wordList = wordsByStartKey.get(startKey);
                              if (wordList == null) {
                                wordList = new CompactWordList();
                                wordsByStartKey.put(startKey, wordList);
                              }
                              wordList.add(dictIndex, wordIndex, cornersOffsetForDict + wordIndex);
                            }
                          }
                        }

                        if (!e.isDisposed()) {
                          Logger.d(TAG, "generating done");
                          e.onNext(LoadingState.LOADED);
                          e.onComplete();
                        }
                      } catch (OutOfMemoryError oomError) {
                        Logger.e(TAG, oomError, "OOM during corner generation");
                        if (!e.isDisposed()) {
                          e.onError(oomError);
                        }
                      } catch (Exception exception) {
                        Logger.e(TAG, exception, "Error during corner generation");
                        if (!e.isDisposed()) {
                          e.onError(exception);
                        }
                      }
                    }))
        .lastOrError()
        .doOnSuccess(
            state -> {
              // Trim all CompactWordLists to reclaim unused memory after loading
              for (CompactWordList list : wordsByStartKey.values()) {
                list.trim();
              }
            })
        .onErrorReturnItem(LoadingState.NOT_LOADED)
        .observeOn(RxSchedulers.mainThread());
  }

  private static short[] generatePath(
      char[] word, SparseArray<Keyboard.Key> keysByCharacter, WorkspaceData workspaceData) {
    workspaceData.reset();
    // word = Normalizer.normalize(word, Normalizer.Form.NFD);
    char lastLetter = '\0';

    // Add points for each key
    for (char c : word) {
      c = Character.toLowerCase(c);
      if (lastLetter == c) continue; // Avoid duplicate letters

      Keyboard.Key keyHit = keysByCharacter.get(c);

      if (keyHit == null) {
        // Try finding the base character instead, e.g., the "e" key instead of "é"
        char baseCharacter = Dictionary.toLowerCase(c);
        keyHit = keysByCharacter.get(baseCharacter);
        if (keyHit == null) {
          Logger.w(TAG, "Key %s not found on keyboard!", c);
          continue;
        }
      }

      lastLetter = c;
      workspaceData.addPoint(Keyboard.Key.getCenterX(keyHit), Keyboard.Key.getCenterY(keyHit));
    }

    return getPathCorners(workspaceData);
  }

  /**
   * Adds a point to the gesture path, if it is meaningful
   *
   * @param x the new pointer X position
   * @param y the new pointer Y position
   * @return squared distance from the previous point. Or 0 if not meaningful.
   */
  public int addPoint(int x, int y) {
    if (mGenerateStateSubject.getValue() != LoadingState.LOADED) return 0;

    int distance = 0;
    if (mWorkspaceData.mCurrentGestureArraySize > 0) {
      int previousIndex = mWorkspaceData.mCurrentGestureArraySize - 1;
      final int dx = mWorkspaceData.mCurrentGestureXs[previousIndex] - x;
      final int dy = mWorkspaceData.mCurrentGestureYs[previousIndex] - y;

      distance = dx * dx + dy * dy;
      if (distance <= mMinPointDistanceSquared) return 0;
    }

    mWorkspaceData.addPoint(x, y);
    return distance;
  }

  public void clearGesture() {
    mWorkspaceData.reset();
  }

  private static short[] getPathCorners(WorkspaceData workspaceData) {
    workspaceData.mMaximaArraySize = 0;
    if (workspaceData.mCurrentGestureArraySize > 0) {
      workspaceData.addMaximaPointOfIndex(0);
    }

    for (int gesturePointIndex = 1;
        gesturePointIndex < workspaceData.mCurrentGestureArraySize - 1;
        gesturePointIndex++) {
      if (hasEnoughCurvature(
          workspaceData.mCurrentGestureXs, workspaceData.mCurrentGestureYs, gesturePointIndex)) {
        workspaceData.addMaximaPointOfIndex(gesturePointIndex);
      }
    }

    if (workspaceData.mCurrentGestureArraySize > 1) {
      workspaceData.addMaximaPointOfIndex(workspaceData.mCurrentGestureArraySize - 1);
    }

    short[] arr = new short[workspaceData.mMaximaArraySize];
    System.arraycopy(workspaceData.mMaximaWorkspace, 0, arr, 0, workspaceData.mMaximaArraySize);
    return arr;
  }

  @VisibleForTesting
  static boolean hasEnoughCurvature(final int[] xs, final int[] ys, final int middlePointIndex) {
    // Calculate the radianValue formed between middlePointIndex, and one point in either
    // direction
    final int startPointIndex = middlePointIndex - CURVATURE_NEIGHBORHOOD;
    final int startX = xs[startPointIndex];
    final int startY = ys[startPointIndex];

    final int endPointIndex = middlePointIndex + CURVATURE_NEIGHBORHOOD;
    final int endX = xs[endPointIndex];
    final int endY = ys[endPointIndex];

    final int middleX = xs[middlePointIndex];
    final int middleY = ys[middlePointIndex];

    final double radianValue =
        getArcInRadiansBetweenThreePoints(startX, startY, middleX, middleY, endX, endY);

    return radianValue <= CURVATURE_THRESHOLD;
  }

  private static double getArcInRadiansBetweenThreePoints(
      int startX, int startY, int middleX, int middleY, int endX, int endY) {
    final int firstSectionXDiff = startX - middleX;
    final int firstSectionYDiff = startY - middleY;
    final double firstSectionLength =
        Math.sqrt(firstSectionXDiff * firstSectionXDiff + firstSectionYDiff * firstSectionYDiff);

    final int secondSectionXDiff = endX - middleX;
    final int secondSectionYDiff = endY - middleY;
    final double secondSectionLength =
        Math.sqrt(
            secondSectionXDiff * secondSectionXDiff + secondSectionYDiff * secondSectionYDiff);

    final double dotProduct =
        firstSectionXDiff * secondSectionXDiff + firstSectionYDiff * secondSectionYDiff;

    return Math.acos(dotProduct / firstSectionLength / secondSectionLength);
  }

  public ArrayList<String> getCandidates() {
    mCandidates.clear();
    if (mGenerateStateSubject.getValue() != LoadingState.LOADED) {
      return mCandidates;
    }

    final short[] corners = getPathCorners(mWorkspaceData);
    final int gestureStartX = corners[0];
    final int gestureStartY = corners[1];
    final int gestureEndX = corners[corners.length - 2];
    final int gestureEndY = corners[corners.length - 1];

    // Find the key where gesture starts (may be null if gesture starts between keys)
    Keyboard.Key startKey = null;
    for (Keyboard.Key k : mKeys) {
      if (k.isInside(gestureStartX, gestureStartY)) {
        startKey = k;
        break;
      }
    }

    mCandidateWeights.clear();

    // Iterate only over keys within proximity threshold and process their words
    for (Map.Entry<Keyboard.Key, CompactWordList> entry : mWordsByStartKey.entrySet()) {
      final Keyboard.Key wordStartKey = entry.getKey();

      // Calculate proximity penalty for this key
      double proximityPenalty = 0;
      if (wordStartKey != startKey) {
        // Calculate squared distance from gesture start to this key
        final int distanceSquared = wordStartKey.squaredDistanceFrom(gestureStartX, gestureStartY);

        // Skip keys that are too far from gesture start
        if (distanceSquared > mStartKeyProximityThresholdSquared) {
          continue;
        }

        // Quadratic penalty based on squared distance
        proximityPenalty = distanceSquared * PROXIMITY_PENALTY_FACTOR;
      }

      // Process all words starting with this key using SoA iteration
      final CompactWordList wordList = entry.getValue();
      for (int i = 0; i < wordList.size; i++) {
        final int dictIndex = wordList.dictIndices[i];
        final int wordIndex = wordList.wordIndices[i];
        final int cornersIndex = wordList.cornersIndices[i];

        final char[][] words = mWords.get(dictIndex);
        final int[] wordFrequencies = mWordFrequencies.get(dictIndex);

        // End-key pruning: check if gesture ends near the word's last character key.
        // Use 25x the start-key threshold squared (5x linear distance) - more lenient than
        // start-key because users often overshoot/undershoot at gesture end. This covers
        // most of the keyboard width (~300px with 60dp threshold) while filtering words
        // whose end keys are on the opposite side of the keyboard.
        final char[] word = words[wordIndex];
        if (word.length > 0) {
          final char lastChar = Dictionary.toLowerCase(word[word.length - 1]);
          final Keyboard.Key endKey = mKeysByCharacter.get(lastChar);
          if (endKey != null) {
            final int endDistanceSquared = endKey.squaredDistanceFrom(gestureEndX, gestureEndY);
            if (endDistanceSquared > mStartKeyProximityThresholdSquared * 25) {
              continue; // Gesture ends too far from word's last character
            }
          }
        }

        final double distanceFromCurve =
            calculateDistanceBetweenUserPathAndWord(corners, mWordsCorners.get(cornersIndex));
        if (distanceFromCurve > MINIMUM_DISTANCE_FILTER) {
          continue;
        }

        // TODO: convert wordFrequencies to a double[] in the loading phase.
        final double revisedDistanceFromCurve =
            distanceFromCurve - (mFrequencyFactor * ((double) wordFrequencies[wordIndex]));

        // Final weight combines path distance (with direction penalties) and proximity penalty.
        final double finalWeight = revisedDistanceFromCurve + proximityPenalty;

        int candidateDistanceSortedIndex = 0;
        while (candidateDistanceSortedIndex < mCandidateWeights.size()
            && mCandidateWeights.get(candidateDistanceSortedIndex) <= finalWeight) {
          candidateDistanceSortedIndex++;
        }

        if (candidateDistanceSortedIndex < mMaxSuggestions) {
          mCandidateWeights.add(candidateDistanceSortedIndex, finalWeight);
          mCandidates.add(candidateDistanceSortedIndex, new String(words[wordIndex]));
          if (mCandidateWeights.size() > mMaxSuggestions) {
            mCandidateWeights.remove(mMaxSuggestions);
            mCandidates.remove(mMaxSuggestions);
          }
        }
      }
    }

    return mCandidates;
  }

  /**
   * Iterate over all gesture path corners.
   *
   * <p>Until the next generated word path corner is closer, keep calculating and cumulating the
   * distance between the current generated word path corner and the next gesture path corner.
   *
   * <p>For each iteration over a gesture path corner, compare the vector direction of this current
   * gesture path segment with the vector direction of the current generated word path segment: -
   * when both vectors have exactly the same direction (0°), then distance is considered zero, -
   * when the vectors are perpendicular in direction (90°), then distance is considered normal, -
   * when the vectors are opposite in direction (180°), then distance is doubled, - ... and
   * everything in-between.
   *
   * <p>The motivation for adding this vector direction advantage/penalty is to punish generated
   * word paths that come close to the gesture path corners, but go in a very different direction.
   * Hence, distance alone is not enough to properly consider matching word paths.
   *
   * @param actualUserPath the flat array of gesture path coordinates
   * @param generatedWordPath the flat array of generated word path coordinates
   * @return the cumulative distance between gesture path corners and word path corners, for each
   *     segment multiplied by the reward/penalty factor for adhering to the direction.
   */
  static double calculateDistanceBetweenUserPathAndWord(
      short[] actualUserPath, short[] generatedWordPath) {
    // Debugging is still needed, but at least ASK won't crash this way
    if (actualUserPath.length < 2 || generatedWordPath.length == 0) {
      Logger.w(
          TAG,
          "calculateDistanceBetweenUserPathAndWord: actualUserPath = \"%s\","
              + " generatedWordPath = \"%s\"",
          actualUserPath,
          generatedWordPath);
      Logger.w(TAG, "Some strings are too short; will return maximum distance.");
      return Double.MAX_VALUE;
    }
    if (generatedWordPath.length > actualUserPath.length) return Double.MAX_VALUE;

    double cumulativeDistance = 0;
    int generatedWordCornerIndex = 0;

    // Track last gesture direction for use in second loop
    double lastGestureDirX = 0;
    double lastGestureDirY = 0;
    boolean hasLastGestureDirection = false;

    for (int userPathIndex = 0; userPathIndex < actualUserPath.length; userPathIndex += 2) {
      final int ux = actualUserPath[userPathIndex];
      final int uy = actualUserPath[userPathIndex + 1];
      int wx = generatedWordPath[generatedWordCornerIndex];
      int wy = generatedWordPath[generatedWordCornerIndex + 1];
      double distanceToGeneratedCorner = dist(ux, uy, wx, wy);

      if (generatedWordCornerIndex < generatedWordPath.length - 2) {
        // maybe this new point is closer to the next corner?
        // we only need to check one point ahead since the generated path little corners.
        short nextWx = generatedWordPath[generatedWordCornerIndex + 2];
        short nextWy = generatedWordPath[generatedWordCornerIndex + 3];
        final double distanceToNextGeneratedCorner = dist(ux, uy, nextWx, nextWy);
        if (distanceToNextGeneratedCorner < distanceToGeneratedCorner) {
          generatedWordCornerIndex += 2;
          distanceToGeneratedCorner = distanceToNextGeneratedCorner;
          wx = nextWx;
          wy = nextWy;
        }
      }

      // Calculate direction penalty if we have next points for both paths
      double directionPenaltyMultiplier = 1.0;

      // Check if previous gesture point exists
      // (userPathIndex >= 2 means we have a previous point)
      boolean hasPreviousGesturePoint = userPathIndex >= 2;
      // Check if previous generated point exists
      // (generatedWordCornerIndex >= 2 means we have a previous point)
      boolean hasPreviousGeneratedPoint = generatedWordCornerIndex >= 2;

      if (hasPreviousGesturePoint && hasPreviousGeneratedPoint) {
        // Calculate direction vectors
        double gestureDirX = ux - actualUserPath[userPathIndex - 2];
        double gestureDirY = uy - actualUserPath[userPathIndex - 1];
        double generatedDirX = wx - generatedWordPath[generatedWordCornerIndex - 2];
        double generatedDirY = wy - generatedWordPath[generatedWordCornerIndex - 1];

        directionPenaltyMultiplier =
            calculateDirectionPenaltyMultiplier(
                gestureDirX, gestureDirY, generatedDirX, generatedDirY);

        // Store last gesture direction for use in second loop
        lastGestureDirX = gestureDirX;
        lastGestureDirY = gestureDirY;
        hasLastGestureDirection = true;
      }

      cumulativeDistance += distanceToGeneratedCorner * directionPenaltyMultiplier;
    }

    // we finished the user-path, but for this word there could still be additional
    // generated-path corners.
    // we'll need to add those too, with direction penalty using last known gesture direction.
    final int lastUx = actualUserPath[actualUserPath.length - 2];
    final int lastUy = actualUserPath[actualUserPath.length - 1];

    while (generatedWordCornerIndex < generatedWordPath.length) {
      short wx = generatedWordPath[generatedWordCornerIndex];
      short wy = generatedWordPath[generatedWordCornerIndex + 1];
      double distance = dist(lastUx, lastUy, wx, wy);

      // Apply direction penalty if we have last gesture direction and previous generated corner
      double directionPenaltyMultiplier = 1.0;

      // In this loop we always advance sequentially, so previous is generatedWordCornerIndex - 2
      boolean hasPreviousGeneratedCorner = generatedWordCornerIndex >= 2;

      if (hasLastGestureDirection && hasPreviousGeneratedCorner) {
        double generatedDirX = wx - generatedWordPath[generatedWordCornerIndex - 2];
        double generatedDirY = wy - generatedWordPath[generatedWordCornerIndex - 1];

        directionPenaltyMultiplier =
            calculateDirectionPenaltyMultiplier(
                lastGestureDirX, lastGestureDirY, generatedDirX, generatedDirY);
      }

      cumulativeDistance += distance * directionPenaltyMultiplier;
      generatedWordCornerIndex += 2;
    }

    return cumulativeDistance;
  }

  /**
   * Calculates the direction penalty multiplier based on the angle between gesture and generated
   * word direction vectors. Same direction = 1.0 (no penalty), opposite = 3.0 (max penalty).
   */
  private static double calculateDirectionPenaltyMultiplier(
      double gestureDirX, double gestureDirY, double generatedDirX, double generatedDirY) {
    double cosAngle =
        calculateCosineOfAngleBetweenVectors(
            gestureDirX, gestureDirY, generatedDirX, generatedDirY);
    return 1.0 + DIRECTION_PENALTY_FACTOR * (1.0 - cosAngle);
  }

  /**
   * Calculates the cosine of the angle between two 2D vectors. Uses the dot product formula: cos(θ)
   * = (v1 · v2) / (|v1| * |v2|)
   *
   * @return cosine of angle between vectors, in range [-1, 1]. Returns 1.0 (no penalty) if either
   *     vector has zero length.
   */
  static double calculateCosineOfAngleBetweenVectors(
      double v1x, double v1y, double v2x, double v2y) {
    double magnitudeSquared1 = v1x * v1x + v1y * v1y;
    double magnitudeSquared2 = v2x * v2x + v2y * v2y;

    // If either vector has zero length, return 1.0 (no penalty)
    if (magnitudeSquared1 == 0 || magnitudeSquared2 == 0) {
      return 1.0;
    }

    double dotProduct = v1x * v2x + v1y * v2y;
    double magnitudeProduct = Math.sqrt(magnitudeSquared1 * magnitudeSquared2);

    // Clamp to [-1, 1] to handle floating point errors
    double cosine = dotProduct / magnitudeProduct;
    return Math.max(-1.0, Math.min(1.0, cosine));
  }

  private static double dist(double x1, double y1, double x2, double y2) {
    return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
  }

  private static class WorkspaceData {
    static final int MAX_GESTURE_LENGTH = 1024;
    private int mCurrentGestureArraySize = 0;
    private final int[] mCurrentGestureXs = new int[MAX_GESTURE_LENGTH];
    private final int[] mCurrentGestureYs = new int[MAX_GESTURE_LENGTH];

    private int mMaximaArraySize = 0;
    private final short[] mMaximaWorkspace = new short[4 * MAX_GESTURE_LENGTH];

    void reset() {
      mCurrentGestureArraySize = 0;
      mMaximaArraySize = 0;
    }

    void addPoint(int x, int y) {
      if (MAX_GESTURE_LENGTH == mCurrentGestureArraySize) {
        if (BuildConfig.TESTING_BUILD) {
          Logger.w(TAG, "Discarding gesture");
        }
        return;
      }

      mCurrentGestureXs[mCurrentGestureArraySize] = x;
      mCurrentGestureYs[mCurrentGestureArraySize] = y;
      mCurrentGestureArraySize++;
    }

    void addMaximaPointOfIndex(int gesturePointIndex) {
      mMaximaWorkspace[mMaximaArraySize] = (short) mCurrentGestureXs[gesturePointIndex];
      mMaximaArraySize++;
      mMaximaWorkspace[mMaximaArraySize] = (short) mCurrentGestureYs[gesturePointIndex];
      mMaximaArraySize++;
    }
  }
}
