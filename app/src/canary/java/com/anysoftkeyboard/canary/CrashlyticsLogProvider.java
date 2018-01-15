package com.anysoftkeyboard.debug;

import com.anysoftkeyboard.utils.LogProvider;
import com.crashlytics.android.Crashlytics;

public class CrashlyticsLogProvider implements LogProvider {
    @Override
    public void v(final String tag, String text) {
        Crashlytics.log(1, tag, text);
    }

    @Override
    public void d(final String tag, String text) {
        Crashlytics.log(2, tag, text);
    }

    @Override
    public void i(final String tag, String text) {
        Crashlytics.log(3, tag, text);
    }

    @Override
    public void w(final String tag, String text) {
        Crashlytics.log(4, tag, text);
    }

    @Override
    public void e(final String tag, String text) {
        Crashlytics.log(5, tag, text);
    }

    @Override
    public void wtf(final String tag, String text) {
        Crashlytics.log(6, tag, text);
    }

    @Override
    public void yell(final String tag, String text) {
        Crashlytics.log(6, tag, text);
    }
}
