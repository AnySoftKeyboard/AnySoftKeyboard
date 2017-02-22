package com.anysoftkeyboard.gesturetyping;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.anysoftkeyboard.keyboards.Keyboard;

import java.util.ArrayList;
import java.util.List;

// A bunch of temporary code to draw debugging info for the gesture detector
public class GestureTypingDebugUtils {

    public static final boolean DEBUG = false;
    public static CharSequence DEBUG_WORD = "hello";
    public static final List<Point> DEBUG_INPUT = new ArrayList<>();
    public static List<Keyboard.Key> DEBUG_KEYS = null;
    private static Paint mGesturePaint = new Paint();

    // Temporary code to check the correctness
    public static void drawGestureDebugInfo(Canvas canvas, List<Point> gestureInput,
                                             List<Keyboard.Key> keys, CharSequence compareTo) {
        if (gestureInput.isEmpty()) return;

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

    private static void drawGestureMatch(List<Point> gestureInput, List<Point> generated, Canvas canvas) {
        int genIndex = 0;
        float along = 0;

        for (Point p : gestureInput) {
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

            Point from = new Point(genCurrent.x, genCurrent.y);
            from.x = from.x + (genNext.x-genCurrent.x)*along;
            from.y = from.y + (genNext.y-genCurrent.y)*along;

            drawLine(from, p, canvas);
        }
    }

    private static void drawLine(Point m1, Point m2, Canvas canvas) {
        canvas.drawLine(m1.x, m1.y, m2.x, m2.y, mGesturePaint);
    }
}
