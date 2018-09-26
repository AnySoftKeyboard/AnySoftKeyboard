/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.keyboards.views;

import android.view.MotionEvent;

import com.anysoftkeyboard.devicespecific.AskOnGestureListener;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.BuildConfig;

final class AskGestureEventsListener implements
        AskOnGestureListener {

    private static final String TAG = "AskGestureEventsListener";

    private final AnyKeyboardViewBase mKeyboardView;

    public AskGestureEventsListener(AnyKeyboardViewBase keyboardView) {
        mKeyboardView = keyboardView;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //in two fingers state we might still want to report a scroll, if BOTH pointers are moving in the same direction
        if (mKeyboardView.isAtTwoFingersState() && !pointersMovingInTheSameDirection(e1, e2)) {
            return false;
        }

        final float scrollXDistance = Math.abs(e2.getX() - e1.getX());
        final float scrollYDistance = Math.abs(e2.getY() - e1.getY());
        final float totalScrollTime = ((float) (e2.getEventTime() - e1.getEventTime()));
        //velocity is per second, not per millisecond.
        final float velocityX = 1000 * Math.abs(scrollXDistance / totalScrollTime);
        final float velocityY = 1000 * Math.abs(scrollYDistance / totalScrollTime);
        Logger.d(TAG, "onScroll scrollX %f, scrollY %f, velocityX %f, velocityY %f", scrollXDistance, scrollYDistance, velocityX, velocityY);
        if (velocityX > velocityY) {
            Logger.v(TAG, "Scrolling on X axis");
            if (velocityX > mKeyboardView.mSwipeVelocityThreshold) {
                Logger.v(TAG, "Scroll broke the velocity barrier");
                final int swipeXDistance = mKeyboardView.isFirstDownEventInsideSpaceBar() ? mKeyboardView.mSwipeSpaceXDistanceThreshold : mKeyboardView.mSwipeXDistanceThreshold;
                if (scrollXDistance > swipeXDistance) {
                    Logger.v(TAG, "Scroll broke the distance barrier");
                    mKeyboardView.disableTouchesTillFingersAreUp();
                    if (e2.getX() > e1.getX()) {
                        //to right
                        mKeyboardView.mKeyboardActionListener.onSwipeRight(
                                mKeyboardView.isAtTwoFingersState());
                    } else {
                        mKeyboardView.mKeyboardActionListener.onSwipeLeft(
                                mKeyboardView.isAtTwoFingersState());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static final int DIRECTION_NONE = -1;
    private static final int DIRECTION_UP = 0;
    private static final int DIRECTION_DOWN = 1;
    private static final int DIRECTION_LEFT = 2;
    private static final int DIRECTION_RIGHT = 3;

    private static int getPointerDirection(MotionEvent e1, MotionEvent e2, final int pointerIndex) {
        final int pointerId = e1.getPointerId(pointerIndex);
        final int secondPointerIndex = e2.findPointerIndex(pointerId);
        if (secondPointerIndex == -1) return DIRECTION_NONE;

        final float xDistance = e2.getX(secondPointerIndex) - e1.getX(pointerIndex);
        final float yDistance = e2.getY(secondPointerIndex) - e1.getY(pointerIndex);
        if (Math.abs(xDistance - yDistance) < 1f) return DIRECTION_NONE;
        if (Math.abs(xDistance) > Math.abs(yDistance)) {
            //major movement in the X axis
            if (xDistance > 0)
                return DIRECTION_RIGHT;
            else
                return DIRECTION_LEFT;
        } else {
            if (yDistance > 0)
                return DIRECTION_DOWN;
            else
                return DIRECTION_UP;
        }
    }

    private static boolean pointersMovingInTheSameDirection(MotionEvent e1, MotionEvent e2) {
        //TODO: PROBLEM, the first event should be the first event with TWO fingers.
        final int direction = getPointerDirection(e1, e2, 0);
        for (int pointerIndex = 1; pointerIndex < e2.getPointerCount(); pointerIndex++) {
            final int otherPointerDirection = getPointerDirection(e1, e2, pointerIndex);
            if (otherPointerDirection != direction)
                return false;
        }
        //if we got here, it means that all pointers are moving in the same direction
        return true;
    }


    @Override
    public boolean onFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        if (mKeyboardView.isAtTwoFingersState()) {
            Logger.v(TAG, "onFling ignored due to isAtTwoFingersState");
            return false;
        }

        final boolean isHorizontalFling = Math.abs(velocityX) > Math.abs(velocityY);

        float deltaX = me2.getX() - me1.getX();
        float deltaY = me2.getY() - me1.getY();

        if (BuildConfig.DEBUG) {
            Logger.d(TAG, "mSwipeVelocityThreshold %d, mSwipeYDistanceThreshold %d", mKeyboardView.mSwipeVelocityThreshold, mKeyboardView.mSwipeYDistanceThreshold);
            Logger.d(TAG, "onFling vx %f, vy %f, deltaX %f, deltaY %f, isHorizontalFling: %s", velocityX, velocityY, deltaX, deltaY, Boolean.toString(isHorizontalFling));
        }
        final int swipeXDistance = mKeyboardView.isFirstDownEventInsideSpaceBar() ? mKeyboardView.mSwipeSpaceXDistanceThreshold : mKeyboardView.mSwipeXDistanceThreshold;
        if (velocityX > mKeyboardView.mSwipeVelocityThreshold && isHorizontalFling && deltaX > swipeXDistance) {
            Logger.d(TAG, "onSwipeRight");
            mKeyboardView.disableTouchesTillFingersAreUp();
            mKeyboardView.mKeyboardActionListener.onSwipeRight(mKeyboardView.isAtTwoFingersState());
            return true;
        } else if (velocityX < -mKeyboardView.mSwipeVelocityThreshold && isHorizontalFling && deltaX < -swipeXDistance) {
            Logger.d(TAG, "onSwipeLeft");
            mKeyboardView.disableTouchesTillFingersAreUp();
            mKeyboardView.mKeyboardActionListener.onSwipeLeft(mKeyboardView.isAtTwoFingersState());
            return true;
        } else if (velocityY < -mKeyboardView.mSwipeVelocityThreshold && (!isHorizontalFling) && deltaY < -mKeyboardView.mSwipeYDistanceThreshold) {
            Logger.d(TAG, "onSwipeUp");
            mKeyboardView.disableTouchesTillFingersAreUp();
            mKeyboardView.mKeyboardActionListener.onSwipeUp();
            return true;
        } else if (velocityY > mKeyboardView.mSwipeVelocityThreshold && (!isHorizontalFling) && deltaY > mKeyboardView.mSwipeYDistanceThreshold) {
            Logger.d(TAG, "onSwipeDown");
            mKeyboardView.disableTouchesTillFingersAreUp();
            mKeyboardView.mKeyboardActionListener.onSwipeDown();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPinch(float factor) {
        if (factor < 0.65) {
            mKeyboardView.disableTouchesTillFingersAreUp();
            mKeyboardView.mKeyboardActionListener.onPinch();
            return true;
        }
        return false;
    }

    @Override
    public boolean onSeparate(float factor) {
        if (factor > 1.35) {
            mKeyboardView.disableTouchesTillFingersAreUp();
            mKeyboardView.mKeyboardActionListener.onSeparate();
            return true;
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}