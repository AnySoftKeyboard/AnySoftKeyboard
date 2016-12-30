package com.anysoftkeyboard.gesturetyping;

import android.content.Context;
import android.util.Log;

import com.anysoftkeyboard.keyboards.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GestureTypingDetector {

    /*
        From Java 8 Standard Library
     */
    public interface Consumer<T> {
        void accept(T var1);
    }

    private static final String TAG = "GestureTypingDetector";
    private static final ArrayList<Keyboard.Key> keysWithinGap = new ArrayList<>();
    static final float MAX_PATH_DIST = 50;
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

            if (xDist <= key.row.defaultHorizontalGap + 50 &&
                    yDist <= key.row.verticalGap + 10) {
                keysWithinGap.add(key);
            }
        }

        return keysWithinGap;
    }

    static float dist(Point a, Point b) {
        return (float) Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y));
    }

    static List<Point> generatePath(char[] word, List<Keyboard.Key> keys, int desiredLength) {
        List<Point> path = new LinkedList<>();
        if (word.length == 0) return path;

        char lastLetter = '-';

        for (char c : word) {
            c = Character.toLowerCase(c);
            if (!Character.isLetter(c)) continue; //TODO hack
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

            path.add(new Point(keyHit.x + keyHit.width/2, keyHit.y + keyHit.height/2));
        }

        if (path.size() == 1) return path;

        // Add extra points to the path to fill it out
        float maxDist = MAX_PATH_DIST;
        while (path.size() < desiredLength) {
            fillPath(maxDist, path);
            maxDist *= 0.75f;
        }

        //Smooth path
        final int leftNeighbours = 3, rightNeighbours = 3;
        int index = 0;

        for (Point p : path) {
            if (index == 0 || index == path.size()-1) {
                index++;
                continue;
            }

            float px = 0, py = 0;
            int totalWeight = 0;

            for (int i = Math.max(0, index - leftNeighbours); i < index; i++) {
                float weight = leftNeighbours - (index - i) + 1;
                Point p2 = path.get(i);

                totalWeight += weight;
                px += p2.x * weight;
                py += p2.y * weight;
            }

            for (int i = Math.min(index + 1, index + rightNeighbours); i < Math.min(path.size(), index + rightNeighbours + 1); i++) {
                float weight = rightNeighbours - (i - index) + 1;
                Point p2 = path.get(i);

                totalWeight += weight;
                px += p2.x * weight;
                py += p2.y * weight;
            }

            //Weight the original point most heavily
            px += p.x*(leftNeighbours+rightNeighbours);
            py += p.y*(leftNeighbours+rightNeighbours);
            totalWeight += (leftNeighbours+rightNeighbours);

            p.x = px/totalWeight;
            p.y = py/totalWeight;

            index++;
        }

        fillPath(maxDist, path);

        return path;
    }

    static void fillPath(float maxDist, List<Point> path) {
        int index = 0;

        while (index < path.size() - 1) {
            Point p1 = path.get(index);
            Point p2 = path.get(index + 1);

            if (dist(p1, p2) <= maxDist) {
                index++;
                continue;
            }

            path.add(index + 1, new Point((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f));
        }
    }

    // Find the next closest point in generated to match to the user point
    static int nextMapping(List<Point> generated, Point user, int genIndex) {
        if (genIndex >= generated.size()) return generated.size()-1;

        float dist = dist(generated.get(genIndex), user);
        while (genIndex < generated.size()-1) {
            float dist2 = dist(generated.get(genIndex+1), user);
            if (dist2 > dist) break;
            dist = dist2;
            genIndex++;
        }

        return genIndex;
    }

    // Requires that generated be larger than user
    private static float pathDifference(List<Point> generated, List<Point> user, List<Keyboard.Key> keys) {
        float dist = 0;
        int genIndex = 0, userIndex=0;

        while (genIndex < generated.size() && userIndex < user.size()) {
            dist += keyboardDistance(user.get(userIndex), generated.get(genIndex), keys);
            genIndex = nextMapping(generated, user.get(userIndex), genIndex+1);

            userIndex++;
        }

        while (userIndex < user.size()) {
            dist+=keyboardDistance(user.get(userIndex), generated.get(generated.size()-1), keys);
            userIndex++;
        }

        while (genIndex < generated.size()) {
            dist+=keyboardDistance(user.get(user.size()-1), generated.get(genIndex), keys);
            genIndex++;
        }

        return dist;
    }

    // Adjust distance if two points are on the same key, so that the user's intention is captured
    //  more clearly
    private static float keyboardDistance(Point p1, Point p2, List<Keyboard.Key> keys) {
        for (Keyboard.Key key : keys) {
            if (key.label == null || key.label.length()!=1) continue;

            if (keyXDist(p1, key) <= 5
                    && keyYDist(p1, key) <= 5
                    && keyXDist(p2, key) <= 5
                    && keyYDist(p2, key) <= 5) return 0.5f*dist(p1, p2);
        }

        return dist(p1, p2);
    }

    private static float keyXDist(Point p, Keyboard.Key key) {
        final float closestX = (p.x < key.x) ? key.x
                : (p.x > (key.x + key.width)) ? (key.x + key.width) : p.x;
        return Math.abs(closestX - p.x);
    }

    private static float keyYDist(Point p, Keyboard.Key key) {
        final float closestY = (p.y < key.y) ? key.y
                : (p.y > (key.y + key.height)) ? (key.y + key.height) : p.y;
        return Math.abs(closestY - p.y);
    }


    private static float gestureDistance(String word, List<Point> userPath, List<Keyboard.Key> keys) {
        return pathDifference(generatePath(word.toCharArray(), keys, userPath.size()), userPath, keys);
    }

    public static List<String> getGestureWords(final List<Point> gestureInput,
                                       final List<CharSequence> wordsForPath,
                                       final List<Integer> frequenciesInPath,
                                       final List<Keyboard.Key> keys) {
        // Details: Recognizing input for Swipe based keyboards, Rémi de Zoeten, University of Amsterdam
        // https://esc.fnwi.uva.nl/thesis/centraal/files/f2109327052.pdf
        final int threads = Math.max(Runtime.getRuntime().availableProcessors(), 1);
        final ExecutorService executor = Executors.newFixedThreadPool(threads);

        ArrayList<String> list = new ArrayList<>();

        // Only add points that are further than maxDist, to save time
        final ArrayList<Point> userPath = new ArrayList<>();
        Point last = gestureInput.get(0);
        userPath.add(last);

        //TODO examine corners and time spent on each letter
        for (Point p : gestureInput) {
            if (dist(last, p) >= MAX_PATH_DIST) {
                userPath.add(p);
                last = p;
            }
        }

        userPath.add(gestureInput.get(gestureInput.size()-1));
        fillPath(MAX_PATH_DIST, userPath); // So that there aren't bunches of points at the corners

        if (userPath.size() <= 1) {
            return list;
        }

        // kept in sorted order according to distances
        final String[] suggestions = new String[SUGGEST_SIZE];
        final float[] distances = new float[SUGGEST_SIZE];
        final int[] frequencies = new int[SUGGEST_SIZE];

        Arrays.fill(distances, Float.MAX_VALUE);

        final int wordsPerThread = (int) Math.ceil((float) wordsForPath.size()/threads);
        for (int thread=0; thread<threads; thread++) {
            final int start = thread*wordsPerThread;
            final int end = Math.min(start+wordsPerThread, wordsForPath.size());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    for (int n=start; n<end; n++) {
                        CharSequence word = wordsForPath.get(n);
                        int freq = frequenciesInPath.get(n);

                        String asString = word.toString().toLowerCase(Locale.US);
                        float dist = gestureDistance(asString, userPath, keys);

                        synchronized (suggestions) {
                            for (int i = 0; i < distances.length; i++) {
                                if (dist < distances[i]) {
                                    for (int j = distances.length - 2; j >= i; j--) {
                                        distances[j + 1] = distances[j];
                                        frequencies[j + 1] = frequencies[j];
                                        suggestions[j + 1] = suggestions[j];
                                    }

                                    distances[i] = dist;
                                    suggestions[i] = asString;
                                    frequencies[i] = freq;
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                Log.e(TAG, "Executor did not finish in time");
                return list;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "executor interrupted", e);
            return list;
        }

        float distanceSum = 0;
        int frequencySum = 0;
        for (int i=0; i<suggestions.length; i++) {
            distanceSum += 1/distances[i];
            frequencySum += frequencies[i];
        }

        for (int i=0; i<suggestions.length; i++) {
            // Weighted average of probabilities puts more popular words upfront, but still lets you gesture them
            //  more precisely if you have to
            distances[i] = (2*(1f/distances[i])/distanceSum + (float)frequencies[i]/frequencySum)/3f;
        }

        for (int i=0; i<suggestions.length; i++) {
            int j = i-1;

            String w = suggestions[i];
            float f = distances[i];

            while (j >= 0 && distances[j] < f) {
                suggestions[j+1] = suggestions[j];
                distances[j+1] = distances[j];
                j--;
            }

            suggestions[j+1] = w;
            distances[j+1] = f;
        }

        for (String w : suggestions) {
            if (w != null) list.add(w);
        }

        return list;
    }

    /**
     * Are we tapping or long-pressing the same key (i.e to get a popup menu)
     */
    public static boolean stayedInKey(Keyboard.Key[] keys, ArrayList<Point> gestureMotion) {
        Keyboard.Key sameKey = null;

        for (Point me : gestureMotion) {
            boolean hasSameKey = false;

            for (Keyboard.Key key : keysWithinKeyGap(keys, me.x, me.y)) {
                if (key.isInside(Math.round(me.x), Math.round(me.y))) {
                    if (sameKey == null) sameKey = key;

                    if (sameKey == key) {
                        hasSameKey = true;
                        break;
                    }
                }
            }

            if (!hasSameKey) return false;
        }

        return true;
    }
}
