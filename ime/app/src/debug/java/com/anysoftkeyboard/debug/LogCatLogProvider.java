package com.anysoftkeyboard.debug;

import android.util.Log;
import com.anysoftkeyboard.base.utils.LogProvider;

/** Logger messages to Android's LogCat. Should be used only in DEBUG builds. */
public class LogCatLogProvider implements LogProvider {

    @Override
    public boolean supportsV() {
        return true;
    }

    @Override
    public void v(final String tag, String text) {
        Log.v(tag, text);
    }

    @Override
    public boolean supportsD() {
        return true;
    }

    @Override
    public void d(final String tag, String text) {
        Log.d(tag, text);
    }

    @Override
    public boolean supportsYell() {
        return true;
    }

    @Override
    public void yell(final String tag, String text) {
        Log.w(tag + "-YELL", text);
    }

    @Override
    public boolean supportsI() {
        return true;
    }

    @Override
    public void i(final String tag, String text) {
        Log.i(tag, text);
    }

    @Override
    public boolean supportsW() {
        return true;
    }

    @Override
    public void w(final String tag, String text) {
        Log.w(tag, text);
    }

    @Override
    public boolean supportsE() {
        return true;
    }

    @Override
    public void e(final String tag, String text) {
        Log.e(tag, text);
    }

    @Override
    public boolean supportsWTF() {
        return true;
    }

    @Override
    public void wtf(final String tag, String text) {
        Log.wtf(tag, text);
    }
}
