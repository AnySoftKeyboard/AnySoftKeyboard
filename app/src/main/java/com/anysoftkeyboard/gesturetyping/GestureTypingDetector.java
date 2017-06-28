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

    private final ArrayList<Integer> xs = new ArrayList<>();
    private final ArrayList<Integer> ys = new ArrayList<>();
    private final ArrayList<Long> times = new ArrayList<>();

    private final Iterable<Keyboard.Key> keys;
    private final ArrayList<String> words = new ArrayList<>();

    public GestureTypingDetector(Iterable<Keyboard.Key> keys, Context context) {
        this.keys = keys;

        try {
            InputStream is = context.getResources().openRawResource(R.raw.gesturetyping_temp_dictionary);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) words.add(line);
            }

            // Since we crash anyway, it is fine if this isn't in a finally
            reader.close();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPoint(int x, int y, long time) {
        if (xs.size() > 0) {
            int dx = xs.get(xs.size()-1) - x;
            int dy = ys.get(ys.size()-1) - y;

            if (dx*dx + dy*dy <= 5) return;
        }
        xs.add(x);
        ys.add(y);
        times.add(time);
    }

    public void clearGesture() {
        xs.clear();
        ys.clear();
        times.clear();
    }

    private ArrayList<Integer> getPathCorners() {
        ArrayList<Integer> maxima = new ArrayList<>();
        if (xs.size() > 0) {
            maxima.add(xs.get(0));
            maxima.add(ys.get(0));
        }

        for (int i=0; i<xs.size(); i++) {
            if (curvature(i)) {
                int end = i;

                while (end<xs.size()) {
                    if (curvature(end)) {
                        break;
                    }
                    end++;
                }

                int avg_x = 0;
                int avg_y = 0;

                for (int j=i; j<=end; j++) {
                    avg_x += xs.get(i);
                    avg_y += ys.get(i);
                }

                avg_x /= (end - i + 1);
                avg_y /= (end - i + 1);
                maxima.add(avg_x);
                maxima.add(avg_y);

                i = end;
            }
        }

        if (xs.size() > 1) {
            maxima.add(xs.get(xs.size()-1));
            maxima.add(ys.get(ys.size()-1));
        }

        return maxima;
    }

    private boolean curvature(int middle) {
        // Calculate the angle formed between middle, and one point in either direction
        int si = Math.max(0, middle-CURVATURE_SIZE);
        int sx = xs.get(si);
        int sy = ys.get(si);

        int ei = Math.min(xs.size()-1, middle+CURVATURE_SIZE);
        int ex = xs.get(ei);
        int ey = ys.get(ei);

        int mx = xs.get(middle);
        int my = ys.get(middle);

        double m1 = Math.sqrt((sx-mx)*(sx-mx) + (sy-my)*(sy-my));
        double m2 = Math.sqrt((ex-mx)*(ex-mx) + (ey-my)*(ey-my));

        double dot = (sx-mx)*(ex-mx)+(sy-my)*(ey-my);
        double angle = Math.abs(Math.acos(dot/m1/m2));

        System.out.println("***************** Angle: " + Math.toDegrees(angle));

        return angle > 0 && angle <= CURVATURE_THRESHOLD;
    }

    public ArrayList<String> getCandidates() {
        DEBUG_PATH_CORNERS = getPathCorners();
        DEBUG_PATH_X.clear(); DEBUG_PATH_X.addAll(xs);
        DEBUG_PATH_Y.clear(); DEBUG_PATH_Y.addAll(ys);

        ArrayList<String> arr = new ArrayList<>();
        arr.add(words.get(0));
        arr.add(words.get(1));
        return arr;
    }

    /**
     * Did we come close enough to a normal (alphabet) character for this
     * to be considered the start of a gesture?
     */
    public boolean isValidStartTouch(int x, int y) {
        for (Keyboard.Key key : keys) {
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
