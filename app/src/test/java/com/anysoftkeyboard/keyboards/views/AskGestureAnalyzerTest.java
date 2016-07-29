package com.anysoftkeyboard.keyboards.views;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowSystemClock;

@TargetApi(Build.VERSION_CODES.FROYO)
@RunWith(RobolectricTestRunner.class)
public class AskGestureAnalyzerTest {

    private static final int SLOPE = 3;
    private static final int DOUBLE_TAP_DELAY = 500;
    private static final int DOUBLE_TAP_MAX_DOWN = 100;
    private static final int SWIPE_X_DISTANCE = 200;
    private static final int SWIPE_Y_DISTANCE = 150;

    private AskGestureAnalyzer mAskGestureAnalyzerUnderTest;

    @Before
    public void setup() {
        ShadowSystemClock.sleep(123456/*starting at some future time*/);
        mAskGestureAnalyzerUnderTest = new AskGestureAnalyzer(SLOPE, DOUBLE_TAP_DELAY, DOUBLE_TAP_MAX_DOWN, SWIPE_X_DISTANCE, SWIPE_Y_DISTANCE);

        //some sanity checks
        Assert.assertEquals(0, MotionEventCompat.getActionIndex(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0)));
        Assert.assertEquals(0, MotionEventCompat.getActionIndex(createMotionEvent(MotionEvent.ACTION_MOVE, 0, 0, 0)));
        Assert.assertEquals(0, MotionEventCompat.getActionIndex(createMotionEvent(MotionEvent.ACTION_UP, 0, 0, 0)));

        Assert.assertEquals(1, MotionEventCompat.getActionIndex(createMotionEvent(MotionEvent.ACTION_DOWN, 1, 0, 0)));
        Assert.assertEquals(1, MotionEventCompat.getActionIndex(createMotionEvent(MotionEvent.ACTION_MOVE, 1, 0, 0)));
        Assert.assertEquals(1, MotionEventCompat.getActionIndex(createMotionEvent(MotionEvent.ACTION_UP, 1, 0, 0)));

        Assert.assertEquals(MotionEvent.ACTION_DOWN, MotionEventCompat.getActionMasked(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0)));
        Assert.assertEquals(MotionEvent.ACTION_MOVE, MotionEventCompat.getActionMasked(createMotionEvent(MotionEvent.ACTION_MOVE, 0, 0, 0)));
        Assert.assertEquals(MotionEvent.ACTION_UP, MotionEventCompat.getActionMasked(createMotionEvent(MotionEvent.ACTION_UP, 0, 0, 0)));

        Assert.assertEquals(MotionEvent.ACTION_DOWN, MotionEventCompat.getActionMasked(createMotionEvent(MotionEvent.ACTION_DOWN, 1, 0, 0)));
        Assert.assertEquals(MotionEvent.ACTION_MOVE, MotionEventCompat.getActionMasked(createMotionEvent(MotionEvent.ACTION_MOVE, 1, 0, 0)));
        Assert.assertEquals(MotionEvent.ACTION_UP, MotionEventCompat.getActionMasked(createMotionEvent(MotionEvent.ACTION_UP, 1, 0, 0)));
    }

    @Test
    public void testResetGestureTracking() throws Exception {
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFinalPointerGestureShouldNotAcceptMove() throws Exception {
        mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_MOVE, 0, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFinalPointerGestureShouldNotAcceptDown() throws Exception {
        mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFinalPointerGestureShouldNotAcceptDown2() throws Exception {
        mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_POINTER_DOWN, 0, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFinalPointerGestureShouldNotAcceptCancel() throws Exception {
        mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPointerTrackingShouldNotAcceptMove() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_MOVE, 0, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPointerTrackingShouldNotAcceptUp() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_UP, 0, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPointerTrackingShouldNotAcceptUp2() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_POINTER_UP, 0, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPointerTrackingShouldNotAcceptCancel() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    @Test
    public void testNothingHappens() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0));
        Assert.assertEquals(AskGestureAnalyzer.NONE, mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_UP, 0, 1, 0)).getGestureFlag());
    }

    @Test
    public void testGetGestureDoubleTap() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0));
        ShadowSystemClock.sleep(1);
        Assert.assertEquals(AskGestureAnalyzer.NONE,mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_UP, 0, 1, 0)).getGestureFlag());
        ShadowSystemClock.sleep(1);
        mAskGestureAnalyzerUnderTest.resetGestureTracking();
        ShadowSystemClock.sleep(DOUBLE_TAP_DELAY - 50);
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0));
        ShadowSystemClock.sleep(1);
        Assert.assertEquals(AskGestureAnalyzer.DOUBLE_TAP_1, mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_UP, 0, 0, 1)).getGestureFlag());
    }

    @Test
    public void testGetGestureNotDoubleTap() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0));
        ShadowSystemClock.sleep(1);
        Assert.assertEquals(AskGestureAnalyzer.NONE, mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_UP, 0, 1, 0)).getGestureFlag());
        ShadowSystemClock.sleep(1);
        mAskGestureAnalyzerUnderTest.resetGestureTracking();
        ShadowSystemClock.sleep(DOUBLE_TAP_DELAY + 10);
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0));
        ShadowSystemClock.sleep(1);
        Assert.assertEquals(AskGestureAnalyzer.NONE, mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_UP, 0, 0, 1)).getGestureFlag());
    }

    @Test
    public void testGetGestureSwipeLeftOneFinger() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0));
        ShadowSystemClock.sleep(1);
        Assert.assertEquals(AskGestureAnalyzer.SWIPE_1_LEFT, mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_UP, 0, -SWIPE_X_DISTANCE-10, 0)).getGestureFlag());
    }

    @Test
    public void testGetGestureSwipeRightOneFinger() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0));
        ShadowSystemClock.sleep(1);
        Assert.assertEquals(AskGestureAnalyzer.SWIPE_1_RIGHT, mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_UP, 0, SWIPE_X_DISTANCE+10, 0)).getGestureFlag());

    }

    @Test
    public void testGetGestureSwipeUpOneFinger() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0));
        ShadowSystemClock.sleep(1);
        Assert.assertEquals(AskGestureAnalyzer.SWIPE_1_UP, mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_UP, 0, 0, -SWIPE_Y_DISTANCE-10)).getGestureFlag());
    }

    @Test
    public void testGetGestureSwipeDownOneFinger() throws Exception {
        mAskGestureAnalyzerUnderTest.startPointerTracking(createMotionEvent(MotionEvent.ACTION_DOWN, 0, 0, 0));
        ShadowSystemClock.sleep(1);
        Assert.assertEquals(AskGestureAnalyzer.SWIPE_1_DOWN, mAskGestureAnalyzerUnderTest.getFinalGesture(createMotionEvent(MotionEvent.ACTION_UP, 0, 0, SWIPE_Y_DISTANCE+10)).getGestureFlag());
    }

    @Test
    public void testGetGestureSwipeLeftTwoFinger() throws Exception {

    }

    @Test
    public void testGetGestureSwipeRightTwoFinger() throws Exception {

    }

    @Test
    public void testGetGestureSwipeUpTwoFinger() throws Exception {

    }

    @Test
    public void testGetGestureSwipeDownTwoFinger() throws Exception {

    }

    @Test
    public void testGetGestureTwoFingerNoneDirection() throws Exception {

    }

    private static MotionEvent createMotionEvent(int action, int pointerIndex, int x, int y) {
        final int actionWithPointer = action + (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
        return MotionEvent.obtain(SystemClock.elapsedRealtime(), SystemClock.elapsedRealtime(), actionWithPointer, x, y, 0);
    }
}