package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.view.MotionEvent;

@TargetApi(5)
class WMotionEventV5 extends WMotionEvent {

	public WMotionEventV5(MotionEvent nativeMotionEvent) {
		super(nativeMotionEvent);
	}

	public int getActionMasked() {
		return mNativeMotionEvent.getAction() & MotionEvent.ACTION_MASK;
	}

	public int getPointerCount() {
		return mNativeMotionEvent.getPointerCount();
	}

	public int getActionIndex() {
		return (mNativeMotionEvent.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
	}

	public int getPointerId(int index) {
		return mNativeMotionEvent.getPointerId(index);
	}

	public float getX(int index) {
		return mNativeMotionEvent.getX(index);
	}

	public float getY(int index) {
		return mNativeMotionEvent.getY(index);
	}

	public int findPointerIndex(int pointerId) {
		return mNativeMotionEvent.findPointerIndex(pointerId);
	}

}
