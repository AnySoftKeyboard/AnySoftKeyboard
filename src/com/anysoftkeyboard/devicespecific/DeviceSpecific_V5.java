package com.anysoftkeyboard.devicespecific;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DeviceSpecific_V5 extends DeviceSpecific_V3 {

	public DeviceSpecific_V5(Context context) {
		super(context);
	}

	public DeviceSpecific_V5(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public DeviceSpecific_V5(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }

	@Override
	public String getApiLevel() {
		return "DeviceSpecific_V5";
	}

	@Override
	public WMotionEvent createMotionEventWrapper(MotionEvent nativeMotionEvent) {
		return new WMotionEventV5(nativeMotionEvent);
	}
	
}
