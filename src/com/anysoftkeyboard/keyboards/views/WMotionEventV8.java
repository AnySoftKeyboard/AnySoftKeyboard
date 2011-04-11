package com.anysoftkeyboard.keyboards.views;

import android.view.MotionEvent;

public class WMotionEventV8 extends WMotionEventV5 {

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
