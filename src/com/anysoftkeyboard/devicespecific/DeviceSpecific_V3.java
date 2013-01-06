package com.anysoftkeyboard.devicespecific;


import android.annotation.TargetApi;
import android.content.Context;
import android.view.GestureDetector;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.WordComposer;

@TargetApi(3)
public class DeviceSpecific_V3 implements DeviceSpecific
{
	public DeviceSpecific_V3() {
	}

	public String getApiLevel() {
		return "DeviceSpecific_V3";
	}

	public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext) {
		return MultiTouchSupportLevel.None;
	}

	public GestureDetector createGestureDetector(Context appContext, 
			AskOnGestureListener listener) {
		return new GestureDetector(appContext, listener, null);
	}
	
	public void commitCorrectionToInputConnection(InputConnection ic, WordComposer word) {
		ic.commitText(word.getPreferredWord(), 1);
	}
}