package com.anysoftkeyboard.gesturetyping;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.anysoftkeyboard.keyboards.Keyboard;

import java.util.ArrayList;
import java.util.List;

// A bunch of temporary code to draw debugging info for the gesture detector
public class GestureTypingDebugUtils {

    public static boolean DEBUG = false;
    public static CharSequence DEBUG_WORD = "hello";
    public static final List<Point> DEBUG_INPUT = new ArrayList<>();
    public static List<Keyboard.Key> DEBUG_KEYS = null;
    public static int keyboardWidth, keyboardHeight;
    private static Paint mGesturePaint = new Paint();
    private static int testCounter = 0;

    // Temporary code to check the correctness
    public static void drawGestureDebugInfo(Canvas canvas) {
        if (DEBUG_INPUT.size() <= 1) return;

        String name = (testCounter++) + "_" + DEBUG_WORD.toString().toLowerCase();
        for (char c : "'\" .,".toCharArray()) name = name.replace(""+c, "");

        System.out.print("[GestureTypingDebugUtils] @Test public void test_" + name + "() {testGivenInput(\""
                + name + "\", \"" + DEBUG_WORD.toString().toLowerCase() + "\"");
        for (Point p : DEBUG_INPUT) System.out.print(", new Point(" + p.x/keyboardWidth + "f, " + p.y/keyboardHeight + "f)");
        System.out.println(");}");

        GestureTypingDetector.preprocessGestureInput(DEBUG_INPUT);

        mGesturePaint.setStrokeWidth(2);
        mGesturePaint.setStyle(Paint.Style.STROKE);

        List<Point> generated = GestureTypingDetector.generatePath(DEBUG_WORD.toString().toCharArray(), DEBUG_KEYS);
        if (generated.size() <= 1) return;
        float dist = GestureTypingDetector.pathDifference(generated, DEBUG_INPUT)
                + GestureTypingDetector.pathDifference(DEBUG_INPUT, generated);

        mGesturePaint.setColor(Color.BLUE);
        for (int i = 1; i < generated.size(); i++) {
            drawLine(generated.get(i - 1),generated.get(i), canvas);
        }

        mGesturePaint.setColor(Color.WHITE);
        for (Point m : generated) {
            canvas.drawCircle(m.x, m.y, 5, mGesturePaint);
        }

        mGesturePaint.setColor(Color.MAGENTA);
        for (int i = 1; i < DEBUG_INPUT.size(); i++) {
            drawLine(DEBUG_INPUT.get(i - 1), DEBUG_INPUT.get(i), canvas);
        }

        mGesturePaint.setTextAlign(Paint.Align.LEFT);
        mGesturePaint.setTextSize(50);
        canvas.drawText("" + dist, 5, canvas.getHeight()-55, mGesturePaint);

        mGesturePaint.setColor(Color.WHITE);
        for (Point m : DEBUG_INPUT) {
            canvas.drawCircle(m.x, m.y, 5, mGesturePaint);
        }

        if (generated.size() <= 1) return;

        mGesturePaint.setColor(Color.GREEN);
        drawGestureMatch(DEBUG_INPUT, generated, canvas);
        mGesturePaint.setColor(Color.RED);
        drawGestureMatch(generated, DEBUG_INPUT, canvas);
    }

    static void drawGestureMatch(List<Point> generated, List<Point> user, final Canvas c) {
        if (generated.size() <= 1) return;

        GestureTypingDetector.MatchPathsHandler handler = new GestureTypingDetector.MatchPathsHandler() {
            Point p2 = new Point(0,0);
            @Override
            public void handle(Point start, Point next, float along, float fx, float fy, Point p) {
                p2.x = fx;
                p2.y = fy;
                drawLine(p, p2, c);
            }
        };

        GestureTypingDetector.matchPaths(user, generated, handler);
    }

    private static void drawLine(Point m1, Point m2, Canvas canvas) {
        canvas.drawLine(m1.x, m1.y, m2.x, m2.y, mGesturePaint);
    }
}
