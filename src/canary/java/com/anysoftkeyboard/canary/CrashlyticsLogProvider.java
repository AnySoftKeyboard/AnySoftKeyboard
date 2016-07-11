package com.anysoftkeyboard.canary;

import com.anysoftkeyboard.utils.LogProvider;
import com.crashlytics.android.Crashlytics;

public class CrashlyticsLogProvider implements LogProvider {
    @Override
    public void v(String TAG, String text) {
        Crashlytics.log(1, TAG, text);
    }

    @Override
    public void d(String TAG, String text) {
        Crashlytics.log(2, TAG, text);
    }

    @Override
    public void yell(String TAG, String text) {
        Crashlytics.log(2, TAG+" YELL", text);
    }

    @Override
    public void i(String TAG, String text) {
        Crashlytics.log(3, TAG, text);
    }

    @Override
    public void w(String TAG, String text) {
        Crashlytics.log(4, TAG, text);
    }

    @Override
    public void e(String TAG, String text) {
        Crashlytics.log(5, TAG, text);
    }

    @Override
    public void wtf(String TAG, String text) {
        Crashlytics.log(6, TAG, text);
    }
}
