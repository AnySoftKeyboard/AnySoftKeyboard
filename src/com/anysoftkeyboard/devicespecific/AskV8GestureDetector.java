package com.anysoftkeyboard.devicespecific;

import com.menny.android.anysoftkeyboard.AnyApplication;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

@TargetApi(8)
public class AskV8GestureDetector extends GestureDetector {
	private static final String TAG = "AskV8GestureDetector";
	
	private final ScaleGestureDetector mScaleGestureDetector;
	private final AskOnGestureListener mListener;
	private boolean mScaleEventHandled = false;
	
	public AskV8GestureDetector(Context context, AskOnGestureListener listener,
			Handler handler, boolean ignoreMultitouch) {
		super(context, listener, handler, ignoreMultitouch);
		
		mListener = listener;
		
		mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
			public void onScaleEnd(ScaleGestureDetector detector) {
				final float factor = detector.getScaleFactor();
				if (AnyApplication.DEBUG) Log.d(TAG, "onScaleEnd factor "+factor);
				
				if (factor > 1.1)
					mScaleEventHandled = mListener.onSeparate(factor);
				else if (factor < 0.9)
					mScaleEventHandled = mListener.onPinch(factor);
			}
		});
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		try
		{
			//https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/26
			mScaleGestureDetector.onTouchEvent(ev);
		}
		catch(IllegalArgumentException e) {
			//I have nothing I can do here.
		}
		final boolean scaleEventHandled = mScaleEventHandled;
		mScaleEventHandled = false;
		return super.onTouchEvent(ev) || scaleEventHandled;
	}

}
