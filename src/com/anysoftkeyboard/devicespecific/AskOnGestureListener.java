package com.anysoftkeyboard.devicespecific;

import android.view.GestureDetector.OnGestureListener;

public interface AskOnGestureListener extends OnGestureListener {
	boolean onPinch(float factor);
	boolean onSeparate(float factor);
}
