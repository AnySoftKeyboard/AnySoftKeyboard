package com.anysoftkeyboard.devicespecific;

import android.view.GestureDetector.OnGestureListener;

public interface AskOnGestureListener extends OnGestureListener {
	void onPinch(float factor);
	void onSeparate(float factor);
}
