package com.anysoftkeyboard.devicespecific;

import com.anysoftkeyboard.dictionaries.DictionaryFactory;
import com.anysoftkeyboard.dictionaries.DictionaryFactoryAPI5;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

@TargetApi(5)
class FactoryView_V5 extends FactoryView_V3 {

	public FactoryView_V5(Context context) {
		super(context);
	}

	public FactoryView_V5(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public FactoryView_V5(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }
	
	@Override
	public DeviceSpecific createDeviceSpecific() {
		return new DeviceSpecific_V5();
	}
	
	public static class DeviceSpecific_V5 extends DeviceSpecific_V3
	{
		@Override
		public String getApiLevel() {
			return "DeviceSpecific_V5";
		}
	
		@Override
		public WMotionEvent createMotionEventWrapper(MotionEvent nativeMotionEvent) {
			return new WMotionEventV5(nativeMotionEvent);
		}
		
		@Override
		public DictionaryFactory createDictionaryFactory() {
			return new DictionaryFactoryAPI5();
		}
	}
}
