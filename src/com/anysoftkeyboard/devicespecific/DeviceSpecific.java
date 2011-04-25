package com.anysoftkeyboard.devicespecific;

import com.anysoftkeyboard.backup.CloudBackupRequester;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

public interface DeviceSpecific {

	public abstract String getApiLevel();
	
	public WMotionEvent createMotionEventWrapper(MotionEvent nativeMotionEvent);
	
	public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext);
	
	public GestureDetector createGestureDetector(Context appContext, GestureDetector.SimpleOnGestureListener listener);

	public CloudBackupRequester createCloudBackupRequester(String packageName);
}
