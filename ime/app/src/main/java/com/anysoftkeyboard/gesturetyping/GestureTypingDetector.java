package com.anysoftkeyboard.gesturetyping;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.SparseArray;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GestureTypingDetector {
    private static final String TAG = "ASKGestureTypingDetector";

    /** The minimum angle that's indicative of a corner in a gesture. */
    private static final double CURVATURE_THRESHOLD = Math.toRadians(170);
    // How many points away from the current point do we use when calculating hasEnoughCurvature?
    private static final int CURVATURE_NEIGHBORHOOD = 1;
    private static final double MINIMUM_DISTANCE_FILTER = 1000000;

    // How far away do two points of the gesture have to be (distance squared)?
    private final int mMinPointDistanceSquared;

    private final ArrayList<String> mCandidates;
    /**
     * When finding candidate words, candidates are chosen by doing a weighted sum of the distance
     * between the user gesture and the word gesture, and the frequency of the candidate word.
     * This factor weighs the frequency term. The distance term has a weight of 1.
     */
    private final double mFrequencyFactor;

    private final ArrayList<Double> mCandidateWeights;

    private final WorkspaceData mWorkspaceData = new WorkspaceData();

    @NonNull private final Iterable<Keyboard.Key> mKeys;

    @NonNull private final SparseArray<Keyboard.Key> mKeysByCharacter = new SparseArray<>();

    @NonNull private List<char[][]> mWords = Collections.emptyList();
    @NonNull private List<int[]> mWordFrequencies = Collections.emptyList();

    @NonNull private Disposable mGeneratingDisposable = Disposables.empty();
    private int mMaxSuggestions;

    public enum LoadingState {
        NOT_LOADED,
        LOADING,
        LOADED
    }

    private final ReplaySubject<LoadingState> mGenerateStateSubject =
            ReplaySubject.createWithSize(1);
    private final ArrayList<int[]> mWordsCorners = new ArrayList<>();

    public GestureTypingDetector(
            double frequencyFactor,
            int maxSuggestions,
            int minPointDistance,
            @NonNull Iterable<Keyboard.Key> keys) {
        mFrequencyFactor = frequencyFactor;
        mMaxSuggestions = maxSuggestions;
        mCandidates = new ArrayList<>(mMaxSuggestions * 3);
        mCandidateWeights = new ArrayList<>(mMaxSuggestions * 3);
        mMinPointDistanceSquared = minPointDistance * minPointDistance;
        mKeys = keys;

        mGenerateStateSubject.onNext(LoadingState.NOT_LOADED);
    }

    @NonNull
    public Observable<LoadingState> state() {
        return mGenerateStateSubject;
    }

    /**
     * Sets the list of detectable words and their frequencies, and finds their corners by which
     * they'll be detected.
     *
     * @param words
     * @param wordFrequencies
     */
    public void setWords(@NonNull List<char[][]> words, @NonNull List<int[]> wordFrequencies) {
        mWords = words;
        mWordFrequencies = wordFrequencies;

        Logger.d(TAG, "starting generateCorners");
        mGeneratingDisposable.dispose();
        mGenerateStateSubject.onNext(LoadingState.LOADING);
        mGeneratingDisposable =
                generateCornersInBackground(
                                mWords, mWordsCorners, mKeys, mKeysByCharacter, mWorkspaceData)
                        .subscribe(mGenerateStateSubject::onNext, mGenerateStateSubject::onError);
    }

    public void destroy() {
        mGeneratingDisposable.dispose();
        mGenerateStateSubject.onNext(LoadingState.NOT_LOADED);
        mGenerateStateSubject.onComplete();
    }

    /**
     * Generate corner sequences for every word in the dictionary.
     *
     * @param words
     * @param wordsCorners
     * @param keys
     * @param keysByCharacter
     * @param workspaceData
     * @return
     */
    private static Single<LoadingState> generateCornersInBackground(
            Iterable<char[][]> words,
            Collection<int[]> wordsCorners,
            Iterable<Keyboard.Key> keys,
            SparseArray<Keyboard.Key> keysByCharacter,
            WorkspaceData workspaceData) {

        workspaceData.reset();
        wordsCorners.clear();
        keysByCharacter.clear();

        return Observable.fromIterable(words)
                .subscribeOn(RxSchedulers.background())
                .map(
                        wordsArray ->
                                new CornersGenerationData(
                                        wordsArray,
                                        wordsCorners,
                                        keys,
                                        keysByCharacter,
                                        workspaceData))
                // consider adding here groupBy operator to fan-out the generation of paths
                .flatMap(
                        data ->
                                Observable.<LoadingState>create(
                                        e -> {
                                            Logger.d(TAG, "generating in BG.");

                                            // Fill keysByCharacter map for faster path generation.
                                            // This is called for each dictionary,
                                            // but we only need to do it once.
                                            if (data.mKeysByCharacter.size() == 0) {
                                                for (Keyboard.Key key : data.mKeys) {
                                                    for (int i = 0; i < key.getCodesCount(); ++i) {
                                                        char c =
                                                                Character.toLowerCase(
                                                                        (char)
                                                                                key.getCodeAtIndex(
                                                                                        i, false));
                                                        data.mKeysByCharacter.put(c, key);
                                                    }
                                                }
                                            }

                                            for (char[] word : data.mWords) {
                                                int[] path =
                                                        generatePath(
                                                                word,
                                                                data.mKeysByCharacter,
                                                                data.mWorkspace);
                                                if (e.isDisposed()) {
                                                    return;
                                                }
                                                data.mWordsCorners.add(path);
                                            }

                                            Logger.d(TAG, "generating done");
                                            e.onNext(LoadingState.LOADED);
                                            e.onComplete();
                                        }))
                .subscribeOn(RxSchedulers.background())
                .lastOrError()
                .onErrorReturnItem(LoadingState.NOT_LOADED)
                .observeOn(RxSchedulers.mainThread());
    }

    /**
     * Generates the gesture that needs to be traced to enter the word and finds the corresponding
     * corners.
     *
     * @param word The characters that compose the word the user wants to type.
     * @param keysByCharacter The key that needs to be pressed for each character.
     * @param workspaceData An empty gesture object.
     * @return An array of alternating x, y coordinates indicating the corners in the
     * gesture's points.
     */
    private static int[] generatePath(
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
            workspaceData.addPoint(keyHit.centerX, keyHit.centerY);
        }

        return getPathCorners(workspaceData);
    }

    /**
     * Adds a point to the current gesture as long as it's not too far from the previous point.
     *
     * @param x
     * @param y
     */
    public void addPoint(int x, int y) {
        if (mGenerateStateSubject.getValue() != LoadingState.LOADED) return;

        if (mWorkspaceData.mCurrentGestureArraySize > 0) {
            final int dx =
                    mWorkspaceData.mCurrentGestureXs[mWorkspaceData.mCurrentGestureArraySize - 1]
                            - x;
            final int dy =
                    mWorkspaceData.mCurrentGestureYs[mWorkspaceData.mCurrentGestureArraySize - 1]
                            - y;

            if (dx * dx + dy * dy <= mMinPointDistanceSquared) return;
        }

        mWorkspaceData.addPoint(x, y);
    }

    public void clearGesture() {
        mWorkspaceData.reset();
    }

    /**
     * Finds all the corners (large changes in direction) in the current gesture.
     *
     * @param workspaceData The gesture whose corners we want.
     * @return An array of alternating x, y coordinates for every corner found.
     */
    private static int[] getPathCorners(WorkspaceData workspaceData) {
        workspaceData.mMaximaArraySize = 0;
        if (workspaceData.mCurrentGestureArraySize > 0) {
            workspaceData.addMaximaPointOfIndex(0);
        }

        for (int gesturePointIndex = 1;
                gesturePointIndex < workspaceData.mCurrentGestureArraySize - 1;
                gesturePointIndex++) {
            if (hasEnoughCurvature(
                    workspaceData.mCurrentGestureXs,
                    workspaceData.mCurrentGestureYs,
                    gesturePointIndex)) {
                workspaceData.addMaximaPointOfIndex(gesturePointIndex);
            }
        }

        if (workspaceData.mCurrentGestureArraySize > 1) {
            workspaceData.addMaximaPointOfIndex(workspaceData.mCurrentGestureArraySize - 1);
        }

        int[] arr = new int[workspaceData.mMaximaArraySize];
        System.arraycopy(workspaceData.mMaximaWorkspace, 0, arr, 0, workspaceData.mMaximaArraySize);
        return arr;
    }

    /**
     * Checks whether the points around a given middle-point present a curvature above a
     * certain threshold.
     *
     * @param xs The x coordinates for the points in the gesture.
     * @param ys The y coordinates for the points in the gesture.
     * @param middlePointIndex The point whose nearby curvature we want to calculate.
     * @return Whether there is a curvature indicative of a corner near the given
     * point.
     */
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

        final int firstSectionXDiff = startX - middleX;
        final int firstSectionYDiff = startY - middleY;
        final double firstSectionLength =
                Math.sqrt(
                        firstSectionXDiff * firstSectionXDiff
                                + firstSectionYDiff * firstSectionYDiff);

        final int secondSectionXDiff = endX - middleX;
        final int secondSectionYDiff = endY - middleY;
        final double secondSectionLength =
                Math.sqrt(
                        secondSectionXDiff * secondSectionXDiff
                                + secondSectionYDiff * secondSectionYDiff);

        final double dotProduct =
                firstSectionXDiff * secondSectionXDiff + firstSectionYDiff * secondSectionYDiff;
        final double radianValue = Math.acos(dotProduct / firstSectionLength / secondSectionLength);

        return radianValue <= CURVATURE_THRESHOLD;
    }

    /**
     * Gets an array of candidate words based on the user's gesture by calculating the distance
     * between the gesture and the gestures for the word set. The candidate words are weighed by
     * distance and frequency and sorted from best to worst.
     *
     * @return An array of candidate words sorted from best to worst.
     */
    public ArrayList<String> getCandidates() {
        mCandidates.clear();
        if (mGenerateStateSubject.getValue() != LoadingState.LOADED) {
            return mCandidates;
        }

        final int[] corners = getPathCorners(mWorkspaceData);

        Keyboard.Key startKey = null;
        for (Keyboard.Key k : mKeys) {
            if (k.isInside(corners[0], corners[1])) {
                startKey = k;
                break;
            }
        }

        if (startKey == null) {
            Logger.w(TAG, "Could not find a key that is inside %d,%d", corners[0], corners[1]);
            return mCandidates;
        }

        mCandidateWeights.clear();
        int dictionaryWordsCornersOffset = 0;
        for (int dictIndex = 0; dictIndex < mWords.size(); dictIndex++) {
            final char[][] words = mWords.get(dictIndex);
            final int[] wordFrequencies = mWordFrequencies.get(dictIndex);
            for (int i = 0; i < words.length; i++) {
                // Check if current word would start with the same key
                final Keyboard.Key wordStartKey =
                        mKeysByCharacter.get(Dictionary.toLowerCase(words[i][0]));
                // filtering all words that do not start with the initial pressed key
                if (wordStartKey != startKey) {
                    continue;
                }

                final double distanceFromCurve =
                        calculateDistanceBetweenUserPathAndWord(
                                corners, mWordsCorners.get(i + dictionaryWordsCornersOffset));
                if (distanceFromCurve > MINIMUM_DISTANCE_FILTER) {
                    continue;
                }

                // TODO: convert wordFrequencies to a double[] in the loading phase.
                final double revisedDistanceFromCurve =
                        distanceFromCurve - (mFrequencyFactor * ((double) wordFrequencies[i]));

                int candidateDistanceSortedIndex = 0;
                while (candidateDistanceSortedIndex < mCandidateWeights.size()
                        && mCandidateWeights.get(candidateDistanceSortedIndex)
                                <= revisedDistanceFromCurve) {
                    candidateDistanceSortedIndex++;
                }

                if (candidateDistanceSortedIndex < mMaxSuggestions) {
                    mCandidateWeights.add(candidateDistanceSortedIndex, revisedDistanceFromCurve);
                    mCandidates.add(candidateDistanceSortedIndex, new String(words[i]));
                    if (mCandidateWeights.size() > mMaxSuggestions) {
                        mCandidateWeights.remove(mMaxSuggestions);
                        mCandidates.remove(mMaxSuggestions);
                    }
                }
            }

            dictionaryWordsCornersOffset += words.length;
        }

        return mCandidates;
    }

    /**
     * Matches two sequences of corners, of potentially different lengths, and calculates the
     * cumulative distance between them.
     *
     * @param actualUserPath The sequence of corners in the user gesture as an alternative array
     *                       of x, y coordinates.
     * @param generatedWordPath The sequence of corners for a chosen word in the same format.
     * @return The cumulative distance between the paths.
     */
    private static double calculateDistanceBetweenUserPathAndWord(
            int[] actualUserPath, int[] generatedWordPath) {
        // Debugging is still needed, but at least ASK won't crash this way
        if (actualUserPath.length < 2 || generatedWordPath.length == 0) {
            Logger.w(
                    TAG,
                    "calculateDistanceBetweenUserPathAndWord: actualUserPath = \"%s\", generatedWordPath = \"%s\"",
                    actualUserPath,
                    generatedWordPath);
            Logger.w(TAG, "Some strings are too short; will return maximum distance.");
            return Double.MAX_VALUE;
        }
        if (generatedWordPath.length > actualUserPath.length) return Double.MAX_VALUE;

        double cumulativeDistance = 0;
        int generatedWordCornerIndex = 0;

        for (int userPathIndex = 0; userPathIndex < actualUserPath.length; userPathIndex += 2) {
            final int ux = actualUserPath[userPathIndex];
            final int uy = actualUserPath[userPathIndex + 1];
            double distanceToGeneratedCorner =
                    dist(
                            ux,
                            uy,
                            generatedWordPath[generatedWordCornerIndex],
                            generatedWordPath[generatedWordCornerIndex + 1]);

            if (generatedWordCornerIndex < generatedWordPath.length - 2) {
                // maybe this new point is closer to the next corner?
                // we only need to check one point ahead since the generated path little corners.
                final double distanceToNextGeneratedCorner =
                        dist(
                                ux,
                                uy,
                                generatedWordPath[generatedWordCornerIndex + 2],
                                generatedWordPath[generatedWordCornerIndex + 3]);
                if (distanceToNextGeneratedCorner < distanceToGeneratedCorner) {
                    generatedWordCornerIndex += 2;
                    distanceToGeneratedCorner = distanceToNextGeneratedCorner;
                }
            }

            cumulativeDistance += distanceToGeneratedCorner;
        }

        // we finished the user-path, but for this word there could still be additional
        // generated-path corners.
        // we'll need to those too.
        for (int ux = actualUserPath[actualUserPath.length - 2],
                        uy = actualUserPath[actualUserPath.length - 1];
                generatedWordCornerIndex < generatedWordPath.length;
                generatedWordCornerIndex += 2) {
            cumulativeDistance +=
                    dist(
                            ux,
                            uy,
                            generatedWordPath[generatedWordCornerIndex],
                            generatedWordPath[generatedWordCornerIndex + 1]);
        }

        return cumulativeDistance;
    }

    /**
     * Calculates the euclidean distance between two 2D points.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return The distance.
     */
    private static double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /**
     * Gesture data.
     */
    private static class WorkspaceData {
        static final int MAX_GESTURE_LENGTH = 2048;
        private int mCurrentGestureArraySize = 0;
        private final int[] mCurrentGestureXs = new int[MAX_GESTURE_LENGTH];
        private final int[] mCurrentGestureYs = new int[MAX_GESTURE_LENGTH];

        private int mMaximaArraySize = 0;
        private final int[] mMaximaWorkspace = new int[4 * MAX_GESTURE_LENGTH];

        void reset() {
            mCurrentGestureArraySize = 0;
            mMaximaArraySize = 0;
        }

        /**
         * Adds a point to the gesture path.
         *
         * @param x
         * @param y
         */
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

        /**
         * Adds a point to the gesture's array of corners, as consecutive x, y coordinates.
         *
         * @param gesturePointIndex The index of the corner in the gesture path.
         */
        void addMaximaPointOfIndex(int gesturePointIndex) {
            mMaximaWorkspace[mMaximaArraySize] = mCurrentGestureXs[gesturePointIndex];
            mMaximaArraySize++;
            mMaximaWorkspace[mMaximaArraySize] = mCurrentGestureYs[gesturePointIndex];
            mMaximaArraySize++;
        }
    }

    public String getWorkspaceToString() {
        int[] x_coords = Arrays.copyOfRange(mWorkspaceData.mCurrentGestureXs, 0, mWorkspaceData.mCurrentGestureArraySize);
        int[] y_coords = Arrays.copyOfRange(mWorkspaceData.mCurrentGestureYs, 0, mWorkspaceData.mCurrentGestureArraySize);

        String x_string = Arrays.toString(x_coords);
        String y_string = Arrays.toString(y_coords);
        return x_string + "," + y_string;
    }

    private static class CornersGenerationData {
        private final char[][] mWords;
        private final Collection<int[]> mWordsCorners;
        private final Iterable<Keyboard.Key> mKeys;
        private final SparseArray<Keyboard.Key> mKeysByCharacter;
        private final WorkspaceData mWorkspace;

        CornersGenerationData(
                char[][] words,
                Collection<int[]> wordsCorners,
                Iterable<Keyboard.Key> keys,
                SparseArray<Keyboard.Key> keysByCharacter,
                WorkspaceData workspace) {
            mWords = words;
            mWordsCorners = wordsCorners;
            mKeys = keys;
            mKeysByCharacter = keysByCharacter;
            mWorkspace = workspace;
        }
    }
}
