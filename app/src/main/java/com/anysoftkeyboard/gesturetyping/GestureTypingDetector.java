package com.anysoftkeyboard.gesturetyping;

import com.anysoftkeyboard.keyboards.Keyboard;

import java.util.ArrayList;

public class GestureTypingDetector {
    private final ArrayList<Integer> xs = new ArrayList<>();
    private final ArrayList<Integer> ys = new ArrayList<>();
    private final ArrayList<Long> times = new ArrayList<>();

    private final Iterable<Keyboard.Key> keys;

    public GestureTypingDetector(Iterable<Keyboard.Key> keys) {
        this.keys = keys;
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

    public ArrayList<String> getCandidates() {
        ArrayList<String> arr = new ArrayList<>();
        arr.add("hello");
        arr.add("world");
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
