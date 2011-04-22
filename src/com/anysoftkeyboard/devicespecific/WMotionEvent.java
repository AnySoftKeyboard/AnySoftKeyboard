package com.anysoftkeyboard.devicespecific;

import android.view.MotionEvent;

public class WMotionEvent {
	protected final MotionEvent mNativeMotionEvent;
	
	public WMotionEvent(MotionEvent nativeMotionEvent)
	{
		mNativeMotionEvent = nativeMotionEvent;
	}

	public MotionEvent getNativeMotionEvent() {
		return mNativeMotionEvent;
	}

	public int getActionMasked() {
		return mNativeMotionEvent.getAction();
	}

	public int getPointerCount() {
		return 1;
	}

	public long getEventTime() {
		return mNativeMotionEvent.getEventTime();
	}

	public int getActionIndex() {
		return 1;
	}

	public int getPointerId(int index) {
		return 1;
	}

	public float getX(int index) {
		return mNativeMotionEvent.getX();
	}

	public float getY(int index) {
		return mNativeMotionEvent.getY();
	}

	public int findPointerIndex(int pointerId) {
		return 1;
	}
}
