package com.anysoftkeyboard.gesturetyping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by suntabu on 17/9/27.
 */
public class GestureTypingPathDrawHelper {

    static class LineElement {
        static final int ALPHA_STEP = 20;
        private int mAlpha = 255;

        LineElement(float pathWidth) {
            mPaint = new Paint();
            mPaint.setARGB(255, 255, 255, 0);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(1);
            mPaint.setStrokeCap(Paint.Cap.BUTT);
            mPaint.setStyle(Paint.Style.FILL);
            mPath = new Path();
            mPathWidth = mTempPathWidth = pathWidth;
            for (int i = 0; i < mPoints.length; i++) {
                mPoints[i] = new PointF();
            }
        }

        public void setPaint(Paint paint) {
            if (paint != null) {
                mPaint.setColor(paint.getColor());
            }
        }

        public void setAlpha(int alpha) {
            mAlpha = alpha;
            mPaint.setAlpha(mAlpha);

            mPathWidth = (mAlpha * mTempPathWidth) / 255;
        }

        boolean updatePathPoints() {
            float distance = mPathWidth / 2;

            PointF direction = new PointF(mEndX - mStartX, mEndY - mStartY);
            direction.x = direction.x / direction.length();
            direction.y = direction.y / direction.length();
            PointF directionV = new PointF(direction.y, -direction.x);

            float dx = distance * directionV.x;
            float dy = distance * directionV.y;
            PointF leftTop = new PointF(mEndX + dx, mEndY + dy);
            PointF rightTop = new PointF(mEndX - dx, mEndY - dy);
            PointF leftBottom = new PointF(mStartX + dx, mStartY + dy);
            PointF rightBottom = new PointF(mStartX - dx, mStartY - dy);

            mPoints[0] = leftTop;
            mPoints[1] = rightTop;
            mPoints[2] = rightBottom;
            mPoints[3] = leftBottom;
            return true;
        }

        // for the first line
        void updatePath() {
            //update path
            mPath.reset();
            mPath.moveTo(mPoints[0].x, mPoints[0].y);
            mPath.lineTo(mPoints[1].x, mPoints[1].y);
            mPath.lineTo(mPoints[2].x, mPoints[2].y);
            mPath.lineTo(mPoints[3].x, mPoints[3].y);
            mPath.close();
        }

        // for middle line
        void updatePathWithStartPoints(PointF pt1, PointF pt2) {
            mPath.reset();
            mPath.moveTo(pt1.x, pt1.y);
            mPath.lineTo(pt2.x, pt2.y);
            mPath.lineTo(mPoints[2].x, mPoints[2].y);
            mPath.lineTo(mPoints[3].x, mPoints[3].y);
            mPath.close();
        }

        float mStartX = -1;
        float mStartY = -1;
        float mEndX = -1;
        float mEndY = -1;
        Paint mPaint;
        Path mPath;
        PointF[] mPoints = new PointF[4]; //path's vertex
        float mPathWidth;
        float mTempPathWidth;

        public int getAlpha() {
            return mAlpha;
        }
    }

    public interface OnInvalidateCallback {
        void invalidate();
    }

    @NonNull
    private final OnInvalidateCallback mCallback;
    @NonNull
    private LineElement mCurrentLine;
    @NonNull
    private final List<LineElement> mLines = new ArrayList<>();
    private final float mStrokeWidth;
    @NonNull
    private final Paint mPaint;

    public GestureTypingPathDrawHelper(@NonNull Context context, @NonNull OnInvalidateCallback callback, @NonNull Paint paint) {
        mPaint = paint;
        mCallback = callback;
        mStrokeWidth = context.getResources().getDimension(R.dimen.gesture_stroke_width);
        mCurrentLine = new LineElement(mStrokeWidth);
    }

    public void draw(Canvas canvas) {
        updatePaths();
        for (LineElement e : mLines) {
            if (e.mStartX < 0 || e.mEndY < 0 || e.mPath.isEmpty()) continue;
            e.setPaint(mPaint);
            canvas.drawPath(e.mPath, e.mPaint);
        }
        compactPaths();
    }

    public void handleTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        int action = event.getAction();


        if (action == MotionEvent.ACTION_UP) {// end one line after finger release
            if (isValidLine(mCurrentLine.mStartX, mCurrentLine.mStartY, x, y)) {
                mCurrentLine.mEndX = x;
                mCurrentLine.mEndY = y;
                mLines.add(mCurrentLine);
            }

            mLines.clear();
            mCurrentLine = new LineElement(mStrokeWidth);
        } else if (action == MotionEvent.ACTION_MOVE && isValidLine(mCurrentLine.mStartX, mCurrentLine.mStartY, x, y)) {
            if (mCurrentLine.mStartX == -1) {
                mCurrentLine.mStartX = x;
                mCurrentLine.mStartY = y;
            }

            mCurrentLine.mEndX = x;
            mCurrentLine.mEndY = y;
            mLines.add(mCurrentLine);

            mCurrentLine = new LineElement(mStrokeWidth);
            mCurrentLine.mStartX = x;
            mCurrentLine.mStartY = y;
        }

        mCallback.invalidate();
    }

    private boolean isValidLine(float x1, float y1, float x2, float y2) {
        return Math.abs(x1 - x2) > 1 || Math.abs(y1 - y2) > 1;
    }

    private void updatePaths() {
        int size = mLines.size();
        if (size == 0) return;


        LineElement line = null;
        int j = 0;
        for (; j < size; j++) {
            line = mLines.get(j);
            if (line.updatePathPoints()) break;
        }

        if (j == size) {
            mLines.clear();
            return;
        } else {
            for (j--; j >= 0; j--) {
                mLines.remove(0);
            }
        }

        line.updatePath();
        size = mLines.size();

        LineElement lastLine;
        for (int i = 1; i < size; i++) {
            line = mLines.get(i);
            if (line.updatePathPoints()) {
                lastLine = mLines.get(i - 1);
                line.updatePathWithStartPoints(lastLine.mPoints[3], lastLine.mPoints[2]);
            } else {
                mLines.remove(i);
                size = mLines.size();
            }
        }
    }

    private void compactPaths() {
        final int size = mLines.size();
        int index = size - 1;
        if (size == 0) return;
        int baseAlpha = 255 - LineElement.ALPHA_STEP;
        int itselfAlpha;
        LineElement line;
        for (; index >= 0; index--, baseAlpha -= LineElement.ALPHA_STEP) {
            line = mLines.get(index);
            itselfAlpha = line.getAlpha();
            if (itselfAlpha == 255) {
                if (baseAlpha <= 0 || line.mPathWidth < 1) {
                    ++index;
                    break;
                }
                line.setAlpha(baseAlpha);
            } else {
                itselfAlpha -= LineElement.ALPHA_STEP;
                if (itselfAlpha <= 0 || line.mPathWidth < 1) {
                    ++index;
                    break;
                }
                line.setAlpha(itselfAlpha);
            }
        }

        if (index >= size) {
            // all sub-path should disappear
            mLines.clear();
        } else if (index >= 0) {
            final int targetSize = size - index;
            while (mLines.size() > targetSize) mLines.remove(0);
        }
    }
}
