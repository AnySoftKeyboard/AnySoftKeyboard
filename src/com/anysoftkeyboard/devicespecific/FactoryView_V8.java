package com.anysoftkeyboard.devicespecific;

import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.backup.CloudBackupRequesterApi8;
import com.anysoftkeyboard.voice.VoiceInput;
import com.anysoftkeyboard.voice.VoiceRecognitionTrigger;

import android.content.Context;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

class FactoryView_V8 extends FactoryView_V7 {

	public FactoryView_V8(Context context) {
		super(context);
	}

	public FactoryView_V8(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public FactoryView_V8(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }
	
	@Override
	public DeviceSpecific createDeviceSpecific() {
		return new DeviceSpecific_V8();
	}

	public static class DeviceSpecific_V8 extends DeviceSpecific_V7
	{		
		@Override
		public String getApiLevel() {
			return "DeviceSpecific_V8";
		}
	
		@Override
		public WMotionEvent createMotionEventWrapper(MotionEvent nativeMotionEvent) {
			return new WMotionEventV8(nativeMotionEvent);
		}
	
		@Override
		public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext) {
			PackageManager pkg = appContext.getPackageManager();
			boolean hasDistintMultitouch = pkg.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT);
			boolean hasMultitouch = pkg.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
			
			if (hasDistintMultitouch)
				return MultiTouchSupportLevel.Distinct;
			else if (hasMultitouch)
				return MultiTouchSupportLevel.Basic;
			else
				return MultiTouchSupportLevel.None;
		}
	
		@Override
		public GestureDetector createGestureDetector(Context appContext,
				AskOnGestureListener listener) {
			final boolean ignoreMultitouch = true;
			return new AskV8GestureDetector(appContext, listener, null, ignoreMultitouch);
//			return new GestureDetector(appContext, listener, null, ignoreMultitouch)
//			{
//				private final ScaleGestureDetector mScaleDetector;
//				
//				public GestureDetector()
//				{
//					super(appContext, listener, handler, ingnoreMultitouch);
//				}
//			}
		}
		
		@Override
		public CloudBackupRequester createCloudBackupRequester(Context appContext) {
			return new CloudBackupRequesterApi8(appContext);
		}
		
		@Override
		public VoiceInput createVoiceInput(InputMethodService ime) {
			return new VoiceRecognitionTrigger(ime);
		}
	}
	
}
