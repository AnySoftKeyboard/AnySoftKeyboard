package com.anysoftkeyboard.devicespecific;


import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class DeviceSpecific_V3 extends DeviceSpecific {

	public DeviceSpecific_V3(Context context) {
		super(context);
	}

	public DeviceSpecific_V3(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public DeviceSpecific_V3(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }
	
	@Override
	public String getApiLevel() {
		return "DeviceSpecific_V3";
	}

	@Override
	public WMotionEvent createMotionEventWrapper(MotionEvent nativeMotionEvent) {
		return new WMotionEvent(nativeMotionEvent);
	}

	@Override
	public MultiTouchSupportLevel getMultiTouchSupportLevel() {
		return MultiTouchSupportLevel.None;
	}

	@Override
	public GestureDetector createGestureDetector(
			SimpleOnGestureListener listener) {
		return new GestureDetector(getContext(), listener, null);
	}

	
}
