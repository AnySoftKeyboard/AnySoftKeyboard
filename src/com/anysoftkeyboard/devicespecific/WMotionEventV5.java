package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.view.MotionEvent;

@TargetApi(5)
public class WMotionEventV5 extends WMotionEventV3 {

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
