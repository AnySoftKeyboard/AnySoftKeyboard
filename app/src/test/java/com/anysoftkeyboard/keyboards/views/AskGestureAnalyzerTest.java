package com.anysoftkeyboard.keyboards.views;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.view.MotionEvent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowMotionEvent;
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
    private MotionEvent mMotionEvent;
    private ShadowMotionEvent mShadowMotionEvent;

    @Before
    public void setup() {
        ShadowSystemClock.sleep(123456/*starting at some future time*/);
        mAskGestureAnalyzerUnderTest = new AskGestureAnalyzer(SLOPE, DOUBLE_TAP_DELAY, DOUBLE_TAP_MAX_DOWN, SWIPE_X_DISTANCE, SWIPE_Y_DISTANCE);
        resetMotionEventField();
    }

    private void resetMotionEventField() {
        mMotionEvent = MotionEvent.obtain(SystemClock.elapsedRealtime(), SystemClock.elapsedRealtime(), MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mShadowMotionEvent = Shadows.shadowOf(mMotionEvent);
    }

    @Test
    public void testResetGestureTracking() throws Exception {
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFinalPointerGestureShouldNotAcceptMove() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_MOVE);
        mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFinalPointerGestureShouldNotAcceptDown() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFinalPointerGestureShouldNotAcceptDown2() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_POINTER_DOWN);
        mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFinalPointerGestureShouldNotAcceptCancel() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_CANCEL);
        mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPointerTrackingShouldNotAcceptMove() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_MOVE);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPointerTrackingShouldNotAcceptUp() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPointerTrackingShouldNotAcceptUp2() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_POINTER_UP);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartPointerTrackingShouldNotAcceptCancel() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_CANCEL);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
    }

    @Test
    public void testNothingHappens() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mShadowMotionEvent.setLocation(1, 0);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        mShadowMotionEvent.setLocation(1, 1);
        Assert.assertEquals(AskGestureAnalyzer.NONE, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());
    }

    @Test
    public void testGetGestureDoubleTap() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mShadowMotionEvent.setLocation(1, 0);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
        ShadowSystemClock.sleep(1);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        Assert.assertEquals(AskGestureAnalyzer.NONE, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());
        ShadowSystemClock.sleep(1);
        mAskGestureAnalyzerUnderTest.resetGestureTracking();
        ShadowSystemClock.sleep(DOUBLE_TAP_DELAY - 50);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
        ShadowSystemClock.sleep(1);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        Assert.assertEquals(AskGestureAnalyzer.DOUBLE_TAP_1, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());
    }

    @Test
    public void testGetGestureNotDoubleTap() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
        ShadowSystemClock.sleep(1);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        Assert.assertEquals(AskGestureAnalyzer.NONE, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());
        ShadowSystemClock.sleep(1);
        mAskGestureAnalyzerUnderTest.resetGestureTracking();
        ShadowSystemClock.sleep(DOUBLE_TAP_DELAY + 10);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
        ShadowSystemClock.sleep(1);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        Assert.assertEquals(AskGestureAnalyzer.NONE, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());
    }

    @Test
    public void testGetGestureSwipeLeftOneFinger() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
        ShadowSystemClock.sleep(1);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        mShadowMotionEvent.setLocation(-SWIPE_X_DISTANCE - 10, 0);
        Assert.assertEquals(AskGestureAnalyzer.SWIPE_1_LEFT, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());
    }

    @Test
    public void testGetGestureSwipeRightOneFinger() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
        ShadowSystemClock.sleep(1);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        mShadowMotionEvent.setLocation(SWIPE_X_DISTANCE + 10, 0);
        Assert.assertEquals(AskGestureAnalyzer.SWIPE_1_RIGHT, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());

    }

    @Test
    public void testGetGestureSwipeUpOneFinger() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
        ShadowSystemClock.sleep(1);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        mShadowMotionEvent.setLocation(0, -SWIPE_Y_DISTANCE - 10);
        Assert.assertEquals(AskGestureAnalyzer.SWIPE_1_UP, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());
    }

    @Test
    public void testGetGestureSwipeDownOneFinger() throws Exception {
        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
        ShadowSystemClock.sleep(1);
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        mShadowMotionEvent.setLocation(0, SWIPE_Y_DISTANCE + 10);
        Assert.assertEquals(AskGestureAnalyzer.SWIPE_1_DOWN, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());
    }

    @Test
    public void testGetGestureSwipeLeftTwoFinger() throws Exception {
        /*mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mShadowMotionEvent.setLocation(0, 0);
        mShadowMotionEvent.setPointerIndex(0);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);

        mShadowMotionEvent.setAction(MotionEvent.ACTION_DOWN);
        mShadowMotionEvent.setPointer2(1, 0);
        mShadowMotionEvent.setPointerIndex(1);
        mAskGestureAnalyzerUnderTest.startPointerTracking(mMotionEvent);
        ShadowSystemClock.sleep(1);


        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        mShadowMotionEvent.setLocation(-SWIPE_X_DISTANCE - 10, 0);
        mShadowMotionEvent.setPointerIndex(0);
        Assert.assertEquals(AskGestureAnalyzer.NONE, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());

        resetMotionEventField();
        mShadowMotionEvent.setAction(MotionEvent.ACTION_UP);
        mShadowMotionEvent.setLocation(-SWIPE_X_DISTANCE - 10, 10);
        Assert.assertEquals(AskGestureAnalyzer.SWIPE_2_LEFT, mAskGestureAnalyzerUnderTest.getFinalGesture(mMotionEvent).getGestureFlag());*/
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
/*
    private static MotionEvent createMotionEvent(int action, int pointerIndex, int totalPointersCount, int x, int y) {
        final int actionWithPointer = action + (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
        MotionEvent event =
        ShadowMotionEvent shadowMotionEvent = Shadows.shadowOf(event);
        shadowMotionEvent.setPointerIndex(pointerIndex);
        if (totalPointersCount > 1) {
            shadowMotionEvent.setAction();
            if (pointerIndex == 1) {
                shadowMotionEvent.setPointer2(x, y);
            } else {
                shadowMotionEvent.setPointer2(0, 0);
            }
        }
        return event;
    }
    */
}