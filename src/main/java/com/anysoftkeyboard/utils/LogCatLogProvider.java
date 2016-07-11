package com.anysoftkeyboard.utils;

import android.os.Build;
import android.util.Log;

/**
 * Logger messages to Android's LogCat. Should be used only in DEBUG builds.
 */
public class LogCatLogProvider implements LogProvider {
    @Override
    public void v(String TAG, String text) {
        Log.v(TAG, text);
    }

    @Override
    public void d(String TAG, String text) {
        Log.d(TAG, text);
    }

    @Override
    public void yell(String TAG, String text) {
        Log.w(TAG+"-YELL", text);
    }

    @Override
    public void i(String TAG, String text) {
        Log.i(TAG, text);
    }

    @Override
    public void w(String TAG, String text) {
        Log.w(TAG, text);
    }

    @Override
    public void e(String TAG, String text) {
        Log.e(TAG, text);
    }

    @Override
    public void wtf(String TAG, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            Log.wtf(TAG, text);
        } else {
            Log.e(TAG+" WTF", text);
        }
    }
}
