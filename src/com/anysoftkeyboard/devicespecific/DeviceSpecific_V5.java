package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.view.MotionEvent;

import com.anysoftkeyboard.dictionaries.DictionaryFactory;
import com.anysoftkeyboard.dictionaries.DictionaryFactoryAPI5;

@TargetApi(5)
public class DeviceSpecific_V5 extends DeviceSpecific_V3
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