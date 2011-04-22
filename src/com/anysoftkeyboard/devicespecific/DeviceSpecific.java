package com.anysoftkeyboard.devicespecific;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class DeviceSpecific extends View {

	protected DeviceSpecific(Context context) {
		super(context);
		Log.d(getApiLevel(), "Just created DeviceSpecific of type "+getApiLevel());
	}
	
	protected DeviceSpecific(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(getApiLevel(), "Just created DeviceSpecific of type "+getApiLevel());
	}
	 
	protected DeviceSpecific(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Log.d(getApiLevel(), "Just created DeviceSpecific of type "+getApiLevel());
    }

	public abstract String getApiLevel();
	
	public abstract WMotionEvent createMotionEventWrapper(MotionEvent nativeMotionEvent);
	
	public abstract MultiTouchSupportLevel getMultiTouchSupportLevel();
	
	public abstract GestureDetector createGestureDetector(GestureDetector.SimpleOnGestureListener listener);
}
