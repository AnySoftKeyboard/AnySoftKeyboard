package com.anysoftkeyboard.crashlytics;

import com.anysoftkeyboard.base.utils.LogProvider;
import com.crashlytics.android.Crashlytics;

public class CrashlyticsLogProvider implements LogProvider {
    @Override
    public boolean supportsV() {
        return true;
    }

    @Override
    public void v(final String tag, String text) {
        Crashlytics.log(1, tag, text);
    }

    @Override
    public boolean supportsD() {
        return true;
    }

    @Override
    public void d(final String tag, String text) {
        Crashlytics.log(2, tag, text);
    }

    @Override
    public boolean supportsI() {
        return true;
    }

    @Override
    public void i(final String tag, String text) {
        Crashlytics.log(3, tag, text);
    }

    @Override
    public boolean supportsW() {
        return true;
    }

    @Override
    public void w(final String tag, String text) {
        Crashlytics.log(4, tag, text);
    }

    @Override
    public boolean supportsE() {
        return true;
    }

    @Override
    public void e(final String tag, String text) {
        Crashlytics.log(5, tag, text);
    }

    @Override
    public boolean supportsWTF() {
        return true;
    }

    @Override
    public void wtf(final String tag, String text) {
        Crashlytics.log(6, tag, text);
    }

    @Override
    public boolean supportsYell() {
        return true;
    }

    @Override
    public void yell(final String tag, String text) {
        Crashlytics.log(6, tag, text);
    }
}
