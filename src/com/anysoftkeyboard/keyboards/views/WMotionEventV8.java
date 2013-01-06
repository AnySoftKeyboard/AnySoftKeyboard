package com.anysoftkeyboard.keyboards.views;

import android.annotation.TargetApi;

@TargetApi(8)
public class WMotionEventV8 extends WMotionEventV5 {
	public int getActionMasked() {
		return mNativeMotionEvent.getActionMasked();
	}

	public int getActionIndex() {
		return mNativeMotionEvent.getActionIndex();
	}

}
