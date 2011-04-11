package com.anysoftkeyboard.keyboards.views;

import android.view.MotionEvent;

public class WMotionEventV5 extends WMotionEvent {

	WMotionEventV5(MotionEvent nativeMotionEvent) {
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
