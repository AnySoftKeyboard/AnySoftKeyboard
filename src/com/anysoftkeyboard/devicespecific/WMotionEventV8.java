package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.view.MotionEvent;

@TargetApi(8)
class WMotionEventV8 extends WMotionEventV5 {

	WMotionEventV8(MotionEvent nativeMotionEvent) {
		super(nativeMotionEvent);
	}

	public int getActionMasked() {
		return mNativeMotionEvent.getActionMasked();
	}

	public int getActionIndex() {
		return mNativeMotionEvent.getActionIndex();
	}

}
