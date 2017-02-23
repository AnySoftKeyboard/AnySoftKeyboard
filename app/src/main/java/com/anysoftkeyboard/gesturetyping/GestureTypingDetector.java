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
            current.weight = 10;

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

    static class SearchResult {
        public Point start, next;
        public int index=0;
        public float along=0, dist;
    }

    interface MatchPathsHandler {
        void handle(float fx, float fy, Point p);
    }

    // Match every point in gestureInput to its closest* point on the generated curve without backtracking
    // * see the comment below about deficiencies in this implementation
    static void matchPaths(List<Point> gestureInput, List<Point> generated, MatchPathsHandler handler) {
        if (generated.size() <= 1) return;

        SearchResult current = new SearchResult();
        current.start = generated.get(current.index);
        current.next = generated.get(current.index+1);

        for (Point p : gestureInput) {
            findMinimaDistance(p, generated, current.index, current.along, current);

            SearchResult next = new SearchResult();
            int currentIndex = current.index;

            // Look ahead to climb over local minima
            // I have no idea what the best number to use here is. It needs to weigh the probability
            //  of getting stuck in a local minima vs the probability of jumping ahead too far and
            //  causing the rest of the path to be matched incorrectly
            // Perhaps some kind of path-completion metric could be used, so that we don't jump too far
            //   ahead or fall too far behind?
            for (int i=0; i<3; i++) {
                findMinimaDistance(p, generated, currentIndex+1, 0, next);
                if (next.dist < current.dist) {
                    current = next;
                    next = new SearchResult();
                }

                currentIndex++;
            }

            float fx = current.start.x + (current.next.x-current.start.x)*current.along;
            float fy = current.start.y + (current.next.y-current.start.y)*current.along;

            handler.handle(fx, fy, p);
        }
    }

    // Minimize distance between "from" and a point along the path described by "to" using
    //  hill climbing, starting at startIndex
    static void findMinimaDistance(Point from, List<Point> to, int startIndex, float startAlong, SearchResult result) {
        if (startIndex+1 >= to.size()) {
            result.index = -1;
            result.start = null;
            result.next = null;
            result.along = -1;
            result.dist = Float.MAX_VALUE;
            return;
        }

        // Find our first solution
        Point start = to.get(startIndex);
        Point next = to.get(startIndex+1);
        startAlong = GestureTypingDetector.closestScalar(start, next, from, startAlong);
        float dist = GestureTypingDetector.distAlong(start, next, startAlong, from);

        // See if the next solution is better
        while (startIndex+2 < to.size()) {
            Point next2 = to.get(startIndex+2);
            float along2 = GestureTypingDetector.closestScalar(next, next2, from, 0);
            float dist2 = GestureTypingDetector.distAlong(next, next2, along2, from);

            if (dist2 < dist) {
                startIndex++;
                startAlong = along2;
                start = next;
                next = next2;
                dist = dist2;
            }
            else break;
        }

        result.index = startIndex;
        result.start = start;
        result.next = next;
        result.along = startAlong;
        result.dist = dist;
    }

    static float pathDifference(List<Point> generated, List<Point> user) {
        if (generated.size() <= 1) return Float.MAX_VALUE;

        class MyHandler implements MatchPathsHandler {
            private float dist = 0, sumWeight = 0;

            @Override
            public void handle(float fx, float fy, Point p) {
                dist += Math.hypot(fx - p.x, fy - p.y) * p.weight;
                sumWeight += p.weight;
            }
        }
        MyHandler handler = new MyHandler();

        matchPaths(user, generated, handler);
        float result = handler.dist/handler.sumWeight;

        // These checks ensure that there are no strange bugs when sorting
        if (Float.isNaN(result)) throw new RuntimeException("NaN result: " + handler.dist + " " + handler.sumWeight);

        return result;
    }

    static float gestureDistance(String word, List<Point> userPath, List<Keyboard.Key> keys) {
        List<Point> generated = generatePath(word.toCharArray(), keys);
        // Look at shortest distance both ways, so that long generated paths do not match short substrings
        return pathDifference(generated, userPath)
                + pathDifference(userPath, generated);
    }

    // Remove points that are too close together to stop pathDifference from becoming
    //  trapped in local minima
    // Set the weighting of the points based on how long the user spends. Areas with points
    //  that are closer together were under the user's finger for longer
    static void preprocessGestureInput(final List<Point> gestureInput) {

        // TODO this weighting doesn't work
//        for (int i=0; i+1<gestureInput.size(); i++) {
//            float dist = dist(gestureInput.get(i), gestureInput.get(i+1));
//
//            float weight = 11-dist/5f;
//            if (weight > 10) weight = 10;
//            if (weight < 1) weight = 1;
//
//            gestureInput.get(i).weight = weight;
//            gestureInput.get(i+1).weight = weight;
//        }

        int index = 0;
        while (index+1 < gestureInput.size()) {
            float dist = dist(gestureInput.get(index), gestureInput.get(index+1));

            if (dist < 10f) {
                gestureInput.remove(index);
            }
            else index++;
        }
    }

    public static List<CharSequence> getGestureWords(final List<Point> gestureInput,
                                       final List<CharSequence> wordsForPath,
                                       final List<Integer> frequenciesInPath,
                                       final List<Keyboard.Key> keys) {
        preprocessGestureInput(gestureInput);
        if (gestureInput.size() <= 1) return new ArrayList<>();

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
