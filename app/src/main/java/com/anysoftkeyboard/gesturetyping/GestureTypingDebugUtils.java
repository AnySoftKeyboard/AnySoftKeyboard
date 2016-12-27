package com.anysoftkeyboard.gesturetyping;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.anysoftkeyboard.keyboards.Keyboard;

import java.util.ArrayList;
import java.util.List;

// A bunch of temporary code to draw debugging info for the gesture detector
public class GestureTypingDebugUtils {

    public static final boolean DEBUG = true;
    public static CharSequence DEBUG_WORD = "";
    public static List<Point> DEBUG_INPUT = null;
    public static Keyboard.Key[] DEBUG_KEYS = null;
    private static Paint mGesturePaint = new Paint();

    public static void drawGestureDebugInfo(Canvas canvas, List<Point> mGestureMotion,
                                             Keyboard.Key[] keys, CharSequence compareTo) {
        if (mGestureMotion.isEmpty()) return;

        mGesturePaint.setStrokeWidth(10);
        mGesturePaint.setStyle(Paint.Style.STROKE);

        ArrayList<Point> userPath = new ArrayList<>();
        Point last = mGestureMotion.get(0);
        userPath.add(last);

        for (Point p : mGestureMotion) {
            if (GestureTypingDetector.dist(last, p) >= GestureTypingDetector.MAX_PATH_DIST) {
                userPath.add(p);
                last = p;
            }
        }

        mGesturePaint.setColor(Color.RED);
        for (int i = 1; i < userPath.size(); i++) {
            drawLine(userPath.get(i - 1),userPath.get(i), canvas);
        }

//        mGesturePaint.setColor(Color.BLUE);
//        for (GestureTypingDetector.Point m : userPath) {
//            canvas.drawCircle(m.x, m.y, 5, mGesturePaint);
//        }

        drawGesture(compareTo, GestureTypingDetector.MAX_PATH_DIST, canvas, userPath, keys);
    }

    private static void drawGesture(CharSequence word, float maxDist, Canvas canvas, List<Point> userPath, Keyboard.Key[] keys) {
        char[] wordArray = new char[word.length()];
        for (int i=0; i<word.length(); i++) wordArray[i]=word.charAt(i);

        List<Point> generated =
                GestureTypingDetector.generatePath(wordArray, keys, maxDist);
        if (generated.isEmpty()) return;

        mGesturePaint.setColor(Color.BLUE);
        for (int i = 1; i < generated.size(); i++) {
            drawLine(generated.get(i - 1),generated.get(i), canvas);
        }

//        mGesturePaint.setColor(Color.RED);
//        for (GestureTypingDetector.Point m : generated) {
//            canvas.drawCircle(m.x, m.y, 5, mGesturePaint);
//        }

        mGesturePaint.setColor(Color.MAGENTA);

        int genIndex = 0, userIndex=0;

        while (genIndex < generated.size() && userIndex < userPath.size()) {
            drawLine(userPath.get(userIndex), generated.get(genIndex), canvas);
            genIndex = GestureTypingDetector.nextMapping(generated, userPath.get(userIndex), genIndex+1);

            userIndex++;
        }

        mGesturePaint.setColor(Color.GREEN);
        while (userIndex < userPath.size()) {
            drawLine(userPath.get(userIndex), generated.get(generated.size()-1), canvas);
            userIndex++;
        }

        mGesturePaint.setColor(Color.GRAY);
        while (genIndex < generated.size()) {
            drawLine(userPath.get(userPath.size()-1), generated.get(genIndex), canvas);
            genIndex++;
        }
    }

    private static void drawLine(Point m1, Point m2, Canvas canvas) {
        canvas.drawLine(m1.x, m1.y, m2.x, m2.y, mGesturePaint);
    }
}
