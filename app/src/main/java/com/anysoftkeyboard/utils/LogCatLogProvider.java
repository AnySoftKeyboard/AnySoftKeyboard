package com.anysoftkeyboard.utils;

import android.os.Build;
import android.util.Log;

/**
 * Logger messages to Android's LogCat. Should be used only in DEBUG builds.
 */
public class LogCatLogProvider implements LogProvider {
    @Override
    public void v(final String tag, String text) {
        Log.v(tag, text);
    }

    @Override
    public void d(final String tag, String text) {
        Log.d(tag, text);
    }

    @Override
    public void yell(final String tag, String text) {
        Log.w(tag+"-YELL", text);
    }

    @Override
    public void i(final String tag, String text) {
        Log.i(tag, text);
    }

    @Override
    public void w(final String tag, String text) {
        Log.w(tag, text);
    }

    @Override
    public void e(final String tag, String text) {
        Log.e(tag, text);
    }

    @Override
    public void wtf(final String tag, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            Log.wtf(tag, text);
        } else {
            Log.e(tag+" WTF", text);
        }
    }
}
