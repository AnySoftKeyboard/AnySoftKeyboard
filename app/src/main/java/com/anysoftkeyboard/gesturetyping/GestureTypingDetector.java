package com.anysoftkeyboard.gesturetyping;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class GestureTypingDetector {
    private static final String TAG = "GestureTypingDetector";

    // How many points away from the current point to we use when calculating curvature?
    private static final int CURVATURE_SIZE = 5;
    private static final double CURVATURE_THRESHOLD = Math.toRadians(160);

    private int mWidth = 0;
    private int mHeight = 0;

    private final ArrayList<Integer> mXs = new ArrayList<>();
    private final ArrayList<Integer> mYs = new ArrayList<>();

    private Iterable<Keyboard.Key> mKeys = null;
    private final ArrayList<String> mWords = new ArrayList<>();

    private enum LoadingState {
        NOT_LOADED,
        LOADING,
        LOADED
    }

    private LoadingState mWordsCornersState = LoadingState.NOT_LOADED;
    private final ArrayList<int[]> mWordsCorners = new ArrayList<>();

    public synchronized void setKeys(Iterable<Keyboard.Key> keys, Context context, int width, int height) {
        if (mWordsCornersState == LoadingState.LOADING) return;
        if (mWordsCornersState == LoadingState.LOADED
                && keys.equals(mKeys)
                && mWidth == width
                && mHeight == height) return;
        this.mKeys = keys;
        this.mWidth = width;
        this.mHeight = height;

        mWordsCornersState = LoadingState.LOADING;
        new GenerateCornersTask().execute();
    }

    @SuppressFBWarnings(value = "OS_OPEN_STREAM_EXCEPTION_PATH", justification = "This loading process is temporary")
    public void loadResources(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.gesturetyping_temp_dictionary);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!TextUtils.isEmpty(line)) mWords.add(line);
            }

            // Since we crash anyway, it is fine if this isn't in a finally
            reader.close();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class GenerateCornersTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... unused) {
            for (String word : mWords) {
                mWordsCorners.add(generatePath(word.toCharArray()));
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result)
                mWordsCornersState = LoadingState.LOADED;
            else
                mWordsCornersState = LoadingState.NOT_LOADED;
        }

        @Override
        protected void onPreExecute() {
            if (mWordsCornersState != LoadingState.LOADING)
                throw new RuntimeException();
            mWordsCorners.clear();
        }

        @Override
        protected void onProgressUpdate(Void... unused) {
        }
    }

    private int[] generatePath(char[] word) {
        ArrayList<Integer> xs = new ArrayList<>();
        ArrayList<Integer> ys = new ArrayList<>();
        if (word.length == 0) {
            return getPathCorners(xs, ys, 1);
        }

        char lastLetter = '-';

        // Add points for each key
        for (char c : word) {
            c = Character.toLowerCase(c);
            if (!Character.isLetter(c)) continue; //Avoid special characters
            if (lastLetter == c) continue; //Avoid duplicate letters
            lastLetter = c;

            Keyboard.Key keyHit = null;
            for (Keyboard.Key key : mKeys) {
                if (key.getPrimaryCode() == c) {
                    keyHit = key;
                    break;
                }
            }

            if (keyHit == null) {
                Log.e(TAG, "Key " + c + " not found on keyboard!");
                return getPathCorners(xs, ys, 1);
            }

            xs.add(keyHit.x + keyHit.width / 2);
            ys.add(keyHit.y + keyHit.height / 2);
        }

        return getPathCorners(xs, ys, 1);
    }

    public void addPoint(int x, int y, long time) {
        if (mXs.size() > 0) {
            int dx = mXs.get(mXs.size() - 1) - x;
            int dy = mYs.get(mYs.size() - 1) - y;

            if (dx * dx + dy * dy <= 5) return;
        }
        mXs.add(x);
        mYs.add(y);
    }

    public void clearGesture() {
        mXs.clear();
        mYs.clear();
    }

    private int[] getPathCorners(ArrayList<Integer> xs, ArrayList<Integer> ys, int curvatureSize) {
        ArrayList<Integer> maxima = new ArrayList<>();
        if (xs.size() > 0) {
            maxima.add(xs.get(0));
            maxima.add(ys.get(0));
        }

        for (int i = 0; i < xs.size(); i++) {
            if (curvature(xs, ys, i, curvatureSize)) {
                maxima.add(xs.get(i));
                maxima.add(ys.get(i));
            }
        }

        if (xs.size() > 1) {
            maxima.add(xs.get(xs.size() - 1));
            maxima.add(ys.get(ys.size() - 1));
        }

        int[] arr = new int[maxima.size()];
        for (int i = 0; i < maxima.size(); i++) arr[i] = maxima.get(i);
        return arr;
    }

    private boolean curvature(ArrayList<Integer> xs, ArrayList<Integer> ys, int middle, int curvatureSize) {
        // Calculate the angle formed between middle, and one point in either direction
        int si = Math.max(0, middle - curvatureSize);
        int sx = xs.get(si);
        int sy = ys.get(si);

        int ei = Math.min(xs.size() - 1, middle + curvatureSize);
        int ex = xs.get(ei);
        int ey = ys.get(ei);

        if (sx == ex && sy == ey) return true;

        int mx = xs.get(middle);
        int my = ys.get(middle);

        double m1 = Math.sqrt((sx - mx) * (sx - mx) + (sy - my) * (sy - my));
        double m2 = Math.sqrt((ex - mx) * (ex - mx) + (ey - my) * (ey - my));

        double dot = (sx - mx) * (ex - mx) + (sy - my) * (ey - my);
        double angle = Math.abs(Math.acos(dot / m1 / m2));

        return angle <= CURVATURE_THRESHOLD;
    }

    public ArrayList<String> getCandidates() {

        ArrayList<String> candidates = new ArrayList<>();
        if (mWordsCorners.size() != mWords.size()) {
            return candidates;
        }

        int[] corners = getPathCorners(mXs, mYs, CURVATURE_SIZE);
        int numSuggestions = 5;

        ArrayList<Double> weights = new ArrayList<>();

        int startChar = '-';
        for (Keyboard.Key k : mKeys) {
            if (Math.abs(k.x + k.width / 2 - corners[0]) < k.width / 2
                    && Math.abs(k.y + k.height / 2 - corners[1]) < k.height / 2) {
                startChar = k.getPrimaryCode();
                break;
            }
        }

        for (int i = 0; i < mWords.size(); i++) {
            int code = mWords.get(i).charAt(0);
            if (code < startChar) continue;
            if (code > startChar) break;

            double weight = getWordDistance(corners, mWordsCorners.get(i));
            if (weights.size() == numSuggestions && weight >= weights.get(weights.size() - 1))
                continue;

            int j = 0;
            while (j < weights.size() && weights.get(j) <= weight) j++;
            weights.add(j, weight);
            candidates.add(j, mWords.get(i));

            if (weights.size() > 5) {
                weights.remove(weights.size() - 1);
                candidates.remove(candidates.size() - 1);
            }
        }

        return candidates;
    }

    private double getWordDistance(int[] user, int[] word) {
        if (word.length > user.length) return Float.MAX_VALUE;

        double dist = 0;
        int currentWordIndex = 0;

        for (int i = 0; i < user.length / 2; i++) {
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
}
