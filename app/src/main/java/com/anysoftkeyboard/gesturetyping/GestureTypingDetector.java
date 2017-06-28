package com.anysoftkeyboard.gesturetyping;

import android.content.Context;

import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GestureTypingDetector {

    // How many points away from the current point to we use when calculating curvature?
    private final static int CURVATURE_SIZE = 3;
    private final static double CURVATURE_THRESHOLD = Math.toRadians(160);

    public static ArrayList<Integer> DEBUG_PATH_CORNERS = null;
    public static final ArrayList<Integer> DEBUG_PATH_X = new ArrayList<>();
    public static final ArrayList<Integer> DEBUG_PATH_Y = new ArrayList<>();

    private final ArrayList<Integer> mXs = new ArrayList<>();
    private final ArrayList<Integer> mYs = new ArrayList<>();
    private final ArrayList<Long> mTimestamps = new ArrayList<>();

    private final Iterable<Keyboard.Key> mKeys;
    private final ArrayList<String> mWords = new ArrayList<>();

    public GestureTypingDetector(Iterable<Keyboard.Key> keys, Context context) {
        this.mKeys = keys;

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

    private ArrayList<Integer> getPathCorners() {
        ArrayList<Integer> maxima = new ArrayList<>();
        if (mXs.size() > 0) {
            maxima.add(mXs.get(0));
            maxima.add(mYs.get(0));
        }

        for (int i = 0; i< mXs.size(); i++) {
            if (curvature(i)) {
                int end = i;

                while (end< mXs.size()) {
                    if (curvature(end)) {
                        break;
                    }
                    end++;
                }

                int avgX = 0;
                int avgY = 0;

                for (int j=i; j<=end; j++) {
                    avgX += mXs.get(i);
                    avgY += mYs.get(i);
                }

                avgX /= (end - i + 1);
                avgY /= (end - i + 1);
                maxima.add(avgX);
                maxima.add(avgY);

                i = end;
            }
        }

        if (mXs.size() > 1) {
            maxima.add(mXs.get(mXs.size()-1));
            maxima.add(mYs.get(mYs.size()-1));
        }

        return maxima;
    }

    private boolean curvature(int middle) {
        // Calculate the angle formed between middle, and one point in either direction
        int si = Math.max(0, middle-CURVATURE_SIZE);
        int sx = mXs.get(si);
        int sy = mYs.get(si);

        int ei = Math.min(mXs.size()-1, middle+CURVATURE_SIZE);
        int ex = mXs.get(ei);
        int ey = mYs.get(ei);

        int mx = mXs.get(middle);
        int my = mYs.get(middle);

        double m1 = Math.sqrt((sx-mx)*(sx-mx) + (sy-my)*(sy-my));
        double m2 = Math.sqrt((ex-mx)*(ex-mx) + (ey-my)*(ey-my));

        double dot = (sx-mx)*(ex-mx)+(sy-my)*(ey-my);
        double angle = Math.abs(Math.acos(dot/m1/m2));

        System.out.println("***************** Angle: " + Math.toDegrees(angle));

        return angle > 0 && angle <= CURVATURE_THRESHOLD;
    }

    public ArrayList<String> getCandidates() {
        DEBUG_PATH_CORNERS = getPathCorners();
        DEBUG_PATH_X.clear(); DEBUG_PATH_X.addAll(mXs);
        DEBUG_PATH_Y.clear(); DEBUG_PATH_Y.addAll(mYs);

        ArrayList<String> arr = new ArrayList<>();
        arr.add(mWords.get(0));
        arr.add(mWords.get(1));
        return arr;
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
