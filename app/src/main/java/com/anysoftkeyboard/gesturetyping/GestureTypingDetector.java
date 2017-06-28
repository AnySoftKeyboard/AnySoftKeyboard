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

    public static int[] DEBUG_PATH_CORNERS = null;
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
        xs.add(x);
        ys.add(y);
        times.add(time);
    }

    public void clearGesture() {
        xs.clear();
        ys.clear();
        times.clear();
    }

    public int[] getPathCorners() {
        return new int[] {10, 10, 100, 100};
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
