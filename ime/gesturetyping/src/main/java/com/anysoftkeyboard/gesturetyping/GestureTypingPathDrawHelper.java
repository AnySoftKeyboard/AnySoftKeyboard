package com.anysoftkeyboard.gesturetyping;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

public class GestureTypingPathDrawHelper implements GestureTypingPathDraw {
    private static final PointF END_OF_PATH = new PointF(-1f, -1f);

    @VisibleForTesting
    static final GestureTypingPathDraw NO_OP =
            new GestureTypingPathDraw() {
                @Override
                public void draw(Canvas canvas) {}

                @Override
                public void handleTouchEvent(MotionEvent event) {}
            };

    @NonNull public static GestureTypingPathDraw create(
            @NonNull OnInvalidateCallback callback, @NonNull GestureTrailTheme theme) {
        if (theme.maxTrailLength <= 0) return NO_OP;
        return new GestureTypingPathDrawHelper(callback, theme);
    }

    @NonNull private final OnInvalidateCallback mCallback;
    private final int mArraysSize;
    @NonNull private final PointF[] mPointsCircularArray;
    @NonNull private final Paint[] mPaints;
    private int mPointsCurrentIndex = 0;

    private GestureTypingPathDrawHelper(
            @NonNull OnInvalidateCallback callback, @NonNull GestureTrailTheme theme) {
        mCallback = callback;
        mArraysSize = theme.maxTrailLength;
        mPaints = new Paint[mArraysSize];
        mPointsCircularArray = new PointF[mArraysSize];
        for (int elementIndex = 0; elementIndex < theme.maxTrailLength; elementIndex++) {
            Paint paint = new Paint();
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(theme.strokeSizeFor(elementIndex));
            paint.setColor(theme.strokeColorFor(elementIndex));
            mPaints[elementIndex] = paint;
            mPointsCircularArray[elementIndex] = new PointF();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mPointsCurrentIndex > 1) {
            PointF lastDrawnPoint = mPointsCircularArray[(mPointsCurrentIndex - 1) % mArraysSize];
            for (int elementIndex = 1; elementIndex < mArraysSize; elementIndex++) {
                PointF currentPoint =
                        mPointsCircularArray[
                                Math.floorMod(mPointsCurrentIndex - 1 - elementIndex, mArraysSize)];
                if (currentPoint.equals(END_OF_PATH)) break;

                Paint paint = mPaints[Math.min(elementIndex - 1, mArraysSize - 1)];
                canvas.drawLine(
                        lastDrawnPoint.x, lastDrawnPoint.y, currentPoint.x, currentPoint.y, paint);
                lastDrawnPoint = currentPoint;
            }
            mCallback.invalidate();
        }
    }

    @Override
    @SuppressWarnings("fallthrough")
    public void handleTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mPointsCurrentIndex = 0;
                mPointsCircularArray[mArraysSize - 1].set(END_OF_PATH);
                // falling through, on purpose
            case MotionEvent.ACTION_MOVE:
                mPointsCircularArray[mPointsCurrentIndex % mArraysSize].set(x, y);
                mPointsCurrentIndex++;
                mCallback.invalidate();
                break;
        }
    }
}
