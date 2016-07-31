package com.anysoftkeyboard.keyboards.views;


import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
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

    private final int swipeSlopeIntolerance;
    private final int swipeXDistanceThreshold;
    private final int swipeYDistanceThreshold;
    private final long doubleTapMaxDelayMillis;
    private final long doubleTapMaxDownMillis;
    private final GestureType mReusableGestureType = new GestureType();
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

    public void startPointerTracking(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_POINTER_DOWN) throw new IllegalArgumentException("Should only given DOWN actions!");

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

    @NonNull
    public GestureType getFinalGesture(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_POINTER_UP) throw new IllegalArgumentException("Should only given UP actions!");

        double averageXDistance = 0.0;
        double averageYDistance = 0.0;
        double averageDistance = 0.0;

        mReusableGestureType.reset();

        //returning NONE until last finger is released
        if (ev.getPointerCount() != 1)
            return mReusableGestureType;

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
        if (averageXDistance < swipeXDistanceThreshold && averageYDistance < swipeYDistanceThreshold) {
            mReusableGestureType.setGestureFlag(isDoubleTap()? DOUBLE_TAP_1 : NONE);
            return mReusableGestureType;
        }

        finalT = SystemClock.uptimeMillis();
        mReusableGestureType.setGestureFlag(calcGesture());
        mReusableGestureType.setGestureDuration(finalT - initialT);
        mReusableGestureType.setGestureDistance(averageDistance);

        return mReusableGestureType;
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
        //calling this method means that the movement delta is larger than swipe-distance-threshold
        switch (numFingers) {
            case 1:
                return calcGestureForFingersCount(numFingers, SWIPE_1_UP, SWIPE_1_DOWN, SWIPE_1_LEFT, SWIPE_1_RIGHT, NONE, NONE);
            case 2:
                return calcGestureForFingersCount(numFingers, SWIPE_2_UP, SWIPE_2_DOWN, SWIPE_2_LEFT, SWIPE_2_RIGHT, UNPINCH_2, PINCH_2);
            case 3:
                return calcGestureForFingersCount(numFingers, SWIPE_3_UP, SWIPE_3_DOWN, SWIPE_2_LEFT, SWIPE_3_RIGHT, UNPINCH_3, PINCH_3);
            case 4:
                return calcGestureForFingersCount(numFingers, SWIPE_4_UP, SWIPE_4_DOWN, SWIPE_4_LEFT, SWIPE_4_RIGHT, UNPINCH_4, PINCH_4);
            default:
                return NONE;
        }
    }

    @GestureTypeFlag
    private int calcGestureForFingersCount(final int numFingers, @GestureTypeFlag final int swipeUpFlag, @GestureTypeFlag final int swipeDownFlag, @GestureTypeFlag final int swipeLeftFlag, @GestureTypeFlag final int swipeRightFlag, @GestureTypeFlag final int unpinchFlag, @GestureTypeFlag final int pinchFlag) {
        //to be a gesture a specific direction, the deltas need to be swipeSlopeIntolerance times larger
        if (swipeLeftDeltas(numFingers)) return swipeLeftFlag;
        if (swipeRightDeltas(numFingers)) return swipeRightFlag;
        if (swipeUpDeltas(numFingers)) return swipeUpFlag;
        if (swipeDownDeltas(numFingers)) return swipeDownFlag;

        if (numFingers > 1) {
            //also checking for pinch:
            //comparing all pairs of fingers -
            // if the distance is twice larger it means a PINCH
            // if the distance is half the distance it means a UN-PINCH
            final double unpinchThreshold = ((double) numFingers) / ((double) numFingers - 1);
            final double pinchThreshold = ((double) numFingers - 1) / ((double) numFingers);
            for (int firstFinger = 0; firstFinger < numFingers; firstFinger++) {
                for (int secondFinger = firstFinger + 1; secondFinger < numFingers; secondFinger++) {
                    if (finalFingerDist(firstFinger, secondFinger) > unpinchThreshold * (initialFingerDist(firstFinger, secondFinger))) {
                        return unpinchFlag;
                    }
                    if (finalFingerDist(firstFinger, secondFinger) < pinchThreshold * (initialFingerDist(firstFinger, secondFinger))) {
                        return pinchFlag;
                    }
                }
            }
        }

        return NONE;
    }

    private boolean swipeLeftDeltas(int numFingers) {
        for (int fingerIndex = 0; fingerIndex < numFingers; fingerIndex++) {
            if ((-delX[fingerIndex]) < (swipeSlopeIntolerance * Math.abs(delY[fingerIndex])))
                return false;
        }
        return true;
    }

    private boolean swipeRightDeltas(int numFingers) {
        for (int fingerIndex = 0; fingerIndex < numFingers; fingerIndex++) {
            if ((delX[fingerIndex]) < (swipeSlopeIntolerance * Math.abs(delY[fingerIndex])))
                return false;
        }
        return true;
    }

    private boolean swipeUpDeltas(int numFingers) {
        for (int fingerIndex = 0; fingerIndex < numFingers; fingerIndex++) {
            if ((-delY[fingerIndex]) < (swipeSlopeIntolerance * Math.abs(delX[fingerIndex])))
                return false;
        }
        return true;
    }

    private boolean swipeDownDeltas(int numFingers) {
        for (int fingerIndex = 0; fingerIndex < numFingers; fingerIndex++) {
            if ((delY[fingerIndex]) < (swipeSlopeIntolerance * Math.abs(delX[fingerIndex])))
                return false;
        }
        return true;
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

        private void setGestureDuration(long gestureDuration) {
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

        private void reset() {
            gestureFlag = NONE;
            gestureDuration = 0;
            gestureDistance = 0;
        }
    }


}
