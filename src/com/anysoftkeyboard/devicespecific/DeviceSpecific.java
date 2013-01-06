package com.anysoftkeyboard.devicespecific;

import android.content.Context;
import android.view.GestureDetector;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.WordComposer;

public interface DeviceSpecific {

	public String getApiLevel();

	public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext);
	
	public GestureDetector createGestureDetector(Context appContext, AskOnGestureListener listener);

	public void commitCorrectionToInputConnection(InputConnection ic, WordComposer word);
}
