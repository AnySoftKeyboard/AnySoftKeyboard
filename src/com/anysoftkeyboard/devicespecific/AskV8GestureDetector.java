package com.anysoftkeyboard.devicespecific;

import com.menny.android.anysoftkeyboard.AnyApplication;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class AskV8GestureDetector extends GestureDetector {
	private static final String TAG = "AskV8GestureDetector";
	
	private final ScaleGestureDetector mScaleGestureDetector;
	private final AskOnGestureListener mListener;
	
	public AskV8GestureDetector(Context context, AskOnGestureListener listener,
			Handler handler, boolean ignoreMultitouch) {
		super(context, listener, handler, ignoreMultitouch);
		
		mListener = listener;
		
		mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
			public void onScaleEnd(ScaleGestureDetector detector) {
				final float factor = detector.getScaleFactor();
				if (AnyApplication.DEBUG) Log.d(TAG, "onScaleEnd factor "+factor);
				
				if (factor > 1.1)
					mListener.onSeparate(factor);
				else if (factor < 0.9)
					mListener.onPinch(factor);
			}
		});
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mScaleGestureDetector.onTouchEvent(ev);
		return super.onTouchEvent(ev) || mScaleGestureDetector.isInProgress();
	}

}
