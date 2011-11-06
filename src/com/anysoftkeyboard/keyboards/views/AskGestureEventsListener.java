package com.anysoftkeyboard.keyboards.views;

import android.util.Log;
import android.view.MotionEvent;

import com.anysoftkeyboard.devicespecific.AskOnGestureListener;
import com.menny.android.anysoftkeyboard.AnyApplication;

final class AskGestureEventsListener implements
		AskOnGestureListener {
	
	private final SwipeTracker mSwipeTracker;
    
	private final AnyKeyboardBaseView mKeyboardView;
	
	public AskGestureEventsListener(AnyKeyboardBaseView keyboardView, SwipeTracker swipeTracker)
	{
		mKeyboardView = keyboardView;
		mSwipeTracker = swipeTracker;
	}
	
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		//if (AnyApplication.DEBUG) Log.d(TAG, String.format("onScroll dx %f, dy %f", distanceX, distanceY));
		//return Math.abs(distanceX) > mKeyboardView.mScrollXDistanceThreshold || Math.abs(distanceY) > mKeyboardView.mScrollYDistanceThreshold;
		return false;
	}

	public boolean onFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
		if (AnyApplication.DEBUG) Log.d(AnyKeyboardBaseView.TAG, String.format("onFling vx %f, vy %f", velocityX, velocityY));
		
		final float absX = Math.abs(velocityX);
	    final float absY = Math.abs(velocityY);
	    float deltaX = me2.getX() - me1.getX();
	    float deltaY = me2.getY() - me1.getY();
	    mSwipeTracker.computeCurrentVelocity(1000);
	    final float endingVelocityX = mSwipeTracker.getXVelocity();
	    final float endingVelocityY = mSwipeTracker.getYVelocity();
	    final int swipeXDistance = mKeyboardView.isFirstDownEventInsideSpaceBar()? mKeyboardView.mSwipeSpaceXDistanceThreshold : mKeyboardView.mSwipeXDistanceThreshold;
	    if (velocityX > mKeyboardView.mSwipeVelocityThreshold && absY < absX && deltaX > swipeXDistance) {
	        if (mKeyboardView.mDisambiguateSwipe && endingVelocityX >= velocityX / 4) {
	        	mKeyboardView.mKeyboardActionListener.onSwipeRight(mKeyboardView.isFirstDownEventInsideSpaceBar());
	            return true;
	        }
	    } else if (velocityX < -mKeyboardView.mSwipeVelocityThreshold && absY < absX && deltaX < -swipeXDistance) {
	        if (mKeyboardView.mDisambiguateSwipe && endingVelocityX <= velocityX / 4) {
	        	mKeyboardView.mKeyboardActionListener.onSwipeLeft(mKeyboardView.isFirstDownEventInsideSpaceBar());
	            return true;
	        }
	    } else if (velocityY < -mKeyboardView.mSwipeVelocityThreshold && absX < absY && deltaY < -mKeyboardView.mSwipeYDistanceThreshold) {
	        if (mKeyboardView.mDisambiguateSwipe && endingVelocityY <= velocityY / 4) {
	        	mKeyboardView.mKeyboardActionListener.onSwipeUp(mKeyboardView.isFirstDownEventInsideSpaceBar());
	            return true;
	        }
	    } else if (velocityY > mKeyboardView.mSwipeVelocityThreshold && absX < absY / 2 && deltaY > mKeyboardView.mSwipeYDistanceThreshold) {
	        if (mKeyboardView.mDisambiguateSwipe && endingVelocityY >= velocityY / 4) {
	        	mKeyboardView.mKeyboardActionListener.onSwipeDown(mKeyboardView.isFirstDownEventInsideSpaceBar());
	            return true;
	        }
	    }
	    return false;
	}

	public boolean onPinch(float factor) {
		if (factor < 0.5)
		{
			//mKeyboardView.dismissKeyPreview();
			mKeyboardView.mPointerQueue.cancelAllTrackers();
	        mKeyboardView.mKeyboardActionListener.onPinch();
	        return true;
		}
		return false;
	}

	public boolean onSeparate(float factor) {
		if (factor > 1.5)
		{
			//mKeyboardView.dismissKeyPreview();
			mKeyboardView.mPointerQueue.cancelAllTrackers();
			mKeyboardView.mKeyboardActionListener.onSeparate();
			return true;
		}
		return false;
	}

	public boolean onDown(MotionEvent e) {return false;}

	public void onLongPress(MotionEvent e) {}

	public void onShowPress(MotionEvent e) {}

	public boolean onSingleTapUp(MotionEvent e) {return false;}
}