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
import com.anysoftkeyboard.utils.Log;

final class AskGestureEventsListener implements
        AskOnGestureListener {

    private static final String TAG = "AskGestureEventsListener";
    private final SwipeTracker mSwipeTracker;

    private final AnyKeyboardBaseView mKeyboardView;

    public AskGestureEventsListener(AnyKeyboardBaseView keyboardView, SwipeTracker swipeTracker) {
        mKeyboardView = keyboardView;
        mSwipeTracker = swipeTracker;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        final float scrollXDistance = Math.abs(e2.getX() - e1.getX());
        final float scrollYDistance = Math.abs(e2.getY() - e1.getY());
        final float totalScrollTime = ((float)(e2.getEventTime() - e1.getEventTime()));
        //velocity is per second, not per millisecond.
        final float velocityX = 1000 * Math.abs(scrollXDistance / totalScrollTime);
        final float velocityY = 1000 * Math.abs(scrollYDistance / totalScrollTime);
        Log.d(TAG, "onScroll scrollX %f, scrollY %f, velocityX %f, velocityY %f", scrollXDistance, scrollYDistance, velocityX, velocityY);
        if (velocityX > velocityY) {
            Log.v(TAG, "Scrolling on X axis");
            if (velocityX > mKeyboardView.mSwipeVelocityThreshold) {
                Log.v(TAG, "Scroll broke the velocity barrier");
                final int swipeXDistance = mKeyboardView.isFirstDownEventInsideSpaceBar() ? mKeyboardView.mSwipeSpaceXDistanceThreshold : mKeyboardView.mSwipeXDistanceThreshold;
                if (scrollXDistance > swipeXDistance) {
                    Log.v(TAG, "Scroll broke the distance barrier");
                    mKeyboardView.disableTouchesTillFingersAreUp();
                    if (e2.getX() > e1.getX()) {
                        //to right
                        mKeyboardView.mKeyboardActionListener.onSwipeRight(
                                mKeyboardView.isFirstDownEventInsideSpaceBar(),
                                mKeyboardView.isAtTwoFingersState());
                    } else {
                        mKeyboardView.mKeyboardActionListener.onSwipeLeft(
                                mKeyboardView.isFirstDownEventInsideSpaceBar(),
                                mKeyboardView.isAtTwoFingersState());
                    }
                    return true;
                }
            }
        } else {
            Log.v(TAG, "Scrolling on Y axis");
            if (velocityX > mKeyboardView.mSwipeVelocityThreshold) {
                Log.v(TAG, "Scroll broke the velocity barrier");
                if (scrollYDistance > mKeyboardView.mSwipeYDistanceThreshold) {
                    mKeyboardView.disableTouchesTillFingersAreUp();
                    Log.v(TAG, "Scroll broke the distance barrier");
                    if (e2.getY() > e1.getY()) {
                        //to down
                        mKeyboardView.mKeyboardActionListener.onSwipeDown(
                                mKeyboardView.isFirstDownEventInsideSpaceBar());
                    } else {
                        mKeyboardView.mKeyboardActionListener.onSwipeUp(
                                mKeyboardView.isFirstDownEventInsideSpaceBar());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.d(TAG, "onFling vx %f, vy %f", velocityX, velocityY);

        final float absX = Math.abs(velocityX);
        final float absY = Math.abs(velocityY);
        float deltaX = me2.getX() - me1.getX();
        float deltaY = me2.getY() - me1.getY();
        mSwipeTracker.computeCurrentVelocity(1000);
        final float endingVelocityX = mSwipeTracker.getXVelocity();
        final float endingVelocityY = mSwipeTracker.getYVelocity();
        final int swipeXDistance = mKeyboardView.isFirstDownEventInsideSpaceBar() ? mKeyboardView.mSwipeSpaceXDistanceThreshold : mKeyboardView.mSwipeXDistanceThreshold;
        if (velocityX > mKeyboardView.mSwipeVelocityThreshold && absY < absX && deltaX > swipeXDistance) {
            if (mKeyboardView.mDisambiguateSwipe && endingVelocityX >= velocityX / 4) {
                mKeyboardView.disableTouchesTillFingersAreUp();
                mKeyboardView.mKeyboardActionListener.onSwipeRight(mKeyboardView.isFirstDownEventInsideSpaceBar(), mKeyboardView.isAtTwoFingersState());
                return true;
            }
        } else if (velocityX < -mKeyboardView.mSwipeVelocityThreshold && absY < absX && deltaX < -swipeXDistance) {
            if (mKeyboardView.mDisambiguateSwipe && endingVelocityX <= velocityX / 4) {
                mKeyboardView.disableTouchesTillFingersAreUp();
                mKeyboardView.mKeyboardActionListener.onSwipeLeft(mKeyboardView.isFirstDownEventInsideSpaceBar(), mKeyboardView.isAtTwoFingersState());
                return true;
            }
        } else if (velocityY < -mKeyboardView.mSwipeVelocityThreshold && absX < absY && deltaY < -mKeyboardView.mSwipeYDistanceThreshold) {
            if (mKeyboardView.mDisambiguateSwipe && endingVelocityY <= velocityY / 4) {
                mKeyboardView.disableTouchesTillFingersAreUp();
                mKeyboardView.mKeyboardActionListener.onSwipeUp(mKeyboardView.isFirstDownEventInsideSpaceBar());
                return true;
            }
        } else if (velocityY > mKeyboardView.mSwipeVelocityThreshold && absX < absY / 2 && deltaY > mKeyboardView.mSwipeYDistanceThreshold) {
            if (mKeyboardView.mDisambiguateSwipe && endingVelocityY >= velocityY / 4) {
                mKeyboardView.disableTouchesTillFingersAreUp();
                mKeyboardView.mKeyboardActionListener.onSwipeDown(mKeyboardView.isFirstDownEventInsideSpaceBar());
                return true;
            }
        }
        return false;
    }

    public boolean onPinch(float factor) {
        if (factor < 0.65) {
            mKeyboardView.disableTouchesTillFingersAreUp();
            mKeyboardView.mKeyboardActionListener.onPinch();
            return true;
        }
        return false;
    }

    public boolean onSeparate(float factor) {
        if (factor > 1.35) {
            mKeyboardView.disableTouchesTillFingersAreUp();
            mKeyboardView.mKeyboardActionListener.onSeparate();
            return true;
        }
        return false;
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}