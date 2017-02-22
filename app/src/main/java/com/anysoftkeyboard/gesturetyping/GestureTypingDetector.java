package com.anysoftkeyboard.gesturetyping;

import android.support.v4.util.Pair;
import android.util.Log;

import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GestureTypingDetector {

    private static final String TAG = "GestureTypingDetector";
    private static final ArrayList<Keyboard.Key> keysWithinGap = new ArrayList<>();
    private static final float MAX_PATH_DIST = 75;
    private static final int SUGGEST_SIZE = 5;

    /**
     * Did we come close enough to a normal (alphabet) character for this
     * to be considered the start of a gesture?
     */
    public static boolean isValidStartTouch(Keyboard.Key[] keys, int x, int y) {
        for (Keyboard.Key key : keysWithinKeyGap(keys, x, y)) {
            // If we aren't close to a normal key, then don't start a gesture
            // so that single-finger gestures (like swiping up from space) still work

            if (key.label != null &&
                    key.label.length() == 1 &&
                    Character.isLetter(key.label.charAt(0))) {
                //TODO this is a hack; How can we determine if the key is in the main keyboard or not,
                // even if it isn't an english character?
                // Also, when that is done, how can this be made to not activate on the mini keyboard popups?
                return true;
            }
        }

        return false;
    }

    private static List<Keyboard.Key> keysWithinKeyGap(Keyboard.Key[] keys, float x, float y) {
        keysWithinGap.clear();

        for (Keyboard.Key key : keys) {
            // If we aren't close to a normal key, then don't start a gesture
            // so that single-finger gestures (like swiping up from space) still work
            final float closestX = (x < key.x) ? key.x
                    : (x > (key.x + key.width)) ? (key.x + key.width) : x;
            final float closestY = (y < key.y) ? key.y
                    : (y > (key.y + key.height)) ? (key.y + key.height) : y;
            final float xDist = Math.abs(closestX - x);
            final float yDist = Math.abs(closestY - y);

            if (xDist <= key.width/4f &&
                    yDist <= key.height/4f) {
                keysWithinGap.add(key);
            }
        }

        return keysWithinGap;
    }

    public static int[] nearbyKeys(List<Keyboard.Key> keys, Point p) {
        List<Keyboard.Key> nearbyKeys = keysWithinKeyGap(keys.toArray(new Keyboard.Key[0]), p.x, p.y);

        int[] result = new int[nearbyKeys.size()];
        for (int i=0; i<nearbyKeys.size(); i++) result[i] = nearbyKeys.get(i).getPrimaryCode();
        return result;
    }

    private static float dist(Point a, Point b) {
        return (float) Math.hypot(b.x-a.x, b.y-a.y);
    }

    static List<Point> generatePath(char[] word, List<Keyboard.Key> keys) {
        List<Point> path = new ArrayList<>();
        if (word.length == 0) return path;

        char lastLetter = '-';
        Point previous = null;

        // Add points for each key
        for (char c : word) {
            c = Character.toLowerCase(c);
            if (!Character.isLetter(c)) continue; //Avoid special characters
            if (lastLetter == c) continue; //Avoid duplicate letters
            lastLetter = c;

            Keyboard.Key keyHit = null;
            for (Keyboard.Key key : keys) {
                if (key.getPrimaryCode() == c) {
                    keyHit = key;
                    break;
                }
            }

            if (keyHit == null) {
                Log.e(TAG, "Key " + c + " not found on keyboard!");
                return path;
            }

            Point current = new Point(keyHit.x + keyHit.width/2, keyHit.y + keyHit.height/2);

            if (previous != null) {
                float dist = dist(current, previous);
                int steps = (int) Math.ceil(dist/MAX_PATH_DIST);

                // Add points to fill in the path up until current
                for (int i=1; i<steps; i++) {
                    int b = steps-i; //Weight of previous
                    int a = i; //Weight of current

                    path.add(new Point((current.x*a+previous.x*b)/(a+b), (current.y*a+previous.y*b)/(a+b)));
                }
            }

            path.add(current);
            previous = current;
        }

        return path;
    }



    //Find the scalar multiple of next-current, >= last, giving smallest distance to p
    static float closestScalar(Point current, Point next, Point p, float last) {
        float dx = next.x-current.x;
        float dy = next.y-current.y;
        float dl = (float) Math.hypot(dx, dy);

        float px = p.x-current.x;
        float py = p.y-current.y;

        float dot = (dx*px+dy*py)/dl/dl;

        if (dot > 1) dot = 1;
        else if (dot < last) dot = last;

        // If current and next are the same point, then the only option is this point
        if (Float.isNaN(dot)) return 0;
        return dot;
    }

    // Distance between p and the point "along" units along the line to-from
    static float distAlong(Point current, Point next, float along, Point p) {
        float dx = next.x-current.x;
        float dy = next.y-current.y;

        float px = current.x+dx*along;
        float py = current.y+dy*along;

        return (float) Math.hypot(px-p.x, py-p.y);
    }

    static float pathDifference(List<Point> generated, List<Point> user) {
        if (generated.size() <= 1) return Float.MAX_VALUE;

        // TODO find sharp turns and weight them more heavily in the distance calculation
        float dist = 0;
        int genIndex = 0;
        float along = 0;

        // Match every point in user to a point on the generated curve without backtracking
        // Stop when the distances start to increase
        for (Point p : user) {
            Point genCurrent = generated.get(genIndex);
            Point genNext = generated.get(genIndex+1);
            along = GestureTypingDetector.closestScalar(genCurrent, genNext, p, along);

            while (genIndex+2 < generated.size()) {
                Point genNext2 = generated.get(genIndex+2);
                float along2 = GestureTypingDetector.closestScalar(genNext, genNext2, p, 0);

                if (GestureTypingDetector.distAlong(genNext, genNext2, along2, p)
                        < GestureTypingDetector.distAlong(genCurrent, genNext, along, p)) {
                    genIndex++;
                    along = along2;
                    genCurrent = genNext;
                    genNext = genNext2;
                }
                else break;
            }

            // Point on generated that we hit
            float fx = genCurrent.x + (genNext.x-genCurrent.x)*along;
            float fy = genCurrent.y + (genNext.y-genCurrent.y)*along;

            double d = Math.hypot(fx-p.x, fy-p.y);
            dist += d;
        }

        // These checks ensure that there are no strange bugs when sorting
        if (Float.isNaN(dist)) throw new RuntimeException("NaN result!");

        return dist;
    }


    private static float gestureDistance(String word, List<Point> userPath, List<Keyboard.Key> keys) {
        List<Point> generated = generatePath(word.toCharArray(), keys);
        // Look at shortest distance both ways, so that long generated paths do not match short substrings
        return pathDifference(generated, userPath)
                + pathDifference(userPath, generated);
    }

    public static List<CharSequence> getGestureWords(final List<Point> gestureInput,
                                       final List<CharSequence> wordsForPath,
                                       final List<Integer> frequenciesInPath,
                                       final List<Keyboard.Key> keys) {
        // Details: Recognizing input for Swipe based keyboards, Rémi de Zoeten, University of Amsterdam
        // https://esc.fnwi.uva.nl/thesis/centraal/files/f2109327052.pdf
        ArrayList<Pair<CharSequence, Float>> list = new ArrayList<>();

        for (int i=0; i<wordsForPath.size(); i++) {
            list.add(new Pair<>(wordsForPath.get(i),
                    gestureDistance(wordsForPath.get(i).toString(), gestureInput, keys)));
        }

        Collections.sort(list, new Comparator<Pair<CharSequence, Float>>() {
            @Override
            public int compare(Pair<CharSequence, Float> a, Pair<CharSequence, Float> b) {
                return Float.compare(a.second, b.second);
            }
        });

        //TODO use word frequencies

        ArrayList<CharSequence> best = new ArrayList<>();
        for (int i=0; i<SUGGEST_SIZE && i<list.size(); i++) {
            best.add(list.get(i).first);
        }
        return best;
    }

}
