package com.anysoftkeyboard.gesturetyping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by suntabu on 17/9/27.
 */
public class GestureTypingPathDrawHelper {
    public static float convertDipToPx(Context context, float dp) {
        float fPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
        return fPx;
    }

    static final String TAG = "GestureTypingPathDrawHelper";

    static class LineElement {
        public static final int ALPHA_STEP = 20;
        private int mAlpha = 255;

        public LineElement(float pathWidth) {
            mPaint = new Paint();
            mPaint.setARGB(255, 255, 255, 0);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(1);
//            mPaint.setStyle(Paint.Style.STROKE);
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

        public boolean updatePathPoints() {
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
        public void updatePath() {
            //update path
            mPath.reset();
            mPath.moveTo(mPoints[0].x, mPoints[0].y);
            mPath.lineTo(mPoints[1].x, mPoints[1].y);
            mPath.lineTo(mPoints[2].x, mPoints[2].y);
            mPath.lineTo(mPoints[3].x, mPoints[3].y);
            mPath.close();
        }

        // for middle line
        public void updatePathWithStartPoints(PointF pt1, PointF pt2) {
            mPath.reset();
            mPath.moveTo(pt1.x, pt1.y);
            mPath.lineTo(pt2.x, pt2.y);
            mPath.lineTo(mPoints[2].x, mPoints[2].y);
            mPath.lineTo(mPoints[3].x, mPoints[3].y);
            mPath.close();
        }

        public float mStartX = -1;
        public float mStartY = -1;
        public float mEndX = -1;
        public float mEndY = -1;
        public Paint mPaint;
        public Path mPath;
        public PointF[] mPoints = new PointF[4]; //path's vertex
        float mPathWidth;
        float mTempPathWidth;

        public int getAlpha() {
            return mAlpha;
        }
    }

    public interface OnInvalidateCallback {
        void invalidate();
    }

    private OnInvalidateCallback mCallback;
    private LineElement mCurrentLine = null;
    private List<LineElement> mLines = null;
    private long mElapsed = 0;
    private float mStrokeWidth = 15;
    public Paint mPaint;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mCallback != null) {
                mCallback.invalidate();
            }
        }
    };

    public GestureTypingPathDrawHelper(Context context, OnInvalidateCallback callback, Paint paint) {
        initialize(context, paint);
        mCallback = callback;
    }

    private void initialize(Context context, Paint paint) {
        mPaint = paint;
    }

    public void draw(Canvas canvas) {
        mElapsed = SystemClock.elapsedRealtime();

        if (mLines != null) {
            updatePaths();
            for (LineElement e : mLines) {
                if (e.mStartX < 0 || e.mEndY < 0 || e.mPath.isEmpty()) continue;
                e.setPaint(mPaint);
                canvas.drawPath(e.mPath, e.mPaint);
            }
            compactPaths();
        }
    }

    public boolean handleTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {// end one line after finger release
            if (isValidLine(mCurrentLine.mStartX, mCurrentLine.mStartY, x, y)) {
                mCurrentLine.mEndX = x;
                mCurrentLine.mEndY = y;
                addToPaths(mCurrentLine);
            }
            //mCurrentLine.updatePathPoints();
            mCurrentLine = null;
            if (mCallback != null) {
                mCallback.invalidate();
            }
            return true;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            mLines = null;
            mCurrentLine = new LineElement(mStrokeWidth);

            mCurrentLine.mStartX = x;
            mCurrentLine.mStartY = y;
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (isValidLine(mCurrentLine.mStartX, mCurrentLine.mStartY, x, y)) {
                mCurrentLine.mEndX = x;
                mCurrentLine.mEndY = y;
                addToPaths(mCurrentLine);

                mCurrentLine = new LineElement(mStrokeWidth);
                mCurrentLine.mStartX = x;
                mCurrentLine.mStartY = y;

            } else {
                //do nothing, wait next point
            }
        }

        if (mHandler.hasMessages(1)) {
            mHandler.removeMessages(1);
        }
        Message msg = new Message();
        msg.what = 1;
        mHandler.sendMessageDelayed(msg, 0);

        return true;
    }

    private boolean isValidLine(float x1, float y1, float x2, float y2) {
        return Math.abs(x1 - x2) > 1 || Math.abs(y1 - y2) > 1;
    }


    private void addToPaths(LineElement element) {
        if (mLines == null) {
            mLines = new ArrayList<LineElement>();
        }
        mLines.add(element);
    }

    private void updatePaths() {
        int size = mLines.size();
        if (size == 0) return;


        LineElement line = null;
        int j = 0;
        for (; j < size; j++) {
            line = mLines.get(j);
//            line.updatePathPoints();
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

        LineElement lastLine = null;
        for (int i = 1; i < size; i++) {
            line = mLines.get(i);
            if (line.updatePathPoints()) {
                if (lastLine == null) {
                    lastLine = mLines.get(i - 1);
                }
                line.updatePathWithStartPoints(lastLine.mPoints[3], lastLine.mPoints[2]);
                lastLine = null;
            } else {
                mLines.remove(i);
                size = mLines.size();
            }
        }
    }

    public void compactPaths() {
        int size = mLines.size();
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
            mLines = null;
        } else if (index >= 0) {
            //Log.i(TAG, "compactPaths from " + index + " to " + (size - 1));
            mLines = mLines.subList(index, size);
        } else {
            // no sub-path should disappear
        }

        long interval = 40 - SystemClock.elapsedRealtime() + mElapsed;
        if (interval < 0) interval = 0;
        Message msg = new Message();
        msg.what = 1;
        mHandler.sendMessageDelayed(msg, interval);
    }
}
