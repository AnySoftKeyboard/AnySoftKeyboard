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
    public static CharSequence DEBUG_WORD = "hello";
    public static final List<Point> DEBUG_INPUT = new ArrayList<>();
    public static List<Keyboard.Key> DEBUG_KEYS = null;
    private static Paint mGesturePaint = new Paint();

    // Temporary code to check the correctness
    public static void drawGestureDebugInfo(Canvas canvas, List<Point> gestureInput,
                                             List<Keyboard.Key> keys, CharSequence compareTo) {
        if (gestureInput.isEmpty()) return;
        GestureTypingDetector.preprocessGestureInput(gestureInput);

        mGesturePaint.setStrokeWidth(2);
        mGesturePaint.setStyle(Paint.Style.STROKE);

        List<Point> generated = GestureTypingDetector.generatePath(compareTo.toString().toCharArray(), keys);
        float dist = GestureTypingDetector.pathDifference(generated, gestureInput)
                + GestureTypingDetector.pathDifference(gestureInput, generated);
        if (generated.isEmpty()) return;

        mGesturePaint.setColor(Color.BLUE);
        for (int i = 1; i < generated.size(); i++) {
            drawLine(generated.get(i - 1),generated.get(i), canvas);
        }

        mGesturePaint.setColor(Color.WHITE);
        for (Point m : generated) {
            canvas.drawCircle(m.x, m.y, 5, mGesturePaint);
        }

        mGesturePaint.setColor(Color.MAGENTA);
        for (int i = 1; i < gestureInput.size(); i++) {
            drawLine(gestureInput.get(i - 1), gestureInput.get(i), canvas);
        }

        mGesturePaint.setTextAlign(Paint.Align.LEFT);
        mGesturePaint.setTextSize(50);
        canvas.drawText("" + dist, 5, canvas.getHeight()-55, mGesturePaint);

        mGesturePaint.setColor(Color.WHITE);
        for (Point m : gestureInput) {
            canvas.drawCircle(m.x, m.y, 5, mGesturePaint);
        }

        if (generated.size() <= 1) return;

        mGesturePaint.setColor(Color.GREEN);
        drawGestureMatch(gestureInput, generated, canvas);
        mGesturePaint.setColor(Color.RED);
        drawGestureMatch(generated, gestureInput, canvas);
    }

    static void drawGestureMatch(List<Point> generated, List<Point> user, final Canvas c) {
        if (generated.size() <= 1) return;

        GestureTypingDetector.MatchPathsHandler handler = new GestureTypingDetector.MatchPathsHandler() {
            Point p2 = new Point(0,0);
            @Override
            public void handle(float fx, float fy, Point p) {
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
