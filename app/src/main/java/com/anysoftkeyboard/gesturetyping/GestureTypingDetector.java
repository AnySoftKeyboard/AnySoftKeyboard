package com.anysoftkeyboard.gesturetyping;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class GestureTypingDetector {

    private static final String TAG = "GestureTypingDetector";
    private static final ArrayList<Keyboard.Key> keysWithinGap = new ArrayList<>();
    private static ArrayList<String> words = null;
    static final float MAX_PATH_DIST = 50;

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

    public static float dist(Point a, Point b) {
        return (float) Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y));
    }

    public static List<Point> generatePath(char[] word, Keyboard.Key[] keys, float maxDist) {
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
                if (key.label != null && key.label.length()==1 && key.label.charAt(0) == c) { //TODO hack
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

        // Add extra points to the path to fill it out
        fillPath(maxDist, path);

        //Smooth path
        final int leftNeighbours = 2, rightNeighbours = 6;

        for (int index = 1; index < path.size() - 1; index++) {
            Point p = path.get(index);

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
            px += p.x*5*(leftNeighbours+rightNeighbours);
            py += p.y*5*(leftNeighbours+rightNeighbours);
            totalWeight += 5*(leftNeighbours+rightNeighbours);

            p.x = px/totalWeight;
            p.y = py/totalWeight;
        }

        fillPath(maxDist, path);

        return path;
    }

    private static void fillPath(float maxDist, List<Point> path) {
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
    public static int nextMapping(List<Point> generated, Point user, int genIndex) {
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
    private static float pathDifference(List<Point> generated, List<Point> user) {
        float dist = 0;
        int genIndex = 0, userIndex=0;

        while (genIndex < generated.size() && userIndex < user.size()) {
            dist += dist(user.get(userIndex), generated.get(genIndex));
            genIndex = nextMapping(generated, user.get(userIndex), genIndex+1);

            userIndex++;
        }

        while (userIndex < user.size()) {
            dist+=dist(user.get(userIndex), generated.get(generated.size()-1));
            userIndex++;
        }

        while (genIndex < generated.size()) {
            dist+=dist(user.get(user.size()-1), generated.get(genIndex));
            genIndex++;
        }

        return dist;
    }

    private static boolean isOnKey(Point p, char c, Keyboard.Key[] keys) {
        List<Keyboard.Key> startKeys = keysWithinKeyGap(keys, Math.round(p.x), Math.round(p.y));

        for (Keyboard.Key key : startKeys) {
            if (key.label != null && key.label.length()==1 &&
                    Character.toLowerCase(key.label.charAt(0)) == c) {
                return true;
            }
        }

        return false;
    }

    private static float gestureDistance(String word, List<Point> userPath, Keyboard.Key[] keys,
                                         float maxDist) {
        return pathDifference(generatePath(word.toCharArray(), keys, maxDist), userPath);
    }

    public static ArrayList<String> getGestureWords(final List<Point> gestureInput,
                                                    Context context,
                                                    Keyboard.Key[] keys) {
        ArrayList<String> list = new ArrayList<>();
        // Details: Recognizing input for Swipe based keyboards, Rémi de Zoeten, University of Amsterdam
        // https://esc.fnwi.uva.nl/thesis/centraal/files/f2109327052.pdf
        // TODO reduce the number of allocations here

        if (words == null) {
            // TODO This is a temporary workaround. How can we get a list of words from the dictionary?

            words = new ArrayList<>();
            Resources res = context.getResources();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(res.openRawResource(R.raw.wordlist_temporary)));

                String line;
                while ((line = reader.readLine()) != null) {
                    words.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (reader != null) try {
                reader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            Log.d(TAG, "Loaded temporary word list of length " + words.size());
        }

        // Only add points that are further than maxDist, to save time
        final ArrayList<Point> userPath = new ArrayList<>();
        Point last = gestureInput.get(0);
        userPath.add(last);

        for (Point p : gestureInput) {
            if (dist(last, p) >= MAX_PATH_DIST) {
                userPath.add(p);
                last = p;
            }
        }

        userPath.add(gestureInput.get(gestureInput.size()-1));

        if (userPath.size() <= 1) return list;
        HashMap<String, Float> distances = new HashMap<>();

        int comp = 0;

        for (String word : words) {
            if (word.length() <= 1) continue;
            char startChar = Character.toLowerCase(word.charAt(0));
            char endChar = Character.toLowerCase(word.charAt(word.length()-1));

            if (!isOnKey(userPath.get(0), startChar, keys)
                    || !isOnKey(userPath.get(userPath.size()-1), endChar, keys)) continue;

            comp++;
            distances.put(word, gestureDistance(word, userPath, keys, MAX_PATH_DIST));
        }

        if (GestureTypingDebugUtils.DEBUG)
            System.out.println("************************** Examined " + comp + " words");

        for (int i=0; i<5; i++) {
            float minDist = Float.MAX_VALUE;
            String minWord = null;

            for (String w : distances.keySet()) {
                if (list.contains(w)) continue;
                if (distances.get(w) < minDist) {
                    minDist = distances.get(w);
                    minWord = w;
                }
            }

            if (minWord == null) break;
            list.add(minWord);
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

    /**
     * Did we jump across multiple keys (making this an invalid gesture)?
     */
    public static boolean jumpedAcrossKeys(AnyKeyboard keyboard, ArrayList<Point> gestureMotion) {
        for (int i=1; i<gestureMotion.size(); i++) {
            Point p1 = gestureMotion.get(i-1);
            Point p2 = gestureMotion.get(i);

            if (Math.abs(p1.x - p2.x) > keyboard.getKeys().get(0).width
                    || Math.abs(p2.y - p1.y) > keyboard.getKeys().get(0).height) {
                return true;
            }
        }

        return false;
    }
}
