package com.anysoftkeyboard.rx;

import android.os.Looper;
import android.support.annotation.NonNull;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxSchedulers {

    static Scheduler msBackground;
    static Scheduler msMainThread;

    static {
        msBackground = Schedulers.io();
        // https://medium.com/@sweers/rxandroids-new-async-api-4ab5b3ad3e93
        msMainThread = AndroidSchedulers.from(Looper.getMainLooper(), true);
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(callable -> msMainThread);
    }

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
