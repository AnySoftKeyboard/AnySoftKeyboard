package com.anysoftkeyboard.gesturetyping;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.menny.android.anysoftkeyboard.BuildConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.subjects.ReplaySubject;

public class GestureTypingDetector {
    private static final String TAG = "GestureTypingDetector";

    private static final double CURVATURE_THRESHOLD = Math.toRadians(160);
    // How many points away from the current point to we use when calculating curvature?
    private final int mCurvatureSize;

    private final ArrayList<CharSequence> mCandidates = new ArrayList<>(64);

    private ArrayList<Double> mCandidateWeights = new ArrayList<>();

    private final WorkspaceData mWorkspaceData = new WorkspaceData();

    @NonNull
    private final Iterable<Keyboard.Key> mKeys;
    //TODO: Optimize character lookup with this sparse-array (key is character code)
    @NonNull
    private final SparseArray<Keyboard.Key> mKeysByCharacter = new SparseArray<>();

    @NonNull
    private List<char[][]> mWords = Collections.emptyList();

    @NonNull
    private Disposable mGeneratingDisposable = Disposables.empty();

    public enum LoadingState {
        NOT_LOADED,
        LOADING,
        LOADED
    }

    private final ReplaySubject<LoadingState> mGenerateStateSubject = ReplaySubject.createWithSize(1);
    private final ArrayList<int[]> mWordsCorners = new ArrayList<>();

    public GestureTypingDetector(int curvatureSize, @NonNull Iterable<Keyboard.Key> keys) {
        mCurvatureSize = curvatureSize;
        mKeys = keys;

        mGenerateStateSubject.onNext(LoadingState.NOT_LOADED);
    }

    @NonNull
    public Observable<LoadingState> state() {
        return mGenerateStateSubject;
    }

    public void setWords(List<char[][]> words) {
        mWords = words;

        Logger.d(TAG, "starting generateCorners");
        mGeneratingDisposable.dispose();
        mGenerateStateSubject.onNext(LoadingState.LOADING);
        mGeneratingDisposable = generateCornersInBackground(mWords, mWordsCorners, mKeys, mCurvatureSize, mWorkspaceData)
                .subscribe(mGenerateStateSubject::onNext, mGenerateStateSubject::onError);
    }

    public void destroy() {
        mGeneratingDisposable.dispose();
        mGenerateStateSubject.onNext(LoadingState.NOT_LOADED);
        mGenerateStateSubject.onComplete();
    }

    private static Single<LoadingState> generateCornersInBackground(Iterable<char[][]> words, Collection<int[]> wordsCorners, Iterable<Keyboard.Key> keys, int curvatureSize,
            WorkspaceData workspaceData) {

        workspaceData.reset();
        wordsCorners.clear();

        return Observable.fromIterable(words)
                .subscribeOn(RxSchedulers.background())
                .map(wordsArray -> new CornersGenerationData(wordsArray, wordsCorners, keys, curvatureSize, workspaceData))
                //consider adding here groupBy operator to fan-out the generation of paths
                .flatMap(data -> Observable.<LoadingState>create(e -> {
                    Logger.d(TAG, "generating in BG.");
                    int index = 0;
                    for (char[] word : data.mWords) {
                        if (index % 20 == 0) {
                            Logger.d(TAG, "generated %d paths in thread %s", index, Thread.currentThread().toString());
                        }
                        index++;
                        int[] path = generatePath(word, data.mKeys, data.mCurvatureSize, data.mWorkspace);
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

    private static int[] generatePath(char[] word, Iterable<Keyboard.Key> keysList, int curvatureSize, WorkspaceData workspaceData) {
        workspaceData.reset();
        //word = Normalizer.normalize(word, Normalizer.Form.NFD);
        char lastLetter = '\0';

        // Add points for each key
        for (char c : word) {
            c = Dictionary.toLowerCase(c);
            if (lastLetter == c) continue; //Avoid duplicate letters

            Keyboard.Key keyHit = null;
            outer:
            for (Keyboard.Key key : keysList) {
                for (int i = 0; i < key.getCodesCount(); ++i) {
                    if (Dictionary.toLowerCase((char) key.getCodeAtIndex(i, false)) == c) {
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
        if (mGenerateStateSubject.getValue() != LoadingState.LOADED) return;

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
        if (mGenerateStateSubject.getValue() != LoadingState.LOADED) {
            return mCandidates;
        }

        mCandidateWeights.clear();
        int[] corners = getPathCorners(mWorkspaceData, mCurvatureSize);
        final int numSuggestions = 15;

        char startChar = '\0';
        for (Keyboard.Key k : mKeys) {
            if (k.isInside(corners[0], corners[1])) {
                startChar = Dictionary.toLowerCase((char) k.getPrimaryCode());
                break;
            }
        }

        if (startChar == '\0') {
            Logger.w(TAG, "Could not find a key that is inside %d,%d", corners[0], corners[1]);
            return mCandidates;
        }

        int cornersOffset = 0;
        for (int dictIndex = 0; dictIndex < mWords.size(); dictIndex++) {
            final char[][] words = mWords.get(dictIndex);
            for (int i = 0; i < words.length; i++) {
                if (Dictionary.toLowerCase(words[i][0]) != startChar) continue;

                double weight = getWordDistance(corners, mWordsCorners.get(i + cornersOffset));
                /*if (mCandidateWeights.size() == numSuggestions && weight >= mCandidateWeights.get(mCandidateWeights.size() - 1)) {
                    continue;
                }*/

                int j = 0;
                while (j < mCandidateWeights.size() && mCandidateWeights.get(j) <= weight) j++;
                mCandidateWeights.add(j, weight);
                mCandidates.add(j, new String(words[i]));

                if (mCandidateWeights.size() > numSuggestions) {
                    mCandidateWeights.remove(mCandidateWeights.size() - 1);
                    mCandidates.remove(mCandidates.size() - 1);
                }
            }

            cornersOffset += words.length;
        }

        return mCandidates;
    }

    private static double getWordDistance(int[] user, int[] word) {
        if (word.length > user.length) return Double.MAX_VALUE;

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

    private static double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
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
    }
}
