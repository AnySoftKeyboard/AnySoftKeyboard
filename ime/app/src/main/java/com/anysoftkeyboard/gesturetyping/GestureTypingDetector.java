package com.anysoftkeyboard.gesturetyping;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.BuildConfig;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.subjects.ReplaySubject;

public abstract class GestureTypingDetector {
    private static final String TAG = "ASKGestureTypingDetector";

    /** The gesture currently being traced by the user. */
    private final GestureTypingDetector.Gesture mUserGesture = new GestureTypingDetector.Gesture();

    /** The square of the minimum distance between two points in the user gesture. */
    private final int mMinPointDistanceSquared;

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

    private final ReplaySubject<GestureTypingDetector.LoadingState> mGenerateStateSubject =
            ReplaySubject.createWithSize(1);

    public GestureTypingDetector(
            int maxSuggestions,
            int minPointDistance,
            @NonNull Iterable<Keyboard.Key> keys)
    {
        mMaxSuggestions = maxSuggestions;
        mMinPointDistanceSquared = minPointDistance * minPointDistance;
        mKeys = keys;
        mGenerateStateSubject.onNext(GestureTypingDetector.LoadingState.NOT_LOADED);
    }

    @NonNull
    public Observable<GestureTypingDetector.LoadingState> state() {
        return mGenerateStateSubject;
    }

    /**
     * Sets the list of detectable words and their frequencies.
     *
     * @param words
     * @param wordFrequencies
     */
    public void setWords(@NonNull List<char[][]> words, @NonNull List<int[]> wordFrequencies) {
        mWords = words;
        mWordFrequencies = wordFrequencies;
    }

    public void destroy() {
        mGeneratingDisposable.dispose();
        mGenerateStateSubject.onNext(GestureTypingDetector.LoadingState.NOT_LOADED);
        mGenerateStateSubject.onComplete();
    }

    /**
     * Adds a point to the  user's gesture as long as it's not too far from the previous point.
     *
     * @param x
     * @param y
     */
    public void addPoint(int x, int y) {
        if (mGenerateStateSubject.getValue() != LoadingState.LOADED) return;

        if (mUserGesture.mCurrentLength > 0) {
            int previousPointIndex = mUserGesture.mCurrentLength - 1;
            final int dx = mUserGesture.mXs[previousPointIndex] - x;
            final int dy = mUserGesture.mYs[previousPointIndex] - y;

            if (dx * dx + dy * dy <= mMinPointDistanceSquared) return;
        }

        mUserGesture.addPoint(x, y);
    }

    public void clearUserGesture() {
        mUserGesture.reset();
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
    public static double euclideanDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /**
     * Represents a gesture, either traced by a user or generated from a known words.
     */
    protected static class Gesture {
        static final int MAX_LENGTH = 2048;
        private int mCurrentLength = 0;
        private final int[] mXs = new int[MAX_LENGTH];
        private final int[] mYs = new int[MAX_LENGTH];


        void reset() {
            mCurrentLength = 0;
        }

        /**
         * Adds a point to the gesture's path.
         *
         * @param x
         * @param y
         */
        void addPoint(int x, int y) {
            if (MAX_LENGTH == mCurrentLength) {
                if (BuildConfig.TESTING_BUILD) {
                    Logger.w(TAG, "Discarding gesture");
                }
                return;
            }

            mXs[mCurrentLength] = x;
            mYs[mCurrentLength] = y;
            mCurrentLength++;
        }

        int getFirstX() {
            return mXs[0];
        }

        int getFirstY() {
            return mYs[0];
        }

        int getLastX() {
            return mYs[mCurrentLength-1];
        }

        int getLastY() {
            return mYs[mCurrentLength-1];
        }

        double getLength() {
            int currentX, currentY;
            int previousX, previousY;
            double length;

            length = 0;
            for (int i=1; i < mCurrentLength; i++) {
                previousX = mXs[i-1];
                previousY = mYs[i-1];

                currentX = mXs[i];
                currentY = mYs[i];

                length += euclideanDistance(previousX, previousY, currentX, currentY);
            }

            return length;
        }

        /**
         * Generates an ideal gesture for the given word. An ideal gesture is one that passes
         * through the center of every character in the target word.
         *
         * @param word The target word whose gesture we want.
         * @param keysByCharacter An array that associates characters to the corresponding keyboard
         *                        key.
         *
         * @return A new gesture that goes through the center of every key in the word.
         */
        static Gesture generateIdealGesture(char[] word, SparseArray<Keyboard.Key> keysByCharacter) {
            Gesture idealGesture = new Gesture();

            char previousLetter = '\0';

            // Add points for each key
            for (char c : word) {
                c = Character.toLowerCase(c);
                if (previousLetter == c) continue; // Avoid duplicate letters

                Keyboard.Key key = keysByCharacter.get(c);

                if (key == null) {
                    // Try finding the base character instead, e.g., the "e" key instead of "é"
                    char baseCharacter = Dictionary.toLowerCase(c);
                    key = keysByCharacter.get(baseCharacter);
                    if (key == null) {
                        Logger.w(TAG, "Key %s not found on keyboard!", c);
                        continue;
                    }
                }

                previousLetter = c;
                idealGesture.addPoint(key.centerX, key.centerY);
            }

            return idealGesture;
        }
    }

}
