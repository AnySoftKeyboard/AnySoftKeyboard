package com.anysoftkeyboard.gesturetyping;

import android.support.annotation.NonNull;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.menny.android.anysoftkeyboard.BuildConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class GestureTypingDetector {
    private static final String TAG = "GestureTypingDetector";

    private static final double CURVATURE_THRESHOLD = Math.toRadians(160);
    // How many points away from the current point to we use when calculating curvature?
    private final int mCurvatureSize;

    private int mWidth = 0;
    private int mHeight = 0;

    private final ArrayList<CharSequence> mCandidates = new ArrayList<>(64);

    private ArrayList<Double> mCandidateWeights = new ArrayList<>();

    private final WorkspaceData mWorkspaceData = new WorkspaceData();

    private Iterable<Keyboard.Key> mKeys = null;
    private List<char[][]> mWords = new ArrayList<>();
    @NonNull
    private Disposable mGeneratingDisposable = Disposables.empty();

    public enum LoadingState {
        NOT_LOADED,
        LOADING,
        LOADED
    }

    private LoadingState mWordsCornersState = LoadingState.NOT_LOADED;
    private final ArrayList<int[]> mWordsCorners = new ArrayList<>();

    public GestureTypingDetector(int curvatureSize) {
        mCurvatureSize = curvatureSize;
        clearGesture();
    }

    public void setWords(List<char[][]> words) {
        mWords = words;

        generateCorners();
    }

    public void setKeys(Iterable<Keyboard.Key> keys, int width, int height) {
        if (mWordsCornersState == LoadingState.LOADED
                && keys.equals(mKeys)
                && mWidth == width
                && mHeight == height) {
            return;
        }
        mKeys = keys;
        mWidth = width;
        mHeight = height;

        generateCorners();
    }

    private void generateCorners() {
        if (mKeys != null && mWords.size() > 0) {
            mGeneratingDisposable.dispose();
            mGeneratingDisposable = generateCornersInBackground(mWords, mWordsCorners, mKeys, mCurvatureSize, mWorkspaceData)
                    .subscribe(loadingState -> mWordsCornersState = loadingState, GenericOnError.onError("generateCornersInBackground"));
        }
    }

    public void destroy() {
        mGeneratingDisposable.dispose();
    }

    LoadingState getLoadingState() {
        return mWordsCornersState;
    }

    private static Observable<LoadingState> generateCornersInBackground(Iterable<char[][]> words, Collection<int[]> wordsCorners, Iterable<Keyboard.Key> keys, int curvatureSize,
            WorkspaceData workspaceData) {
        return Observable.fromIterable(words)
                .map(wordsArray -> new CornersGenerationData(wordsArray, wordsCorners, keys, curvatureSize, workspaceData))
                .map(data -> {
                    data.reset();
                    return data;
                })
                .subscribeOn(RxSchedulers.background())
                //consider adding here groupBy operator to fan-out the generation of paths
                .flatMap(data -> Observable.<LoadingState>create(e -> {
                    for (char[] word : data.mWords) {
                        int[] path = generatePath(word, data.mKeys, data.mCurvatureSize, data.mWorkspace);
                        if (e.isDisposed()) {
                            return;
                        }
                        data.mWordsCorners.add(path);
                    }

                    e.onNext(LoadingState.LOADED);
                    e.onComplete();
                }))
                .onErrorReturnItem(LoadingState.NOT_LOADED)
                .startWith(LoadingState.LOADING)
                .observeOn(RxSchedulers.mainThread());
    }

    private static int[] generatePath(char[] word, Iterable<Keyboard.Key> keysList, int curvatureSize, WorkspaceData workspaceData) {
        workspaceData.reset();
        //word = Normalizer.normalize(word, Normalizer.Form.NFD);
        char lastLetter = '\0';

        // Add points for each key
        for (char c : word) {
            c = Character.toLowerCase(c);
            if (lastLetter == c) continue; //Avoid duplicate letters

            Keyboard.Key keyHit = null;
            outer:
            for (Keyboard.Key key : keysList) {
                for (int i = 0; i < key.getCodesCount(); ++i) {
                    if (Character.toLowerCase(key.getCodeAtIndex(i, false)) == c) {
                        keyHit = key;
                        break outer;
                    }
                }
            }

            if (keyHit == null) {
                Logger.w(TAG, "Key %s not found on keyboard!", c);
                continue;
            }

            lastLetter = c;
            workspaceData.addPoint(keyHit.centerX, keyHit.centerY);
        }

        return getPathCorners(workspaceData, curvatureSize);
    }

    public void addPoint(int x, int y) {
        if (mWordsCornersState != LoadingState.LOADED) return;

        if (mWorkspaceData.mCurrentGestureArraySize > 0) {
            final int dx = mWorkspaceData.mCurrentGestureXs[mWorkspaceData.mCurrentGestureArraySize - 1] - x;
            final int dy = mWorkspaceData.mCurrentGestureYs[mWorkspaceData.mCurrentGestureArraySize - 1] - y;

            if (dx * dx + dy * dy <= mCurvatureSize) return;
        }

        mWorkspaceData.addPoint(x, y);
    }

    public void clearGesture() {
        mWorkspaceData.reset();
    }

    private static int[] getPathCorners(WorkspaceData workspaceData, int curvatureSize) {
        workspaceData.mMaximaArraySize = 0;
        if (workspaceData.mCurrentGestureArraySize > 0) {
            workspaceData.addMaximaPointOfIndex(0);
        }

        for (int gesturePointIndex = 0; gesturePointIndex < workspaceData.mCurrentGestureArraySize; gesturePointIndex++) {
            if (curvature(workspaceData, gesturePointIndex, curvatureSize)) {
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

    private static boolean curvature(WorkspaceData workspaceData, int middle, int curvatureSize) {
        // Calculate the angle formed between middle, and one point in either direction
        final int si = Math.max(0, middle - curvatureSize);
        final int sx = workspaceData.mCurrentGestureXs[si];
        final int sy = workspaceData.mCurrentGestureYs[si];

        final int ei = Math.min(workspaceData.mCurrentGestureArraySize - 1, middle + curvatureSize);
        final int ex = workspaceData.mCurrentGestureXs[ei];
        final int ey = workspaceData.mCurrentGestureYs[ei];

        if (sx == ex && sy == ey) return true;

        final int mx = workspaceData.mCurrentGestureXs[middle];
        final int my = workspaceData.mCurrentGestureYs[middle];

        double m1 = Math.sqrt((sx - mx) * (sx - mx) + (sy - my) * (sy - my));
        double m2 = Math.sqrt((ex - mx) * (ex - mx) + (ey - my) * (ey - my));

        double dot = (sx - mx) * (ex - mx) + (sy - my) * (ey - my);
        double angle = Math.abs(Math.acos(dot / m1 / m2));

        return angle <= CURVATURE_THRESHOLD;
    }

    public ArrayList<CharSequence> getCandidates() {
        mCandidates.clear();
        if (mWordsCornersState != LoadingState.LOADED) {
            return mCandidates;
        }

        mCandidateWeights.clear();
        int[] corners = getPathCorners(mWorkspaceData, mCurvatureSize);
        int numSuggestions = 15;

        int startChar = '-';
        for (Keyboard.Key k : mKeys) {
            if (Math.abs(k.centerX - corners[0]) < k.width / 2
                    && Math.abs(k.centerY - corners[1]) < k.height / 2) {
                startChar = k.getPrimaryCode();
                break;
            }
        }

        for (int dictIndex = 0; dictIndex < mWords.size(); dictIndex++) {
            final char[][] words = mWords.get(dictIndex);
            for (int i = 0; i < words.length; i++) {
                int code = words[i][0];
                if (code < startChar) continue;
                if (code > startChar) break;

                double weight = getWordDistance(corners, mWordsCorners.get(i));
                if (mCandidateWeights.size() == numSuggestions && weight >= mCandidateWeights.get(mCandidateWeights.size() - 1)) {
                    continue;
                }

                int j = 0;
                while (j < mCandidateWeights.size() && mCandidateWeights.get(j) <= weight) j++;
                mCandidateWeights.add(j, weight);
                mCandidates.add(j, new String(words[i]));

                if (mCandidateWeights.size() > 5) {
                    mCandidateWeights.remove(mCandidateWeights.size() - 1);
                    mCandidates.remove(mCandidates.size() - 1);
                }
            }
        }

        return mCandidates;
    }

    private double getWordDistance(int[] user, int[] word) {
        if (word.length > user.length) return Float.MAX_VALUE;

        double dist = 0;
        int currentWordIndex = 0;

        for (int i = 0; i < user.length / 2 && currentWordIndex < word.length / 2; i++) {
            int ux = user[i * 2];
            int uy = user[i * 2 + 1];
            double d = dist(ux, uy, word[currentWordIndex * 2],
                    word[currentWordIndex * 2 + 1]);
            double d2;

            if (currentWordIndex + 1 < word.length / 2 && i > 0 &&
                    (d2 = dist(ux, uy, word[currentWordIndex * 2 + 2],
                            word[currentWordIndex * 2 + 3])) < d) {
                d = d2;
                currentWordIndex++;
            }

            dist += d;
        }

        while (currentWordIndex + 1 < word.length / 2) {
            currentWordIndex++;
            dist += 10 * dist(user[user.length - 2], user[user.length - 1],
                    word[currentWordIndex * 2], word[currentWordIndex * 2 + 1]);
        }

        return dist;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /**
     * Did we come close enough to a normal (alphabet) character for this
     * to be considered the start of a gesture?
     */
    public boolean isValidStartTouch(int x, int y) {
        if (mKeys == null || mWordsCornersState != LoadingState.LOADED) return false;

        /*
         * Whether drawing or not, I don't think should be determined by the word corners loading state.
         * We have the keys, so we draw the gesture path.
         * We have the generated word corners, so we show the candidate words.
         */

        for (Keyboard.Key key : mKeys) {
            // If we aren't close to a normal key, then don't start a gesture
            // so that single-finger gestures (like swiping up from space) still work
            final float closestX = (x < key.x) ? key.x
                    : (x > (key.x + key.width)) ? (key.x + key.width) : x;
            final float closestY = (y < key.y) ? key.y
                    : (y > (key.y + key.height)) ? (key.y + key.height) : y;
            final float xDist = Math.abs(closestX - x);
            final float yDist = Math.abs(closestY - y);

            if (xDist <= key.width / 3f &&
                    yDist <= key.height / 3f &&
                    key.label != null &&
                    key.label.length() == 1 &&
                    Character.isLetter(key.label.charAt(0))) {
                return true;
            }
        }

        return false;
    }

    public boolean isPerformingGesture() {
        return mWordsCornersState == LoadingState.LOADED && mWorkspaceData.mCurrentGestureArraySize > 0;
    }

    private static class WorkspaceData {
        public static final int MAX_GESTURE_LENGTH = 2048;
        private int mCurrentGestureArraySize = 0;
        private final int[] mCurrentGestureXs = new int[MAX_GESTURE_LENGTH];
        private final int[] mCurrentGestureYs = new int[MAX_GESTURE_LENGTH];

        private int mMaximaArraySize = 0;
        private final int[] mMaximaWorkspace = new int[4 * MAX_GESTURE_LENGTH];

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
            mMaximaWorkspace[mMaximaArraySize] = mCurrentGestureXs[gesturePointIndex];
            mMaximaArraySize++;
            mMaximaWorkspace[mMaximaArraySize] = mCurrentGestureYs[gesturePointIndex];
            mMaximaArraySize++;
        }
    }

    private static class CornersGenerationData {
        private final char[][] mWords;
        private final Collection<int[]> mWordsCorners;
        private final Iterable<Keyboard.Key> mKeys;
        private final int mCurvatureSize;
        private final WorkspaceData mWorkspace;

        CornersGenerationData(char[][] words, Collection<int[]> wordsCorners, Iterable<Keyboard.Key> keys, int curvatureSize, WorkspaceData workspace) {
            mWords = words;
            mWordsCorners = wordsCorners;
            mKeys = keys;
            mCurvatureSize = curvatureSize;
            mWorkspace = workspace;
        }

        public void reset() {
            mWorkspace.reset();
            mWordsCorners.clear();
        }
    }
}
