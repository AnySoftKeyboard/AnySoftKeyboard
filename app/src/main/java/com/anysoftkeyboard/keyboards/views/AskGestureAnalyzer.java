package com.anysoftkeyboard.keyboards.views;


import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AskGestureAnalyzer {
    // Finished gestures flags
    public static final int NONE = 0;
    public static final int SWIPE_1_UP = 11;
    public static final int SWIPE_1_DOWN = 12;
    public static final int SWIPE_1_LEFT = 13;
    public static final int SWIPE_1_RIGHT = 14;
    public static final int SWIPE_2_UP = 21;
    public static final int SWIPE_2_DOWN = 22;
    public static final int SWIPE_2_LEFT = 23;
    public static final int SWIPE_2_RIGHT = 24;
    public static final int SWIPE_3_UP = 31;
    public static final int SWIPE_3_DOWN = 32;
    public static final int SWIPE_3_LEFT = 33;
    public static final int SWIPE_3_RIGHT = 34;
    public static final int SWIPE_4_UP = 41;
    public static final int SWIPE_4_DOWN = 42;
    public static final int SWIPE_4_LEFT = 43;
    public static final int SWIPE_4_RIGHT = 44;
    public static final int PINCH_2 = 25;
    public static final int UNPINCH_2 = 26;
    public static final int PINCH_3 = 35;
    public static final int UNPINCH_3 = 36;
    public static final int PINCH_4 = 45;
    public static final int UNPINCH_4 = 46;

    public static final int DOUBLE_TAP_1 = 107;
    private double[] initialX = new double[5];
    private double[] initialY = new double[5];
    private double[] finalX = new double[5];
    private double[] finalY = new double[5];
    private double[] currentX = new double[5];
    private double[] currentY = new double[5];
    private double[] delX = new double[5];
    private double[] delY = new double[5];
    private int numFingers = 0;
    private long initialT, finalT, currentT;
    private long prevInitialT, prevFinalT;
    private int swipeSlopeIntolerance = 3;
    private int swipeXDistanceThreshold;
    private int swipeYDistanceThreshold;
    private long doubleTapMaxDelayMillis;
    private long doubleTapMaxDownMillis;

    public AskGestureAnalyzer(int swipeXDistanceThreshold, int swipeYDistanceThreshold) {
        this(3, 500, 100, swipeXDistanceThreshold, swipeYDistanceThreshold);
    }

    public AskGestureAnalyzer(int swipeSlopeIntolerance, int doubleTapMaxDelayMillis, int doubleTapMaxDownMillis,
                              int swipeXDistanceThreshold, int swipeYDistanceThreshold) {
        this.swipeSlopeIntolerance = swipeSlopeIntolerance;
        this.doubleTapMaxDownMillis = doubleTapMaxDownMillis;
        this.doubleTapMaxDelayMillis = doubleTapMaxDelayMillis;

        this.swipeXDistanceThreshold = swipeXDistanceThreshold;
        this.swipeYDistanceThreshold = swipeYDistanceThreshold;
    }

    public void trackGesture(MotionEvent ev) {
        int n = ev.getPointerCount();
        for (int i = 0; i < n; i++) {
            initialX[i] = ev.getX(i);
            initialY[i] = ev.getY(i);
        }
        numFingers = n;
        initialT = SystemClock.uptimeMillis();
    }

    public void resetGestureTracking() {
        numFingers = 0;
        prevFinalT = SystemClock.uptimeMillis();
        prevInitialT = initialT;
    }

    @Nullable
    public GestureType getGesture(MotionEvent ev) {
        double averageXDistance = 0.0;
        double averageYDistance = 0.0;
        double averageDistance = 0.0;

        if (numFingers > ev.getPointerCount())
            return null;

        for (int i = 0; i < numFingers; i++) {
            finalX[i] = ev.getX(i);
            finalY[i] = ev.getY(i);
            delX[i] = finalX[i] - initialX[i];
            delY[i] = finalY[i] - initialY[i];

            averageXDistance += Math.abs(finalX[i] - initialX[i]);
            averageYDistance += Math.abs(finalY[i] - initialY[i]);
        }
        averageXDistance /= numFingers;
        averageYDistance /= numFingers;
        if (averageXDistance < swipeXDistanceThreshold && averageYDistance < swipeYDistanceThreshold)
            return null;

        finalT = SystemClock.uptimeMillis();
        GestureType gt = new GestureType();
        gt.setGestureFlag(calcGesture());
        gt.setGestureDuration(finalT - initialT);
        gt.setGestureDistance(averageDistance);

        return gt;
    }

    @GestureTypeFlag
    public int getOngoingGesture(MotionEvent ev) {
        for (int i = 0; i < numFingers; i++) {
            currentX[i] = ev.getX(i);
            currentY[i] = ev.getY(i);
            delX[i] = finalX[i] - initialX[i];
            delY[i] = finalY[i] - initialY[i];
        }
        currentT = SystemClock.uptimeMillis();
        return calcGesture();
    }

    @GestureTypeFlag
    private int calcGesture() {
        if (isDoubleTap()) {
            return DOUBLE_TAP_1;
        }

        if (numFingers == 1) {
            if ((-(delY[0])) > (swipeSlopeIntolerance * (Math.abs(delX[0])))) {
                return SWIPE_1_UP;
            }

            if (((delY[0])) > (swipeSlopeIntolerance * (Math.abs(delX[0])))) {
                return SWIPE_1_DOWN;
            }

            if ((-(delX[0])) > (swipeSlopeIntolerance * (Math.abs(delY[0])))) {
                return SWIPE_1_LEFT;
            }

            if (((delX[0])) > (swipeSlopeIntolerance * (Math.abs(delY[0])))) {
                return SWIPE_1_RIGHT;
            }
        }
        if (numFingers == 2) {
            if (((-delY[0]) > (swipeSlopeIntolerance * Math.abs(delX[0]))) && ((-delY[1]) > (swipeSlopeIntolerance * Math.abs(delX[1])))) {
                return SWIPE_2_UP;
            }
            if (((delY[0]) > (swipeSlopeIntolerance * Math.abs(delX[0]))) && ((delY[1]) > (swipeSlopeIntolerance * Math.abs(delX[1])))) {
                return SWIPE_2_DOWN;
            }
            if (((-delX[0]) > (swipeSlopeIntolerance * Math.abs(delY[0]))) && ((-delX[1]) > (swipeSlopeIntolerance * Math.abs(delY[1])))) {
                return SWIPE_2_LEFT;
            }
            if (((delX[0]) > (swipeSlopeIntolerance * Math.abs(delY[0]))) && ((delX[1]) > (swipeSlopeIntolerance * Math.abs(delY[1])))) {
                return SWIPE_2_RIGHT;
            }
            if (finalFingerDist(0, 1) > 2 * (initialFingerDist(0, 1))) {
                return UNPINCH_2;
            }
            if (finalFingerDist(0, 1) < 0.5 * (initialFingerDist(0, 1))) {
                return PINCH_2;
            }
        }
        if (numFingers == 3) {
            if (((-delY[0]) > (swipeSlopeIntolerance * Math.abs(delX[0])))
                    && ((-delY[1]) > (swipeSlopeIntolerance * Math.abs(delX[1])))
                    && ((-delY[2]) > (swipeSlopeIntolerance * Math.abs(delX[2])))) {
                return SWIPE_3_UP;
            }
            if (((delY[0]) > (swipeSlopeIntolerance * Math.abs(delX[0])))
                    && ((delY[1]) > (swipeSlopeIntolerance * Math.abs(delX[1])))
                    && ((delY[2]) > (swipeSlopeIntolerance * Math.abs(delX[2])))) {
                return SWIPE_3_DOWN;
            }
            if (((-delX[0]) > (swipeSlopeIntolerance * Math.abs(delY[0])))
                    && ((-delX[1]) > (swipeSlopeIntolerance * Math.abs(delY[1])))
                    && ((-delX[2]) > (swipeSlopeIntolerance * Math.abs(delY[2])))) {
                return SWIPE_3_LEFT;
            }
            if (((delX[0]) > (swipeSlopeIntolerance * Math.abs(delY[0])))
                    && ((delX[1]) > (swipeSlopeIntolerance * Math.abs(delY[1])))
                    && ((delX[2]) > (swipeSlopeIntolerance * Math.abs(delY[2])))) {
                return SWIPE_3_RIGHT;
            }

            if ((finalFingerDist(0, 1) > 1.75 * (initialFingerDist(0, 1)))
                    && (finalFingerDist(1, 2) > 1.75 * (initialFingerDist(1, 2)))
                    && (finalFingerDist(2, 0) > 1.75 * (initialFingerDist(2, 0)))) {
                return UNPINCH_3;
            }
            if ((finalFingerDist(0, 1) < 0.66 * (initialFingerDist(0, 1)))
                    && (finalFingerDist(1, 2) < 0.66 * (initialFingerDist(1, 2)))
                    && (finalFingerDist(2, 0) < 0.66 * (initialFingerDist(2, 0)))) {
                return PINCH_3;
            }

        }
        if (numFingers == 4) {
            if (((-delY[0]) > (swipeSlopeIntolerance * Math.abs(delX[0])))
                    && ((-delY[1]) > (swipeSlopeIntolerance * Math.abs(delX[1])))
                    && ((-delY[2]) > (swipeSlopeIntolerance * Math.abs(delX[2])))
                    && ((-delY[3]) > (swipeSlopeIntolerance * Math.abs(delX[3])))) {
                return SWIPE_4_UP;
            }
            if (((delY[0]) > (swipeSlopeIntolerance * Math.abs(delX[0])))
                    && ((delY[1]) > (swipeSlopeIntolerance * Math.abs(delX[1])))
                    && ((delY[2]) > (swipeSlopeIntolerance * Math.abs(delX[2])))
                    && ((delY[3]) > (swipeSlopeIntolerance * Math.abs(delX[3])))) {
                return SWIPE_4_DOWN;
            }
            if (((-delX[0]) > (swipeSlopeIntolerance * Math.abs(delY[0])))
                    && ((-delX[1]) > (swipeSlopeIntolerance * Math.abs(delY[1])))
                    && ((-delX[2]) > (swipeSlopeIntolerance * Math.abs(delY[2])))
                    && ((-delX[3]) > (swipeSlopeIntolerance * Math.abs(delY[3])))) {
                return SWIPE_4_LEFT;
            }
            if (((delX[0]) > (swipeSlopeIntolerance * Math.abs(delY[0])))
                    && ((delX[1]) > (swipeSlopeIntolerance * Math.abs(delY[1])))
                    && ((delX[2]) > (swipeSlopeIntolerance * Math.abs(delY[2])))
                    && ((delX[3]) > (swipeSlopeIntolerance * Math.abs(delY[3])))) {
                return SWIPE_4_RIGHT;
            }
            if ((finalFingerDist(0, 1) > 1.5 * (initialFingerDist(0, 1)))
                    && (finalFingerDist(1, 2) > 1.5 * (initialFingerDist(1, 2)))
                    && (finalFingerDist(2, 3) > 1.5 * (initialFingerDist(2, 3)))
                    && (finalFingerDist(3, 0) > 1.5 * (initialFingerDist(3, 0)))) {
                return UNPINCH_4;
            }
            if ((finalFingerDist(0, 1) < 0.8 * (initialFingerDist(0, 1)))
                    && (finalFingerDist(1, 2) < 0.8 * (initialFingerDist(1, 2)))
                    && (finalFingerDist(2, 3) < 0.8 * (initialFingerDist(2, 3)))
                    && (finalFingerDist(3, 0) < 0.8 * (initialFingerDist(3, 0)))) {
                return PINCH_4;
            }
        }
        return NONE;
    }

    private double initialFingerDist(int fingerNum1, int fingerNum2) {

        return Math.sqrt(Math.pow((initialX[fingerNum1] - initialX[fingerNum2]), 2)
                + Math.pow((initialY[fingerNum1] - initialY[fingerNum2]), 2));
    }

    private double finalFingerDist(int fingerNum1, int fingerNum2) {

        return Math.sqrt(Math.pow((finalX[fingerNum1] - finalX[fingerNum2]), 2)
                + Math.pow((finalY[fingerNum1] - finalY[fingerNum2]), 2));
    }

    private boolean isDoubleTap() {
        return (initialT - prevFinalT < doubleTapMaxDelayMillis && finalT - initialT < doubleTapMaxDownMillis && prevFinalT - prevInitialT < doubleTapMaxDownMillis);
    }

    @IntDef({NONE,
            SWIPE_1_UP, SWIPE_1_DOWN, SWIPE_1_LEFT, SWIPE_1_RIGHT,
            SWIPE_2_UP, SWIPE_2_DOWN, SWIPE_2_LEFT, SWIPE_2_RIGHT, PINCH_2, UNPINCH_2,
            SWIPE_3_UP, SWIPE_3_DOWN, SWIPE_3_LEFT, SWIPE_3_RIGHT, PINCH_3, UNPINCH_3,
            SWIPE_4_UP, SWIPE_4_DOWN, SWIPE_4_LEFT, SWIPE_4_RIGHT, PINCH_4, UNPINCH_4,
            DOUBLE_TAP_1})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GestureTypeFlag {
    }

    public static class GestureType {
        @GestureTypeFlag
        private int gestureFlag;
        private long gestureDuration;

        private double gestureDistance;

        public long getGestureDuration() {
            return gestureDuration;
        }

        public void setGestureDuration(long gestureDuration) {
            this.gestureDuration = gestureDuration;
        }

        @GestureTypeFlag
        public int getGestureFlag() {
            return gestureFlag;
        }

        private void setGestureFlag(@GestureTypeFlag int gestureFlag) {
            this.gestureFlag = gestureFlag;
        }


        public double getGestureDistance() {
            return gestureDistance;
        }

        private void setGestureDistance(double gestureDistance) {
            this.gestureDistance = gestureDistance;
        }
    }


}
