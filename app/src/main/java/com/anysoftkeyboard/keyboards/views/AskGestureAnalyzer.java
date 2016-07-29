package com.anysoftkeyboard.keyboards.views;

import android.os.SystemClock;
import android.view.MotionEvent;
public class AskGestureAnalyzer {
    public static final String SWIPE_ACTION_CLOSE = "close";
    public static final String SWIPE_ACTION_SETTINGS = "settings";
    public static final String SWIPE_ACTION_SUGGESTIONS = "suggestions";
    public static final String SWIPE_ACTION_LANG_NEXT = "lang_next";
    public static final String SWIPE_ACTION_LANG_PREV = "lang_prev";
    public static final String SWIPE_ACTION_DEBUG_AUTO_PLAY = "debug_auto_play";
    public static final String SWIPE_ACTION_FULL_MODE = "full_mode";
    public static final String SWIPE_ACTION_EXTENSION = "extension";
    public static final String SWIPE_ACTION_HEIGHT_UP = "height_up";
    public static final String SWIPE_ACTION_HEIGHT_DOWN = "height_down";
    public static final String SWIPE_ACTION_TOGGLE_SHIFT = "toggle_shift";
    public static final String SWIPE_ACTION_NEXT_KEYBOARD = "next_keyboard";
    public static final String SWIPE_ACTION_NEXT_THEME = "next_theme";
    public static final String SWIPE_ACTION_PREV_THEME = "prev_theme";
    public static final String SWIPE_ACTION_DELETE_LAST= "delete_last";

    public static final String SWIPE_1_UP_NAME = "SWIPE_1_UP";
    public static final String SWIPE_1_DOWN_NAME = "SWIPE_1_DOWN";
    public static final String SWIPE_1_LEFT_NAME = "SWIPE_1_LEFT";
    public static final String SWIPE_1_RIGHT_NAME = "SWIPE_1_RIGHT";
    public static final String SWIPE_2_UP_NAME = "SWIPE_2_UP";
    public static final String SWIPE_2_DOWN_NAME = "SWIPE_2_DOWN";
    public static final String SWIPE_2_LEFT_NAME = "SWIPE_2_LEFT";
    public static final String SWIPE_2_RIGHT_NAME = "SWIPE_2_RIGHT";

    public static final boolean DEBUG = true;
    // Finished gestures flags
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

    //Ongoing gesture flags
    public static final int SWIPING_1_UP = 101;
    public static final int SWIPING_1_DOWN = 102;
    public static final int SWIPING_1_LEFT = 103;
    public static final int SWIPING_1_RIGHT = 104;
    public static final int SWIPING_2_UP = 201;
    public static final int SWIPING_2_DOWN = 202;
    public static final int SWIPING_2_LEFT = 203;
    public static final int SWIPING_2_RIGHT = 204;
    public static final int PINCHING = 205;
    public static final int UNPINCHING = 206;
    private static final String TAG = "GestureAnalyser";
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

    public void untrackGesture() {
        numFingers = 0;
        prevFinalT = SystemClock.uptimeMillis();
        prevInitialT = initialT;
    }

    public GestureType getGesture(MotionEvent ev) {
        double averageXDistance = 0.0;
        double averageYDistance = 0.0;
        double averageDistance = 0.0;

      //  System.out.println("get gesture");
        if (numFingers > ev.getPointerCount())
            return null;

        for (int i = 0; i < numFingers; i++) {
            finalX[i] = ev.getX(i);
            finalY[i] = ev.getY(i);
            delX[i] = finalX[i] - initialX[i];
            delY[i] = finalY[i] - initialY[i];

            //averageDistance += Math.sqrt(Math.pow(finalX[i] - initialX[i], 2) + Math.pow(finalY[i] - initialY[i], 2));
            averageXDistance += Math.abs(finalX[i] - initialX[i]);
            averageYDistance += Math.abs(finalY[i] - initialY[i]);
        }
        //averageDistance /= numFingers;
        averageXDistance /= numFingers;
        averageYDistance /= numFingers;
       // System.out.println("averageXDistance "+ averageXDistance);
        //System.out.println("averageYDistance "+ averageYDistance);
        if (averageXDistance < swipeXDistanceThreshold && averageYDistance < swipeYDistanceThreshold)
            return null;

        finalT = SystemClock.uptimeMillis();
        GestureType gt = new GestureType();
        int gestureType = calcGesture();
        //System.out.println("gestureType "+ gestureType);
        gt.setGestureFlag(calcGesture());
        gt.setGestureDuration(finalT - initialT);
        gt.setGestureDistance(averageDistance);

        return gt;
    }

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
            if (finalFingDist(0, 1) > 2 * (initialFingDist(0, 1))) {
                return UNPINCH_2;
            }
            if (finalFingDist(0, 1) < 0.5 * (initialFingDist(0, 1))) {
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

            if ((finalFingDist(0,1) > 1.75*(initialFingDist(0,1)))
                    && (finalFingDist(1,2) > 1.75*(initialFingDist(1,2)))
                    && (finalFingDist(2,0) > 1.75*(initialFingDist(2,0))) ) {
                return UNPINCH_3;
            }
            if ((finalFingDist(0,1) < 0.66*(initialFingDist(0,1)))
                    && (finalFingDist(1,2) < 0.66*(initialFingDist(1,2)))
                    && (finalFingDist(2,0) < 0.66*(initialFingDist(2,0))) ) {
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
            if ((finalFingDist(0,1) > 1.5*(initialFingDist(0,1)))
                    && (finalFingDist(1,2) > 1.5*(initialFingDist(1,2)))
                    && (finalFingDist(2,3) > 1.5*(initialFingDist(2,3)))
                    && (finalFingDist(3,0) > 1.5*(initialFingDist(3,0))) ) {
                return UNPINCH_4;
            }
            if ((finalFingDist(0,1) < 0.8*(initialFingDist(0,1)))
                    && (finalFingDist(1,2) < 0.8*(initialFingDist(1,2)))
                    && (finalFingDist(2,3) < 0.8*(initialFingDist(2,3)))
                    && (finalFingDist(3,0) < 0.8*(initialFingDist(3,0))) ) {
                return PINCH_4;
            }
        }
        return 0;
    }

    private double initialFingDist(int fingNum1, int fingNum2) {

        return Math.sqrt(Math.pow((initialX[fingNum1] - initialX[fingNum2]), 2)
                + Math.pow((initialY[fingNum1] - initialY[fingNum2]), 2));
    }

    private double finalFingDist(int fingNum1, int fingNum2) {

        return Math.sqrt(Math.pow((finalX[fingNum1] - finalX[fingNum2]), 2)
                + Math.pow((finalY[fingNum1] - finalY[fingNum2]), 2));
    }

    public boolean isDoubleTap() {
        if (initialT - prevFinalT < doubleTapMaxDelayMillis && finalT - initialT < doubleTapMaxDownMillis && prevFinalT - prevInitialT < doubleTapMaxDownMillis) {
            return true;
        } else {
            return false;
        }
    }

    public class GestureType {
        private int gestureFlag;
        private long gestureDuration;

        private double gestureDistance;

        public long getGestureDuration() {
            return gestureDuration;
        }

        public void setGestureDuration(long gestureDuration) {
            this.gestureDuration = gestureDuration;
        }


        public int getGestureFlag() {
            return gestureFlag;
        }

        public void setGestureFlag(int gestureFlag) {
            this.gestureFlag = gestureFlag;
        }


        public double getGestureDistance() {
            return gestureDistance;
        }

        public void setGestureDistance(double gestureDistance) {
            this.gestureDistance = gestureDistance;
        }
    }


}
