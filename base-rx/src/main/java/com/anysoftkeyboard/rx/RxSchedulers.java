package com.anysoftkeyboard.rx;

import android.support.annotation.NonNull;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxSchedulers {

    static {
        msBackground = Schedulers.io();
        msMainThread = AndroidSchedulers.mainThread();
    }

    static Scheduler msBackground;
    static Scheduler msMainThread;

    private RxSchedulers() {}

    @NonNull
    public static Scheduler mainThread() {
        return msMainThread;
    }

    @NonNull
    public static Scheduler background() {
        return msBackground;
    }
}
