package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;

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
	public DictionaryFactory createDictionaryFactory() {
		return new DictionaryFactoryAPI5();
	}
}