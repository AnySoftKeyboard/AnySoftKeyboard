package com.anysoftkeyboard.utils;

import com.menny.android.anysoftkeyboard.AnyApplication;

public class Log {
	private static final boolean DEBUG = AnyApplication.DEBUG;
	
	private Log() {
		//no instances please.
	}
	
	public static void v(String TAG, String text) {
		if (DEBUG) android.util.Log.v(TAG, text);
	}
	
	public static void v(String TAG, String text, Throwable t) {
		if (DEBUG) android.util.Log.v(TAG, text, t);
	}
	
	public static void d(String TAG, String text) {
		if (DEBUG) android.util.Log.d(TAG, text);
	}
	
	public static void d(String TAG, String text, Throwable t) {
		if (DEBUG) android.util.Log.d(TAG, text, t);
	}
	
	public static void i(String TAG, String text) {
		android.util.Log.i(TAG, text);
	}
	
	public static void i(String TAG, String text, Throwable t) {
		android.util.Log.i(TAG, text, t);
	}
	
	public static void w(String TAG, String text) {
		android.util.Log.w(TAG, text);
	}
	
	public static void w(String TAG, String text, Throwable t) {
		android.util.Log.w(TAG, text, t);
	}
	
	public static void e(String TAG, String text) {
		android.util.Log.e(TAG, text);
	}
	
	public static void e(String TAG, String text, Throwable t) {
		android.util.Log.e(TAG, text, t);
	}
	
}
