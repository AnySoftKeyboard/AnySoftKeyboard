package com.anysoftkeyboard.keyboards.views;

import android.view.MotionEvent;

public interface WMotionEvent {
	public static class Diagram extends net.evendanan.frankenrobot.Diagram<WMotionEvent> {
		
	}
	
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
