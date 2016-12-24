package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to detect words typed by dragging your finger
 */

public class GestureTypingDetector {

    public static class MotionEvent {
        public final int action;
        public final long downTime, eventTime;

        public final float origX, origY;
        public final int metaState;

        public final int x, y;

        public MotionEvent(android.view.MotionEvent me) {
            origX = me.getX();
            origY = me.getY();
            x = Math.round(me.getX());
            y = Math.round(me.getY());

            action = me.getAction();
            downTime = me.getDownTime();
            eventTime = me.getEventTime();
            metaState = me.getMetaState();

        }
    }

    private static final ArrayList<Keyboard.Key> keysWithinGap = new ArrayList<>();
    private static ArrayList<String> words = null;

    /**
     * Did we come close enough to a normal (alphabet) character for this
     * to be considered the start of a gesture?
     */
    public static boolean isValidStartTouch(Keyboard keyboard, int x, int y) {
        for (Keyboard.Key key : keysWithinKeyGap(keyboard, x, y)) {
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

    private static List<Keyboard.Key> keysWithinKeyGap(Keyboard keyboard, int x, int y) {
        int[] nearestKeys = keyboard.getNearestKeys(x,y);
        keysWithinGap.clear();

        for (int index : nearestKeys) {

            Keyboard.Key key = keyboard.getKeys().get(index);

            // If we aren't close to a normal key, then don't start a gesture
            // so that single-finger gestures (like swiping up from space) still work
            final int closestX = (x < key.x) ? key.x
                    : (x > (key.x + key.width)) ? (key.x + key.width) : x;
            final int closestY = (y < key.y) ? key.y
                    : (y > (key.y + key.height)) ? (key.y + key.height) : y;
            final int xDist = Math.abs(closestX - x);
            final int yDist = Math.abs(closestY - y);

            if (xDist <= key.row.defaultHorizontalGap + 50 &&
                    yDist <= key.row.verticalGap + 10) {
                keysWithinGap.add(key);
            }
        }

        return keysWithinGap;
    }

    public static ArrayList<CharSequence> getGestureWords(AnyKeyboard keyboard,
                                                          ArrayList<MotionEvent> gestureMotion,
                                                          Context context) {
        ArrayList<CharSequence> list = new ArrayList<>();
        // TODO Better prediction: https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/264#issuecomment-257992695
        // This implementation is a quick and dirty hack to make sure everything else works

        // Proposed implementation:
        // - Do a linear search through the dictionary (we can speed this up with some kind of tree later)
        // - For each word, calculate the probability of the word being chosen
        // - Produce a rank of the top N words

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

            Log.d("GestureTypingDetector", "Loaded temporary word list of length " + words.size());
        }

        // List of keys hit during our motion
        ArrayList<Character> charsHit = new ArrayList<>();
        ArrayList<Keyboard.Key> keysHit = new ArrayList<>();

        //TODO consider nearby keys
        for (MotionEvent me : gestureMotion) {
            Keyboard.Key keyHit = null;
            for (Keyboard.Key key: keysWithinKeyGap(keyboard, me.x, me.y)) {
                if (key.isInside(me.x, me.y)) {
                    keyHit = key;
                    break;
                }
            }

            if (keyHit == null) continue;
            if (keyHit.label == null || keyHit.label.length() != 1) continue;
            if (charsHit.size() == 0 || charsHit.get(charsHit.size()-1) != keyHit.label.charAt(0)) {
                charsHit.add(keyHit.label.charAt(0)); //TODO This is a hack
                keysHit.add(keyHit);
            }
        }

        if (charsHit.size() == 0) return list;
        if (charsHit.size() == 1) {
            list.add(""+charsHit.get(0));
            return list;
        }

        //TODO sort by likelyhood and limit results
        for (String word : words) {
            if (word.length() <= 1) continue;
            if (word.charAt(0) != charsHit.get(0)
                    || word.charAt(word.length()-1) != charsHit.get(charsHit.size()-1)) continue;

            if (!isGestureMatch(charsHit, word)) continue;

            list.add(word);
        }

        return list;
    }

    private static boolean isGestureMatch(ArrayList<Character> keysHit, String word) {
        int pathIndex=0;

        outer:
        for (int ci=0; ci<word.length(); ci++) {
            // Duplicate letters (like two ls in hello)
            if (ci > 0 && word.charAt(ci)==word.charAt(ci-1)) continue;

            for (int i=pathIndex; i<keysHit.size(); i++) {
                if (keysHit.get(i) == word.charAt(ci)) {
                    pathIndex = i+1;
                    continue outer;
                }
            }

            //We couldn't find this letter
            return false;
        }

        return true;
    }

    /**
     * Are we tapping or long-pressing the same key (i.e to get a popup menu)
     */
    public static boolean stayedInKey(AnyKeyboard keyboard, ArrayList<MotionEvent> gestureMotion) {
        Keyboard.Key sameKey = null;

        for (MotionEvent me : gestureMotion) {
            boolean hasSameKey = false;

            for (Keyboard.Key key : keysWithinKeyGap(keyboard, me.x, me.y)) {
                if (key.isInside(me.x, me.y)) {
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
    public static boolean jumpedAcrossKeys(AnyKeyboard keyboard, ArrayList<MotionEvent> gestureMotion) {
        for (int i=1; i<gestureMotion.size(); i++) {
            MotionEvent me1 = gestureMotion.get(i-1);
            MotionEvent me2 = gestureMotion.get(i);

            if (Math.abs(me1.origX - me1.origX) > keyboard.getKeys().get(0).width
                    || Math.abs(me1.origY - me2.origY) > keyboard.getKeys().get(0).height) {
                return true;
            }
        }

        return false;
    }
}
