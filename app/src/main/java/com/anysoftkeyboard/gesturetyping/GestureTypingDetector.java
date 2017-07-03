package com.anysoftkeyboard.gesturetyping;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GestureTypingDetector {
    private static final String TAG = "GestureTypingDetector";

    // How many points away from the current point to we use when calculating curvature?
    private static final int CURVATURE_SIZE = 5;
    private static final double CURVATURE_THRESHOLD = Math.toRadians(160);
    // What size is the keyboard that mWordCorners has pixel positions for?
    private static final float WORDS_SIZE = 1000f;

    private int width = 0, height = 0;

    public static int[] DEBUG_PATH_CORNERS = null;

    private final ArrayList<Integer> mXs = new ArrayList<>();
    private final ArrayList<Integer> mYs = new ArrayList<>();
    private final ArrayList<Long> mTimestamps = new ArrayList<>();

    private Iterable<Keyboard.Key> mKeys = null;
    private final ArrayList<String> mWords = new ArrayList<>();
    private final ArrayList<int[]> mWordsCorners = new ArrayList<>();

    public void setKeys(Iterable<Keyboard.Key> keys, Context context, int width, int height) {
        this.mKeys = keys;
        this.width = width;
        this.height = height;
        // Manually generate the corners file whenever the dictionary changes
        // generateCorners();
        // saveCorners(context);
    }

    public void loadResources(Context context) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.gesturetyping_temp_dictionary);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) mWords.add(line);
            }

            // Since we crash anyway, it is fine if this isn't in a finally
            reader.close();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            InputStream is = context.getResources().openRawResource(R.raw.gesturetyping_word_corners);
            DataInputStream reader = new DataInputStream(new BufferedInputStream(new GZIPInputStream(is)));

            short len = reader.readShort();
            while (len > 0) {
                int[] corners = new int[len];

                for (int i = 0; i < len/2; i++) {
                    corners[i*2] = (int) reader.readShort();
                    corners[i*2+1] = (int) reader.readShort();
                }

                mWordsCorners.add(corners);
                try {
                    len = reader.readShort();
                } catch (EOFException e) {
                    break;
                }
            }

            // Since we crash anyway, it is fine if this isn't in a finally
            reader.close();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    private void generateCorners() {
        for (String word : mWords) {
            mWordsCorners.add(generatePath(word.toCharArray()));
        }
    }

    @SuppressWarnings("unused")
    private void saveCorners(Context context) {
        // Used to manually save the generated corners, so that they can be added to raw
        try {
            final File outFile = new File(context.getFilesDir(), "wordCorners.txt");
            outFile.createNewFile();

            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outFile))));

            for (int[] corners : mWordsCorners) {
                writer.writeShort(corners.length);
                for (int i=0; i<corners.length/2; i++) {
                    writer.writeShort((short) (corners[i*2]/(float)width * WORDS_SIZE));
                    writer.writeShort((short) (corners[i*2+1]/(float)height * WORDS_SIZE));
                }
            }

            // Since we crash anyway, it is fine if this isn't in a finally
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error saving corners", e);
            throw new RuntimeException(e);
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

            xs.add(keyHit.x + keyHit.width/2);
            ys.add(keyHit.y + keyHit.height/2);
        }

        return getPathCorners(xs, ys, 1);
    }

    public void addPoint(int x, int y, long time) {
        if (mXs.size() > 0) {
            int dx = mXs.get(mXs.size()-1) - x;
            int dy = mYs.get(mYs.size()-1) - y;

            if (dx*dx + dy*dy <= 5) return;
        }
        mXs.add(x);
        mYs.add(y);
        mTimestamps.add(time);
    }

    public void clearGesture() {
        mXs.clear();
        mYs.clear();
        mTimestamps.clear();
    }

    private int[] getPathCorners(ArrayList<Integer> xs, ArrayList<Integer> ys, int curvatureSize) {
        ArrayList<Integer> maxima = new ArrayList<>();
        if (xs.size() > 0) {
            maxima.add(xs.get(0));
            maxima.add(ys.get(0));
        }

        for (int i = 0; i< xs.size(); i++) {
            if (curvature(xs, ys, i, curvatureSize)) {
                int end = i;

                while (end< xs.size()) {
                    if (curvature(xs, ys, end, curvatureSize)) {
                        break;
                    }
                    end++;
                }

                int avgX = 0;
                int avgY = 0;

                for (int j=i; j<=end; j++) {
                    avgX += xs.get(i);
                    avgY += ys.get(i);
                }

                avgX /= (end - i + 1);
                avgY /= (end - i + 1);
                maxima.add(avgX);
                maxima.add(avgY);

                i = end;
            }
        }

        if (xs.size() > 1) {
            maxima.add(xs.get(xs.size()-1));
            maxima.add(ys.get(ys.size()-1));
        }

        int[] arr = new int[maxima.size()];
        for (int i=0; i<maxima.size(); i++) arr[i] = maxima.get(i);
        return arr;
    }

    private boolean curvature(ArrayList<Integer> xs, ArrayList<Integer> ys, int middle, int curvatureSize) {
        // Calculate the angle formed between middle, and one point in either direction
        int si = Math.max(0, middle-curvatureSize);
        int sx = xs.get(si);
        int sy = ys.get(si);

        int ei = Math.min(xs.size()-1, middle+curvatureSize);
        int ex = xs.get(ei);
        int ey = ys.get(ei);

        if (sx == ex && sy == ey) return true;

        int mx = xs.get(middle);
        int my = ys.get(middle);

        double m1 = Math.sqrt((sx-mx)*(sx-mx) + (sy-my)*(sy-my));
        double m2 = Math.sqrt((ex-mx)*(ex-mx) + (ey-my)*(ey-my));

        double dot = (sx-mx)*(ex-mx)+(sy-my)*(ey-my);
        double angle = Math.abs(Math.acos(dot/m1/m2));

        return angle <= CURVATURE_THRESHOLD;
    }

    public ArrayList<String> getCandidates() {
        int[] corners = getPathCorners(mXs, mYs, CURVATURE_SIZE);
        int numSuggestions = 5;
        ArrayList<String> candidates = new ArrayList<>();
        ArrayList<Double> weights = new ArrayList<>();

        int startChar = '-';
        for (Keyboard.Key k : mKeys) {
            if (Math.abs(k.x + k.width/2 - corners[0]) < k.width/2
                    && Math.abs(k.y + k.height/2 - corners[1]) < k.height/2) {
                startChar = k.getPrimaryCode();
                break;
            }
        }

        for (int i=0; i<mWords.size(); i++) {
            int code = mWords.get(i).charAt(0);
            if (code < startChar) continue;
            if (code > startChar) break;

            double weight = getWordDistance(corners, mWordsCorners.get(i));
            if (weights.size() == numSuggestions && weight >= weights.get(weights.size()-1)) continue;

            int j = 0;
            while (j < weights.size() && weights.get(j) <= weight) j++;
            weights.add(j, weight);
            candidates.add(j, mWords.get(i));

            if (weights.size() > 5) {
                weights.remove(weights.size()-1);
                candidates.remove(candidates.size()-1);
            }
        }

        return candidates;
    }

    private double getWordDistance(int[] user, int[] word) {
        if (word.length > user.length) return Float.MAX_VALUE;

        double dist = 0;
        int currentWordIndex = 0;

        for (int i=0; i<user.length/2; i++) {
            int ux = user[i*2];
            int uy = user[i*2 + 1];
            double d = dist(ux,uy, word[currentWordIndex*2]/WORDS_SIZE*width,
                    word[currentWordIndex*2+1]/WORDS_SIZE*height);
            double d2;

            if (currentWordIndex+1 < word.length/2 && i>0 &&
                    (d2 = dist(ux,uy, word[currentWordIndex*2 + 2]/WORDS_SIZE*width,
                            word[currentWordIndex*2+3]/WORDS_SIZE*width)) < d) {
                d = d2;
                currentWordIndex++;
            }

            dist += d;
        }

        while (currentWordIndex+1 < word.length/2) {
            currentWordIndex++;
            dist += 10*dist(user[user.length-2],user[user.length-1],
                    word[currentWordIndex*2]/WORDS_SIZE*width, word[currentWordIndex*2+1]/WORDS_SIZE*width);
        }

        return dist;
    }

    private double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }

    /**
     * Did we come close enough to a normal (alphabet) character for this
     * to be considered the start of a gesture?
     */
    public boolean isValidStartTouch(int x, int y) {
        for (Keyboard.Key key : mKeys) {
            // If we aren't close to a normal key, then don't start a gesture
            // so that single-finger gestures (like swiping up from space) still work
            final float closestX = (x < key.x) ? key.x
                    : (x > (key.x + key.width)) ? (key.x + key.width) : x;
            final float closestY = (y < key.y) ? key.y
                    : (y > (key.y + key.height)) ? (key.y + key.height) : y;
            final float xDist = Math.abs(closestX - x);
            final float yDist = Math.abs(closestY - y);

            if (xDist <= key.width/3f &&
                    yDist <= key.height/3f &&
                    key.label != null &&
                    key.label.length() == 1 &&
                    Character.isLetter(key.label.charAt(0))) {
                return true;
            }
        }

        return false;
    }
}
