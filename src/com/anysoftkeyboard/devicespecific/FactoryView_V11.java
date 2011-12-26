package com.anysoftkeyboard.devicespecific;

import com.anysoftkeyboard.devicespecific.FactoryView_V8.DeviceSpecific_V8;
import com.anysoftkeyboard.voice.VoiceInput;
import com.anysoftkeyboard.voice.VoiceRecognitionTriggerV11;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.util.AttributeSet;

class FactoryView_V11 extends FactoryView_V7 {

	public FactoryView_V11(Context context) {
		super(context);
	}

	public FactoryView_V11(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public FactoryView_V11(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }
	
	@Override
	public DeviceSpecific createDeviceSpecific() {
		return new DeviceSpecific_V11();
	}

	public static class DeviceSpecific_V11 extends DeviceSpecific_V8
	{		
		@Override
		public String getApiLevel() {
			return "DeviceSpecific_V11";
		}
		@Override
		public VoiceInput createVoiceInput(InputMethodService ime) {
			return new VoiceRecognitionTriggerV11(ime);
		}
	}
	
}
