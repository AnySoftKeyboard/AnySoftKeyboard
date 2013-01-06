package com.anysoftkeyboard.keyboards.views;

import android.view.MotionEvent;

public interface WMotionEvent {
	void setNativeMotionEvent(MotionEvent nativeMotionEvent);
	
	int getActionMasked() ;

	int getPointerCount();

	long getEventTime();

	int getActionIndex();

	int getPointerId(int index);

	float getX(int index);

	float getY(int index);

	int findPointerIndex(int pointerId);
}
