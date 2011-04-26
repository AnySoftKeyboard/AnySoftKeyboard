package com.anysoftkeyboard.devicespecific;


import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.dictionaries.DictionaryFactory;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

class FactoryView_V3 extends FactoryViewBase {

	public FactoryView_V3(Context context) {
		super(context);
	}

	public FactoryView_V3(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public FactoryView_V3(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }
	
	@Override
	public DeviceSpecific createDeviceSpecific() {
		return new DeviceSpecific_V3();
	}
	
	//Has to be 'static' class, since I do no want any reference to the factory view (let GC clean it)
	public static class DeviceSpecific_V3 implements DeviceSpecific
	{
		public DeviceSpecific_V3()
		{
			Log.d(getApiLevel(), "Just created DeviceSpecific of type "+getApiLevel());
		}

		public String getApiLevel() {
			return "DeviceSpecific_V3";
		}

		public WMotionEvent createMotionEventWrapper(MotionEvent nativeMotionEvent) {
			return new WMotionEvent(nativeMotionEvent);
		}

		public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext) {
			return MultiTouchSupportLevel.None;
		}

		public GestureDetector createGestureDetector(Context appContext, 
				SimpleOnGestureListener listener) {
			return new GestureDetector(appContext, listener, null);
		}
		
		public CloudBackupRequester createCloudBackupRequester(String packageName) {
			return null;
		}
		
		public DictionaryFactory createDictionaryFactory() {
			return new DictionaryFactory();
		}
	}
	
}
